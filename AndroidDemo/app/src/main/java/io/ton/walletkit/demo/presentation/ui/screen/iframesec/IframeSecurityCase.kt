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
package io.ton.walletkit.demo.presentation.ui.screen.iframesec

import android.util.Base64

/**
 * Synthetic iframe-security matrix (diagnostic only).
 *
 * Each case serves a self-contained page (main frame at https://parent-dapp.test/) plus iframes
 * that exercise one attack surface. A diagnostic bridge object (window.iframeSecLog, wired via
 * WebViewCompat.addWebMessageListener) logs every postMessage together with the REAL per-frame
 * origin the platform reports (sourceOrigin + isMainFrame) — so the claimed and actual origins can
 * be compared side by side. No real WalletKit bridge is involved here.
 */
enum class IframeSecurityCase(val routeKey: String) {
    BASELINE("baseline"),
    SAME_ORIGIN_IFRAME("sameOriginIframe"),
    PROGRAMMATIC_IFRAME("programmaticIframe"),
    SAME_ORIGIN_VIA_PATH("sameOriginViaPath"),
    SUFFIX_SPOOFED_SUBDOMAIN("suffixSpoofedSubdomain"),
    CROSS_ORIGIN_IFRAME("crossOriginIframe"),
    NESTED_IFRAME("nestedIframe"),
    SIBLING_IFRAMES("siblingIframes"),
    PAYLOAD_ORIGIN_SPOOF("payloadOriginSpoof"),
    SANDBOXED_IFRAME("sandboxedIframe");

    val title: String
        get() = when (this) {
            BASELINE -> "1. Baseline (no iframe)"
            SAME_ORIGIN_IFRAME -> "2. Same-origin iframe (srcdoc)"
            PROGRAMMATIC_IFRAME -> "2a. Programmatic iframe (no src)"
            SAME_ORIGIN_VIA_PATH -> "2b. Same-origin via path"
            SUFFIX_SPOOFED_SUBDOMAIN -> "2c. Suffix-spoofed subdomain"
            CROSS_ORIGIN_IFRAME -> "3. Cross-origin iframe (data:)"
            NESTED_IFRAME -> "4. Nested iframes (3 levels)"
            SIBLING_IFRAMES -> "5. Sibling iframes (cross-origin)"
            PAYLOAD_ORIGIN_SPOOF -> "6. Payload origin spoofing"
            SANDBOXED_IFRAME -> "7. Sandboxed iframe"
        }

    val summary: String
        get() = when (this) {
            BASELINE ->
                "Sanity check. Parent page, no iframes. Verifies the diagnostic bridge logs " +
                    "main-frame events with the expected origin."
            SAME_ORIGIN_IFRAME ->
                "srcdoc iframe inherits the parent's origin. A bridge keyed by origin alone cannot " +
                    "distinguish parent from iframe."
            PROGRAMMATIC_IFRAME ->
                "Parent creates an iframe via document.createElement('iframe') with no src/srcdoc. " +
                    "The about:blank document inherits the parent's origin."
            SAME_ORIGIN_VIA_PATH ->
                "Parent at https://parent-dapp.test/ embeds an iframe at /widget on the same host. " +
                    "Different path does not break SOP — both frames share an origin."
            SUFFIX_SPOOFED_SUBDOMAIN ->
                "Iframe loaded from https://evil.parent-dapp.test/widget. A naive " +
                    "origin.endsWith(\"parent-dapp.test\") check would accept it; exact-host match rejects."
            CROSS_ORIGIN_IFRAME ->
                "iframe loaded via data: URL gets an opaque (\"null\") origin. If approval is tied to " +
                    "the parent origin, the iframe must be rejected."
            NESTED_IFRAME ->
                "Three-level: parent -> iframe A (data:) -> iframe B (data:). The deepest frame " +
                    "triggers an event; the bridge should see B's origin, not the parent's."
            SIBLING_IFRAMES ->
                "Two opaque-origin siblings under one parent. One simulates a connect, the other a " +
                    "transaction. They must not share permission state."
            PAYLOAD_ORIGIN_SPOOF ->
                "iframe sends a payload that claims origin https://parent-dapp.test. The bridge must " +
                    "trust the platform-reported sourceOrigin, never the payload."
            SANDBOXED_IFRAME ->
                "iframe sandbox=\"allow-scripts\" (no allow-same-origin) gets a fresh opaque origin " +
                    "even with srcdoc."
        }

