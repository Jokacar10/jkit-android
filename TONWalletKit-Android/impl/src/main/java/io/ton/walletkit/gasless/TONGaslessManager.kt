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
import io.ton.walletkit.engine.WalletKitEngine

internal class TONGaslessManager(
    private val engine: WalletKitEngine,
) : ITONGaslessManager {

    override suspend fun registerProvider(provider: TONGaslessProvider) =
        engine.registerGaslessProvider(provider.providerId)

    override suspend fun setDefaultProvider(provider: TONGaslessProvider) =
        engine.setDefaultGaslessProvider(provider.providerId)

    override suspend fun providers(): List<TONGaslessProvider> =
        engine.getRegisteredGaslessProviders().map { TONGaslessProvider(it) }

    override suspend fun hasProvider(provider: TONGaslessProvider): Boolean =
        engine.hasGaslessProvider(provider.providerId)

    override suspend fun getMetadata(provider: TONGaslessProvider?): TONGaslessProviderMetadata =
        engine.getGaslessMetadata(provider?.providerId)

    override suspend fun getConfig(network: TONNetwork?, provider: TONGaslessProvider?): TONGaslessConfig =
        engine.getGaslessConfig(network, provider?.providerId)

    override suspend fun getQuote(params: TONGaslessQuoteParams, provider: TONGaslessProvider?): TONGaslessQuote =
        engine.getGaslessQuote(params, provider?.providerId)

    override suspend fun sendTransaction(
        params: TONGaslessSendParams,
        provider: TONGaslessProvider?,
    ): TONGaslessSendResponse = engine.gaslessSendTransaction(params, provider?.providerId)
}
