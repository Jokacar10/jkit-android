/*
 * Copyright (c) 2025 TonTech
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */
package io.ton.walletkit.bridge

import org.json.JSONArray
import org.json.JSONObject

/**
 * Mirror of iOS's `JSValueEncodable` protocol — types that know how to render themselves
 * for the JS bridge. The bridge codec walks values, calling `encodeForBridge()` on anything
 * that implements this so the bridge layer never special-cases domain types.
 *
 * Allowed return shapes (anything else throws at codec time):
 *   - kotlin primitives: String, Int, Long, Double, Float, Short, Byte, Boolean
 *   - null
 *   - org.json.JSONObject / JSONArray
 *   - List<Any?> / Map<String, Any?> (the codec recurses, applying encodeForBridge again)
 *   - Another BridgeEncodable (the codec re-applies)
 */
interface BridgeEncodable {
    fun encodeForBridge(): Any?
}

/**
 * Mirror of iOS's `JSValueDecodable` protocol — companion-object factory that knows how
 * to lift a value coming back from JS into the typed Kotlin domain object. `raw` is the
 * codec's already-unwrapped representation: String / Number / Boolean / JSONObject /
 * JSONArray / Map / List / null. Returns null if the shape doesn't match (mirrors the
 * Swift `from` returning `Self?`).
 */
interface BridgeDecodable<T> {
    fun decodeFromBridge(raw: Any?): T?
}

/** Marker used by the codec when an object encodes to a JSON tree. */
typealias BridgeJson = JSONObject

/** Marker used by the codec when an object encodes to a JSON array. */
typealias BridgeJsonArray = JSONArray
