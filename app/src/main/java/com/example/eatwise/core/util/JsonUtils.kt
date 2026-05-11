package com.example.eatwise.core.util

import com.example.eatwise.domain.model.MealAnalysisResult
import kotlinx.serialization.SerializationException
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

object JsonUtils {
    val json = Json {
        ignoreUnknownKeys = true
        explicitNulls = false
        encodeDefaults = true
    }

    fun parseMealAnalysis(raw: String): MealAnalysisResult {
        val content = extractJson(raw)
        return try {
            json.decodeFromString(MealAnalysisResult.serializer(), content)
        } catch (error: SerializationException) {
            throw IllegalArgumentException("结果格式异常，请重新分析。", error)
        }
    }

    fun extractJson(raw: String): String {
        val trimmed = raw.trim()
        val codeBlock = Regex("```(?:json)?\\s*([\\s\\S]*?)```", RegexOption.IGNORE_CASE)
            .find(trimmed)
            ?.groupValues
            ?.getOrNull(1)
            ?.trim()
        if (!codeBlock.isNullOrBlank()) return codeBlock

        val start = trimmed.indexOf('{')
        val end = trimmed.lastIndexOf('}')
        return if (start >= 0 && end > start) trimmed.substring(start, end + 1) else trimmed
    }

    inline fun <reified T> encode(value: T): String = json.encodeToString(value)

    inline fun <reified T> decodeList(raw: String): List<T> =
        runCatching { json.decodeFromString<List<T>>(raw) }.getOrDefault(emptyList())
}
