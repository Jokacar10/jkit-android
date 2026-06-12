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
 * Non-fungible token (NFT) on the TON blockchain.
 *
 * @param address
 * @param index Index of the item within its collection
 * @param info
 * @param attributes Custom attributes/traits of the NFT (e.g., rarity, properties)
 * @param collection
 * @param auctionContractAddress
 * @param codeHash
 * @param dataHash
 * @param isInited Whether the NFT contract has been initialized
 * @param isSoulbound Whether the NFT is soulbound (non-transferable)
 * @param isOnSale Whether the NFT is currently listed for sale
 * @param ownerAddress
 * @param realOwnerAddress
 * @param saleContractAddress
 * @param extra Off-chain metadata of the NFT (key-value pairs)
 */
@Serializable
data class TONNFT(

    @SerialName(value = "address")
    var address: io.ton.walletkit.model.TONUserFriendlyAddress,

    /* Index of the item within its collection */
    @SerialName(value = "index")
    var index: kotlin.String? = null,

    @SerialName(value = "info")
    var info: TONTokenInfo? = null,

    /* Custom attributes/traits of the NFT (e.g., rarity, properties) */
    @SerialName(value = "attributes")
    var attributes: kotlin.collections.List<TONNFTAttribute>? = null,

    @SerialName(value = "collection")
    var collection: TONNFTCollection? = null,

    @SerialName(value = "auctionContractAddress")
    var auctionContractAddress: io.ton.walletkit.model.TONUserFriendlyAddress? = null,

    @Contextual @SerialName(value = "codeHash")
    var codeHash: io.ton.walletkit.model.TONHex? = null,

    @Contextual @SerialName(value = "dataHash")
    var dataHash: io.ton.walletkit.model.TONHex? = null,

    /* Whether the NFT contract has been initialized */
    @SerialName(value = "isInited")
    var isInited: kotlin.Boolean? = null,

    /* Whether the NFT is soulbound (non-transferable) */
    @SerialName(value = "isSoulbound")
    var isSoulbound: kotlin.Boolean? = null,

    /* Whether the NFT is currently listed for sale */
    @SerialName(value = "isOnSale")
    var isOnSale: kotlin.Boolean? = null,

    @SerialName(value = "ownerAddress")
    var ownerAddress: io.ton.walletkit.model.TONUserFriendlyAddress? = null,

    @SerialName(value = "realOwnerAddress")
    var realOwnerAddress: io.ton.walletkit.model.TONUserFriendlyAddress? = null,

    @SerialName(value = "saleContractAddress")
    var saleContractAddress: io.ton.walletkit.model.TONUserFriendlyAddress? = null,

    /* Off-chain metadata of the NFT (key-value pairs) */
    @Contextual @SerialName(value = "extra")
    var extra: kotlin.collections.Map<kotlin.String, kotlinx.serialization.json.JsonElement>? = null,

) {

    companion object
}
