/*
 * Copyright (c) 2025 TonTech
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */
package io.ton.walletkit.bridge

import io.ton.walletkit.model.TONBase64
import io.ton.walletkit.model.TONHex
import io.ton.walletkit.model.TONRawAddress
import io.ton.walletkit.model.TONUserFriendlyAddress

// Bridge codecs for the four wrapper types. Wire format is a plain string (matches the
// existing custom KSerializers) — this layer just gives the bridge codec a uniform way
// to ask any domain value how to render itself for JS, without the bridge layer naming
// each type. Adding a new wrapper type = add it here, nothing else.

fun TONHex.encodeForBridge(): Any = value

object TONHexBridgeDecoder : BridgeDecodable<TONHex> {
    override fun decodeFromBridge(raw: Any?): TONHex? = (raw as? String)?.let(::TONHex)
}

fun TONBase64.encodeForBridge(): Any = value

object TONBase64BridgeDecoder : BridgeDecodable<TONBase64> {
    override fun decodeFromBridge(raw: Any?): TONBase64? = (raw as? String)?.let(::TONBase64)
}

fun TONUserFriendlyAddress.encodeForBridge(): Any = value

object TONUserFriendlyAddressBridgeDecoder : BridgeDecodable<TONUserFriendlyAddress> {
    override fun decodeFromBridge(raw: Any?): TONUserFriendlyAddress? =
        (raw as? String)?.let { runCatching { TONUserFriendlyAddress.parse(it) }.getOrNull() }
}

fun TONRawAddress.encodeForBridge(): Any = string

object TONRawAddressBridgeDecoder : BridgeDecodable<TONRawAddress> {
    override fun decodeFromBridge(raw: Any?): TONRawAddress? =
        (raw as? String)?.let { runCatching { TONRawAddress.parse(it) }.getOrNull() }
}
