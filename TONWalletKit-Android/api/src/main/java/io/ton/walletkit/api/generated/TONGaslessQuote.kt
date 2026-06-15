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
@file:Suppress(
    "ArrayInDataClass",
    "EnumEntryName",
    "RemoveRedundantQualifierName",
    "UnusedImport",
)

package io.ton.walletkit.api.generated

import io.ton.walletkit.model.TONUserFriendlyAddress
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Quote for a gasless transaction produced by `GaslessProvider.getQuote`.  Contains relayer-wrapped messages that should be passed to `wallet.signMessage` in place of the caller's original messages, together with the fee the relayer will deduct and the timestamp after which the bundle expires.
 *
 * @param network
 * @param messages Relayer-wrapped messages ready to be signed
 * @param fee
 * @param validUntil Unix timestamp after which the bundle becomes invalid for relay
 * @param from
 */
@Serializable
data class TONGaslessQuote(

    @SerialName(value = "network")
    var network: TONNetwork,

    /* Relayer-wrapped messages ready to be signed */
    @SerialName(value = "messages")
    var messages: kotlin.collections.List<TONTransactionRequestMessage>,

    @SerialName(value = "fee")
    var fee: kotlin.String,

    /* Unix timestamp after which the bundle becomes invalid for relay */
    @SerialName(value = "validUntil")
    var validUntil: kotlin.Double,

    @SerialName(value = "from")
    var from: io.ton.walletkit.model.TONUserFriendlyAddress,

) {

    companion object
}
