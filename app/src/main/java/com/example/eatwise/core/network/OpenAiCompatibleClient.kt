package com.example.eatwise.core.network

import android.util.Base64
import com.example.eatwise.core.util.JsonUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonArray
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import kotlinx.serialization.json.putJsonArray
import kotlinx.serialization.json.putJsonObject
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File

class OpenAiCompatibleClient(
    private val okHttpClient: OkHttpClient,
) {
    private val json = Json { ignoreUnknownKeys = true }
    private val mediaType = "application/json; charset=utf-8".toMediaType()

    suspend fun analyzeMeal(
        config: LlmConfig,
        userGoal: String,
        imageFile: File,
    ): String = withContext(Dispatchers.IO) {
        val base64 = Base64.encodeToString(imageFile.readBytes(), Base64.NO_WRAP)
        val body = buildRequestBody(config.modelName, userGoal, base64)
        val url = buildEndpoint(config.baseUrl)
        val requestBuilder = Request.Builder()
            .url(url)
            .post(json.encodeToString(JsonObject.serializer(), body).toRequestBody(mediaType))
            .addHeader("Authorization", "Bearer ${config.apiKey}")
            .addHeader("Content-Type", "application/json")

        if (config.baseUrl.contains("openrouter.ai", ignoreCase = true)) {
            requestBuilder
                .addHeader("HTTP-Referer", "meal-ai-local")
                .addHeader("X-OpenRouter-Title", "Meal AI Local")
        }

        okHttpClient.newCall(requestBuilder.build()).execute().use { response ->
            val responseBody = response.body.string()
            if (!response.isSuccessful) {
                throw ApiException(response.code, mapStatusMessage(response.code, responseBody))
            }

            val completion = json.decodeFromString(ChatCompletionResponse.serializer(), responseBody)
            completion.choices.firstOrNull()?.message?.content
                ?: throw ApiException(null, "AI 返回为空，请重试。")
        }
    }

    suspend fun testConnection(config: LlmConfig): Unit = withContext(Dispatchers.IO) {
        val body = buildJsonObject {
            put("model", config.modelName)
            put("temperature", 0.0)
            put("max_tokens", 50)
            putJsonArray("messages") {
                add(message("user", JsonPrimitive("请只回复 JSON：{\"ok\":true}")))
            }
        }
        val requestBuilder = Request.Builder()
            .url(buildEndpoint(config.baseUrl))
            .post(json.encodeToString(JsonObject.serializer(), body).toRequestBody(mediaType))
            .addHeader("Authorization", "Bearer ${config.apiKey}")
            .addHeader("Content-Type", "application/json")

        if (config.baseUrl.contains("openrouter.ai", ignoreCase = true)) {
            requestBuilder
                .addHeader("HTTP-Referer", "meal-ai-local")
                .addHeader("X-OpenRouter-Title", "Meal AI Local")
        }

        okHttpClient.newCall(requestBuilder.build()).execute().use { response ->
            val responseBody = response.body.string()
            if (!response.isSuccessful) {
                throw ApiException(response.code, mapStatusMessage(response.code, responseBody))
            }
            val content = json.decodeFromString(ChatCompletionResponse.serializer(), responseBody)
                .choices
                .firstOrNull()
                ?.message
                ?.content
            if (content.isNullOrBlank()) throw ApiException(null, "模型返回内容为空。")
            runCatching {
                json.decodeFromString(JsonObject.serializer(), JsonUtils.extractJson(content))
            }
                .getOrElse { throw ApiException(null, "模型连接正常，但返回格式异常。") }
            Unit
        }
    }

    private fun buildRequestBody(
        modelName: String,
        userGoal: String,
        base64: String,
    ): JsonObject = buildJsonObject {
        put("model", modelName)
        put("temperature", 0.2)
        put("max_tokens", 2000)
        putJsonArray("messages") {
            add(message("system", JsonPrimitive(systemPrompt)))
            add(
                message(
                    "user",
                    buildJsonArray {
                        add(buildJsonObject {
                            put("type", "text")
                            put("text", userPrompt(userGoal))
                        })
                        add(buildJsonObject {
                            put("type", "image_url")
                            putJsonObject("image_url") {
                                put("url", "data:image/jpeg;base64,$base64")
                            }
                        })
                    },
                ),
            )
        }
    }

    private fun message(role: String, content: kotlinx.serialization.json.JsonElement) = buildJsonObject {
        put("role", role)
        put("content", content)
    }

    private fun buildEndpoint(baseUrl: String): String {
        val base = baseUrl.trim().trimEnd('/')
        return if (base.endsWith("/chat/completions", ignoreCase = true)) {
            base
        } else {
            "$base/chat/completions"
        }
    }

    private fun userPrompt(userGoal: String) = """
        用户目标：
        $userGoal

        请分析这张餐食图片。

        必须只返回一个 JSON 对象，字段名必须使用下面的英文 key，不要使用中文 key：
        {
          "meal_name": "餐食名称",
          "summary": "简短摘要",
          "total_kcal": 0,
          "confidence": 0.7,
          "macros": {
            "protein_g": 0,
            "carbs_g": 0,
            "fat_g": 0
          },
          "goal_match": {
            "level": "good|partial|poor|unknown",
            "score": 1,
            "reason": "原因"
          },
          "ingredients": [
            {
              "name": "食材名",
              "amount": "估算分量",
              "kcal": 0
            }
          ],
          "suggestions": ["建议1", "建议2"],
          "tags": ["标签1"],
          "disclaimer": "以上是基于图片的粗略估算，仅供饮食记录参考。"
        }

        约束：
        - goal_match.level 只能是 good、partial、poor、unknown。
        - suggestions 返回 1 到 3 条。
        - 数字字段不确定时可返回 null。
        - 不要 Markdown，不要代码块，不要额外解释。
    """.trimIndent()

    private fun mapStatusMessage(statusCode: Int, responseBody: String): String {
        val apiMessage = extractApiErrorMessage(responseBody)
        return when (statusCode) {
            400 -> if (apiMessage.contains("image", true) || apiMessage.contains("vision", true)) {
                "当前模型可能不支持图片分析，请更换支持视觉输入的模型。"
            } else {
                "请求参数可能不兼容，请检查模型是否支持图片输入。"
            }
            401 -> "API Key 可能无效，请检查设置。"
            else -> "网络请求失败，请检查网络或 API 配置。"
        }
    }

    private fun extractApiErrorMessage(responseBody: String): String =
        runCatching {
            json.decodeFromString(ApiErrorEnvelope.serializer(), responseBody).error?.message
        }.getOrNull().orEmpty().take(240)

    companion object {
        const val promptVersion = 1

        private val systemPrompt = """
            你是一个个人饮食记录和营养分析助手。
            用户会上传一张餐食图片，并提供自己的健康管理目标。
            你的任务是：
            1. 识别图片中的主要食物；
            2. 粗略估算总热量和三大营养素；
            3. 根据用户目标判断这餐是否合适；
            4. 给出简洁、具体、可执行的建议；
            5. 不要做医学诊断；
            6. 不要替代医生、营养师或药物治疗建议；
            7. 如果不确定，请明确说明这是估算；
            8. 必须只返回 JSON，不要 Markdown，不要代码块，不要额外解释。
        """.trimIndent()
    }
}

class ApiException(
    val statusCode: Int?,
    override val message: String,
) : Exception(message)