    val vulnerabilityClass: String
        get() = when (this) {
            BASELINE -> "(none) — reference baseline."
            SAME_ORIGIN_IFRAME -> "CWE-940. Origin-only permission is bypassed by frames that inherit origin."
            PROGRAMMATIC_IFRAME -> "Same as case 2 — origin-only permission; iframe is also fully scriptable from the parent."
            SAME_ORIGIN_VIA_PATH -> "Origin-only permission. sourceOrigin is path-agnostic; per-path control needs explicit URL tracking."
            SUFFIX_SPOOFED_SUBDOMAIN -> "CWE-1390 / weak origin verification. Substring/suffix matching + subdomain trust."
            CROSS_ORIGIN_IFRAME -> "CWE-346. Bridge must reject events whose sourceOrigin differs from the granted origin."
            NESTED_IFRAME -> "Confused-deputy / nested-frame relay. Trust does not transit through intermediate frames."
            SIBLING_IFRAMES -> "Shared permission state. Per-tab/per-WebView permissions leak between unrelated iframes."
            PAYLOAD_ORIGIN_SPOOF -> "Self-reported origin. Bridge must use the platform sourceOrigin, never the JS payload."
            SANDBOXED_IFRAME -> "Sandbox origin semantics. allow-scripts without allow-same-origin yields an opaque origin."
        }

    val observation: String
        get() = when (this) {
            BASELINE -> "Real and claimed origin both = https://parent-dapp.test. isMainFrame = true."
            SAME_ORIGIN_IFRAME -> "Real origin equals the parent's (green badge) but isMainFrame = false. Origin alone is not enough."
            PROGRAMMATIC_IFRAME -> "Real origin = https://parent-dapp.test (same as parent). isMainFrame = false."
            SAME_ORIGIN_VIA_PATH -> "Both frames report https://parent-dapp.test. The /widget path is not part of sourceOrigin."
            SUFFIX_SPOOFED_SUBDOMAIN -> "Iframe origin = https://evil.parent-dapp.test (different host). A suffix match would wrongly accept it."
            CROSS_ORIGIN_IFRAME -> "Iframe real origin is logged as opaque/null. isMainFrame = false."
            NESTED_IFRAME -> "The deepest frame logs its own opaque origin. Parent never appears for that event."
            SIBLING_IFRAMES -> "Each sibling reports a distinct opaque origin. No shared identity."
            PAYLOAD_ORIGIN_SPOOF -> "Claimed origin (red) = parent-dapp.test, but the real origin is opaque. The mismatch is the vulnerability."
            SANDBOXED_IFRAME -> "Origin is opaque even though srcdoc would normally inherit. Sandbox strips same-origin."
        }

    /** Main URL the WebView loads for this case. */
    val mainUrl: String get() = "$PARENT_ORIGIN/"

