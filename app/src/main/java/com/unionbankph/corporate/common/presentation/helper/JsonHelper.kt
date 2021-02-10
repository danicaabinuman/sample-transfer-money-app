package com.unionbankph.corporate.common.presentation.helper

import com.unionbankph.corporate.common.domain.exception.JsonParseException
import kotlinx.serialization.ImplicitReflectionSerializer
import kotlinx.serialization.UnstableDefault
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonConfiguration
import kotlinx.serialization.parse
import kotlinx.serialization.parseList
import kotlinx.serialization.stringify

@OptIn(ImplicitReflectionSerializer::class, UnstableDefault::class)
object JsonHelper {

    val json = Json(JsonConfiguration.Stable.copy(isLenient = true, ignoreUnknownKeys = true))

    inline fun <reified T : Any> fromJson(value: String?): T {
        return try {
            json.parse(value!!)
        } catch (e: Exception) {
            throw JsonParseException()
        }
    }

    inline fun <reified T : Any> fromListJson(value: String?): MutableList<T> {
        return try {
            json.parseList<T>(value!!).toMutableList()
        } catch (e: Exception) {
            throw JsonParseException()
        }

    }

    inline fun <reified T : Any> toJson(value: List<T>?): String {
        return value?.let {
            json.stringify(value)
        } ?: ""
    }

    inline fun <reified T : Any> toJson(value: T?): String {
        return value?.let {
            json.stringify(value)
        } ?: ""
    }
}
