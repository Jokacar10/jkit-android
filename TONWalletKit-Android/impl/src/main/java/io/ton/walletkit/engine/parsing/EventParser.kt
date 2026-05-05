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
package io.ton.walletkit.engine.parsing

import io.ton.walletkit.api.generated.TONBalanceUpdate
import io.ton.walletkit.api.generated.TONConnectionRequestEvent
import io.ton.walletkit.api.generated.TONDisconnectionEvent
import io.ton.walletkit.api.generated.TONDisconnectionEventPreview
import io.ton.walletkit.api.generated.TONJettonUpdate
import io.ton.walletkit.api.generated.TONRequestErrorEvent
import io.ton.walletkit.api.generated.TONSendTransactionRequestEvent
import io.ton.walletkit.api.generated.TONSignDataRequestEvent
import io.ton.walletkit.api.generated.TONStreamingUpdate
import io.ton.walletkit.api.generated.TONTransactionsUpdate
import io.ton.walletkit.bridge.decodeFromBridge
import io.ton.walletkit.core.streaming.StreamingEvent
import io.ton.walletkit.engine.WalletKitEngine
import io.ton.walletkit.event.TONWalletKitEvent
import io.ton.walletkit.internal.constants.EventTypeConstants
import io.ton.walletkit.internal.constants.JsonConstants
import io.ton.walletkit.internal.constants.LogConstants
import io.ton.walletkit.internal.constants.ResponseConstants
import io.ton.walletkit.internal.util.Logger
import io.ton.walletkit.request.TONWalletConnectionRequest
import io.ton.walletkit.request.TONWalletSignDataRequest
import io.ton.walletkit.request.TONWalletTransactionRequest
import kotlinx.serialization.json.Json
import org.json.JSONObject

/**
 * Parses raw JSON event payloads from the JS bridge into typed SDK events.
 *
 * @suppress Internal component. Use through [WebViewWalletKitEngine].
 */
internal class EventParser(
    private val json: Json,
    private val engine: WalletKitEngine,
) {
    fun parseEvent(type: String, data: JSONObject): TONWalletKitEvent? = when (type) {
        EventTypeConstants.EVENT_CONNECT_REQUEST ->
            TONWalletKitEvent.ConnectRequest(
                TONWalletConnectionRequest(decode<TONConnectionRequestEvent>(data), engine),
            )

        EventTypeConstants.EVENT_TRANSACTION_REQUEST ->
            TONWalletKitEvent.SendTransactionRequest(
                TONWalletTransactionRequest(decode<TONSendTransactionRequestEvent>(data), engine),
            )

        EventTypeConstants.EVENT_SIGN_DATA_REQUEST ->
            TONWalletKitEvent.SignDataRequest(
                TONWalletSignDataRequest(decode<TONSignDataRequestEvent>(data), engine),
            )

        EventTypeConstants.EVENT_REQUEST_ERROR ->
            TONWalletKitEvent.RequestError(decode<TONRequestErrorEvent>(data))

        EventTypeConstants.EVENT_DISCONNECT -> {
            val sessionId = data.optNullableString(ResponseConstants.KEY_SESSION_ID)
                ?: data.optNullableString(JsonConstants.KEY_ID)
                ?: return null
            Logger.d(TAG, "Disconnect event received. sessionId=$sessionId, dataKeys=${data.keys().asSequence().toList()}")
            TONWalletKitEvent.Disconnect(
                TONDisconnectionEvent(
                    id = sessionId,
                    sessionId = sessionId,
                    preview = TONDisconnectionEventPreview(),
                ),
            )
        }

        else -> null
    }

    fun parseStreamingEvent(type: String, data: JSONObject): StreamingEvent? {
        val subscriptionId = data.optString("subscriptionId")
        return when (type) {
            EventTypeConstants.EVENT_STREAMING_UPDATE ->
                decodeUpdate<TONStreamingUpdate>(type, data)?.let { StreamingEvent.Update(subscriptionId, it) }
            EventTypeConstants.EVENT_STREAMING_BALANCE_UPDATE ->
                decodeUpdate<TONBalanceUpdate>(type, data)?.let { StreamingEvent.BalanceUpdate(subscriptionId, it) }
            EventTypeConstants.EVENT_STREAMING_TRANSACTIONS_UPDATE ->
                decodeUpdate<TONTransactionsUpdate>(type, data)?.let { StreamingEvent.TransactionsUpdate(subscriptionId, it) }
            EventTypeConstants.EVENT_STREAMING_JETTONS_UPDATE ->
                decodeUpdate<TONJettonUpdate>(type, data)?.let { StreamingEvent.JettonsUpdate(subscriptionId, it) }
            EventTypeConstants.EVENT_STREAMING_CONNECTION_CHANGE ->
                StreamingEvent.ConnectionChange(subscriptionId, data.optBoolean("connected", false))
            else -> null
        }
    }

    private inline fun <reified T : Any> decode(data: JSONObject): T = json.decodeFromBridge(data)

    private inline fun <reified T : Any> decodeUpdate(type: String, data: JSONObject): T? = try {
        val update = data.optJSONObject("update") ?: error("Missing 'update' field")
        json.decodeFromBridge<T>(update)
    } catch (e: Exception) {
        Logger.e(TAG, "Failed to parse $type: ${e.message}", e)
        null
    }

    private fun JSONObject.optNullableString(key: String): String? = when (val value = opt(key)) {
        null, JSONObject.NULL -> null
        else -> value.toString()
    }

    private companion object {
        private const val TAG = LogConstants.TAG_WEBVIEW_ENGINE
    }
}
