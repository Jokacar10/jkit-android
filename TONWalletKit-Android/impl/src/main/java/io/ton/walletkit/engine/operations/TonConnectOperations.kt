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
import io.ton.walletkit.api.generated.TONConnectionApprovalResponse
import io.ton.walletkit.api.generated.TONConnectionRequestEvent
import io.ton.walletkit.api.generated.TONSendTransactionApprovalResponse
import io.ton.walletkit.api.generated.TONSendTransactionRequestEvent
import io.ton.walletkit.api.generated.TONSignDataApprovalResponse
import io.ton.walletkit.api.generated.TONSignDataRequestEvent
import io.ton.walletkit.engine.infrastructure.BridgeRpcClient
import io.ton.walletkit.engine.infrastructure.callTyped
import io.ton.walletkit.engine.operations.responses.SessionEntryDto
import io.ton.walletkit.internal.constants.BridgeMethodConstants
import io.ton.walletkit.internal.constants.LogConstants
import io.ton.walletkit.internal.constants.ResponseConstants
import io.ton.walletkit.internal.util.Logger
import io.ton.walletkit.model.TONUserFriendlyAddress
import io.ton.walletkit.session.TONConnectSession
import kotlinx.serialization.json.Json
import org.json.JSONArray
import org.json.JSONObject
import java.net.URL

/**
 * Wraps TON Connect bridge calls such as processing URLs, responding to connect/sign
 * requests, and session lifecycle management.
 *
 * @property ensureInitialized Suspended callback that ensures the bridge is ready.
 * @property rpcClient Bridge RPC client.
 * @property json Serializer used for transforming Kotlin data classes to JSON payloads.
 *
 * @suppress Internal component used by [WebViewWalletKitEngine].
 */
