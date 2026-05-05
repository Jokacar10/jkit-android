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

import io.mockk.coEvery
import io.mockk.mockk
import io.ton.walletkit.api.WalletVersions
import io.ton.walletkit.api.generated.TONNetwork
import io.ton.walletkit.engine.state.SignerManager
import io.ton.walletkit.internal.constants.NetworkConstants
import io.ton.walletkit.model.TONUserFriendlyAddress
import io.ton.walletkit.model.WalletAdapterInfo
import kotlinx.coroutines.runBlocking
import org.json.JSONArray
import org.json.JSONObject
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * Tests for WalletOperations response parsing and data transformation.
 *
 * Focus: Response parsing logic, hex prefix stripping, default value handling.
 * The mocks model the actual JS bridge contract: getWallets returns a raw array,
 * getWalletAddress / getBalance return raw strings, addWallet / getWallet return
 * `{walletId, wallet}` envelopes.
 */
@RunWith(RobolectricTestRunner::class)
@Config(manifest = Config.NONE, sdk = [28])
class WalletOperationsTest : OperationsTestBase() {

    private lateinit var signerManager: SignerManager
    private lateinit var walletOperations: WalletOperations
    private var currentNetwork = NetworkConstants.DEFAULT_NETWORK

    companion object {
        const val TEST_ADDRESS_1 = "EQCD39VS5jcptHL8vMjEXrzGaRcCVYto7HUn4bpAOg8xqB2N"
        const val TEST_ADDRESS_2 = "Ef8zMzMzMzMzMzMzMzMzMzMzMzMzMzMzMzMzMzMzMzMzM0vF"
    }

    @Before
    override fun setup() {
        super.setup()
        signerManager = mockk(relaxed = true)
        walletOperations = WalletOperations(
            ensureInitialized = ensureInitialized,
            rpcClient = rpcClient,
            signerManager = signerManager,
            adapterManager = mockk(relaxed = true),
            currentNetworkProvider = { currentNetwork },
            json = json,
        )
    }

    // --- createSignerFromMnemonic tests ---

    @Test
    fun createSignerFromMnemonic_extractsSignerFromNestedResponse() = runBlocking {
        givenBridgeReturns(
            jsonOf(
                "signerId" to "signer-123",
                "publicKey" to "0xabcdef1234567890",
            ),
        )

        val result = walletOperations.createSignerFromMnemonic(listOf("word1", "word2"))

        assertEquals("signer-123", result.signerId)
        assertEquals("abcdef1234567890", result.publicKey.value) // 0x prefix stripped
    }

    @Test
    fun createSignerFromMnemonic_stripsHexPrefixFromPublicKey() = runBlocking {
        givenBridgeReturns(
            jsonOf(
                "signerId" to "signer-1",
                "publicKey" to "0x1234abcd",
            ),
        )

        val result = walletOperations.createSignerFromMnemonic(listOf("test"))

        assertEquals("1234abcd", result.publicKey.value)
    }

    @Test
    fun createSignerFromMnemonic_handlesPublicKeyWithoutPrefix() = runBlocking {
        givenBridgeReturns(
            jsonOf(
                "signerId" to "signer-1",
                "publicKey" to "abcd1234",
            ),
        )

        val result = walletOperations.createSignerFromMnemonic(listOf("test"))

        assertEquals("abcd1234", result.publicKey.value)
    }

    // --- getWallets tests ---

    @Test
    fun getWallets_parsesArrayOfWallets() = runBlocking {
        // JS returns the wallet array directly, not an envelope.
        coEvery { rpcClient.callRaw(any(), any()) } coAnswers {
            val method = firstArg<String>()
            capturedMethod = method
            capturedParams = secondArg()
            when (method) {
                "getWalletAddress" -> {
                    val walletId =
                        (secondArg<Any?>() as? io.ton.walletkit.engine.operations.requests.WalletIdRequest)?.walletId ?: ""
                    if (walletId.contains("wallet-1")) TEST_ADDRESS_1 else TEST_ADDRESS_2
                }
                else -> JSONArray().apply {
                    put(
                        JSONObject().apply {
                            put("walletId", "-239:wallet-1")
                            put(
                                "wallet",
                                JSONObject().apply {
                                    put("publicKey", "0xpub1")
                                    put("version", WalletVersions.V5R1)
                                },
                            )
                        },
                    )
                    put(
                        JSONObject().apply {
                            put("walletId", "-3:wallet-2")
                            put(
                                "wallet",
                                JSONObject().apply {
                                    put("publicKey", "pub2")
                                    put("version", WalletVersions.V4R2)
                                },
                            )
                        },
                    )
                }
            }
        }

        val result = walletOperations.getWallets()

        assertEquals(2, result.size)
        assertEquals(TEST_ADDRESS_1, result[0].address.value)
        assertEquals("pub1", result[0].publicKey)
        assertEquals(WalletVersions.V5R1, result[0].version)
        assertEquals(TEST_ADDRESS_2, result[1].address.value)
        assertEquals("pub2", result[1].publicKey)
        assertEquals(WalletVersions.V4R2, result[1].version)
    }

