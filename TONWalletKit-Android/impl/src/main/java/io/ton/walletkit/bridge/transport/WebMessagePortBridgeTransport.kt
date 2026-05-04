/*
 * Copyright (c) 2025 TonTech
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */
package io.ton.walletkit.bridge.transport

import android.net.Uri
import android.os.Handler
import android.webkit.WebView
import androidx.webkit.WebMessageCompat
import androidx.webkit.WebMessagePortCompat
import androidx.webkit.WebViewCompat
import androidx.webkit.WebViewFeature
import io.ton.walletkit.WalletKitBridgeException
import io.ton.walletkit.internal.constants.LogConstants
import io.ton.walletkit.internal.util.Logger
import java.util.concurrent.ConcurrentLinkedQueue
import java.util.concurrent.atomic.AtomicReference

/**
 * [BridgeTransport] backed by a `MessageChannel`-style WebMessage port pair.
 *
 * Lifecycle:
 *   1. After `WebViewClient.onPageFinished` (the JS bundle has executed and registered
 *      its `window.addEventListener('message', …)` handler), call [handOffPortToJs]
 *      from the main thread. That creates a port pair, installs the Kotlin-side message
 *      callback, and posts the JS-side port through the WebView's window.
 *   2. Inbound messages arrive on [setOnMessage] — the implementation dispatches them
 *      on [callbackHandler] so callers don't need to switch threads themselves.
 *   3. Outbound calls to [send] are queued until [handOffPortToJs] completes; once the
 *      port is live, queued messages drain in order before new ones are forwarded.
 */
internal class WebMessagePortBridgeTransport(
    private val webView: WebView,
    private val mainHandler: Handler,
    private val callbackHandler: Handler,
) : BridgeTransport {
    private val portRef = AtomicReference<WebMessagePortCompat?>(null)
    private val callbackRef = AtomicReference<((String) -> Unit)?>(null)
    private val pendingOutbound = ConcurrentLinkedQueue<String>()

    override val isReady: Boolean
        get() = portRef.get() != null

    override fun setOnMessage(callback: (json: String) -> Unit) {
        callbackRef.set(callback)
    }

    override fun send(json: String) {
        val port = portRef.get()
        if (port == null) {
            // Not yet handed off — buffer until JS picks up the channel.
            pendingOutbound.add(json)
            return
        }
        post(port, json)
    }

    override fun close() {
        portRef.getAndSet(null)?.close()
        pendingOutbound.clear()
    }

    /**
     * Creates a port pair, hands one end to JS, and starts listening on the other. Must
     * be called on the main thread (WebView APIs are main-thread-only).
     */
    fun handOffPortToJs() {
        check(WebViewFeature.isFeatureSupported(WebViewFeature.CREATE_WEB_MESSAGE_CHANNEL)) {
            "WebView does not support CREATE_WEB_MESSAGE_CHANNEL — required for the WalletKit bridge."
        }
        check(WebViewFeature.isFeatureSupported(WebViewFeature.POST_WEB_MESSAGE)) {
            "WebView does not support POST_WEB_MESSAGE — required for the WalletKit bridge."
        }
        if (portRef.get() != null) {
            return
        }

        val ports = WebViewCompat.createWebMessageChannel(webView)
        val kotlinPort = ports[0]
        val jsPort = ports[1]

        kotlinPort.setWebMessageCallback(
            callbackHandler,
            object : WebMessagePortCompat.WebMessageCallbackCompat() {
                override fun onMessage(port: WebMessagePortCompat, message: WebMessageCompat?) {
                    val data = message?.data ?: return
                    val cb = callbackRef.get() ?: run {
                        Logger.w(TAG, "Bridge port message arrived before callback was installed")
                        return
                    }
                    cb(data)
                }
            },
        )

        // Hand the JS port to the bundle. It listens via window.addEventListener('message',
        // event => event.ports[0] …). The string body is unused on the JS side but having
        // a stable marker makes the intent obvious in DevTools / tracing.
        WebViewCompat.postWebMessage(
            webView,
            WebMessageCompat(BRIDGE_HANDSHAKE_TAG, arrayOf(jsPort)),
            Uri.EMPTY,
        )

        portRef.set(kotlinPort)
        drainPending(kotlinPort)
    }

    private fun drainPending(port: WebMessagePortCompat) {
        while (true) {
            val next = pendingOutbound.poll() ?: return
            post(port, next)
        }
    }

    private fun post(port: WebMessagePortCompat, json: String) {
        try {
            // Sending must happen on the main thread per the WebView contract.
            mainHandler.post { port.postMessage(WebMessageCompat(json)) }
        } catch (e: Throwable) {
            throw WalletKitBridgeException("Failed to post bridge message: ${e.message}")
        }
    }

    private companion object {
        private const val TAG = LogConstants.TAG_WEBVIEW_ENGINE
        const val BRIDGE_HANDSHAKE_TAG = "__walletkit_bridge_init"
    }
}
