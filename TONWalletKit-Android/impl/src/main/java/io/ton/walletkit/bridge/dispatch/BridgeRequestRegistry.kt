/*
 * Copyright (c) 2025 TonTech
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */
package io.ton.walletkit.bridge.dispatch

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.serializer

/**
 * Registry for reverse-RPC handlers (calls JS makes into Kotlin).
 *
 * Replaces the central `when (method) { … }` switch in `MessageDispatcher`. Each handler
 * registers itself once at engine startup; adding a new reverse-RPC method is a single
 * `register(...)` call plus a request data class — no hand-edited dispatch table.
 *
 * Handlers return the response as a JSON-encoded `String`. That preserves the existing
 * wire contract with the JS side (which also expects a JSON-encoded result) while we
 * land the type-driven dispatch independently of the response-format reshape.
 */
internal class BridgeRequestRegistry(private val json: Json) {
    private val handlers = HashMap<String, suspend (JsonElement) -> String>()

    /** Register a handler that decodes its params manually (for quirky wire formats). */
    fun register(method: String, handler: suspend (JsonElement) -> String) {
        require(handlers.put(method, handler) == null) {
            "Duplicate reverse-RPC handler registration for method: $method"
        }
    }

    /** Register a typed handler. Params are decoded into [T] via kotlinx.serialization. */
    inline fun <reified T> registerTyped(method: String, crossinline handler: suspend (T) -> String) {
        // Capture the serializer at the inline call-site; the lambda below is not itself
        // inlined, so the reified `T` isn't usable inside it.
        val serializer = json.serializersModule.serializer<T>()
        register(method) { raw -> handler(json.decodeFromJsonElement(serializer, raw)) }
    }

    suspend fun dispatch(method: String, params: JsonElement): String {
        val handler = handlers[method]
            ?: throw IllegalArgumentException("Unknown reverse-RPC method: $method")
        return handler(params)
    }

    fun knownMethods(): Set<String> = handlers.keys.toSet()
}
