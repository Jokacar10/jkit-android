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
 * High-level action extracted from an emulated transaction trace.
 *
 * @param actionId
 * @param startLt
 * @param endLt
 * @param startUtime Unix timestamp when the action started
 * @param endUtime Unix timestamp when the action ended
 * @param traceEndLt
 * @param traceEndUtime Unix timestamp when the trace ended
 * @param traceMcSeqnoEnd Masterchain block sequence number when the trace ended
 * @param transactions Hex-encoded hashes of transactions involved in this action
 * @param isSuccess Whether the action completed successfully
 * @param type Action type identifier (e.g. \"jetton_transfer\", \"ton_transfer\", \"jetton_swap\")
 * @param traceExternalHash
 * @param accounts Addresses of accounts involved in this action
 * @param details Action-specific detail fields keyed by name
 * @param traceId Trace identifier this action belongs to
 */
@Serializable
data class TONEmulationAction(

    @Contextual @SerialName(value = "actionId")
    var actionId: io.ton.walletkit.model.TONHex,

    @SerialName(value = "startLt")
    var startLt: kotlin.String,

    @SerialName(value = "endLt")
    var endLt: kotlin.String,

    /* Unix timestamp when the action started */
    @SerialName(value = "startUtime")
    var startUtime: kotlin.Int,

    /* Unix timestamp when the action ended */
    @SerialName(value = "endUtime")
    var endUtime: kotlin.Int,

    @SerialName(value = "traceEndLt")
    var traceEndLt: kotlin.String,

    /* Unix timestamp when the trace ended */
    @SerialName(value = "traceEndUtime")
    var traceEndUtime: kotlin.Int,

    /* Masterchain block sequence number when the trace ended */
    @SerialName(value = "traceMcSeqnoEnd")
    var traceMcSeqnoEnd: kotlin.Int,

    /* Hex-encoded hashes of transactions involved in this action */
    @SerialName(value = "transactions")
    var transactions: kotlin.collections.List<@Contextual io.ton.walletkit.model.TONHex>,

    /* Whether the action completed successfully */
    @SerialName(value = "isSuccess")
    var isSuccess: kotlin.Boolean,

    /* Action type identifier (e.g. \"jetton_transfer\", \"ton_transfer\", \"jetton_swap\") */
    @SerialName(value = "type")
    var type: kotlin.String,

    @Contextual @SerialName(value = "traceExternalHash")
    var traceExternalHash: io.ton.walletkit.model.TONHex,

    /* Addresses of accounts involved in this action */
    @SerialName(value = "accounts")
    var accounts: kotlin.collections.List<io.ton.walletkit.model.TONUserFriendlyAddress>,

    /* Action-specific detail fields keyed by name */
    @Contextual @SerialName(value = "details")
    var details: kotlin.collections.Map<kotlin.String, kotlinx.serialization.json.JsonElement>,

    /* Trace identifier this action belongs to */
    @SerialName(value = "traceId")
    var traceId: kotlin.String? = null,

) {

    companion object
}
