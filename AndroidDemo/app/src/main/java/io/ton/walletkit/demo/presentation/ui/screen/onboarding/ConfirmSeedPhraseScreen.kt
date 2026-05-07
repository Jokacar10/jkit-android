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
package io.ton.walletkit.demo.presentation.ui.screen.onboarding

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import io.ton.walletkit.demo.designsystem.components.button.TonButton
import io.ton.walletkit.demo.designsystem.components.button.TonButtonConfig
import io.ton.walletkit.demo.designsystem.components.navbarbutton.TonNavbarActionButton
import io.ton.walletkit.demo.designsystem.components.seedphrase.SeedWordField
import io.ton.walletkit.demo.designsystem.components.text.TonText
import io.ton.walletkit.demo.designsystem.icons.TonIcon
import io.ton.walletkit.demo.designsystem.theme.TonTheme

@Composable
fun ConfirmSeedPhraseScreen(
    askedIndices: List<Int>,
    answers: Map<Int, String>,
    canContinue: Boolean,
    onAnswerChange: (index: Int, value: String) -> Unit,
    onBack: () -> Unit,
    onContinue: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(TonTheme.colors.bgPrimary)
            .statusBarsPadding()
            .imePadding()
            .padding(horizontal = 16.dp),
    ) {
        Row(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)) {
            TonNavbarActionButton(
                icon = TonIcon.ChevronBackSmall,
                onClick = onBack,
                contentDescription = "Back",
            )
        }

        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Spacer(modifier = Modifier.height(8.dp))
            TonText(
                text = "Confirm your recovery phrase",
                style = TonTheme.typography.title2,
                color = TonTheme.colors.textPrimary,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth(),
            )
            TonText(
                text = "Enter the missing words in correct order.",
                style = TonTheme.typography.body,
                color = TonTheme.colors.textSecondary,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth(),
            )
            Spacer(modifier = Modifier.height(16.dp))

            askedIndices.forEachIndexed { position, index ->
                SeedWordField(
                    index = index + 1,
                    value = answers[index].orEmpty(),
                    onValueChange = { onAnswerChange(index, it) },
                    imeAction = if (position == askedIndices.lastIndex) ImeAction.Done else ImeAction.Next,
                )
            }
        }

        TonButton(
            text = "Continue",
            onClick = onContinue,
            config = TonButtonConfig.Primary,
            enabled = canContinue,
        )
        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Preview(showBackground = true)
@Composable
private fun ConfirmSeedPhraseScreenPreview() {
    TonTheme {
        ConfirmSeedPhraseScreen(
            askedIndices = listOf(3, 6, 11),
            answers = mapOf(3 to "lantern"),
            canContinue = false,
            onAnswerChange = { _, _ -> },
            onBack = {},
            onContinue = {},
        )
    }
}
