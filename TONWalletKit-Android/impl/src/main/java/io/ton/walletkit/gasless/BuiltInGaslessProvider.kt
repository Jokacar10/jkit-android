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

/**
 * Built-in, JS-backed implementation of [ITONGaslessProvider]. Talks to the JS engine directly.
 *
 * Users never construct this: it's created by the SDK (via [io.ton.walletkit.ITONWalletKit.tonApiGaslessProvider]).
 *
 * @suppress Internal implementation.
 */
internal class BuiltInGaslessProvider(
    override val identifier: TONGaslessProviderIdentifier,
    private val engine: WalletKitEngine,
) : ITONGaslessProvider {

    override suspend fun supportedNetworks(): List<TONNetwork> =
        engine.getGaslessProviderSupportedNetworks(identifier.name)

    override suspend fun metadata(): TONGaslessProviderMetadata =
        engine.getGaslessMetadata(identifier.name)

    override suspend fun getConfig(network: TONNetwork?): TONGaslessConfig =
        engine.getGaslessConfig(network, identifier.name)

    override suspend fun getQuote(params: TONGaslessQuoteParams): TONGaslessQuote =
        engine.getGaslessQuote(params, identifier.name)

    override suspend fun sendTransaction(params: TONGaslessSendParams): TONGaslessSendResponse =
        engine.gaslessSendTransaction(params, identifier.name)
}
