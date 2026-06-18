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
package io.ton.walletkit.demo.presentation.ui.sheet.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import io.ton.walletkit.demo.designsystem.components.text.TonText
import io.ton.walletkit.demo.designsystem.icons.TonIcon
import io.ton.walletkit.demo.designsystem.icons.TonIconImage
import io.ton.walletkit.demo.designsystem.theme.SmoothCornerShape
import io.ton.walletkit.demo.designsystem.theme.TonTheme

@Composable
internal fun SheetHeader(
    title: String,
    onClose: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(modifier = modifier.fillMaxWidth()) {
        TonText(
            text = title,
            style = TonTheme.typography.title2,
            color = TonTheme.colors.textPrimary,
            modifier = Modifier.align(Alignment.Center),
        )
        Box(
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .size(28.dp)
                .clip(CircleShape)
                .background(TonTheme.colors.bgSecondary)
                .clickable(role = Role.Button, onClick = onClose),
            contentAlignment = Alignment.Center,
        ) {
            TonIconImage(icon = TonIcon.Close, size = 12.dp, tint = TonTheme.colors.textSecondary)
        }
    }
}

@Composable
internal fun SheetTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    placeholder: String,
    modifier: Modifier = Modifier,
    keyboardType: KeyboardType = KeyboardType.Text,
    trailing: String? = null,
    isError: Boolean = false,
    enabled: Boolean = true,
    supporting: String? = null,
) {
    Column(modifier = modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(6.dp)) {
        TonText(label, style = TonTheme.typography.footnoteCaps, color = TonTheme.colors.textSecondary)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(SmoothCornerShape(12.dp))
                .background(TonTheme.colors.bgSecondary)
                .border(
                    width = 1.dp,
                    color = if (isError) TonTheme.colors.textError else Color.Transparent,
                    shape = SmoothCornerShape(12.dp),
                )
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(modifier = Modifier.weight(1f)) {
                if (value.isEmpty()) {
                    TonText(placeholder, style = TonTheme.typography.body, color = TonTheme.colors.textTertiary)
                }
                BasicTextField(
                    value = value,
                    onValueChange = onValueChange,
                    enabled = enabled,
                    textStyle = TonTheme.typography.body.style.copy(color = TonTheme.colors.textPrimary),
                    cursorBrush = SolidColor(TonTheme.colors.bgBrand),
                    keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
            trailing?.let {
                TonText(it, style = TonTheme.typography.bodySemibold, color = TonTheme.colors.textSecondary)
            }
        }
        supporting?.let {
            TonText(
                it,
                style = TonTheme.typography.caption2Medium,
                color = if (isError) TonTheme.colors.textError else TonTheme.colors.textTertiary,
            )
        }
    }
}

@Composable
internal fun SheetLabeledRow(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
    valueColor: Color? = null,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Top,
    ) {
        TonText(
            text = label,
            style = TonTheme.typography.subheadline2,
            color = TonTheme.colors.textSecondary,
            modifier = Modifier.weight(1f),
        )
        Spacer(modifier = Modifier.width(12.dp))
        TonText(
            text = value,
            style = TonTheme.typography.bodySemibold,
            color = valueColor ?: TonTheme.colors.textPrimary,
            textAlign = TextAlign.End,
        )
    }
}

@Composable
internal fun SheetErrorBox(
    text: String,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(SmoothCornerShape(12.dp))
            .background(TonTheme.colors.bgBrandSubtle)
            .padding(16.dp),
    ) {
        TonText(text, style = TonTheme.typography.subheadline2, color = TonTheme.colors.textError)
    }
}
