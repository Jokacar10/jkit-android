/*
 * Copyright (c) 2025 TonTech
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package io.ton.walletkit.demo.presentation.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import io.ton.walletkit.ITONWallet
import io.ton.walletkit.ITONWalletKit
import io.ton.walletkit.api.generated.TONGaslessQuote
import io.ton.walletkit.api.generated.TONGaslessQuoteParams
import io.ton.walletkit.api.generated.TONGaslessSendParams
import io.ton.walletkit.api.generated.TONJettonsTransferRequest
import io.ton.walletkit.api.generated.TONTransactionRequest
import io.ton.walletkit.api.generated.TONTransferRequest
import io.ton.walletkit.demo.presentation.model.FeeAsset
import io.ton.walletkit.demo.presentation.model.SendableToken
import io.ton.walletkit.demo.presentation.util.TonFormatter
import io.ton.walletkit.gasless.ITONGaslessManager
import io.ton.walletkit.model.TONUserFriendlyAddress
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.math.BigDecimal

/**
 * Drives the "send any token" sheet — pick TON or any held jetton, optionally pay the
 * network fee gaslessly in a jetton. Mirrors the iOS `SendTokensViewModel`.
 */
class SendTokensViewModel(
    private val wallet: ITONWallet,
    private val kit: ITONWalletKit,
) : ViewModel() {

    data class UiState(
        val tokens: List<SendableToken> = emptyList(),
        val selectedToken: SendableToken? = null,
        val recipient: String = "",
        val amount: String = "",
        val isSending: Boolean = false,
        val sent: Boolean = false,
        val error: String? = null,
        val gaslessEnabled: Boolean = false,
        val feeAssets: List<FeeAsset> = emptyList(),
        val selectedFeeAsset: FeeAsset? = null,
        val isQuoting: Boolean = false,
        val gaslessFeeText: String? = null,
        val gaslessError: String? = null,
    ) {
        val canUseGasless: Boolean get() = selectedToken?.masterAddress != null

        val sendButtonTitle: String
            get() = if (gaslessEnabled && canUseGasless) "Send Gasless" else "Send ${selectedToken?.symbol ?: ""}"

        val canSend: Boolean
            get() {
                if (recipient.isBlank() || amount.isBlank() || isSending || selectedToken == null) return false
                if (gaslessEnabled && canUseGasless) {
                    return selectedFeeAsset != null && !isQuoting && gaslessFeeText != null && gaslessError == null
                }
                return true
            }
    }

    private val _state = MutableStateFlow(UiState())
    val state: StateFlow<UiState> = _state.asStateFlow()

    private var relayAddress: TONUserFriendlyAddress? = null
    private var jettonMeta: Map<String, FeeAsset> = emptyMap()
    private var quoteJob: Job? = null

    init {
        loadTokens()
    }

    private fun loadTokens() {
        viewModelScope.launch {
            val tokens = mutableListOf<SendableToken>()
            runCatching {
                tokens += SendableToken(
                    id = "ton",
                    name = "Toncoin",
                    symbol = "TON",
                    decimals = TON_DECIMALS,
                    displayBalance = TonFormatter.formatNanoTon(wallet.balance().value),
                    masterAddress = null,
                    imageSource = null,
                    requiredAmountInfo = "Minimum transaction: 0.0001 TON",
                )
            }.onFailure { Log.e(TAG, "Failed to load TON balance", it) }

            runCatching {
                wallet.jettons().jettons.forEach { jetton ->
                    val decimals = jetton.decimalsNumber ?: TON_DECIMALS
                    val symbol = jetton.info.symbol ?: "UNKNOWN"
                    tokens += SendableToken(
                        id = jetton.address.value,
                        name = jetton.info.name ?: "Unknown Jetton",
                        symbol = symbol,
                        decimals = decimals,
                        displayBalance = formatRaw(jetton.balance, decimals),
                        masterAddress = jetton.address.value,
                        imageSource = jetton.info.image?.mediumUrl ?: jetton.info.image?.url,
                        requiredAmountInfo = "Enter amount in $symbol units",
                    )
                }
            }.onFailure { Log.e(TAG, "Failed to load jettons", it) }

            jettonMeta = tokens.filter { !it.isNativeTON }.associate { token ->
                token.masterAddress!! to FeeAsset(token.masterAddress, token.symbol, token.decimals, token.imageSource)
            }

            _state.update { it.copy(tokens = tokens, selectedToken = it.selectedToken ?: tokens.firstOrNull()) }
        }
    }

    fun selectToken(token: SendableToken) {
        _state.update {
            it.copy(
                selectedToken = token,
                recipient = "",
                amount = "",
                gaslessEnabled = false,
                feeAssets = emptyList(),
                selectedFeeAsset = null,
                gaslessFeeText = null,
                gaslessError = null,
                isQuoting = false,
            )
        }
        relayAddress = null
        quoteJob?.cancel()
    }

    fun setRecipient(value: String) {
        _state.update { it.copy(recipient = value) }
        scheduleQuote()
    }

    fun setAmount(value: String) {
        _state.update { it.copy(amount = value) }
        scheduleQuote()
    }

    fun useMax() {
        _state.update { it.copy(amount = it.selectedToken?.displayBalance ?: "") }
        scheduleQuote()
    }

    fun setGaslessEnabled(enabled: Boolean) {
        _state.update { it.copy(gaslessEnabled = enabled, gaslessError = null, gaslessFeeText = null) }
        if (enabled) loadGaslessConfig() else quoteJob?.cancel()
    }

    fun selectFeeAsset(asset: FeeAsset) {
        _state.update { it.copy(selectedFeeAsset = asset) }
        scheduleQuote()
    }

    fun clearError() {
        _state.update { it.copy(error = null) }
    }

    // MARK: - Sending

    fun send() {
        val current = _state.value
        val token = current.selectedToken ?: return
        if (current.isSending) return

        _state.update { it.copy(isSending = true, error = null) }
        viewModelScope.launch {
            runCatching {
                if (current.gaslessEnabled && current.canUseGasless) {
                    sendGasless(token, current)
                } else if (token.isNativeTON) {
                    val request = TONTransferRequest(
                        transferAmount = TonFormatter.tonToNano(current.amount),
                        recipientAddress = TONUserFriendlyAddress(current.recipient.trim()),
                    )
                    wallet.send(wallet.transferTONTransaction(request))
                } else {
                    val request = TONJettonsTransferRequest(
                        jettonAddress = TONUserFriendlyAddress(token.masterAddress!!),
                        transferAmount = toRaw(current.amount, token.decimals),
                        recipientAddress = TONUserFriendlyAddress(current.recipient.trim()),
                    )
                    wallet.send(wallet.transferJettonTransaction(request))
                }
            }.onSuccess {
                _state.update { it.copy(isSending = false, sent = true) }
            }.onFailure { error ->
                Log.e(TAG, "Failed to send", error)
                _state.update { it.copy(isSending = false, error = error.message ?: "Failed to send transaction") }
            }
        }
    }

    private suspend fun sendGasless(token: SendableToken, current: UiState) {
        val feeAsset = current.selectedFeeAsset ?: error("Fee asset not selected")
        val relay = relayAddress ?: error("Gasless config not loaded")
        val gasless = ensureGasless()
        val quote = quote(gasless, token, feeAsset, current, relay)
        val internalBoc = wallet.signedSignMessage(
            TONTransactionRequest(messages = quote.messages, validUntil = quote.validUntil, network = wallet.network()),
        )
        gasless.sendTransaction(
            TONGaslessSendParams(
                network = wallet.network(),
                walletPublicKey = wallet.publicKey(),
                internalBoc = internalBoc,
            ),
        )
    }

    // MARK: - Gasless config & quoting

    private fun loadGaslessConfig() {
        viewModelScope.launch {
            runCatching {
                val gasless = ensureGasless()
                val config = gasless.getConfig(network = wallet.network())
                relayAddress = config.relayAddress
                val assets = config.supportedAssets.map { supported ->
                    jettonMeta[supported.address.value]
                        ?: FeeAsset(supported.address.value, shortAddress(supported.address.value), TON_DECIMALS, null)
                }
                _state.update { state ->
                    state.copy(
                        feeAssets = assets,
                        selectedFeeAsset = state.selectedFeeAsset ?: pickDefaultFeeAsset(assets),
                    )
                }
                scheduleQuote()
            }.onFailure {
                Log.e(TAG, "Failed to load gasless config", it)
                _state.update { it.copy(gaslessError = "Failed to load gasless configuration") }
            }
        }
    }

    private fun scheduleQuote() {
        quoteJob?.cancel()
        val current = _state.value
        if (!current.gaslessEnabled || !current.canUseGasless) return
        val token = current.selectedToken ?: return
        if (current.selectedFeeAsset == null || relayAddress == null ||
            !isValidRecipient(current.recipient) || !isValidAmount(current.amount, token.displayBalance)
        ) {
            _state.update { it.copy(gaslessFeeText = null) }
            return
        }

        quoteJob = viewModelScope.launch {
            delay(QUOTE_DEBOUNCE_MS)
            _state.update { it.copy(isQuoting = true, gaslessError = null) }
            runCatching {
                val gasless = ensureGasless()
                val quote = quote(gasless, token, current.selectedFeeAsset, current, relayAddress!!)
                _state.update {
                    it.copy(isQuoting = false, gaslessFeeText = formatFee(quote.fee, current.selectedFeeAsset))
                }
            }.onFailure {
                Log.e(TAG, "Failed to get gasless quote", it)
                _state.update { state ->
                    state.copy(isQuoting = false, gaslessFeeText = null, gaslessError = "Failed to get gasless quote")
                }
            }
        }
    }

    private suspend fun quote(
        gasless: ITONGaslessManager,
        token: SendableToken,
        feeAsset: FeeAsset,
        current: UiState,
        relay: TONUserFriendlyAddress,
    ): TONGaslessQuote {
        val transfer = wallet.transferJettonTransaction(
            TONJettonsTransferRequest(
                jettonAddress = TONUserFriendlyAddress(token.masterAddress!!),
                transferAmount = toRaw(current.amount, token.decimals),
                recipientAddress = TONUserFriendlyAddress(current.recipient.trim()),
                responseDestination = relay,
            ),
        )
        return gasless.getQuote(
            TONGaslessQuoteParams(
                network = wallet.network(),
                walletAddress = wallet.address(),
                walletPublicKey = wallet.publicKey(),
                messages = transfer.messages,
                feeAsset = TONUserFriendlyAddress(feeAsset.address),
            ),
        )
    }

    private suspend fun ensureGasless(): ITONGaslessManager {
        val gasless = kit.gasless()
        val provider = kit.tonApiGaslessProvider()
        if (!gasless.hasProvider(provider.identifier)) {
            gasless.registerProvider(provider)
            gasless.setDefaultProvider(provider.identifier)
        }
        return gasless
    }

    private fun pickDefaultFeeAsset(assets: List<FeeAsset>): FeeAsset? = assets.firstOrNull { it.address == USDT_MASTER_MAINNET } ?: assets.firstOrNull()

    private fun formatFee(rawFee: String, asset: FeeAsset): String = "${formatRaw(rawFee, asset.decimals)} ${asset.symbol}"

    private fun formatRaw(raw: String, decimals: Int): String = runCatching {
        BigDecimal(raw).movePointLeft(decimals).stripTrailingZeros().toPlainString()
    }.getOrDefault(raw)

    private fun toRaw(amount: String, decimals: Int): String = BigDecimal(amount).movePointRight(decimals).toBigInteger().toString()

    private fun shortAddress(address: String): String = if (address.length > 8) "${address.take(4)}…${address.takeLast(4)}" else address

    private fun isValidRecipient(address: String): Boolean {
        val a = address.trim()
        return a.length > 40 && (a.startsWith("EQ") || a.startsWith("UQ"))
    }

    private fun isValidAmount(amount: String, balance: String): Boolean {
        val value = amount.toBigDecimalOrNull() ?: return false
        if (value <= BigDecimal.ZERO) return false
        val max = balance.toBigDecimalOrNull() ?: return true
        return value <= max
    }

    companion object {
        private const val TAG = "SendTokensVM"
        private const val TON_DECIMALS = 9
        private const val QUOTE_DEBOUNCE_MS = 400L

        /** Mainnet USDT jetton master — preferred default gasless fee asset, mirroring the iOS/JS demo. */
        private const val USDT_MASTER_MAINNET = "EQCxE6mUtQJKFnGfaROTKOt1lZbDiiX1kCixRv7Nw2Id_sDs"

        fun factory(wallet: ITONWallet, kit: ITONWalletKit): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T = SendTokensViewModel(wallet, kit) as T
        }
    }
}
