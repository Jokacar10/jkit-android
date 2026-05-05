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
package io.ton.walletkit.engine.operations

import io.ton.walletkit.WalletKitBridgeException
import io.ton.walletkit.api.generated.TONNetwork
import io.ton.walletkit.api.generated.TONSignatureDomain
import io.ton.walletkit.engine.adapter.BridgeWalletAdapter
import io.ton.walletkit.engine.infrastructure.BridgeRpcClient
import io.ton.walletkit.engine.infrastructure.callTyped
import io.ton.walletkit.engine.infrastructure.callTypedOrNull
import io.ton.walletkit.engine.model.WalletAccount
import io.ton.walletkit.engine.operations.requests.AdapterIdRequest
import io.ton.walletkit.engine.operations.requests.CreateAdapterRequest
import io.ton.walletkit.engine.operations.requests.CreateSignerFromCustomRequest
import io.ton.walletkit.engine.operations.requests.CreateSignerFromMnemonicRequest
import io.ton.walletkit.engine.operations.requests.CreateSignerFromSecretKeyRequest
import io.ton.walletkit.engine.operations.requests.WalletIdRequest
import io.ton.walletkit.engine.operations.responses.AdapterInfoResponse
import io.ton.walletkit.engine.operations.responses.AddWalletResponse
import io.ton.walletkit.engine.operations.responses.SignerInfoResponse
import io.ton.walletkit.engine.state.AdapterManager
import io.ton.walletkit.engine.state.SignerManager
import io.ton.walletkit.internal.constants.BridgeMethodConstants
import io.ton.walletkit.internal.util.WalletKitUtils
import io.ton.walletkit.model.TONHex
import io.ton.walletkit.model.TONUserFriendlyAddress
import io.ton.walletkit.model.TONWalletAdapter
import io.ton.walletkit.model.WalletAdapterInfo
import io.ton.walletkit.model.WalletSigner
import io.ton.walletkit.model.WalletSignerInfo
import kotlinx.serialization.json.Json

/**
 * Wallet lifecycle and account state operations.
 *
 * Live JS objects (signers, adapters) are held in a JS-side registry by string ID.
 * Kotlin holds only the IDs.
 *
 * @suppress Internal component used by [WebViewWalletKitEngine].
 */