    @Test
    fun getWallets_returnsEmptyListIfEmptyArray() = runBlocking {
        givenBridgeReturnsRaw(JSONArray()) // raw empty array

        val result = walletOperations.getWallets()

        assertTrue(result.isEmpty())
    }

    // --- getWallet tests ---

    @Test
    fun getWallet_parsesWalletObject() = runBlocking {
        coEvery { rpcClient.callRaw(any(), any()) } coAnswers {
            val method = firstArg<String>()
            capturedMethod = method
            capturedParams = secondArg()
            when (method) {
                "getWalletAddress" -> TEST_ADDRESS_1
                else -> jsonOf(
                    "walletId" to "-239:$TEST_ADDRESS_1",
                    "wallet" to JSONObject().apply {
                        put("publicKey", "0xsinglekey")
                        put("version", WalletVersions.V5R1)
                    },
                )
            }
        }

        val result = walletOperations.getWallet(TEST_ADDRESS_1)

        assertNotNull(result)
        assertEquals(TEST_ADDRESS_1, result!!.address.value)
        assertEquals("singlekey", result.publicKey)
        assertEquals(WalletVersions.V5R1, result.version)
    }

    @Test
    fun getWallet_returnsNullIfBridgeReturnsNull() = runBlocking {
        givenBridgeReturnsRaw(null)

        val result = walletOperations.getWallet("nonexistent")

        assertNull(result)
    }

    @Test
    fun getWallet_returnsNullIfAddressMissing() = runBlocking {
        // Mock getWalletAddress to return empty string
        coEvery { rpcClient.callRaw(any(), any()) } coAnswers {
            val method = firstArg<String>()
            capturedMethod = method
            capturedParams = secondArg()
            when (method) {
                "getWalletAddress" -> ""
                else -> jsonOf(
                    "walletId" to "-239:missing",
                    "wallet" to JSONObject().apply {
                        put("publicKey", "0xkey")
                        put("version", WalletVersions.V4R2)
                    },
                )
            }
        }

        val result = walletOperations.getWallet("missing")

        assertNull(result)
    }

    // --- getBalance tests ---

    @Test
    fun getBalance_returnsRawString() = runBlocking {
        givenBridgeReturnsRaw("1000000000")

        val result = walletOperations.getBalance("EQAddress")

        assertEquals("1000000000", result)
    }

    // --- removeWallet tests ---

    @Test
    fun removeWallet_completesSuccessfully() = runBlocking {
        // JS removeWallet returns void; bridge call succeeds without exception
        givenBridgeReturns(JSONObject())

        walletOperations.removeWallet("EQAddress")
    }

    // --- addWallet tests ---

    @Test
    fun addWallet_parsesWalletResponse() = runBlocking {
        coEvery { rpcClient.callRaw(any(), any()) } coAnswers {
            val method = firstArg<String>()
            capturedMethod = method
            capturedParams = secondArg()
            when (method) {
                "getWalletAddress" -> TEST_ADDRESS_1
                else -> jsonOf(
                    "walletId" to "-239:$TEST_ADDRESS_1",
                    "wallet" to JSONObject().apply {
                        put("publicKey", "0xnewkey")
                        put("version", WalletVersions.V5R1)
                    },
                )
            }
        }

        val result = walletOperations.addWallet(
            WalletAdapterInfo(
                adapterId = "adapter-123",
                address = TONUserFriendlyAddress(""),
                network = TONNetwork(chainId = "-239"),
            ),
        )

        assertEquals(TEST_ADDRESS_1, result.address.value)
        assertEquals("newkey", result.publicKey)
        assertEquals(WalletVersions.V5R1, result.version)
    }

    @Test
    fun addWallet_usesUnknownVersionIfMissing() = runBlocking {
        coEvery { rpcClient.callRaw(any(), any()) } coAnswers {
            val method = firstArg<String>()
            capturedMethod = method
            capturedParams = secondArg()
            when (method) {
                "getWalletAddress" -> TEST_ADDRESS_1
                else -> jsonOf(
                    "walletId" to "-239:$TEST_ADDRESS_1",
                    "wallet" to JSONObject(),
                )
            }
        }

        val result = walletOperations.addWallet(
            WalletAdapterInfo(
                adapterId = "adapter-123",
                address = TONUserFriendlyAddress(""),
                network = TONNetwork(chainId = "-239"),
            ),
        )

        assertEquals("unknown", result.version)
    }
}