    /**
     * In-memory route table (url -> html) served via shouldInterceptRequest. Always serves the
     * main page; cases with same-host navigated iframes add extra routes.
     */
    fun routes(): Map<String, String> {
        val routes = linkedMapOf<String, String>()
        when (this) {
            BASELINE -> routes["$PARENT_ORIGIN/"] = doc(
                """
                <div class="frame parent">
                  <div class="label">PARENT</div>
                  <div class="origin">origin: <span id="op"></span></div>
                  <button onclick="send('PARENT','connect-request')">Connect (parent)</button>
                  <button onclick="send('PARENT','send-tx',{to:'EQA',amount:'0.1'})">Send TX (parent)</button>
                </div>
                <script>showOrigin('op');</script>
                """,
            )

            SAME_ORIGIN_IFRAME -> {
                val inner = doc(
                    """
                    <div class="frame iframe">
                      <div class="label">SRCDOC IFRAME — inherits parent origin</div>
                      <div class="origin">origin: <span id="oi"></span></div>
                      <button class="danger" onclick="send('IFRAME-SRCDOC','send-tx',{to:'EQA',amount:'0.5'})">Send TX with no own connect</button>
                      <script>showOrigin('oi');</script>
                    </div>
                    """,
                )
                routes["$PARENT_ORIGIN/"] = doc(
                    """
                    <div class="frame parent">
                      <div class="label">PARENT</div>
                      <div class="origin">origin: <span id="op"></span></div>
                      <button onclick="send('PARENT','connect-request')">Connect (parent)</button>
                    </div>
                    <iframe srcdoc="${srcdocEscape(inner)}"></iframe>
                    <script>showOrigin('op');</script>
                    """,
                )
            }

            PROGRAMMATIC_IFRAME -> routes["$PARENT_ORIGIN/"] = doc(
                """
                <div class="frame parent">
                  <div class="label">PARENT</div>
                  <div class="origin">origin: <span id="op"></span></div>
                  <button onclick="send('PARENT','connect-request')">Connect (parent)</button>
                  <button onclick="createIframe()">Create programmatic iframe (no src)</button>
                </div>
                <div id="host"></div>
                <script>
                  showOrigin('op');
                  function createIframe() {
                    var host = document.getElementById('host');
                    if (host.firstChild) return;
                    var iframe = document.createElement('iframe');
                    iframe.style.cssText = 'width:100%;min-height:150px;border:0;background:#fff;border-radius:8px;margin-top:8px';
                    host.appendChild(iframe);
                    var w = iframe.contentWindow; var d = iframe.contentDocument;
                    w.iframeSecLog = window.iframeSecLog;
                    d.body.innerHTML = '<div style="font-family:sans-serif;padding:10px;background:#ffe5cf;border:2px solid #ff9b5b;border-radius:10px;font-size:13px">' +
                      '<div style="font-weight:700;font-size:12px;margin-bottom:4px">PROGRAMMATIC IFRAME (no src)</div>' +
                      '<div style="font-family:monospace;font-size:11px;color:#444;margin-bottom:8px">origin: ' + (w.location.origin || 'null') + '</div>' +
                      '<button id="pgo" style="padding:6px 10px;border:0;border-radius:6px;background:#ff3b30;color:#fff;font-size:12px">Send TX (no own connect)</button></div>';
                    d.getElementById('pgo').addEventListener('click', function(){
                      try { w.iframeSecLog.postMessage(JSON.stringify({frameLabel:'IFRAME-PROG',action:'send-tx',payload:{to:'EQA',amount:'1.0'},claimedOrigin:(w.location.origin||'null'),ts:Date.now()})); } catch(e){}
                    });
                  }
                </script>
                """,
            )

            SAME_ORIGIN_VIA_PATH -> {
                routes["$PARENT_ORIGIN/"] = doc(
                    """
                    <div class="frame parent">
                      <div class="label">PARENT ($PARENT_ORIGIN/)</div>
                      <div class="origin">origin: <span id="op"></span></div>
                      <button onclick="send('PARENT','connect-request')">Connect (parent)</button>
                    </div>
                    <iframe src="$PARENT_ORIGIN/widget"></iframe>
                    <script>showOrigin('op');</script>
                    """,
                )
                routes["$PARENT_ORIGIN/widget"] = doc(
                    """
                    <div class="frame iframe">
                      <div class="label">SAME-ORIGIN WIDGET (/widget path)</div>
                      <div class="origin">origin: <span id="oi"></span></div>
                      <button class="danger" onclick="send('IFRAME-WIDGET','send-tx',{to:'EQA',amount:'1.0',note:'same origin, different path'})">Send TX without own connect</button>
                      <script>showOrigin('oi');</script>
                    </div>
                    """,
                )
            }

            SUFFIX_SPOOFED_SUBDOMAIN -> {
                routes["$PARENT_ORIGIN/"] = doc(
                    """
                    <div class="frame parent">
                      <div class="label">PARENT ($PARENT_ORIGIN/)</div>
                      <div class="origin">origin: <span id="op"></span></div>
                      <button onclick="send('PARENT','connect-request')">Connect (parent)</button>
                    </div>
                    <iframe src="$EVIL_ORIGIN/widget"></iframe>
                    <script>showOrigin('op');</script>
                    """,
                )
                routes["$EVIL_ORIGIN/widget"] = doc(
                    """
                    <div class="frame deep">
                      <div class="label">SUFFIX-SPOOFED WIDGET (evil.parent-dapp.test)</div>
                      <div class="origin">origin: <span id="oi"></span></div>
                      <button class="danger" onclick="send('IFRAME-EVIL-SUBDOMAIN','send-tx',{to:'EQA',amount:'99.0'})">Send TX</button>
                      <script>showOrigin('oi');</script>
                    </div>
                    """,
                )
            }

            CROSS_ORIGIN_IFRAME -> {
                val inner = doc(
                    """
                    <div class="frame iframe">
                      <div class="label">DATA: IFRAME — opaque origin</div>
                      <div class="origin">origin: <span id="oi"></span></div>
                      <button class="danger" onclick="send('IFRAME-DATA','send-tx',{to:'EQA',amount:'0.5'})">Send TX with no own connect</button>
                      <script>showOrigin('oi');</script>
                    </div>
                    """,
                )
                routes["$PARENT_ORIGIN/"] = doc(
                    """
                    <div class="frame parent">
                      <div class="label">PARENT</div>
                      <div class="origin">origin: <span id="op"></span></div>
                      <button onclick="send('PARENT','connect-request')">Connect (parent)</button>
                    </div>
                    <iframe src="${dataUrl(inner)}"></iframe>
                    <script>showOrigin('op');</script>
                    """,
                )
            }

            NESTED_IFRAME -> {
                val innerB = doc(
                    """
                    <div class="frame deep">
                      <div class="label">IFRAME B — deepest (data:)</div>
                      <div class="origin">origin: <span id="ob"></span></div>
                      <button class="danger" onclick="send('IFRAME-B','send-tx',{to:'EQA',amount:'9.99'})">Send TX from depth 2</button>
                      <script>showOrigin('ob');</script>
                    </div>
                    """,
                )
                val innerA = doc(
                    """
                    <div class="frame iframe">
                      <div class="label">IFRAME A — middle (data:)</div>
                      <div class="origin">origin: <span id="oa"></span></div>
                      <iframe src="${dataUrl(innerB)}" style="min-height:170px"></iframe>
                      <script>showOrigin('oa');</script>
                    </div>
                    """,
                )
                routes["$PARENT_ORIGIN/"] = doc(
                    """
                    <div class="frame parent">
                      <div class="label">PARENT</div>
                      <div class="origin">origin: <span id="op"></span></div>
                    </div>
                    <iframe src="${dataUrl(innerA)}" style="min-height:300px"></iframe>
                    <script>showOrigin('op');</script>
                    """,
                )
            }

            SIBLING_IFRAMES -> {
                val a = doc(
                    """
                    <div class="frame iframe">
                      <div class="label">SIBLING A (data:)</div>
                      <div class="origin">origin: <span id="oa"></span></div>
                      <button onclick="send('SIBLING-A','connect-request')">Connect from sibling A</button>
                      <script>showOrigin('oa');</script>
                    </div>
                    """,
                )
                val b = doc(
                    """
                    <div class="frame iframe" style="background:#dfffdf;border-color:#5bcc5b">
                      <div class="label">SIBLING B (data:)</div>
                      <div class="origin">origin: <span id="ob"></span></div>
                      <button class="danger" onclick="send('SIBLING-B','send-tx',{to:'EQA',amount:'5.0',note:'did not connect'})">Send TX from sibling B</button>
                      <script>showOrigin('ob');</script>
                    </div>
                    """,
                )
                routes["$PARENT_ORIGIN/"] = doc(
                    """
                    <div class="frame parent">
                      <div class="label">PARENT</div>
                      <div class="origin">origin: <span id="op"></span></div>
                    </div>
                    <iframe src="${dataUrl(a)}"></iframe>
                    <iframe src="${dataUrl(b)}"></iframe>
                    <script>showOrigin('op');</script>
                    """,
                )
            }

            PAYLOAD_ORIGIN_SPOOF -> {
                val inner = doc(
                    """
                    <div class="frame iframe">
                      <div class="label">DATA: IFRAME — lies in payload</div>
                      <div class="origin">real origin: <span id="oi"></span></div>
                      <button class="danger" onclick="send('IFRAME-SPOOF','send-tx',{to:'EQA',amount:'42.0'},'$PARENT_ORIGIN')">Send TX claiming origin = parent-dapp.test</button>
                      <script>showOrigin('oi');</script>
                    </div>
                    """,
                )
                routes["$PARENT_ORIGIN/"] = doc(
                    """
                    <div class="frame parent">
                      <div class="label">PARENT</div>
                      <div class="origin">origin: <span id="op"></span></div>
                    </div>
                    <iframe src="${dataUrl(inner)}"></iframe>
                    <script>showOrigin('op');</script>
                    """,
                )
            }

            SANDBOXED_IFRAME -> {
                val inner = doc(
                    """
                    <div class="frame iframe">
                      <div class="label">SANDBOXED SRCDOC (allow-scripts only)</div>
                      <div class="origin">origin: <span id="oi"></span></div>
                      <button class="danger" onclick="send('IFRAME-SANDBOX','send-tx',{to:'EQA',amount:'0.5'})">Send TX from sandboxed</button>
                      <script>showOrigin('oi');</script>
                    </div>
                    """,
                )
                routes["$PARENT_ORIGIN/"] = doc(
                    """
                    <div class="frame parent">
                      <div class="label">PARENT</div>
                      <div class="origin">origin: <span id="op"></span></div>
                    </div>
                    <iframe sandbox="allow-scripts" srcdoc="${srcdocEscape(inner)}"></iframe>
                    <script>showOrigin('op');</script>
                    """,
                )
            }
        }
        return routes
    }

