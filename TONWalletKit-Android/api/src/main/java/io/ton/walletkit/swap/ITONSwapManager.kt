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
package io.ton.walletkit.swap

import io.ton.walletkit.api.generated.TONSwapParams
import io.ton.walletkit.api.generated.TONSwapQuote
import io.ton.walletkit.api.generated.TONSwapQuoteParams
import io.ton.walletkit.api.generated.TONTransactionRequest
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.serializer

/** Manages swap providers and executes swap operations. Obtain via [io.ton.walletkit.ITONWalletKit.swap]. */
interface ITONSwapManager {
    /** Register a provider. Must be called before [getQuote] or [buildSwapTransaction]. */
    suspend fun registerProvider(provider: TONSwapProvider<*, *>)

    /** Set the default provider used by [getQuote] when no identifier is specified. */
    suspend fun setDefaultProvider(identifier: TONSwapProviderIdentifier<*, *>)

    /** Returns typed identifiers for all registered providers. */
    suspend fun registeredProviders(): List<AnyTONSwapProviderIdentifier>

    /** Returns true if a provider with the given [identifier] is currently registered. */
    suspend fun hasProvider(identifier: TONSwapProviderIdentifier<*, *>): Boolean

    /**
     * Get a quote from the provider with [identifier]. Mirrors iOS
     * `quote<Identifier: TONSwapProviderIdentifier>(params:, identifier:)`.
     *
     * Prefer the typed extension `getQuote<TQuoteOptions, TSwapOptions>(params, identifier)`,
     * which serializes typed `providerOptions` automatically.
     */
    suspend fun getQuote(
        params: TONSwapQuoteParams<JsonElement>,
        identifier: TONSwapProviderIdentifier<*, *>,
    ): TONSwapQuote

    /** Get a quote from the default registered provider. Mirrors iOS `quote(params: TONSwapQuoteParams<AnyCodable>)`. */
    suspend fun getQuote(params: TONSwapQuoteParams<JsonElement>): TONSwapQuote

    /**
     * Build a swap transaction. The provider is resolved from [TONSwapParams.quote.providerId].
     * Prefer the typed extension `buildSwapTransaction<TSwapOptions>(params)` for typed options.
     */
    suspend fun buildSwapTransaction(params: TONSwapParams<JsonElement>): TONTransactionRequest
}

/**
 * Returns a typed [TONSwapProvider] for [identifier] if it is currently registered, null otherwise.
 * Type parameters are inferred from the identifier, e.g.:
 * ```kotlin
 * val provider: TONOmnistonSwapProvider? = manager.provider(TONOmnistonSwapProviderIdentifier())
 * ```
 */
suspend inline fun <reified TQuoteOptions, reified TSwapOptions> ITONSwapManager.provider(
    identifier: TONSwapProviderIdentifier<TQuoteOptions, TSwapOptions>,
): TONSwapProvider<TQuoteOptions, TSwapOptions>? {
    val handle = TONSwapProvider(identifier, this)
    return if (hasProvider(identifier)) handle else null
}

/**
 * Get a quote from the provider with [identifier], serializing typed `providerOptions` automatically.
 * Mirrors iOS `quote<Identifier: TONSwapProviderIdentifier>(params: TONSwapQuoteParams<Identifier.QuoteOptions>, identifier: Identifier)`.
 */
suspend inline fun <reified TQuoteOptions, reified TSwapOptions> ITONSwapManager.getQuote(
    params: TONSwapQuoteParams<TQuoteOptions>,
    identifier: TONSwapProviderIdentifier<TQuoteOptions, TSwapOptions>,
): TONSwapQuote = TONSwapProvider(identifier, this).quote(params)

/**
 * Build a swap transaction with typed swap options, serializing `providerOptions` automatically.
 * Mirrors iOS `swapTransaction<QuoteOptions: Codable>(params: TONSwapParams<QuoteOptions>)`.
 */
suspend inline fun <reified TSwapOptions> ITONSwapManager.buildSwapTransaction(
    params: TONSwapParams<TSwapOptions>,
): TONTransactionRequest {
    val jsonOptions = params.providerOptions?.let { Json.encodeToJsonElement(serializer<TSwapOptions>(), it) }
    return buildSwapTransaction(
        TONSwapParams(
            quote = params.quote,
            userAddress = params.userAddress,
            destinationAddress = params.destinationAddress,
            slippageBps = params.slippageBps,
            deadline = params.deadline,
            providerOptions = jsonOptions,
        ),
    )
}
