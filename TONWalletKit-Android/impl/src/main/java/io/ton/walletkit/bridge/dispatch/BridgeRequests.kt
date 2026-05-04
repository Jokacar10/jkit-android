/*
 * Copyright (c) 2025 TonTech
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */
package io.ton.walletkit.bridge.dispatch

import kotlinx.serialization.Serializable

/**
 * Typed request payloads for reverse-RPC methods. Each `@Serializable` mirrors the JSON
 * shape the JS bridge sends in `params` for the matching method name.
 *
 * Quirky wire formats that don't fit a plain `@Serializable` (e.g. ByteArray serialised
 * as `number[]`) are handled inline in the registry registration with a manual
 * `JsonElement` decoder — see `MessageDispatcher` init.
 */

@Serializable
internal data class AdapterByIdRequest(val adapterId: String)

@Serializable
internal data class AdapterSignTransactionRequest(
    val adapterId: String,
    val input: String,
    val fakeSignature: Boolean? = null,
)

@Serializable
internal data class AdapterSignDataRequest(
    val adapterId: String,
    val input: String,
    val fakeSignature: Boolean? = null,
)

@Serializable
internal data class AdapterSignTonProofRequest(
    val adapterId: String,
    val input: String,
    val fakeSignature: Boolean? = null,
)

@Serializable
internal data class KotlinProviderQuoteRequest(val providerId: String, val params: String)

@Serializable
internal data class KotlinProviderBuildRequest(val providerId: String, val params: String)

@Serializable
internal data class KotlinProviderIdRequest(val providerId: String)

@Serializable
internal data class KotlinStakingGetStakedBalanceRequest(
    val providerId: String,
    val userAddress: String,
    val networkChainId: String? = null,
)

@Serializable
internal data class KotlinStakingGetProviderInfoRequest(
    val providerId: String,
    val networkChainId: String? = null,
)

@Serializable
internal data class KotlinProviderWatchRequest(
    val providerId: String,
    val subId: String,
    val type: String,
    val address: String? = null,
)

@Serializable
internal data class KotlinProviderUnwatchRequest(val subId: String)
