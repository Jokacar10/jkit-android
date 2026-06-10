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
@file:SuppressLint("SetJavaScriptEnabled")

package io.ton.walletkit.demo.presentation.ui.screen.iframesec

import android.annotation.SuppressLint
import android.net.Uri
import android.util.Log
import android.view.ViewGroup
import android.webkit.WebResourceRequest
import android.webkit.WebResourceResponse
import android.webkit.WebView
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import androidx.webkit.WebViewClientCompat
import androidx.webkit.WebViewCompat
import androidx.webkit.WebViewFeature
import org.json.JSONObject
import java.io.ByteArrayInputStream

private const val TAG = "IframeSecWebView"

/**
 * Diagnostic WebView for the synthetic iframe-security matrix.
 *
 * - Serves the case's in-memory routes (https://parent-dapp.test/...) via shouldInterceptRequest,
 *   so navigated same-host / subdomain iframes get real, distinct origins.
 * - Wires a diagnostic bridge object `window.iframeSecLog` via WebViewCompat.addWebMessageListener.
 *   The listener receives the platform-reported sourceOrigin + isMainFrame for the posting frame —
 *   the ground-truth origin that JS cannot forge.
 */
@Composable
fun IframeSecurityWebView(
    testCase: IframeSecurityCase,
    log: IframeSecLog,
    modifier: Modifier = Modifier,
) {
    val routes = testCase.routes()
    AndroidView(
        modifier = modifier,
        factory = { context ->
            WebView(context).apply {
                layoutParams = ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT,
                )
                settings.javaScriptEnabled = true
                settings.domStorageEnabled = true

                webViewClient = object : WebViewClientCompat() {
                    override fun shouldInterceptRequest(
                        view: WebView,
                        request: WebResourceRequest,
                    ): WebResourceResponse? {
                        val url = request.url.toString().substringBefore('#').substringBefore('?')
                        val html = routes[url] ?: return null
                        return WebResourceResponse(
                            "text/html",
                            "utf-8",
                            ByteArrayInputStream(html.toByteArray(Charsets.UTF_8)),
                        )
                    }
                }

                if (WebViewFeature.isFeatureSupported(WebViewFeature.WEB_MESSAGE_LISTENER)) {
                    WebViewCompat.addWebMessageListener(
                        this,
                        "iframeSecLog",
                        setOf("*"),
                    ) { _, message, sourceOrigin, isMainFrame, _ ->
                        handleMessage(log, message.data, sourceOrigin, isMainFrame)
                    }
                } else {
                    Log.w(TAG, "WEB_MESSAGE_LISTENER not supported on this WebView")
                }

                loadUrl(testCase.mainUrl)
            }
        },
    )
}

private fun handleMessage(
    log: IframeSecLog,
    raw: String?,
    sourceOrigin: Uri?,
    isMainFrame: Boolean,
) {
    val data = raw ?: return
    val json = runCatching { JSONObject(data) }.getOrNull() ?: return

    val actualOrigin = sourceOrigin?.toString().orEmpty().ifEmpty { "null (opaque)" }
    val payload = json.optJSONObject("payload")?.toString().orEmpty()

    log.add(
        IframeSecLogEntry(
            frameLabel = json.optString("frameLabel", "?"),
            action = json.optString("action", "?"),
            claimedOrigin = json.optString("claimedOrigin", "?"),
            actualOrigin = actualOrigin,
            isMainFrame = isMainFrame,
            payload = if (payload == "{}") "" else payload,
        ),
    )
}
