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
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.booleanOrNull
import kotlinx.serialization.json.doubleOrNull
import kotlinx.serialization.json.longOrNull
import kotlinx.serialization.serializer
import org.json.JSONArray
import org.json.JSONObject

internal class BridgeCodec(private val json: Json) {

    fun encode(value: Any?): Any? = when (value) {
        null -> JSONObject.NULL
        is BridgeEncodable -> encode(value.encodeForBridge())
        is String, is Boolean -> value
        is Number -> value
        is JSONObject, is JSONArray -> value
        is JsonElement -> jsonElementToOrgJson(value)
        is List<*> -> JSONArray().also { arr -> value.forEach { arr.put(encode(it)) } }
        is Map<*, *> -> JSONObject().also { obj ->
            value.forEach { (k, v) -> obj.put(k.toString(), encode(v)) }
        }
        else -> encodeSerializable(value)
    }

    private fun encodeSerializable(value: Any): Any {
        val klass = value::class
        return try {
            @Suppress("UNCHECKED_CAST")
            val ks = json.serializersModule.serializer(klass.java) as KSerializer<Any>
            jsonElementToOrgJson(json.encodeToJsonElement(ks, value))
        } catch (e: Throwable) {
            throw BridgeConversionError.UnableToEncode(klass, e)
        }
    }

    private fun jsonElementToOrgJson(element: JsonElement): Any = when (element) {
        is JsonObject -> JSONObject(element.toString())
        is JsonArray -> JSONArray(element.toString())
        is JsonPrimitive -> when {
            element is JsonNull -> JSONObject.NULL
            element.isString -> element.content
            else ->
                element.booleanOrNull
                    ?: element.longOrNull
                    ?: element.doubleOrNull
                    ?: element.content
        }
    }
}
