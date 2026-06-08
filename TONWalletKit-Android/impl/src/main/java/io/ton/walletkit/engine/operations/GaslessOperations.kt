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
package io.ton.walletkit.engine.operations

import io.ton.walletkit.api.generated.TONGaslessConfig
import io.ton.walletkit.api.generated.TONGaslessProviderMetadata
import io.ton.walletkit.api.generated.TONGaslessQuote
import io.ton.walletkit.api.generated.TONGaslessQuoteParams
import io.ton.walletkit.api.generated.TONGaslessSendParams
import io.ton.walletkit.api.generated.TONGaslessSendResponse
import io.ton.walletkit.api.generated.TONNetwork
import io.ton.walletkit.api.generated.TONTonApiGaslessProviderConfig
import io.ton.walletkit.engine.infrastructure.BridgeRpcClient
import io.ton.walletkit.engine.infrastructure.callTyped
import io.ton.walletkit.engine.operations.requests.CreateTonApiGaslessProviderRequest
import io.ton.walletkit.engine.operations.requests.GaslessSendTransactionRequest
import io.ton.walletkit.engine.operations.requests.GetGaslessConfigRequest
import io.ton.walletkit.engine.operations.requests.GetGaslessMetadataRequest
import io.ton.walletkit.engine.operations.requests.GetGaslessQuoteRequest
import io.ton.walletkit.engine.operations.requests.HasGaslessProviderRequest
import io.ton.walletkit.engine.operations.requests.RegisterGaslessProviderRequest
import io.ton.walletkit.engine.operations.requests.SetDefaultGaslessProviderRequest
import io.ton.walletkit.engine.operations.responses.HasProviderResponse
import io.ton.walletkit.engine.operations.responses.ProviderIdResponse
import io.ton.walletkit.engine.operations.responses.ProviderIdsResponse
import io.ton.walletkit.internal.constants.BridgeMethodConstants
import kotlinx.serialization.json.Json

internal suspend fun BridgeRpcClient.createTonApiGaslessProvider(config: TONTonApiGaslessProviderConfig?): String =
    callTyped<ProviderIdResponse>(
        BridgeMethodConstants.METHOD_CREATE_TONAPI_GASLESS_PROVIDER,
        CreateTonApiGaslessProviderRequest(
            config = config?.let { Json.encodeToJsonElement(TONTonApiGaslessProviderConfig.serializer(), it) },
        ),
    ).providerId

internal suspend fun BridgeRpcClient.registerGaslessProvider(providerId: String) {
    send(BridgeMethodConstants.METHOD_REGISTER_GASLESS_PROVIDER, RegisterGaslessProviderRequest(providerId))
}

internal suspend fun BridgeRpcClient.setDefaultGaslessProvider(providerId: String) {
    send(BridgeMethodConstants.METHOD_SET_DEFAULT_GASLESS_PROVIDER, SetDefaultGaslessProviderRequest(providerId))
}

internal suspend fun BridgeRpcClient.getRegisteredGaslessProviders(): List<String> =
    callTyped<ProviderIdsResponse>(BridgeMethodConstants.METHOD_GET_REGISTERED_GASLESS_PROVIDERS).providerIds

internal suspend fun BridgeRpcClient.hasGaslessProvider(providerId: String): Boolean =
    callTyped<HasProviderResponse>(
        BridgeMethodConstants.METHOD_HAS_GASLESS_PROVIDER,
        HasGaslessProviderRequest(providerId),
    ).result

internal suspend fun BridgeRpcClient.getGaslessMetadata(providerId: String?): TONGaslessProviderMetadata =
    callTyped(
        BridgeMethodConstants.METHOD_GET_GASLESS_METADATA,
        GetGaslessMetadataRequest(providerId),
    )

internal suspend fun BridgeRpcClient.getGaslessConfig(
    network: TONNetwork?,
    providerId: String?,
): TONGaslessConfig = callTyped(
    BridgeMethodConstants.METHOD_GET_GASLESS_CONFIG,
    GetGaslessConfigRequest(
        network = network?.let { json.encodeToJsonElement(TONNetwork.serializer(), it) },
        providerId = providerId,
    ),
)

internal suspend fun BridgeRpcClient.getGaslessQuote(
    params: TONGaslessQuoteParams,
    providerId: String?,
): TONGaslessQuote = callTyped(
    BridgeMethodConstants.METHOD_GET_GASLESS_QUOTE,
    GetGaslessQuoteRequest(
        params = json.encodeToJsonElement(TONGaslessQuoteParams.serializer(), params),
        providerId = providerId,
    ),
)

internal suspend fun BridgeRpcClient.gaslessSendTransaction(
    params: TONGaslessSendParams,
    providerId: String?,
): TONGaslessSendResponse = callTyped(
    BridgeMethodConstants.METHOD_GASLESS_SEND_TRANSACTION,
    GaslessSendTransactionRequest(
        params = json.encodeToJsonElement(TONGaslessSendParams.serializer(), params),
        providerId = providerId,
    ),
)
