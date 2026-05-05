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
package io.ton.walletkit.bridge

import io.ton.walletkit.model.TONBase64
import io.ton.walletkit.model.TONHex
import io.ton.walletkit.model.TONRawAddress
import io.ton.walletkit.model.TONTokenAmount
import io.ton.walletkit.model.TONUserFriendlyAddress

fun TONHex.encodeForBridge(): Any = value
fun TONBase64.encodeForBridge(): Any = value
fun TONUserFriendlyAddress.encodeForBridge(): Any = value
fun TONRawAddress.encodeForBridge(): Any = string
fun TONTokenAmount.encodeForBridge(): Any = value

internal object TONHexBridgeDecoder : BridgeDecodable<TONHex> {
    override fun decodeFromBridge(raw: Any?): TONHex? = (raw as? String)?.let(::TONHex)
}

internal object TONBase64BridgeDecoder : BridgeDecodable<TONBase64> {
    override fun decodeFromBridge(raw: Any?): TONBase64? = (raw as? String)?.let(::TONBase64)
}

internal object TONUserFriendlyAddressBridgeDecoder : BridgeDecodable<TONUserFriendlyAddress> {
    override fun decodeFromBridge(raw: Any?): TONUserFriendlyAddress? =
        (raw as? String)?.let { runCatching { TONUserFriendlyAddress.parse(it) }.getOrNull() }
}

internal object TONRawAddressBridgeDecoder : BridgeDecodable<TONRawAddress> {
    override fun decodeFromBridge(raw: Any?): TONRawAddress? =
        (raw as? String)?.let { runCatching { TONRawAddress.parse(it) }.getOrNull() }
}

internal object TONTokenAmountBridgeDecoder : BridgeDecodable<TONTokenAmount> {
    override fun decodeFromBridge(raw: Any?): TONTokenAmount? = when (raw) {
        is String -> TONTokenAmount(raw)
        is Number -> TONTokenAmount(raw.toString())
        else -> null
    }
}

internal fun registerDomainTypeBridgeDecoders() {
    BridgeDecoders.register(TONHex::class, TONHexBridgeDecoder)
    BridgeDecoders.register(TONBase64::class, TONBase64BridgeDecoder)
    BridgeDecoders.register(TONUserFriendlyAddress::class, TONUserFriendlyAddressBridgeDecoder)
    BridgeDecoders.register(TONRawAddress::class, TONRawAddressBridgeDecoder)
    BridgeDecoders.register(TONTokenAmount::class, TONTokenAmountBridgeDecoder)
}