    companion object {
        const val PARENT_ORIGIN = "https://parent-dapp.test"
        const val EVIL_ORIGIN = "https://evil.parent-dapp.test"

        private val HTML_HEAD = """
            <meta name="viewport" content="width=device-width,initial-scale=1">
            <style>
              *{box-sizing:border-box}
              body{font-family:sans-serif;margin:0;padding:10px;background:#f5f5f7;color:#1d1d1f;font-size:13px}
              .frame{padding:10px;border-radius:10px;margin-bottom:8px}
              .frame.parent{background:#cfe4ff;border:2px solid #5b9bff}
              .frame.iframe{background:#ffe5cf;border:2px solid #ff9b5b}
              .frame.deep{background:#ffcfcf;border:2px solid #ff5b5b}
              .label{font-size:12px;font-weight:700;margin-bottom:4px}
              .origin{font-family:monospace;font-size:11px;color:#444;margin-bottom:8px;word-break:break-all}
              button{display:inline-block;padding:6px 10px;margin:2px 2px 0 0;border-radius:6px;border:0;background:#007aff;color:#fff;font-size:12px}
              button.danger{background:#ff3b30}
              iframe{width:100%;border:0;min-height:130px;background:#fff;border-radius:8px}
            </style>
            <script>
              function send(frameLabel, action, payload, claimedOrigin) {
                var claimed = (claimedOrigin !== undefined) ? claimedOrigin : (location.origin || 'null');
                try {
                  window.iframeSecLog.postMessage(JSON.stringify({
                    frameLabel: frameLabel, action: action, payload: payload || {},
                    claimedOrigin: claimed, ts: Date.now()
                  }));
                } catch (e) { console.error('Bridge unavailable:', String(e)); }
              }
              function showOrigin(id) {
                var el = document.getElementById(id);
                if (el) el.textContent = location.origin || 'null';
              }
            </script>
        """.trimIndent()

        private fun doc(body: String): String =
            "<!doctype html><html><head>$HTML_HEAD</head><body>${body.trimIndent()}</body></html>"

        private fun dataUrl(html: String): String {
            val b64 = Base64.encodeToString(html.toByteArray(Charsets.UTF_8), Base64.NO_WRAP)
            return "data:text/html;charset=utf-8;base64,$b64"
        }

        private fun srcdocEscape(html: String): String =
            html.replace("&", "&amp;").replace("\"", "&quot;")
    }
}
