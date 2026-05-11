package com.example.eatwise.core.network

import android.util.Base64
import com.example.eatwise.core.i18n.AppLanguage
import com.example.eatwise.core.i18n.MealLanguageText
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
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.put
import kotlinx.serialization.json.putJsonArray
import kotlinx.serialization.json.putJsonObject
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.ResponseBody
import java.io.File

class OpenAiCompatibleClient(
    private val okHttpClient: OkHttpClient,
) {
    private val json = Json { ignoreUnknownKeys = true }
    private val mediaType = "application/json; charset=utf-8".toMediaType()

    suspend fun analyzeMeal(
        config: LlmConfig,
        userGoal: String,
        language: AppLanguage,
        imageFile: File,
        onContentChanged: (String) -> Unit = {},
    ): String = withContext(Dispatchers.IO) {
        val base64 = Base64.encodeToString(imageFile.readBytes(), Base64.NO_WRAP)
        val body = buildRequestBody(config.modelName, userGoal, language, base64, stream = true)
        val url = buildEndpoint(config.baseUrl)
        val requestBuilder = Request.Builder()
            .url(url)
            .post(json.encodeToString(JsonObject.serializer(), body).toRequestBody(mediaType))
            .addHeader("Authorization", "Bearer ${config.apiKey}")
            .addHeader("Content-Type", "application/json")
            .addHeader("Accept", "text/event-stream")

        if (config.baseUrl.contains("openrouter.ai", ignoreCase = true)) {
            requestBuilder
                .addHeader("HTTP-Referer", "meal-ai-local")
                .addHeader("X-OpenRouter-Title", "Meal AI Local")
        }

        okHttpClient.newCall(requestBuilder.build()).execute().use { response ->
            if (!response.isSuccessful) {
                val responseBody = response.body.string()
                throw ApiException(response.code, mapStatusMessage(response.code, responseBody, language))
            }

            readCompletionContent(response.body, language, onContentChanged)
        }
    }

    suspend fun testConnection(config: LlmConfig, language: AppLanguage = AppLanguage.default): Unit = withContext(Dispatchers.IO) {
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
                throw ApiException(response.code, mapStatusMessage(response.code, responseBody, language))
            }
            val content = json.decodeFromString(ChatCompletionResponse.serializer(), responseBody)
                .choices
                .firstOrNull()
                ?.message
                ?.content
            if (content.isNullOrBlank()) throw ApiException(null, emptyModelMessage(language))
            runCatching {
                json.decodeFromString(JsonObject.serializer(), JsonUtils.extractJson(content))
            }
                .getOrElse { throw ApiException(null, testFormatMessage(language)) }
                .also { result ->
                    val visionConfirmed = result["vision"]?.jsonPrimitive?.booleanOrNull == true
                    val color = result["color"]?.jsonPrimitive?.contentOrNull.orEmpty()
                    val imageRecognized = color.contains("绿", ignoreCase = true) || color.contains("green", ignoreCase = true)
                    if (!visionConfirmed || !imageRecognized) {
                        throw ApiException(null, imageUnsupportedMessage(language))
                    }
                }
            Unit
        }
    }

    private fun buildRequestBody(
        modelName: String,
        userGoal: String,
        language: AppLanguage,
        base64: String,
        stream: Boolean = false,
    ): JsonObject = buildJsonObject {
        put("model", modelName)
        put("temperature", 0.2)
        put("max_tokens", 2000)
        if (stream) put("stream", true)
        putJsonArray("messages") {
            add(message("system", JsonPrimitive(systemPrompt(language))))
            add(
                message(
                    "user",
                    buildJsonArray {
                        add(buildJsonObject {
                            put("type", "text")
                            put("text", userPrompt(userGoal, language))
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

    private fun readCompletionContent(
        responseBody: ResponseBody,
        language: AppLanguage,
        onContentChanged: (String) -> Unit,
    ): String {
        val streamContent = StringBuilder()
        val fallbackBody = StringBuilder()

        responseBody.charStream().useLines { lines ->
            for (line in lines) {
                val trimmed = line.trim()
                if (trimmed.startsWith("data:", ignoreCase = true)) {
                    val data = trimmed.substringAfter(":").trim()
                    if (data == "[DONE]") break
                    val delta = extractStreamDelta(data)
                    if (delta.isNotEmpty()) {
                        streamContent.append(delta)
                        onContentChanged(streamContent.toString())
                    }
                } else if (trimmed.isNotBlank()) {
                    fallbackBody.appendLine(line)
                }
            }
        }

        val content = if (streamContent.isNotBlank()) {
            streamContent.toString()
        } else if (fallbackBody.isNotBlank()) {
            val completion = json.decodeFromString(ChatCompletionResponse.serializer(), fallbackBody.toString())
            completion.choices.firstOrNull()?.message?.content.orEmpty()
        } else {
            ""
        }
        if (content.isBlank()) throw ApiException(null, emptyModelMessage(language))
        onContentChanged(content)
        return content
    }

    private fun extractStreamDelta(data: String): String =
        runCatching {
            val choice = json.decodeFromString(JsonObject.serializer(), data)["choices"]
                ?.jsonArray
                ?.firstOrNull()
                ?.jsonObject
            choice
                ?.get("delta")
                ?.jsonObject
                ?.get("content")
                ?.jsonPrimitive
                ?.contentOrNull
                ?: choice
                    ?.get("message")
                    ?.jsonObject
                    ?.get("content")
                    ?.jsonPrimitive
                    ?.contentOrNull
                ?: ""
        }.getOrDefault("")

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

    private fun userPrompt(userGoal: String, language: AppLanguage) = """
        ${MealLanguageText.languageInstruction(language)}

        User goal:
        $userGoal

        Analyze this meal photo.

        Return exactly one JSON object. Field names must stay as the English keys below:
        {
          "meal_name": "localized meal name",
          "summary": "localized short summary",
          "eating_advice": "${MealLanguageText.eatingAdviceOptions(language)}",
          "goal_match": {
            "level": "good|partial|poor|unknown",
            "reason": "localized reason"
          },
          "ingredients": [
            {
              "dish": "localized dish name",
              "name": "localized visible ingredient"
            }
          ],
          "suggestions": ["localized actionable tip 1", "localized actionable tip 2"],
          "tags": ["localized tag 1"],
          "disclaimer": "${MealLanguageText.disclaimer(language)}"
        }

        Constraints:
        - If there are multiple dishes, use an overall meal name.
        - ingredients must cover the main dishes and key visible ingredients. For mixed meals, use dish to show which dish an ingredient belongs to.
        - For compound dishes, list only major parts and visible risk points. Do not list invisible details, scattered seasonings, or low-value tiny items.
        - Do not estimate grams, weight, calories, macro nutrients, or phrases like "about 150g" or "about xx kcal".
        - eating_advice must be exactly one localized option from: ${MealLanguageText.eatingAdviceOptions(language)}.
        - Red oil, sauces, fatty meat, and fried food must not be labeled as light burden. Treat them as oil, heavy seasoning, or fried risk when relevant.
        - goal_match.level must be only good, partial, poor, or unknown.
        - suggestions must return 1 to 3 short, concrete, doable actions. Examples in the target language: ${MealLanguageText.suggestionExamples(language)}.
        - Do not write abstract reminders such as "control it", "keep balanced", or "control frequency" unless you also say the exact action.
        - If this meal fits the goal well, still give one maintenance tip.
        - Do not give extreme plans, fasting advice, single-food diets, strict weighing, complex recipes, medical diagnosis, medicine, or treatment advice.
        - tags must return 2 to 4 short localized labels with decision value. Examples: ${MealLanguageText.tagExamples(language)}.
        - Do not return low-value tags such as: ${MealLanguageText.lowValueTagExamples(language)}.
        - Do not express the same fact as both positive and negative.
        - Do not return long tags, repeated tags, diagnosis terms, or vague "healthy/unhealthy" labels.
        - summary should state the 1 to 2 most important features of this meal, short enough for a mobile card.
        - goal_match.reason should briefly explain why this meal fits or does not fit the current goal.
        - Return null for uncertain numeric fields, but avoid numeric nutrition estimates.
        - No Markdown, no code fences, no extra explanation.
    """.trimIndent()

    private fun mapStatusMessage(statusCode: Int, responseBody: String, language: AppLanguage): String {
        val apiMessage = extractApiErrorMessage(responseBody)
        return when (statusCode) {
            400 -> if (apiMessage.contains("image", true) || apiMessage.contains("vision", true)) {
                imageUnsupportedMessage(language)
            } else {
                incompatibleRequestMessage(language)
            }
            401 -> invalidKeyMessage(language)
            else -> MealLanguageText.requestFailed(language)
        }
    }

    private fun emptyModelMessage(language: AppLanguage): String = when (language) {
        AppLanguage.ZhHans -> "模型没有返回内容，请重试。"
        AppLanguage.ZhHant -> "模型沒有返回內容，請重試。"
        AppLanguage.En -> "The model returned no content. Please try again."
        AppLanguage.Ja -> "モデルが内容を返しませんでした。もう一度お試しください。"
    }

    private fun testFormatMessage(language: AppLanguage): String = when (language) {
        AppLanguage.ZhHans -> "连接成功，但测试结果格式不对，请换一个支持图片的模型或重试。"
        AppLanguage.ZhHant -> "連接成功，但測試結果格式不對，請換一個支援圖片的模型或重試。"
        AppLanguage.En -> "Connection worked, but the test result format was invalid. Use an image-capable model or try again."
        AppLanguage.Ja -> "接続は成功しましたが、テスト結果の形式が不正です。画像対応モデルに変更するか再試行してください。"
    }

    private fun imageUnsupportedMessage(language: AppLanguage): String = when (language) {
        AppLanguage.ZhHans -> "这个模型可能看不了图片，请换一个支持图片输入的模型。"
        AppLanguage.ZhHant -> "這個模型可能看不了圖片，請換一個支援圖片輸入的模型。"
        AppLanguage.En -> "This model may not read images. Please use a model that supports image input."
        AppLanguage.Ja -> "このモデルは画像を読めない可能性があります。画像入力対応モデルに変更してください。"
    }

    private fun incompatibleRequestMessage(language: AppLanguage): String = when (language) {
        AppLanguage.ZhHans -> "请求参数不兼容，请检查 Base URL 和模型名称。"
        AppLanguage.ZhHant -> "請求參數不相容，請檢查 Base URL 和模型名稱。"
        AppLanguage.En -> "Request parameters are incompatible. Check the Base URL and model name."
        AppLanguage.Ja -> "リクエストパラメータに互換性がありません。Base URL とモデル名を確認してください。"
    }

    private fun invalidKeyMessage(language: AppLanguage): String = when (language) {
        AppLanguage.ZhHans -> "API Key 可能无效，请检查设置。"
        AppLanguage.ZhHant -> "API Key 可能無效，請檢查設定。"
        AppLanguage.En -> "The API key may be invalid. Please check Settings."
        AppLanguage.Ja -> "API Key が無効な可能性があります。設定を確認してください。"
    }

    private fun extractApiErrorMessage(responseBody: String): String =
        runCatching {
            json.decodeFromString(ApiErrorEnvelope.serializer(), responseBody).error?.message
        }.getOrNull().orEmpty().take(240)

    companion object {
        const val promptVersion = 9
        private const val multimodalTestImageUrl =
            "data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAACAAAAAgCAIAAAD8GO2jAAAAMUlEQVR42mPwWR9AU8QwasGoBaMWDLgF/4kAoxaMWjBqwagFtLZgtLgetWDUgiFhAQDtfCB7H/LRxAAAAABJRU5ErkJggg=="

        private fun systemPrompt(language: AppLanguage) = when (language) {
            AppLanguage.ZhHans -> """
                你是一个个人饮食记录和营养分析助手。
                用户会上传餐食图片，并提供自己的饮食目标。请用简体中文输出所有用户可见内容。
                识别主要食物；多菜品时拆分主要菜品和可见食材；不估算重量、卡路里或宏量营养素。
                根据食物类型、烹饪方式和用户目标判断这餐是否适合，给出普通人当场能做的小动作。
                不做医学诊断，不替代医生、营养师或药物治疗建议。必须只返回 JSON。
            """.trimIndent()
            AppLanguage.ZhHant -> """
                你是一個個人飲食記錄和營養分析助手。
                使用者會上傳餐食圖片，並提供自己的飲食目標。請用繁體中文輸出所有使用者可見內容。
                識別主要食物；多菜品時拆分主要菜品和可見食材；不估算重量、卡路里或宏量營養素。
                根據食物類型、烹飪方式和使用者目標判斷這餐是否適合，給出普通人當場能做的小動作。
                不做醫學診斷，不替代醫生、營養師或藥物治療建議。必須只返回 JSON。
            """.trimIndent()
            AppLanguage.En -> """
                You are a personal meal logging and nutrition guidance assistant.
                The user uploads a meal photo and a meal goal. Write every user-visible value in English.
                Identify main foods; split visible dishes and ingredients when there are multiple dishes; do not estimate weight, calories, or macros.
                Judge whether the meal fits the goal based on food type and cooking style, then give actions a normal person can do immediately.
                Do not diagnose, prescribe medicine, or replace professional medical or nutrition advice. Return JSON only.
            """.trimIndent()
            AppLanguage.Ja -> """
                あなたは個人向けの食事記録と栄養アドバイスのアシスタントです。
                ユーザーは食事写真と食事目標を提供します。ユーザーに見える値はすべて日本語で書いてください。
                主な食べ物を識別し、複数料理の場合は見える料理と食材を分けてください。重量、カロリー、三大栄養素は推定しません。
                食材や調理方法、目標に照らしてこの食事が合うかを判断し、すぐ実行できる小さな行動を提案してください。
                医学的診断、薬の助言、治療提案はしません。必ず JSON のみを返してください。
            """.trimIndent()
        }
    }
}

class ApiException(
    val statusCode: Int?,
    override val message: String,
) : Exception(message)
