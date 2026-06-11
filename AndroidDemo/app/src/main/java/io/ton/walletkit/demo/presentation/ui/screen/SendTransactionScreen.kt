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
package io.ton.walletkit.demo.presentation.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import io.ton.walletkit.demo.designsystem.components.button.TonButton
import io.ton.walletkit.demo.designsystem.components.segmentedcontrol.TonSegmentedControl
import io.ton.walletkit.demo.designsystem.components.text.TonText
import io.ton.walletkit.demo.designsystem.components.toggle.TonSwitch
import io.ton.walletkit.demo.designsystem.theme.SmoothCornerShape
import io.ton.walletkit.demo.designsystem.theme.TonTheme
import io.ton.walletkit.demo.presentation.model.WalletSummary
import io.ton.walletkit.demo.presentation.ui.sheet.components.SheetErrorBox
import io.ton.walletkit.demo.presentation.ui.sheet.components.SheetHeader
import io.ton.walletkit.demo.presentation.ui.sheet.components.SheetTextField
import io.ton.walletkit.demo.presentation.ui.sheet.components.TonConnectSheetScaffold
import io.ton.walletkit.demo.presentation.ui.sheet.components.TonConnectSheetSection
import io.ton.walletkit.demo.presentation.util.abbreviated
import io.ton.walletkit.demo.presentation.viewmodel.SendCurrency

@Composable
fun SendTransactionScreen(
    wallet: WalletSummary,
    onBack: () -> Unit,
    onSend: (recipient: String, amount: String, comment: String, currency: SendCurrency, gasless: Boolean) -> Unit,
    error: String?,
    isLoading: Boolean,
) {
    var currency by remember { mutableStateOf(SendCurrency.TON) }
    var recipient by remember { mutableStateOf("") }
    var amount by remember { mutableStateOf("") }
    var comment by remember { mutableStateOf("") }
    var gasless by remember { mutableStateOf(false) }

    val recipientValid = recipient.isEmpty() || isValidAddress(recipient)
    val amountValid = amount.isEmpty() || isValidAmount(amount)
    val tonTooLarge = currency == SendCurrency.TON && amount.isNotEmpty() && isAmountTooLarge(amount, wallet.balance)
    val canSend = !isLoading &&
        isValidAddress(recipient) &&
        isValidAmount(amount) &&
        !tonTooLarge

    TonConnectSheetScaffold(
        footer = {
            TonText(
                text = if (gasless) {
                    "The relayer covers the TON gas — you pay the network fee in USDT."
                } else {
                    "A small amount of TON is used as the network fee."
                },
                style = TonTheme.typography.caption2Medium,
                color = TonTheme.colors.textTertiary,
                modifier = Modifier.fillMaxWidth(),
            )
            TonButton(
                text = when {
                    isLoading -> "Sending…"
                    amount.isNotEmpty() -> "Send $amount ${currency.label}"
                    else -> "Send ${currency.label}"
                },
                onClick = { onSend(recipient.trim(), amount, comment, currency, gasless) },
                enabled = canSend,
            )
        },
    ) {
        SheetHeader(title = "Send", onClose = onBack)

        TonSegmentedControl(
            selection = currency,
            items = SendCurrency.entries,
            title = { it.label },
            onSelect = {
                currency = it
                if (it == SendCurrency.TON) gasless = false
            },
            modifier = Modifier.fillMaxWidth(),
        )

        TonConnectSheetSection(label = "From") {
            TonText(wallet.name, style = TonTheme.typography.bodySemibold, color = TonTheme.colors.textPrimary)
            TonText(wallet.address.abbreviated(), style = TonTheme.typography.subheadline2, color = TonTheme.colors.textSecondary)
            TonText(
                "Balance: ${wallet.balance ?: "0"} TON",
                style = TonTheme.typography.subheadline2,
                color = TonTheme.colors.textSecondary,
            )
        }

        SheetTextField(
            value = recipient,
            onValueChange = { recipient = it },
            label = "Recipient",
            placeholder = "EQ… / UQ…",
            isError = !recipientValid,
            supporting = if (!recipientValid) "Invalid TON address" else null,
        )

        SheetTextField(
            value = amount,
            onValueChange = { amount = it },
            label = "Amount",
            placeholder = "0",
            keyboardType = KeyboardType.Decimal,
            trailing = currency.label,
            isError = !amountValid || tonTooLarge,
            supporting = when {
                !amountValid -> "Invalid amount"
                tonTooLarge -> "Insufficient balance"
                else -> null
            },
        )

        if (currency == SendCurrency.USDT) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(SmoothCornerShape(12.dp))
                    .background(TonTheme.colors.bgSecondary)
                    .padding(horizontal = 16.dp, vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    TonText("Gasless", style = TonTheme.typography.bodySemibold, color = TonTheme.colors.textPrimary)
                    TonText(
                        "Pay the fee in USDT, no TON required",
                        style = TonTheme.typography.subheadline2,
                        color = TonTheme.colors.textSecondary,
                    )
                }
                TonSwitch(checked = gasless, onCheckedChange = { gasless = it })
            }
        } else {
            SheetTextField(
                value = comment,
                onValueChange = { comment = it },
                label = "Comment (optional)",
                placeholder = "Add a message…",
            )
        }

        error?.let { SheetErrorBox(it) }
    }
}

private fun isValidAddress(address: String): Boolean = address.length > MIN_ADDRESS_LENGTH &&
    (address.startsWith(ADDRESS_MAIN_PREFIX) || address.startsWith(ADDRESS_TEST_PREFIX))

private fun isValidAmount(amount: String): Boolean = runCatching {
    (amount.toDoubleOrNull() ?: return false) > 0
}.getOrDefault(false)

private fun isAmountTooLarge(amount: String, balance: String?): Boolean = runCatching {
    val amountValue = amount.toDoubleOrNull() ?: return false
    val balanceValue = balance?.toDoubleOrNull() ?: return true
    amountValue > balanceValue
}.getOrDefault(true)

private const val MIN_ADDRESS_LENGTH = 10
private const val ADDRESS_MAIN_PREFIX = "EQ"
private const val ADDRESS_TEST_PREFIX = "UQ"
