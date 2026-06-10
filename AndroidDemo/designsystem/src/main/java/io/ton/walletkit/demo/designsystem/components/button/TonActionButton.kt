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
package io.ton.walletkit.demo.designsystem.components.button

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import io.ton.walletkit.demo.designsystem.components.text.TonText
import io.ton.walletkit.demo.designsystem.icons.TonIcon
import io.ton.walletkit.demo.designsystem.icons.TonIconImage
import io.ton.walletkit.demo.designsystem.theme.SmoothCornerShape
import io.ton.walletkit.demo.designsystem.theme.TonTheme

enum class TonActionButtonStyle { Primary, Secondary, Tertiary }

@Composable
fun TonActionButton(
    icon: TonIcon,
    title: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    style: TonActionButtonStyle = TonActionButtonStyle.Primary,
) {
    val background: Color
    val iconColor: Color
    val labelColor: Color
    when (style) {
        TonActionButtonStyle.Primary -> {
            background = TonTheme.colors.bgBrand
            iconColor = TonTheme.colors.textOnBrand
            labelColor = TonTheme.colors.textOnBrand
        }
        TonActionButtonStyle.Secondary -> {
            background = TonTheme.colors.bgBrandFillSubtle
            iconColor = TonTheme.colors.textBrand
            labelColor = TonTheme.colors.textBrand
        }
        TonActionButtonStyle.Tertiary -> {
            background = TonTheme.colors.bgSecondary
            iconColor = TonTheme.colors.textBrand
            labelColor = TonTheme.colors.textPrimary
        }
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(SmoothCornerShape(20.dp))
            .background(background)
            .clickable(onClick = onClick)
            .padding(vertical = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        TonIconImage(icon = icon, size = 24.dp, tint = iconColor)
        TonText(
            text = title,
            style = TonTheme.typography.subheadline2Medium,
            color = labelColor,
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun TonActionButtonPreview() {
    TonTheme {
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.padding(16.dp),
        ) {
            TonActionButton(
                icon = TonIcon.ArrowUpCircle,
                title = "Send",
                style = TonActionButtonStyle.Primary,
                onClick = {},
                modifier = Modifier.weight(1f),
            )
            TonActionButton(
                icon = TonIcon.ArrowDownCircle,
                title = "Receive",
                style = TonActionButtonStyle.Secondary,
                onClick = {},
                modifier = Modifier.weight(1f),
            )
            TonActionButton(
                icon = TonIcon.SwitchVertical24,
                title = "Swap",
                style = TonActionButtonStyle.Secondary,
                onClick = {},
                modifier = Modifier.weight(1f),
            )
        }
    }
}
