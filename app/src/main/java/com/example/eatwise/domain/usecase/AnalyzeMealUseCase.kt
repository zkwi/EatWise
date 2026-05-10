package com.example.eatwise.domain.usecase

import com.example.eatwise.core.network.ApiException
import com.example.eatwise.core.network.LlmConfig
import com.example.eatwise.core.network.OpenAiCompatibleClient
import com.example.eatwise.core.util.AppResult
import com.example.eatwise.core.util.ImageCompressor
import com.example.eatwise.core.util.JsonUtils
import com.example.eatwise.data.repository.SettingsRepository
import com.example.eatwise.domain.model.MealAnalysisResult
import java.io.File

class AnalyzeMealUseCase(
    private val settingsRepository: SettingsRepository,
    private val client: OpenAiCompatibleClient,
    private val imageCompressor: ImageCompressor,
) {
    suspend operator fun invoke(originalImage: File): AppResult<AnalysisOutput> {
        val settings = settingsRepository.current()
        if (settings.apiKey.isBlank()) return AppResult.Failure("请先在设置中配置 API Key。")
        val modelName = normalizeModelName(settings.modelName)
        if (modelName.isBlank()) return AppResult.Failure("请先在设置中填写模型名称。")

        val compressed = try {
            imageCompressor.compress(originalImage)
        } catch (error: Throwable) {
            return AppResult.Failure("图片处理失败，请重试。", error)
        }

        val config = LlmConfig(settings.baseUrl, modelName, settings.apiKey)
        val rawContent = try {
            client.analyzeMeal(config, settings.userGoal, compressed)
        } catch (error: ApiException) {
            return AppResult.Failure(error.message, error)
        } catch (error: Throwable) {
            return AppResult.Failure("网络请求失败，请检查网络或 API 配置。", error)
        }

        val extractedJson = JsonUtils.extractJson(rawContent)
        val result = runCatching { JsonUtils.parseMealAnalysis(rawContent) }.getOrElse { error ->
            return AppResult.Failure("AI 返回格式异常，请重新分析。", error)
        }

        return AppResult.Success(
            AnalysisOutput(
                result = result,
                aiResultJson = extractedJson,
                originalImagePath = originalImage.absolutePath,
                compressedImagePath = compressed.absolutePath,
                userGoalSnapshot = settings.userGoal,
            ),
        )
    }

    private fun normalizeModelName(value: String): String =
        value
            .lines()
            .map { it.trim() }
            .firstOrNull { it.isNotBlank() }
            .orEmpty()
}

data class AnalysisOutput(
    val result: MealAnalysisResult,
    val aiResultJson: String,
    val originalImagePath: String,
    val compressedImagePath: String,
    val userGoalSnapshot: String,
)
