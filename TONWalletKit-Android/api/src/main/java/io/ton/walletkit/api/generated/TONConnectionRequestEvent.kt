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
@file:Suppress(
    "ArrayInDataClass",
    "EnumEntryName",
    "RemoveRedundantQualifierName",
    "UnusedImport",
)

package io.ton.walletkit.api.generated

import io.ton.walletkit.model.TONUserFriendlyAddress
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Event containing a connection request from a dApp via TON Connect.
 *
 * @param id Unique identifier for the bridge event
 * @param requestedItems Items requested by the dApp (e.g., wallet address, proof)
 * @param preview
 * @param from
 * @param walletAddress
 * @param walletId Wallet identifier associated with the event
 * @param domain Domain of the dApp that initiated the event
 * @param isJsBridge Whether the event originated from JS Bridge (injected provider)
 * @param tabId Browser tab ID for JS Bridge events
 * @param sessionId Session identifier for the connection
 * @param isLocal
 * @param messageId
 * @param traceId
 * @param dAppInfo
 * @param returnStrategy Raw TonConnect return strategy string.
 * @param embeddedRequest
 */
@Serializable
data class TONConnectionRequestEvent(

    /* Unique identifier for the bridge event */
    @SerialName(value = "id")
    var id: kotlin.String,

    /* Items requested by the dApp (e.g., wallet address, proof) */
    @SerialName(value = "requestedItems")
    var requestedItems: kotlin.collections.List<TONConnectionRequestEventRequestedItem>,

    @SerialName(value = "preview")
    var preview: TONConnectionRequestEventPreview,

    @SerialName(value = "from")
    val from: kotlin.String? = null,

    @SerialName(value = "walletAddress")
    var walletAddress: io.ton.walletkit.model.TONUserFriendlyAddress? = null,

    /* Wallet identifier associated with the event */
    @SerialName(value = "walletId")
    var walletId: kotlin.String? = null,

    /* Domain of the dApp that initiated the event */
    @SerialName(value = "domain")
    var domain: kotlin.String? = null,

    /* Whether the event originated from JS Bridge (injected provider) */
    @SerialName(value = "isJsBridge")
    var isJsBridge: kotlin.Boolean? = null,

    /* Browser tab ID for JS Bridge events */
    @SerialName(value = "tabId")
    var tabId: kotlin.String? = null,

    /* Session identifier for the connection */
    @SerialName(value = "sessionId")
    var sessionId: kotlin.String? = null,

    @SerialName(value = "isLocal")
    var isLocal: kotlin.Boolean? = null,

    @SerialName(value = "messageId")
    var messageId: kotlin.String? = null,

    @SerialName(value = "traceId")
    var traceId: kotlin.String? = null,

    @SerialName(value = "dAppInfo")
    var dAppInfo: TONDAppInfo? = null,

    /* Raw TonConnect return strategy string. */
    @SerialName(value = "returnStrategy")
    var returnStrategy: kotlin.String? = null,

    @SerialName(value = "embeddedRequest")
    var embeddedRequest: TONEmbeddedRequest? = null,

) {

    companion object
}
