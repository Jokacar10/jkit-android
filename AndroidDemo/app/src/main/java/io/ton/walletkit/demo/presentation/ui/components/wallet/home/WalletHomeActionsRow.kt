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
package io.ton.walletkit.demo.presentation.ui.components.wallet.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import io.ton.walletkit.demo.designsystem.components.button.TonActionButton
import io.ton.walletkit.demo.designsystem.components.button.TonActionButtonStyle
import io.ton.walletkit.demo.designsystem.icons.TonIcon

@Composable
fun WalletHomeActionsRow(
    onSend: () -> Unit,
    onSwap: () -> Unit,
    onStake: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        TonActionButton(
            icon = TonIcon.Send,
            title = "Send",
            style = TonActionButtonStyle.Tertiary,
            onClick = onSend,
            modifier = Modifier.weight(1f),
        )
        TonActionButton(
            icon = TonIcon.SwitchVertical24,
            title = "Swap",
            style = TonActionButtonStyle.Tertiary,
            onClick = onSwap,
            modifier = Modifier.weight(1f),
        )
        TonActionButton(
            icon = TonIcon.Stake,
            title = "Stake",
            style = TonActionButtonStyle.Tertiary,
            onClick = onStake,
            modifier = Modifier.weight(1f),
        )
    }
}
