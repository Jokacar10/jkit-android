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

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.ton.walletkit.demo.designsystem.theme.TonTheme
import io.ton.walletkit.demo.presentation.ui.screen.SubScreenTopBar
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private val timeFormat = SimpleDateFormat("HH:mm:ss.SSS", Locale.US)

/**
 * Synthetic iframe-security matrix. A list of cases; tapping one opens a detail screen with the
 * diagnostic WebView and a live log comparing the JS-claimed origin against the platform-reported
 * (ground-truth) frame origin.
 */
@Composable
fun WalletKitIframeSecurityScreen(
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var selected by remember { mutableStateOf<IframeSecurityCase?>(null) }

    val current = selected
    if (current != null) {
        BackHandler { selected = null }
        IframeSecurityCaseScreen(testCase = current, onBack = { selected = null }, modifier = modifier)
        return
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(TonTheme.colors.bgSecondary),
    ) {
        SubScreenTopBar(title = "Iframe Security", onBack = onBack)
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            item {
                Text(
                    text = "Each case serves a self-contained page (main frame at " +
                        "https://parent-dapp.test/) with iframes that exercise a specific attack " +
                        "surface. A diagnostic bridge logs every postMessage with the real frame " +
                        "origin (WebViewCompat sourceOrigin), so claimed vs actual can be compared.",
                    style = TonTheme.typography.footnote.style,
                    color = TonTheme.colors.textSecondary,
                )
            }
            items(IframeSecurityCase.entries) { testCase ->
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(TonTheme.colors.bgPrimary)
                        .clickable { selected = testCase }
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    Text(
                        text = testCase.title,
                        style = TonTheme.typography.bodySemibold.style,
                        color = TonTheme.colors.textPrimary,
                    )
                    Text(
                        text = testCase.summary,
                        style = TonTheme.typography.footnote.style,
                        color = TonTheme.colors.textSecondary,
                    )
                }
            }
        }
    }
}

@Composable
private fun IframeSecurityCaseScreen(
    testCase: IframeSecurityCase,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val log = remember(testCase) { IframeSecLog() }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(TonTheme.colors.bgSecondary),
    ) {
        SubScreenTopBar(title = testCase.title, onBack = onBack)

        DescriptionPanel(testCase)
        HorizontalDivider()

        IframeSecurityWebView(
            testCase = testCase,
            log = log,
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
        )

        HorizontalDivider()
        LogPanel(log = log, modifier = Modifier.fillMaxWidth())
    }
}

@Composable
private fun DescriptionPanel(testCase: IframeSecurityCase) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(TonTheme.colors.bgPrimary)
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Field("Summary", testCase.summary)
        Field("Class", testCase.vulnerabilityClass)
        Field("What to watch", testCase.observation)
    }
}

@Composable
private fun Field(title: String, body: String) {
    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
        Text(title, style = TonTheme.typography.caption1.style, color = TonTheme.colors.textPrimary, fontWeight = FontWeight.Bold)
        Text(body, style = TonTheme.typography.caption1.style, color = TonTheme.colors.textSecondary)
    }
}

@Composable
private fun LogPanel(log: IframeSecLog, modifier: Modifier = Modifier) {
    val listState = rememberLazyListState()
    LaunchedEffect(log.entries.size) {
        if (log.entries.isNotEmpty()) listState.animateScrollToItem(log.entries.size - 1)
    }

    Column(modifier = modifier.height(240.dp)) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(TonTheme.colors.bgSecondary)
                .padding(horizontal = 12.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = "Bridge log (${log.entries.size})",
                style = TonTheme.typography.caption1.style,
                color = TonTheme.colors.textPrimary,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.weight(1f),
            )
            TextButton(onClick = { log.clear() }, enabled = log.entries.isNotEmpty()) {
                Text("Clear")
            }
        }

        if (log.entries.isEmpty()) {
            Text(
                text = "No bridge messages yet. Tap a button inside the WebView above.",
                style = TonTheme.typography.caption1.style,
                color = TonTheme.colors.textSecondary,
                modifier = Modifier.padding(12.dp),
            )
        } else {
            LazyColumn(
                state = listState,
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(12.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                items(log.entries, key = { it.id }) { entry -> LogRow(entry) }
            }
        }
    }
}

@Composable
internal fun LogRow(entry: IframeSecLogEntry) {
    val claimedMatches = entry.claimedOrigin == entry.actualOrigin
    val badgeColor = when {
        entry.isNative -> Color(0xFF2D7DF6)
        claimedMatches -> Color(0xFF2EA043)
        else -> Color(0xFFE5484D)
    }
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(6.dp))
            .background(if (entry.isNative) Color(0x142D7DF6) else TonTheme.colors.bgPrimary)
            .border(0.5.dp, Color(0x33808080), RoundedCornerShape(6.dp))
            .padding(8.dp),
        verticalArrangement = Arrangement.spacedBy(3.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(4.dp))
                    .background(badgeColor.copy(alpha = 0.18f))
                    .padding(horizontal = 6.dp, vertical = 2.dp),
            ) {
                Text(entry.frameLabel, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = badgeColor)
            }
            Text(
                text = "  ${entry.action}",
                fontSize = 11.sp,
                fontFamily = FontFamily.Monospace,
                color = TonTheme.colors.textPrimary,
                modifier = Modifier.weight(1f),
            )
            if (!entry.isMainFrame && !entry.isNative) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(3.dp))
                        .background(Color(0x33FF9800))
                        .padding(horizontal = 4.dp, vertical = 1.dp),
                ) {
                    Text("iframe", fontSize = 9.sp, color = Color(0xFFE08600))
                }
            }
            Text(
                text = timeFormat.format(Date(entry.timestamp)),
                fontSize = 10.sp,
                fontFamily = FontFamily.Monospace,
                color = TonTheme.colors.textTertiary,
            )
        }
        OriginRow("real:", entry.actualOrigin, TonTheme.colors.textPrimary)
        if (!entry.isNative) {
            OriginRow("claimed:", entry.claimedOrigin, if (claimedMatches) TonTheme.colors.textPrimary else Color(0xFFE5484D))
        }
        if (entry.payload.isNotEmpty()) {
            Text(
                text = entry.payload,
                fontSize = 10.sp,
                fontFamily = FontFamily.Monospace,
                color = TonTheme.colors.textSecondary,
            )
        }
    }
}

@Composable
private fun OriginRow(title: String, value: String, color: Color) {
    Row {
        Text(title, fontSize = 10.sp, color = TonTheme.colors.textTertiary)
        Text("  $value", fontSize = 10.sp, fontFamily = FontFamily.Monospace, color = color)
    }
}
