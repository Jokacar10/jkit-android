/*
 * Copyright (c) 2025 TonTech
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */
package io.ton.walletkit.bridge.transport

/**
 * Carries JSON-encoded bridge envelopes between Kotlin and the JS bundle running in a
 * WebView. Replaces the previous combination of `evaluateJavascript("__walletkitCall(...)")`
 * script injection and `@JavascriptInterface postMessage(...)` callback with a single
 * symmetrical channel.
 *
 * Implementations are responsible for handing the channel off to JS during WebView page
 * load and surfacing inbound messages via [setOnMessage].
 */
internal interface BridgeTransport {
    /** Send a JSON-encoded envelope to JS. Throws if the channel is not yet ready. */
    fun send(json: String)

    /**
     * Install the inbound callback. The callback is invoked on an arbitrary thread —
     * implementations dispatch to the appropriate one.
     */
    fun setOnMessage(callback: (json: String) -> Unit)

    /** Whether the channel has been handed off to JS and is usable. */
    val isReady: Boolean

    /** Tear down the channel; subsequent [send] calls fail. */
    fun close()
}
