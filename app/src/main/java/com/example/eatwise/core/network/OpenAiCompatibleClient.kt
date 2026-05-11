package com.example.eatwise.core.network

import android.util.Base64
import com.example.eatwise.core.util.JsonUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.booleanOrNull
import kotlinx.serialization.json.buildJsonArray
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonPrimitive
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
        val body = buildVisionTestBody(config.modelName)
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
                .getOrElse { throw ApiException(null, "模型连接正常，但多模态测试返回格式异常。") }
                .also { result ->
                    val visionConfirmed = result["vision"]?.jsonPrimitive?.booleanOrNull == true
                    val color = result["color"]?.jsonPrimitive?.contentOrNull.orEmpty()
                    val imageRecognized = color.contains("绿", ignoreCase = true) || color.contains("green", ignoreCase = true)
                    if (!visionConfirmed || !imageRecognized) {
                        throw ApiException(null, "模型未正确识别测试图片，请更换支持视觉输入的模型。")
                    }
                }
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

    private fun buildVisionTestBody(modelName: String): JsonObject = buildJsonObject {
        put("model", modelName)
        put("temperature", 0.0)
        put("max_tokens", 80)
        putJsonArray("messages") {
            add(
                message(
                    "user",
                    buildJsonArray {
                        add(buildJsonObject {
                            put("type", "text")
                            put(
                                "text",
                                """
                                    请观察随附图片，验证你是否能读取图片内容。
                                    必须只返回 JSON：{"ok":true,"vision":true,"color":"主色"}
                                    如果看不到图片，返回：{"ok":false,"vision":false,"color":""}
                                """.trimIndent(),
                            )
                        })
                        add(buildJsonObject {
                            put("type", "image_url")
                            putJsonObject("image_url") {
                                put("url", multimodalTestImageUrl)
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
          "eating_advice": "只能尝一小口|需要严格控量|可以适量吃|可以适量多吃",
          "goal_match": {
            "level": "good|partial|poor|unknown",
            "reason": "原因"
          },
          "ingredients": [
            {
              "dish": "所属菜品",
              "name": "食材名"
            }
          ],
          "suggestions": ["建议1", "建议2"],
          "tags": ["标签1"],
          "disclaimer": "以上是基于图片的定性判断，仅供饮食记录参考。"
        }

        约束：
        - 如果图片里有多个菜品，meal_name 用整体名称，例如“多菜品拼餐”或“米饭配三菜”。
        - ingredients 必须覆盖主要菜品和关键可见食材；多个菜品混合时，用 dish 标明所属菜品。
        - 复合菜品只拆主要构成和明显风险点，不拆不可见细节、零散调料或低价值细项；每个菜品保留 1 到 3 条即可。
        - 不要估算每个食材的克数、重量或“约150g”这类分量；ingredients 不包含分量字段。
        - 不要输出任何具体卡路里、热量数值、克数或宏量营养素数值。
        - 不要使用“约xx kcal、约xx克、蛋白质xx克”这类表达。
        - eating_advice 必须且只能从四个固定值中选一个：只能尝一小口、需要严格控量、可以适量吃、可以适量多吃。
        - 红油、酱料、调料、肥肉、油炸食物不要标为“轻负担”，应按油脂、重口味或油炸风险处理。
        - goal_match.level 只能是 good、partial、poor、unknown。
        - suggestions 返回 1 到 3 条，每条不超过 18 个中文字符，必须具体可执行。
        - suggestions 必须符合普通用户真实场景，优先给“只能尝一小口、需要严格控量、酱料少放、加一份蔬菜、下餐清淡”等可做到的小调整。
        - 不要给极端方案，例如完全禁食、只吃单一食物、严格称重、复杂食谱或药物建议。
        - tags 返回 2 到 4 个短标签，每个不超过 6 个中文字符，只保留对用户决策有帮助的结论。
        - tags 优先使用生活化标签，例如：油脂高、油炸、重口味、糖偏高、钠偏高、蔬菜少、蛋白足、有蔬菜、轻负担、控脂谨慎。
        - tags 不要返回“常规食材、常规分量、常规份量、普通、一般、粗估、蛋白来源、油脂调味”等低信息量或食材角色标签。
        - tags 不要把同一事实同时写成正向和负向，例如“红油调料”不能同时是“轻负担”和“油脂调味”。
        - tags 不要返回长句、重复标签、医学诊断词或“健康/不健康”这类空泛判断。
        - summary 和 goal_match.reason 都要简短，避免长段落。
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
        const val promptVersion = 6
        private const val multimodalTestImageUrl =
            "data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAACAAAAAgCAIAAAD8GO2jAAAAMUlEQVR42mPwWR9AU8QwasGoBaMWDLgF/4kAoxaMWjBqwagFtLZgtLgetWDUgiFhAQDtfCB7H/LRxAAAAABJRU5ErkJggg=="

        private val systemPrompt = """
            你是一个个人饮食记录和营养分析助手。
            用户会上传一张餐食图片，并提供自己的健康管理目标。
            你的任务是：
            1. 识别图片中的主要食物；
            2. 如有多个菜品或复合菜品，拆分主要菜品和可见食材；
            3. 不估算食物重量、卡路里数值或宏量营养素数值；
            4. 根据食物类型、烹饪方式和用户目标，做定性健康判断；
            5. 给出“怎么吃”的短建议，例如只能尝一小口、需要严格控量、可以适量吃、可以适量多吃；
            6. 标签和建议必须短，适合手机卡片展示，标签只表达有用结论，不输出常规、普通、估算类标签；
            7. 不要做医学诊断；
            8. 不要替代医生、营养师或药物治疗建议；
            9. 如果不确定，请明确说明这是估算；
            10. 必须只返回 JSON，不要 Markdown，不要代码块，不要额外解释。
        """.trimIndent()
    }
}

class ApiException(
    val statusCode: Int?,
    override val message: String,
) : Exception(message)
