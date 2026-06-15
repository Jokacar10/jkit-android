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

import kotlinx.serialization.Contextual
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Swap quote response with pricing information
 *
 * @param fromToken
 * @param toToken
 * @param rawFromAmount
 * @param rawToAmount
 * @param fromAmount Amount of tokens to sell
 * @param toAmount Amount of tokens to buy
 * @param rawMinReceived
 * @param minReceived Minimum amount of tokens to receive (after slippage)
 * @param network
 * @param providerId Identifier of the swap provider
 * @param priceImpact Price impact of the swap in basis points (100 = 1%)
 * @param expiresAt Unix timestamp in seconds when the quote expires
 * @param metadata Provider-specific metadata for the quote
 */
@Serializable
data class TONSwapQuote(

    @SerialName(value = "fromToken")
    var fromToken: TONSwapToken,

    @SerialName(value = "toToken")
    var toToken: TONSwapToken,

    @SerialName(value = "rawFromAmount")
    var rawFromAmount: kotlin.String,

    @SerialName(value = "rawToAmount")
    var rawToAmount: kotlin.String,

    /* Amount of tokens to sell */
    @SerialName(value = "fromAmount")
    var fromAmount: kotlin.String,

    /* Amount of tokens to buy */
    @SerialName(value = "toAmount")
    var toAmount: kotlin.String,

    @SerialName(value = "rawMinReceived")
    var rawMinReceived: kotlin.String,

    /* Minimum amount of tokens to receive (after slippage) */
    @SerialName(value = "minReceived")
    var minReceived: kotlin.String,

    @SerialName(value = "network")
    var network: TONNetwork,

    /* Identifier of the swap provider */
    @SerialName(value = "providerId")
    var providerId: kotlin.String,

    /* Price impact of the swap in basis points (100 = 1%) */
    @SerialName(value = "priceImpact")
    var priceImpact: kotlin.Int? = null,

    /* Unix timestamp in seconds when the quote expires */
    @SerialName(value = "expiresAt")
    var expiresAt: kotlin.Int? = null,

    /* Provider-specific metadata for the quote */
    @Contextual @SerialName(value = "metadata")
    var metadata: kotlinx.serialization.json.JsonElement? = null,

) {

    companion object
}
