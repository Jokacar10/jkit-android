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
import io.ton.walletkit.engine.infrastructure.BridgeRpcClient
import io.ton.walletkit.engine.infrastructure.callTyped
import io.ton.walletkit.engine.operations.requests.CreateMnemonicRequest
import io.ton.walletkit.engine.operations.requests.MnemonicToKeyPairRequest
import io.ton.walletkit.engine.operations.requests.SignRequest
import io.ton.walletkit.engine.operations.responses.KeyPairResponse
import io.ton.walletkit.internal.constants.BridgeMethodConstants
import io.ton.walletkit.internal.constants.LogConstants
import io.ton.walletkit.internal.util.Logger
import io.ton.walletkit.internal.util.WalletKitUtils
import io.ton.walletkit.model.KeyPair
import kotlinx.serialization.json.Json

/**
 * Handles cryptographic bridge operations such as mnemonic generation, key derivation,
 * and data signing. Each call ensures the bridge is initialised before delegating to
 * the JavaScript transport.
 *
 * Behaviour and exception semantics match the legacy [WebViewWalletKitEngine] directly.
 *
 * @property ensureInitialized Suspended function that guarantees WalletKit initialisation.
 * @property rpcClient RPC client used for bridge communication.
 *
 * @suppress Internal component consumed by [WebViewWalletKitEngine].
 */
internal class CryptoOperations(
    private val ensureInitialized: suspend () -> Unit,
    private val rpcClient: BridgeRpcClient,
    private val json: Json,
) {

    /**
     * Generate a TON mnemonic of the given size.
     *
     * @param wordCount Number of mnemonic words to generate.
     * @return Generated mnemonic words. Returns an empty list if bridge response omits items.
     * @throws WalletKitBridgeException If the bridge call fails.
     */
    suspend fun createTonMnemonic(wordCount: Int): List<String> {
        ensureInitialized()

        val request = CreateMnemonicRequest(count = wordCount)
        val items: List<String> = rpcClient.callTyped(BridgeMethodConstants.METHOD_CREATE_TON_MNEMONIC, request, json)
        if (items.isEmpty()) {
            Logger.w(TAG, "Mnemonic generation returned no items (wordCount=$wordCount)")
        }
        return items
    }

    /**
     * Convert a mnemonic phrase to an Ed25519 key pair.
     *
     * @param words Mnemonic seed words (12 or 24 words).
     * @param mnemonicType Derivation type: "ton" (default) or "bip39".
     * @return KeyPair containing public key (32 bytes) and secret key (64 bytes).
     * @throws WalletKitBridgeException If the bridge call fails.
     */
    suspend fun mnemonicToKeyPair(
        words: List<String>,
        mnemonicType: String = "ton",
    ): KeyPair {
        ensureInitialized()

        val request = MnemonicToKeyPairRequest(mnemonic = words, mnemonicType = mnemonicType)
        val response: KeyPairResponse =
            rpcClient.callTyped(BridgeMethodConstants.METHOD_MNEMONIC_TO_KEY_PAIR, request, json)
        return KeyPair(response.publicKey, response.secretKey)
    }

    /**
     * Sign arbitrary data using a secret key via the bridge.
     *
     * @param data Data bytes to sign.
     * @param secretKey Secret key bytes for signing.
     * @return Signature bytes returned by the bridge.
     * @throws WalletKitBridgeException If the bridge call fails or omits the signature.
     */
    suspend fun sign(
        data: ByteArray,
        secretKey: ByteArray,
    ): ByteArray {
        ensureInitialized()

        val request = SignRequest(
            data = data.map { it.toInt() and 0xFF },
            secretKey = secretKey.map { it.toInt() and 0xFF },
        )
        val signatureHex: String = rpcClient.callTyped(BridgeMethodConstants.METHOD_SIGN, request, json)
        if (signatureHex.isEmpty() || signatureHex == "null") {
            throw WalletKitBridgeException(ERROR_SIGNATURE_MISSING_SIGN_RESULT)
        }
        return WalletKitUtils.hexToByteArray(signatureHex)
    }

    companion object {
        private const val TAG = "${LogConstants.TAG_WEBVIEW_ENGINE}:CryptoOps"
        private const val ERROR_SIGNATURE_MISSING_SIGN_RESULT =
            "Signature missing from sign result"
    }
}
