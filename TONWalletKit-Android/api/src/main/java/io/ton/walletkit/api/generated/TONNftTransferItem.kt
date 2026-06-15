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

import io.ton.walletkit.model.TONBase64
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 *
 *
 * @param nftAddress
 * @param newOwner
 * @param attachAmount
 * @param queryId
 * @param responseDestination
 * @param customPayload
 * @param forwardAmount
 * @param forwardPayload
 */
@Serializable
data class TONNftTransferItem(

    @SerialName(value = "nftAddress")
    var nftAddress: kotlin.String,

    @SerialName(value = "newOwner")
    var newOwner: kotlin.String,

    @SerialName(value = "attachAmount")
    var attachAmount: kotlin.String? = null,

    @SerialName(value = "queryId")
    var queryId: kotlin.String? = null,

    @SerialName(value = "responseDestination")
    var responseDestination: kotlin.String? = null,

    @SerialName(value = "customPayload")
    var customPayload: io.ton.walletkit.model.TONBase64? = null,

    @SerialName(value = "forwardAmount")
    var forwardAmount: kotlin.String? = null,

    @SerialName(value = "forwardPayload")
    var forwardPayload: io.ton.walletkit.model.TONBase64? = null,
    @SerialName("type")
    val type: kotlin.String = "nft",
) {

    companion object
}
