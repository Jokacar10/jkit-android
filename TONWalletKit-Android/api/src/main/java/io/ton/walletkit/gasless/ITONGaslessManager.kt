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
package io.ton.walletkit.gasless

import io.ton.walletkit.api.generated.TONGaslessConfig
import io.ton.walletkit.api.generated.TONGaslessProviderMetadata
import io.ton.walletkit.api.generated.TONGaslessQuote
import io.ton.walletkit.api.generated.TONGaslessQuoteParams
import io.ton.walletkit.api.generated.TONGaslessSendParams
import io.ton.walletkit.api.generated.TONGaslessSendResponse
import io.ton.walletkit.api.generated.TONNetwork

/**
 * Manages gasless relay providers and executes gasless operations. Obtain via
 * [io.ton.walletkit.ITONWalletKit.gasless].
 *
 * Gasless lets a wallet submit on-chain transactions without paying TON for gas: a relayer
 * co-signs and covers the gas, taking a jetton fee in return. The flow is
 * [getQuote] (sign the returned messages) → [sendTransaction].
 *
 * Providers are registered as objects but referenced everywhere else by their
 * [TONGaslessProviderIdentifier].
 */
interface ITONGaslessManager {
    /**
     * Register a provider. Accepts any [ITONGaslessProvider] — the SDK's built-in provider
     * returned from [io.ton.walletkit.ITONWalletKit.tonApiGaslessProvider], or a custom conformer.
     */
    suspend fun registerProvider(provider: ITONGaslessProvider)

    /** Set the default provider used when no identifier is specified. */
    suspend fun setDefaultProvider(identifier: TONGaslessProviderIdentifier)

    /** All currently-registered providers. */
    suspend fun providers(): List<ITONGaslessProvider>

    /** Returns true if a provider with the given [identifier] is currently registered. */
    suspend fun hasProvider(identifier: TONGaslessProviderIdentifier): Boolean

    /** Returns a handle to the provider with [identifier]. */
    suspend fun provider(identifier: TONGaslessProviderIdentifier): ITONGaslessProvider?

    /** Static metadata (display name, logo, url) for [identifier], or the default provider. */
    suspend fun getMetadata(identifier: TONGaslessProviderIdentifier? = null): TONGaslessProviderMetadata

    /**
     * Fetch the relayer's config — the relay address and the assets it accepts as fee payment.
     * [network] defaults to the provider's first supported network.
     */
    suspend fun getConfig(
        network: TONNetwork? = null,
        identifier: TONGaslessProviderIdentifier? = null,
    ): TONGaslessConfig

    /**
     * Quote fees and obtain relayer-wrapped messages for signing. Pass the returned
     * [TONGaslessQuote.messages] to the wallet's sign-message flow to obtain a signed internal BoC,
     * then submit it via [sendTransaction].
     */
    suspend fun getQuote(
        params: TONGaslessQuoteParams,
        identifier: TONGaslessProviderIdentifier? = null,
    ): TONGaslessQuote

    /** Submit a signed transaction BoC to the relayer for on-chain execution. */
    suspend fun sendTransaction(
        params: TONGaslessSendParams,
        identifier: TONGaslessProviderIdentifier? = null,
    ): TONGaslessSendResponse
}
