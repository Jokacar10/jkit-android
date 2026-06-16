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
package io.ton.walletkit

import io.ton.walletkit.api.generated.*
import io.ton.walletkit.model.ITONWalletAdapter
import io.ton.walletkit.model.TONBalance
import io.ton.walletkit.model.TONUserFriendlyAddress

interface ITONWallet : ITONWalletAdapter {
    suspend fun balance(): TONBalance

    suspend fun transferTONTransaction(request: TONTransferRequest): TONTransactionRequest

    suspend fun transferTONTransaction(requests: List<TONTransferRequest>): TONTransactionRequest

    suspend fun preview(
        transactionRequest: TONTransactionRequest,
        options: TONTransactionPreviewOptions? = null,
    ): TONTransactionEmulatedPreview

    suspend fun send(transactionRequest: TONTransactionRequest): TONSendTransactionResponse

    suspend fun transferNFTTransaction(request: TONNFTTransferRequest): TONTransactionRequest

    suspend fun transferNFTTransaction(request: TONNFTRawTransferRequest): TONTransactionRequest

    suspend fun nfts(request: TONNFTsRequest): TONNFTsResponse

    suspend fun nft(address: TONUserFriendlyAddress): TONNFT?

    suspend fun jettonBalance(jettonAddress: TONUserFriendlyAddress): TONBalance

    suspend fun jettonWalletAddress(jettonAddress: TONUserFriendlyAddress): TONUserFriendlyAddress

    suspend fun transferJettonTransaction(request: TONJettonsTransferRequest): TONTransactionRequest

    suspend fun jettons(request: TONJettonsRequest? = null): TONJettonsResponse
}

suspend fun ITONWallet.nfts(limit: Int): TONNFTsResponse =
    nfts(TONNFTsRequest(pagination = TONPagination(limit = limit)))

suspend fun ITONWallet.jettons(limit: Int): TONJettonsResponse =
    jettons(TONJettonsRequest(pagination = TONPagination(limit = limit)))
