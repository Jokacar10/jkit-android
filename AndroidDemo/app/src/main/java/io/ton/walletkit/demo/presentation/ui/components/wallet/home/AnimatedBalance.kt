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

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Easing
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import java.math.BigDecimal
import java.math.RoundingMode

private const val COUNT_UP_DURATION_MS = 500

// Ease-out cubic — fast start, smooth deceleration into the target (mirrors the TS demo wallet).
private val EaseOutCubic = Easing { t -> 1f - (1f - t) * (1f - t) * (1f - t) }

/**
 * "Casino" count-up: returns a value that eases from the previously-shown number to [target]
 * whenever [target] changes (e.g. a balance update). Double-precise, so large jetton amounts
 * keep their value. The initial value is shown immediately — only later updates animate.
 */
@Composable
fun rememberCountUp(target: Double, durationMillis: Int = COUNT_UP_DURATION_MS): Double {
    val progress = remember { Animatable(1f) }
    var start by remember { mutableStateOf(target) }
    var end by remember { mutableStateOf(target) }

    LaunchedEffect(target) {
        if (target != end) {
            start = start + (end - start) * progress.value // capture the currently-shown value
            end = target
            progress.snapTo(0f)
            progress.animateTo(1f, tween(durationMillis, easing = EaseOutCubic))
        }
    }
    return start + (end - start) * progress.value
}

/** Formats a count-up value, trailing zeros stripped and capped at [maxFractionDigits]. */
fun formatCountUp(value: Double, maxFractionDigits: Int): String = BigDecimal.valueOf(value)
    .setScale(maxFractionDigits, RoundingMode.DOWN)
    .stripTrailingZeros()
    .toPlainString()
