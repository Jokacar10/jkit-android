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
package io.ton.walletkit.demo.presentation.ui.sheet

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import io.ton.walletkit.demo.designsystem.components.button.TonButton
import io.ton.walletkit.demo.designsystem.components.button.TonButtonConfig
import io.ton.walletkit.demo.designsystem.components.segmentedcontrol.TonSegmentedControl
import io.ton.walletkit.demo.designsystem.icons.TonIcon
import io.ton.walletkit.demo.designsystem.icons.TonIconImage
import io.ton.walletkit.demo.designsystem.theme.TonTheme
import io.ton.walletkit.demo.presentation.ui.sheet.components.SheetErrorBox
import io.ton.walletkit.demo.presentation.ui.sheet.components.SheetHeader
import io.ton.walletkit.demo.presentation.ui.sheet.components.SheetLabeledRow
import io.ton.walletkit.demo.presentation.ui.sheet.components.SheetTextField
import io.ton.walletkit.demo.presentation.ui.sheet.components.TonConnectSheetScaffold
import io.ton.walletkit.demo.presentation.ui.sheet.components.TonConnectSheetSection
import io.ton.walletkit.demo.presentation.viewmodel.SwapViewModel
import io.ton.walletkit.demo.presentation.viewmodel.SwapViewModel.PriceImpactLevel
import io.ton.walletkit.demo.presentation.viewmodel.SwapViewModel.SwapProvider
import java.util.Locale

@Composable
fun SwapSheet(
    viewModel: SwapViewModel,
    onDismiss: () -> Unit,
) {
    val state by viewModel.state.collectAsState()
    val isBusy = state.isLoadingQuote || state.isSwapping

    TonConnectSheetScaffold(
        footer = {
            TonButton(
                text = state.buttonTitle,
                onClick = { viewModel.buttonAction() },
                config = TonButtonConfig.Primary.isLoading(isBusy),
                enabled = if (state.currentQuote != null) state.canSwap else state.canGetQuote,
            )
        },
    ) {
        SheetHeader(title = "Swap", onClose = onDismiss)

        TonSegmentedControl(
            selection = state.selectedProvider,
            items = SwapProvider.entries,
            title = { it.displayName },
            onSelect = { if (!isBusy) viewModel.setProvider(it) },
            modifier = Modifier.fillMaxWidth(),
        )

        SheetTextField(
            value = state.fromAmount,
            onValueChange = { viewModel.setFromAmount(it) },
            label = "From",
            placeholder = "0",
            keyboardType = KeyboardType.Decimal,
            trailing = state.fromSymbol,
            enabled = !isBusy,
        )

        Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(TonTheme.colors.bgSecondary)
                    .clickable(role = Role.Button, enabled = !isBusy) { viewModel.swapTokens() },
                contentAlignment = Alignment.Center,
            ) {
                TonIconImage(icon = TonIcon.SwitchVertical24, size = 20.dp, tint = TonTheme.colors.textBrand)
            }
        }

        SheetTextField(
            value = state.toAmount,
            onValueChange = { viewModel.setToAmount(it) },
            label = "To",
            placeholder = "0",
            keyboardType = KeyboardType.Decimal,
            trailing = state.toSymbol,
            enabled = state.isReverseSwap && !isBusy,
        )

        TonConnectSheetSection(label = "Details") {
            SheetLabeledRow("Slippage", "${state.slippageBps / 100}%")
            state.currentQuote?.let { quote ->
                SheetLabeledRow(
                    "Provider",
                    quote.providerId.replaceFirstChar { it.uppercase(Locale.getDefault()) },
                )
                SheetLabeledRow("Min received", "${quote.minReceived} ${quote.toToken.symbol ?: ""}")
                quote.priceImpact?.let { impact ->
                    val impactText = String.format(Locale.getDefault(), "%.2f%%", impact / 100.0)
                    val impactColor = when (state.priceImpactLevel) {
                        PriceImpactLevel.LOW -> TonTheme.colors.textSuccess
                        PriceImpactLevel.MEDIUM -> PriceImpactMediumColor
                        PriceImpactLevel.HIGH -> TonTheme.colors.textError
                    }
                    SheetLabeledRow("Price impact", impactText, valueColor = impactColor)
                }
                quote.expiresAt?.let { expiresAt ->
                    val remaining = expiresAt - (System.currentTimeMillis() / 1000).toInt()
                    if (remaining > 0) {
                        SheetLabeledRow("Expires in", "${remaining}s")
                    }
                }
            }
        }

        state.error?.let { SheetErrorBox(it) }
    }
}

private val PriceImpactMediumColor = Color(0xFFF57C00)