internal class TonConnectOperations(
    private val ensureInitialized: suspend () -> Unit,
    private val rpcClient: BridgeRpcClient,
    private val json: Json,
) {

    suspend fun handleTonConnectUrl(url: String) {
        ensureInitialized()

        // Send just the URL string - walletkit expects: handleTonConnectUrl(url: string)
        rpcClient.send(BridgeMethodConstants.METHOD_HANDLE_TON_CONNECT_URL, url)
    }

    suspend fun connectionEventFromUrl(url: String): TONConnectionRequestEvent {
        ensureInitialized()
        return rpcClient.callTyped(BridgeMethodConstants.METHOD_CONNECTION_EVENT_FROM_URL, url, json)
    }

    suspend fun handleTonConnectRequest(
        messageId: String,
        method: String,
        paramsJson: String?,
        url: String?,
        responseCallback: (JSONObject) -> Unit,
        walletId: String? = null,
    ) {
        try {
            ensureInitialized()

            // Parse params - could be either JSONObject (for connect) or JSONArray (for other methods)
            val params: Any = paramsJson?.let {
                try {
                    // Try as JSONObject first (for connect method which has {manifestUrl, items, ...})
                    JSONObject(it)
                } catch (e: Exception) {
                    try {
                        // Fall back to JSONArray (for other methods)
                        JSONArray(it)
                    } catch (e2: Exception) {
                        // Last resort - empty array
                        JSONArray()
                    }
                }
            } ?: JSONArray()

            val messageInfo = JSONObject().apply {
                put("messageId", messageId)
                put("tabId", messageId)
                put(
                    "domain",
                    url?.let {
                        try {
                            val parsedUrl = URL(it)
                            "${parsedUrl.protocol}://${parsedUrl.host}" + (if (parsedUrl.port != -1 && parsedUrl.port != parsedUrl.defaultPort) ":${parsedUrl.port}" else "")
                        } catch (e: Exception) {
                            "internal-browser"
                        }
                    } ?: "internal-browser",
                )
                walletId?.let { put("walletId", it) }
            }

            val request = JSONObject().apply {
                put("id", messageId)
                put("method", method)
                put("params", params)
            }

            val argsArray = JSONArray().apply {
                put(messageInfo)
                put(request)
            }

            val result = rpcClient.call(BridgeMethodConstants.METHOD_PROCESS_INTERNAL_BROWSER_REQUEST, argsArray)
            responseCallback(result)
        } catch (e: Exception) {
            Logger.e(TAG, "Failed to process internal browser request", e)
            val errorResponse =
                JSONObject().apply {
                    put(
                        ResponseConstants.KEY_ERROR,
                        JSONObject().apply {
                            put(ResponseConstants.KEY_MESSAGE, e.message ?: ERROR_FAILED_PROCESS_REQUEST)
                            put(ResponseConstants.KEY_CODE, 500)
                        },
                    )
                }
            responseCallback(errorResponse)
        }
    }

    suspend fun approveConnect(
        event: TONConnectionRequestEvent,
        response: TONConnectionApprovalResponse? = null,
    ) {
        ensureInitialized()

        val walletAddress = event.walletAddress ?: throw WalletKitBridgeException(ERROR_WALLET_ADDRESS_REQUIRED)
        val walletId = event.walletId ?: throw WalletKitBridgeException("Wallet ID is required")

        rpcClient.send(BridgeMethodConstants.METHOD_APPROVE_CONNECT_REQUEST, listOf(event, response))
    }

    suspend fun rejectConnect(event: TONConnectionRequestEvent, reason: String?, errorCode: Int? = null) {
        ensureInitialized()
        rpcClient.send(BridgeMethodConstants.METHOD_REJECT_CONNECT_REQUEST, listOf(event, reason, errorCode))
    }

    suspend fun approveTransaction(
        event: TONSendTransactionRequestEvent,
        response: TONSendTransactionApprovalResponse? = null,
    ) {
        ensureInitialized()

        val walletAddress = event.walletAddress ?: throw WalletKitBridgeException(ERROR_WALLET_ADDRESS_REQUIRED)
        val walletId = event.walletId ?: throw WalletKitBridgeException(ERROR_WALLET_ID_REQUIRED)

        rpcClient.send(BridgeMethodConstants.METHOD_APPROVE_TRANSACTION_REQUEST, listOf(event, response))
    }

    suspend fun rejectTransaction(event: TONSendTransactionRequestEvent, reason: String?, errorCode: Int? = null) {
        ensureInitialized()

        // reason can be string or {code, message} object
        val reasonValue: Any? = if (errorCode != null) {
            mapOf("code" to errorCode, "message" to (reason ?: ""))
        } else {
            reason
        }
        rpcClient.send(BridgeMethodConstants.METHOD_REJECT_TRANSACTION_REQUEST, listOf(event, reasonValue))
    }

    suspend fun approveSignData(
        event: TONSignDataRequestEvent,
        response: TONSignDataApprovalResponse? = null,
    ) {
        ensureInitialized()

        val walletAddress = event.walletAddress ?: throw WalletKitBridgeException(ERROR_WALLET_ADDRESS_REQUIRED)
        val walletId = event.walletId ?: throw WalletKitBridgeException(ERROR_WALLET_ID_REQUIRED)

        rpcClient.send(BridgeMethodConstants.METHOD_APPROVE_SIGN_DATA_REQUEST, listOf(event, response))
    }

    suspend fun rejectSignData(event: TONSignDataRequestEvent, reason: String?, errorCode: Int? = null) {
        ensureInitialized()
        rpcClient.send(BridgeMethodConstants.METHOD_REJECT_SIGN_DATA_REQUEST, listOf(event, reason))
    }

    suspend fun listSessions(): List<TONConnectSession> {
        ensureInitialized()

        val items: List<SessionEntryDto> =
            rpcClient.callTyped(BridgeMethodConstants.METHOD_LIST_SESSIONS, null, json)
        return items.map { it.toSession() }
    }

    private fun SessionEntryDto.toSession(): TONConnectSession = TONConnectSession(
        sessionId = sessionId ?: "",
        walletId = walletId ?: "",
        walletAddress = TONUserFriendlyAddress(walletAddress ?: ""),
        createdAt = createdAt ?: "",
        lastActivityAt = lastActivityAt ?: "",
        privateKey = privateKey ?: "",
        publicKey = publicKey ?: "",
        domain = domain ?: "",
        schemaVersion = schemaVersion ?: 1,
        dAppName = dAppInfo?.name ?: dAppName,
        dAppDescription = dAppInfo?.description ?: dAppDescription,
        dAppUrl = dAppInfo?.url ?: dAppUrl,
        dAppIconUrl = dAppInfo?.iconUrl ?: dAppIconUrl,
        isJsBridge = isJsBridge ?: false,
    )

    suspend fun disconnectSession(sessionId: String?) {
        ensureInitialized()

        // Send just the sessionId string - walletkit expects: disconnect(sessionId?: string)
        rpcClient.send(BridgeMethodConstants.METHOD_DISCONNECT_SESSION, sessionId)
    }

    companion object {
        private const val TAG = "${LogConstants.TAG_WEBVIEW_ENGINE}:TonConnectOps"

        internal const val ERROR_FAILED_PROCESS_REQUEST = "Failed to process request"
        internal const val ERROR_WALLET_ADDRESS_REQUIRED = "walletAddress is required for TonConnect approval"
        internal const val ERROR_WALLET_ID_REQUIRED = "walletId is required for TonConnect approval"
    }
}
