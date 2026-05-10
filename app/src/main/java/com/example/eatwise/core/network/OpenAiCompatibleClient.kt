package com.example.eatwise.core.network

import android.util.Base64
import com.example.eatwise.core.util.JsonUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonArrayBuilder
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonObjectBuilder
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
        useJsonSchema: Boolean,
    ): String = withContext(Dispatchers.IO) {
        val base64 = Base64.encodeToString(imageFile.readBytes(), Base64.NO_WRAP)
        val body = buildRequestBody(config.modelName, userGoal, base64, useJsonSchema)
        val url = config.baseUrl.trimEnd('/') + "/chat/completions"
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
                throw ApiException(response.code, mapStatusMessage(response.code, responseBody), responseBody)
            }

            val completion = json.decodeFromString(ChatCompletionResponse.serializer(), responseBody)
            completion.choices.firstOrNull()?.message?.content
                ?: throw ApiException(null, "AI 返回为空，请重试。", responseBody)
        }
    }

    private fun buildRequestBody(
        modelName: String,
        userGoal: String,
        base64: String,
        useJsonSchema: Boolean,
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
        put(
            "response_format",
            if (useJsonSchema) jsonSchemaResponseFormat() else buildJsonObject { put("type", "json_object") },
        )
    }

    private fun message(role: String, content: kotlinx.serialization.json.JsonElement) = buildJsonObject {
        put("role", role)
        put("content", content)
    }

    private fun jsonSchemaResponseFormat(): JsonObject = buildJsonObject {
        put("type", "json_schema")
        putJsonObject("json_schema") {
            put("name", "meal_analysis")
            put("strict", true)
            put("schema", MealAnalysisSchema.schema)
        }
    }

    private fun userPrompt(userGoal: String) = """
        用户目标：
        $userGoal

        请分析这张餐食图片，并返回餐食名称、摘要、总热量、三大营养素、目标匹配、食材明细、1 到 3 条建议、标签和免责声明。
    """.trimIndent()

    private fun mapStatusMessage(statusCode: Int, responseBody: String): String {
        val apiMessage = runCatching {
            json.decodeFromString(ApiErrorEnvelope.serializer(), responseBody).error?.message
        }.getOrNull().orEmpty()
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
    val rawBody: String? = null,
) : Exception(message)

private object MealAnalysisSchema {
    val schema: JsonObject = buildJsonObject {
        put("type", "object")
        putJsonObject("properties") {
            string("meal_name")
            string("summary")
            nullableNumber("total_kcal")
            nullableNumber("confidence")
            putJsonObject("macros") {
                put("type", "object")
                putJsonObject("properties") {
                    nullableNumber("protein_g")
                    nullableNumber("carbs_g")
                    nullableNumber("fat_g")
                }
                putJsonArray("required") { addAll("protein_g", "carbs_g", "fat_g") }
                put("additionalProperties", false)
            }
            putJsonObject("goal_match") {
                put("type", "object")
                putJsonObject("properties") {
                    putJsonObject("level") {
                        putJsonArray("enum") {
                            add(JsonPrimitive("good"))
                            add(JsonPrimitive("partial"))
                            add(JsonPrimitive("poor"))
                            add(JsonPrimitive("unknown"))
                        }
                    }
                    nullableInteger("score")
                    string("reason")
                }
                putJsonArray("required") { addAll("level", "score", "reason") }
                put("additionalProperties", false)
            }
            putJsonObject("ingredients") {
                put("type", "array")
                putJsonObject("items") {
                    put("type", "object")
                    putJsonObject("properties") {
                        string("name")
                        string("amount")
                        nullableNumber("kcal")
                    }
                    putJsonArray("required") { addAll("name", "amount", "kcal") }
                    put("additionalProperties", false)
                }
            }
            stringArray("suggestions")
            stringArray("tags")
            string("disclaimer")
        }
        putJsonArray("required") {
            addAll(
                "meal_name",
                "summary",
                "total_kcal",
                "confidence",
                "macros",
                "goal_match",
                "ingredients",
                "suggestions",
                "tags",
                "disclaimer",
            )
        }
        put("additionalProperties", false)
    }

    private fun JsonObjectBuilder.string(name: String) {
        putJsonObject(name) { put("type", "string") }
    }

    private fun JsonObjectBuilder.nullableNumber(name: String) {
        putJsonObject(name) { put("type", JsonArray(listOf(JsonPrimitive("number"), JsonPrimitive("null")))) }
    }

    private fun JsonObjectBuilder.nullableInteger(name: String) {
        putJsonObject(name) { put("type", JsonArray(listOf(JsonPrimitive("integer"), JsonPrimitive("null")))) }
    }

    private fun JsonObjectBuilder.stringArray(name: String) {
        putJsonObject(name) {
            put("type", "array")
            putJsonObject("items") { put("type", "string") }
        }
    }

    private fun JsonArrayBuilder.addAll(vararg values: String) {
        values.forEach { add(JsonPrimitive(it)) }
    }
}
