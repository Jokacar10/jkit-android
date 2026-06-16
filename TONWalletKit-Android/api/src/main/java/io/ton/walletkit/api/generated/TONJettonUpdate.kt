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
import kotlinx.serialization.Contextual
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 *
 *
 * @param status
 * @param masterAddress
 * @param walletAddress
 * @param ownerAddress
 * @param rawBalance
 * @param decimals Decimals mapped from metadata if available
 * @param balance Human readable formatted balance if decimals are known
 */
@Serializable
data class TONJettonUpdate(

    @Contextual @SerialName(value = "status")
    var status: TONStreamingUpdateStatus,

    @SerialName(value = "masterAddress")
    var masterAddress: io.ton.walletkit.model.TONUserFriendlyAddress,

    @SerialName(value = "walletAddress")
    var walletAddress: io.ton.walletkit.model.TONUserFriendlyAddress,

    @SerialName(value = "ownerAddress")
    var ownerAddress: io.ton.walletkit.model.TONUserFriendlyAddress,

    @SerialName(value = "rawBalance")
    var rawBalance: kotlin.String,

    /* Decimals mapped from metadata if available */
    @SerialName(value = "decimals")
    var decimals: kotlin.Double? = null,

    /* Human readable formatted balance if decimals are known */
    @SerialName(value = "balance")
    var balance: kotlin.String? = null,
    @SerialName("type")
    val type: kotlin.String = "jettons",
) {

    companion object
}
