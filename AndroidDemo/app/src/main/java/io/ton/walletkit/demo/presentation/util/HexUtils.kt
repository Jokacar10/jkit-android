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
 */
package io.ton.walletkit.demo.presentation.util

/**
 * Demo-local hex helpers. Kept here — duplicated from the SDK's internal WalletKitUtils —
 * so the demo doesn't depend on the SDK's internal module. Small enough to copy.
 */

private val HEX_CHARS = "0123456789abcdef".toCharArray()

fun ByteArray.toHex(): String {
    if (isEmpty()) return "0x"
    val out = CharArray(size * 2 + 2)
    out[0] = '0'
    out[1] = 'x'
    for (i in indices) {
        val v = this[i].toInt() and 0xFF
        out[2 + i * 2] = HEX_CHARS[v ushr 4]
        out[3 + i * 2] = HEX_CHARS[v and 0x0F]
    }
    return String(out)
}

fun ByteArray.toHexNoPrefix(): String {
    if (isEmpty()) return ""
    val out = CharArray(size * 2)
    for (i in indices) {
        val v = this[i].toInt() and 0xFF
        out[i * 2] = HEX_CHARS[v ushr 4]
        out[i * 2 + 1] = HEX_CHARS[v and 0x0F]
    }
    return String(out)
}

fun String.stripHexPrefix(): String = removePrefix("0x").removePrefix("0X")

fun String.hexToByteArray(): ByteArray {
    val clean = stripHexPrefix()
    require(clean.length % 2 == 0) { "Hex string must have even length" }
    val out = ByteArray(clean.length / 2)
    for (i in out.indices) {
        out[i] = clean.substring(i * 2, i * 2 + 2).toInt(16).toByte()
    }
    return out
}
