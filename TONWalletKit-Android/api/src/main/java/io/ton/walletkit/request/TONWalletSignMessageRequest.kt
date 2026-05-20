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
package io.ton.walletkit.request

import io.ton.walletkit.api.generated.TONEmbeddedSignMessageRequestEvent
import io.ton.walletkit.api.generated.TONSignMessageApprovalResponse
import io.ton.walletkit.api.generated.TONSignMessageRequestEvent

/**
 * A sign-message (sign-only) transaction request from a dApp. Mirrors iOS
 * `TONWalletSignMessageRequest`. Unlike [TONWalletTransactionRequest], the signed BoC is
 * returned to the dApp rather than broadcast.
 *
 * When this request is the embedded follow-up of a connect-with-intent flow, [event] is
 * projected from the embedded event but [approve] / [reject] route the embedded shape to
 * the bridge so the JS side can finalise the connect session.
 */
class TONWalletSignMessageRequest internal constructor(
    val event: TONSignMessageRequestEvent,
    private val embeddedEvent: TONEmbeddedSignMessageRequestEvent?,
    private val handler: RequestHandler,
) {
    constructor(
        event: TONSignMessageRequestEvent,
        handler: RequestHandler,
    ) : this(event = event, embeddedEvent = null, handler = handler)

    internal constructor(
        embeddedEvent: TONEmbeddedSignMessageRequestEvent,
        handler: RequestHandler,
    ) : this(event = embeddedEvent.requestEvent, embeddedEvent = embeddedEvent, handler = handler)

    suspend fun approve(response: TONSignMessageApprovalResponse? = null) {
        if (embeddedEvent != null) {
            handler.approveSignMessage(embeddedEvent, response)
        } else {
            handler.approveSignMessage(event, response)
        }
    }

    suspend fun reject(reason: String? = null, errorCode: Int? = null) {
        if (embeddedEvent != null) {
            handler.rejectSignMessage(embeddedEvent, reason, errorCode)
        } else {
            handler.rejectSignMessage(event, reason, errorCode)
        }
    }
}
