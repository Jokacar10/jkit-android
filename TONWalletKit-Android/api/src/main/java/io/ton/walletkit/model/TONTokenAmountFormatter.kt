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
package io.ton.walletkit.model

import java.math.BigInteger

/** Converts a [TONTokenAmount] between nano units and a decimal string. */
open class TONTokenAmountFormatter {

    // Defaults to 9, as TON uses nano units (1 TON = 10^9 nanoTON)
    var nanoUnitDecimalsNumber: Int = 9

    var allowFractionalTrailingZeroes: Boolean = false

    fun string(from: TONTokenAmount): String? {
        if (nanoUnitDecimalsNumber < 0) return null

        var digits = from.nanoUnits.toString()
        val negative = digits.startsWith("-")
        if (negative) digits = digits.substring(1)

        if (digits.length < nanoUnitDecimalsNumber) {
            digits = "0".repeat(nanoUnitDecimalsNumber - digits.length) + digits
        }

        val splitIndex = digits.length - nanoUnitDecimalsNumber
        val integer = if (splitIndex > 0) digits.substring(0, splitIndex) else ""
        var fraction = digits.takeLast(nanoUnitDecimalsNumber)

        if (!allowFractionalTrailingZeroes) {
            fraction = fraction.trimEnd('0')
        }

        val negativePrefix = if (negative) "-" else ""
        val integerPart = integer.ifEmpty { "0" }
        val fractionPart = if (fraction.isEmpty()) "" else ".$fraction"

        return "$negativePrefix$integerPart$fractionPart"
    }

    fun amount(from: String): TONTokenAmount? {
        val clean = from.trim()
        if (clean.isEmpty()) return null

        val parts = clean.split(".")
        if (parts.size > 2) return null

        val integerPart = parts[0]
        val fractionalPart = if (parts.size == 2) parts[1] else ""

        val integerValue = integerPart.toBigIntegerOrNull() ?: return null
        // Sign must come from the raw string: "-0".toBigInteger() is zero, so signum() can't see it
        val negative = integerPart.startsWith("-")
        var result = integerValue.abs() * BigInteger.TEN.pow(nanoUnitDecimalsNumber)

        if (fractionalPart.isNotEmpty()) {
            if (fractionalPart.any { it !in '0'..'9' }) return null
            val normalized = if (fractionalPart.length > nanoUnitDecimalsNumber) {
                fractionalPart.substring(0, nanoUnitDecimalsNumber)
            } else {
                fractionalPart.padEnd(nanoUnitDecimalsNumber, '0')
            }
            val fractionValue = normalized.toBigIntegerOrNull() ?: return null
            result += fractionValue
        }

        if (negative) result = result.negate()
        return TONTokenAmount(result)
    }
}
