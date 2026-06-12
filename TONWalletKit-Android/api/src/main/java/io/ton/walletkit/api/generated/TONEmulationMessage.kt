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
 * Message sent or received within an emulated transaction trace.
 *
 * @param hash
 * @param destination
 * @param valueExtraCurrencies Map of extra currency IDs to their amounts. Extra currencies are additional tokens that can be attached to TON messages.
 * @param messageContent
 * @param normalizedHash
 * @param source
 * @param `value`
 * @param fwdFee
 * @param ihrFee
 * @param createdLt
 * @param createdAt Unix timestamp when the message was created, or undefined for external inbound messages
 * @param opcode
 * @param ihrDisabled Whether IHR delivery is disabled, or undefined for external inbound messages
 * @param isBounce Whether the message requested a bounce on failure, or undefined for external inbound messages
 * @param isBounced Whether the message was bounced back, or undefined for external inbound messages
 * @param importFee
 * @param initState Initial state (StateInit) attached to the message, if any
 */
@Serializable
data class TONEmulationMessage(

    @Contextual @SerialName(value = "hash")
    var hash: io.ton.walletkit.model.TONHex,

    @SerialName(value = "destination")
    var destination: io.ton.walletkit.model.TONUserFriendlyAddress,

    /* Map of extra currency IDs to their amounts. Extra currencies are additional tokens that can be attached to TON messages. */
    @SerialName(value = "valueExtraCurrencies")
    var valueExtraCurrencies: kotlin.collections.Map<kotlin.String, kotlin.String>,

    @SerialName(value = "messageContent")
    var messageContent: TONEmulationMessageContent,

    @Contextual @SerialName(value = "normalizedHash")
    var normalizedHash: io.ton.walletkit.model.TONHex? = null,

    @SerialName(value = "source")
    var source: io.ton.walletkit.model.TONUserFriendlyAddress? = null,

    @SerialName(value = "value")
    var `value`: kotlin.String? = null,

    @SerialName(value = "fwdFee")
    var fwdFee: kotlin.String? = null,

    @SerialName(value = "ihrFee")
    var ihrFee: kotlin.String? = null,

    @SerialName(value = "createdLt")
    var createdLt: kotlin.String? = null,

    /* Unix timestamp when the message was created, or undefined for external inbound messages */
    @SerialName(value = "createdAt")
    var createdAt: kotlin.Int? = null,

    @Contextual @SerialName(value = "opcode")
    var opcode: io.ton.walletkit.model.TONHex? = null,

    /* Whether IHR delivery is disabled, or undefined for external inbound messages */
    @SerialName(value = "ihrDisabled")
    var ihrDisabled: kotlin.Boolean? = null,

    /* Whether the message requested a bounce on failure, or undefined for external inbound messages */
    @SerialName(value = "isBounce")
    var isBounce: kotlin.Boolean? = null,

    /* Whether the message was bounced back, or undefined for external inbound messages */
    @SerialName(value = "isBounced")
    var isBounced: kotlin.Boolean? = null,

    @SerialName(value = "importFee")
    var importFee: kotlin.String? = null,

    /* Initial state (StateInit) attached to the message, if any */
    @Contextual @SerialName(value = "initState")
    var initState: kotlinx.serialization.json.JsonElement? = null,

) {

    companion object
}