internal class WalletOperations(
    private val ensureInitialized: suspend () -> Unit,
    private val rpcClient: BridgeRpcClient,
    private val signerManager: SignerManager,
    private val adapterManager: AdapterManager,
    private val currentNetworkProvider: () -> String,
    private val json: Json,
) {

    suspend fun createSignerFromMnemonic(
        mnemonic: List<String>,
        mnemonicType: String = "ton",
    ): WalletSignerInfo {
        ensureInitialized()

        val request = CreateSignerFromMnemonicRequest(mnemonic = mnemonic, mnemonicType = mnemonicType)
        val response: SignerInfoResponse =
            rpcClient.callTyped(BridgeMethodConstants.METHOD_CREATE_SIGNER_FROM_MNEMONIC, request, json)
        val signerId = response.signerId?.takeIf { it.isNotEmpty() }
            ?: throw WalletKitBridgeException("JS did not return signerId")
        val publicKeyHex = WalletKitUtils.stripHexPrefix(response.publicKey ?: "")

        return WalletSignerInfo(signerId = signerId, publicKey = TONHex(publicKeyHex))
    }

    suspend fun createSignerFromSecretKey(
        secretKeyHex: String,
    ): WalletSignerInfo {
        ensureInitialized()

        val request = CreateSignerFromSecretKeyRequest(secretKey = secretKeyHex)
        val response: SignerInfoResponse =
            rpcClient.callTyped(BridgeMethodConstants.METHOD_CREATE_SIGNER_FROM_PRIVATE_KEY, request, json)
        val signerId = response.signerId?.takeIf { it.isNotEmpty() }
            ?: throw WalletKitBridgeException("JS did not return signerId")
        val publicKeyHex = WalletKitUtils.stripHexPrefix(response.publicKey ?: "")

        return WalletSignerInfo(signerId = signerId, publicKey = TONHex(publicKeyHex))
    }

    suspend fun createSignerFromCustom(signer: WalletSigner): WalletSignerInfo {
        ensureInitialized()

        val signerId = signerManager.registerSigner(signer)
        val publicKeyHex = WalletKitUtils.ensureHexPrefix(signer.publicKey().value)

        val request = CreateSignerFromCustomRequest(signerId = signerId, publicKey = publicKeyHex)
        rpcClient.send(BridgeMethodConstants.METHOD_CREATE_SIGNER_FROM_CUSTOM, request)

        return WalletSignerInfo(signerId = signerId, publicKey = signer.publicKey())
    }

    suspend fun createAdapter(
        signerId: String,
        publicKey: TONHex,
        version: String,
        network: TONNetwork?,
        workchain: Int,
        walletId: Long,
        domain: TONSignatureDomain? = null,
    ): TONWalletAdapter {
        ensureInitialized()

        val resolvedNetwork = network ?: TONNetwork(chainId = "-239")

        val method = when (version) {
            "v5r1" -> BridgeMethodConstants.METHOD_CREATE_V5R1_WALLET_ADAPTER
            "v4r2" -> BridgeMethodConstants.METHOD_CREATE_V4R2_WALLET_ADAPTER
            else -> throw WalletKitBridgeException("Unsupported wallet version: $version")
        }

        val request = CreateAdapterRequest(
            signerId = signerId,
            network = resolvedNetwork,
            workchain = workchain,
            walletId = walletId,
            domain = domain,
        )
        val response: AdapterInfoResponse = rpcClient.callTyped(method, request, json)
        val adapterId = response.adapterId?.takeIf { it.isNotEmpty() }
            ?: throw WalletKitBridgeException("JS did not return adapterId")
        val address = response.address ?: ""

        return BridgeWalletAdapter(
            adapterId = adapterId,
            cachedPublicKey = publicKey,
            cachedNetwork = resolvedNetwork,
            cachedAddress = TONUserFriendlyAddress(address),
            rpcClient = rpcClient,
        )
    }

    suspend fun addWallet(adapter: WalletAdapterInfo): WalletAccount {
        ensureInitialized()

        val request = AdapterIdRequest(adapterId = adapter.adapterId)
        val response: AddWalletResponse =
            rpcClient.callTyped(BridgeMethodConstants.METHOD_ADD_WALLET, request, json)

        return response.toWalletAccount()
    }

    suspend fun addWallet(adapter: TONWalletAdapter): WalletAccount {
        ensureInitialized()

        // BridgeWalletAdapter wraps a JS-side adapter — route through WalletAdapterInfo path
        // to avoid re-registering in AdapterManager or creating a duplicate proxy in JS.
        if (adapter is BridgeWalletAdapter) {
            return addWallet(adapter.toWalletAdapterInfo())
        }

        val adapterId = adapterManager.registerAdapter(adapter)
        val request = AdapterIdRequest(adapterId = adapterId)
        val response: AddWalletResponse =
            rpcClient.callTyped(BridgeMethodConstants.METHOD_ADD_WALLET, request, json)

        return response.toWalletAccount()
    }

    suspend fun getWallets(): List<WalletAccount> {
        ensureInitialized()

        val items: List<AddWalletResponse> =
            rpcClient.callTyped(BridgeMethodConstants.METHOD_GET_WALLETS, null, json)

        return items.mapNotNull { entry ->
            val walletId = entry.walletId?.takeIf { it.isNotEmpty() } ?: return@mapNotNull null
            val rawPublicKey = entry.wallet?.publicKey?.takeIf { it.isNotEmpty() }
            val publicKey = rawPublicKey?.let { WalletKitUtils.stripHexPrefix(it) }
            val version = entry.wallet?.version?.takeIf { it.isNotEmpty() } ?: "unknown"
            WalletAccount(
                walletId = walletId,
                address = TONUserFriendlyAddress(getWalletAddress(walletId)),
                publicKey = publicKey,
                version = version,
            )
        }
    }

    suspend fun getWalletAddress(walletId: String): String {
        val request = WalletIdRequest(walletId = walletId)
        return rpcClient.callTyped("getWalletAddress", request, json)
    }

    suspend fun getWallet(walletId: String): WalletAccount? {
        ensureInitialized()

        val request = WalletIdRequest(walletId = walletId)
        val response = rpcClient.callTypedOrNull<AddWalletResponse>(
            BridgeMethodConstants.METHOD_GET_WALLET,
            request,
            json,
        ) ?: return null

        val returnedWalletId = response.walletId?.takeIf { it.isNotEmpty() } ?: walletId
        val rawPublicKey = response.wallet?.publicKey
        val publicKey = rawPublicKey?.let { WalletKitUtils.stripHexPrefix(it) }
        val version = response.wallet?.version?.takeIf { it.isNotEmpty() } ?: "unknown"

        val address = getWalletAddress(returnedWalletId)
        if (address.isEmpty()) return null

        return WalletAccount(
            walletId = returnedWalletId,
            address = TONUserFriendlyAddress(address),
            publicKey = publicKey,
            version = version,
        )
    }

    suspend fun removeWallet(walletId: String) {
        ensureInitialized()
        rpcClient.send(BridgeMethodConstants.METHOD_REMOVE_WALLET, WalletIdRequest(walletId = walletId))
    }

    suspend fun getBalance(walletId: String): String {
        ensureInitialized()
        val request = WalletIdRequest(walletId = walletId)
        return rpcClient.callTyped(BridgeMethodConstants.METHOD_GET_BALANCE, request, json)
    }

    private suspend fun AddWalletResponse.toWalletAccount(): WalletAccount {
        val walletId = this.walletId?.takeIf { it.isNotEmpty() }
            ?: throw WalletKitBridgeException(ERROR_NEW_WALLET_NOT_FOUND)
        val rawPublicKey = this.wallet?.publicKey ?: ""
        val pubKey = WalletKitUtils.stripHexPrefix(rawPublicKey)
        val version = this.wallet?.version?.takeIf { it.isNotEmpty() } ?: "unknown"
        val address = getWalletAddress(walletId)
        return WalletAccount(
            walletId = walletId,
            address = TONUserFriendlyAddress(address),
            publicKey = pubKey.takeIf { it.isNotEmpty() },
            version = version,
        )
    }

    companion object {
        internal const val ERROR_NEW_WALLET_NOT_FOUND = "Failed to retrieve newly added wallet"
        internal const val ERROR_FAILED_REMOVE_WALLET = "Failed to remove wallet: "
    }
}
