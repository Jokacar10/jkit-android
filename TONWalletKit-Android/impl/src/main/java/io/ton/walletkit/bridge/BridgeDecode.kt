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
package io.ton.walletkit.bridge

import kotlinx.serialization.KSerializer
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.serializer
import org.json.JSONArray
import org.json.JSONObject
import kotlin.reflect.KClass

inline fun <reified T : Any> Json.decodeFromBridge(raw: Any?): T {
    val klass = T::class
    if (raw == null || raw == JSONObject.NULL) {
        throw BridgeConversionError.UnableToConvertNull(klass)
    }

    BridgeDecoders.decoderFor(klass)?.let { decoder ->
        @Suppress("UNCHECKED_CAST")
        return (decoder.decodeFromBridge(raw) as? T)
            ?: throw BridgeConversionError.UnableToConvert(klass, raw)
    }

    val primitive = decodePrimitive(klass, raw)
    if (primitive != null) {
        @Suppress("UNCHECKED_CAST")
        return primitive as T
    }

    return try {
        @Suppress("UNCHECKED_CAST")
        val ks = serializersModule.serializer<T>() as KSerializer<T>
        decodeFromJsonElement(ks, orgJsonToJsonElement(raw))
    } catch (e: BridgeConversionError) {
        throw e
    } catch (e: Throwable) {
        throw BridgeConversionError.UnableToDecode(klass, e)
    }
}

inline fun <reified T : Any> Json.decodeFromBridgeOrNull(raw: Any?): T? =
    if (raw == null || raw == JSONObject.NULL) null else decodeFromBridge<T>(raw)

@PublishedApi
internal fun decodePrimitive(klass: KClass<*>, raw: Any): Any? = when (klass) {
    String::class -> (raw as? String)
    Boolean::class -> (raw as? Boolean)
    Int::class -> (raw as? Number)?.toInt()
    Long::class -> (raw as? Number)?.toLong()
    Short::class -> (raw as? Number)?.toShort()
    Byte::class -> (raw as? Number)?.toByte()
    Float::class -> (raw as? Number)?.toFloat()
    Double::class -> (raw as? Number)?.toDouble()
    else -> null
}

@PublishedApi
internal fun orgJsonToJsonElement(raw: Any): JsonElement = when (raw) {
    JSONObject.NULL -> JsonNull
    is JSONObject -> Json.parseToJsonElement(raw.toString())
    is JSONArray -> Json.parseToJsonElement(raw.toString())
    is String -> JsonPrimitive(raw)
    is Boolean -> JsonPrimitive(raw)
    is Number -> JsonPrimitive(raw)
    is JsonElement -> raw
    else -> JsonPrimitive(raw.toString())
}
