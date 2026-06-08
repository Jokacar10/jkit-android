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
 * Provider-level configuration for a gasless relayer on a given network.  Bundles every piece of provider state a consumer needs to drive a gasless transfer end-to-end:  - `relayAddress` — where the relayer wants residual TON (e.g. jetton-transfer    `responseDestination`) returned to.  - `supportedAssets` — what the relayer accepts as fee payment.
 *
 * @param relayAddress
 * @param supportedAssets Assets the relayer accepts as fee payment.
 */
@Serializable
data class TONGaslessConfig(

    @SerialName(value = "relayAddress")
    val relayAddress: io.ton.walletkit.model.TONUserFriendlyAddress,

    /* Assets the relayer accepts as fee payment. */
    @SerialName(value = "supportedAssets")
    val supportedAssets: kotlin.collections.List<TONGaslessSupportedAsset>,

) {

    companion object
}
