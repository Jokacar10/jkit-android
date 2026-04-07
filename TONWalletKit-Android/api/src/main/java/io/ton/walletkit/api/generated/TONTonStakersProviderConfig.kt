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
package io.ton.walletkit.api.generated

/**
 * Configuration for creating a TonStakers staking provider.
 *
 * Mirrors the iOS `TONTonStakersProviderConfig` for cross-platform consistency.
 * Fields correspond to TON chain IDs: mainnet = "-239", testnet = "-3".
 *
 * @param mainnet Mainnet-specific configuration (optional)
 * @param testnet Testnet-specific configuration (optional)
 */
data class TONTonStakersProviderConfig(
    val mainnet: TONTonStakersChainConfig? = null,
    val testnet: TONTonStakersChainConfig? = null,
) {
    /**
     * Converts to a chain-ID-keyed map expected by the JavaScript bridge.
     */
    fun toChainConfigMap(): Map<String, TONTonStakersChainConfig> {
        return buildMap {
            mainnet?.let { put("-239", it) }
            testnet?.let { put("-3", it) }
        }
    }

    companion object
}
