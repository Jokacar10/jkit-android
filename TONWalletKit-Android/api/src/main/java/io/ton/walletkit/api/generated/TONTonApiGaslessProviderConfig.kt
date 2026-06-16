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
 * Configuration for `TonApiGaslessProvider`.  One provider instance handles every configured chain. When `chains` is omitted, `createFromContext` auto-registers every network the kit was configured with.
 *
 * @param chains Per-chain settings keyed by `Network#chainId`.
 * @param providerId Provider id. Defaults to `tonapi`.
 * @param sendRetries Number of send retries on transient errors. Defaults to 5.
 * @param sendRetryDelayMs Delay between send retries in ms. Defaults to 1000.
 * @param quoteRetries Number of quote retries on transient errors (5xx / network). Defaults to 5.
 * @param quoteRetryDelayMs Fixed delay between quote retries in ms. Defaults to 1000.
 * @param configCacheTtlMs TTL for the in-memory `/v2/gasless/config` cache (ms). Defaults to 5 minutes. Set to `0` to disable caching.
 */
@Serializable
data class TONTonApiGaslessProviderConfig(

    /* Per-chain settings keyed by `Network#chainId`. */
    @Contextual @SerialName(value = "chains")
    val chains: kotlin.collections.Map<kotlin.String, TONTonApiGaslessChainConfig>? = null,

    /* Provider id. Defaults to `tonapi`. */
    @SerialName(value = "providerId")
    val providerId: kotlin.String? = null,

    /* Number of send retries on transient errors. Defaults to 5. */
    @SerialName(value = "sendRetries")
    val sendRetries: kotlin.Int? = null,

    /* Delay between send retries in ms. Defaults to 1000. */
    @SerialName(value = "sendRetryDelayMs")
    val sendRetryDelayMs: kotlin.Int? = null,

    /* Number of quote retries on transient errors (5xx / network). Defaults to 5. */
    @SerialName(value = "quoteRetries")
    val quoteRetries: kotlin.Int? = null,

    /* Fixed delay between quote retries in ms. Defaults to 1000. */
    @SerialName(value = "quoteRetryDelayMs")
    val quoteRetryDelayMs: kotlin.Int? = null,

    /* TTL for the in-memory `/v2/gasless/config` cache (ms). Defaults to 5 minutes. Set to `0` to disable caching. */
    @SerialName(value = "configCacheTtlMs")
    val configCacheTtlMs: kotlin.Int? = null,

) {

    companion object
}
