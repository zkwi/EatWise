package com.example.eatwise.domain.usecase

import com.example.eatwise.core.network.ApiException
import com.example.eatwise.core.network.LlmConfig
import com.example.eatwise.core.network.OpenAiCompatibleClient
import com.example.eatwise.core.util.AppResult
import com.example.eatwise.core.util.ImageCompressor
import com.example.eatwise.core.util.JsonUtils
import com.example.eatwise.core.util.MealAnalysisPolisher
import com.example.eatwise.data.repository.SettingsRepository
import com.example.eatwise.domain.model.MealAnalysisResult
import kotlinx.coroutines.CancellationException
import java.io.File

class AnalyzeMealUseCase(
    private val settingsRepository: SettingsRepository,
    private val client: OpenAiCompatibleClient,
    private val imageCompressor: ImageCompressor,
) {
    suspend operator fun invoke(
        originalImage: File,
        onStageChanged: (AnalysisStage) -> Unit = {},
        onPromptReady: (String) -> Unit = {},
        onModelOutputChanged: (String) -> Unit = {},
    ): AppResult<AnalysisOutput> {
        onStageChanged(AnalysisStage.CheckingSettings)
        val settings = settingsRepository.current()
        if (settings.apiKey.isBlank()) return AppResult.Failure("请先在设置中配置 API Key。")
        val modelName = normalizeModelName(settings.modelName)
        if (modelName.isBlank()) return AppResult.Failure("请先在设置中填写模型名称。")
        onPromptReady(buildPromptPreview(settings.userGoal))

        onStageChanged(AnalysisStage.PreparingImage)
        val compressed = try {
            imageCompressor.compress(originalImage)
        } catch (error: CancellationException) {
            throw error
        } catch (error: Exception) {
            return AppResult.Failure("图片处理失败，请重试。", error)
        }

        onStageChanged(AnalysisStage.RequestingAi)
        val config = LlmConfig(settings.baseUrl, modelName, settings.apiKey)
        val rawContent = try {
            client.analyzeMeal(config, settings.userGoal, compressed, onModelOutputChanged)
        } catch (error: ApiException) {
            return AppResult.Failure(error.message, error)
        } catch (error: CancellationException) {
            throw error
        } catch (error: Exception) {
            return AppResult.Failure("网络请求失败，请检查网络或 API 配置。", error)
        }

        onStageChanged(AnalysisStage.ParsingResult)
        val extractedJson = JsonUtils.extractJson(rawContent)
        val result = try {
            MealAnalysisPolisher.polish(JsonUtils.parseMealAnalysis(rawContent))
        } catch (error: CancellationException) {
            throw error
        } catch (error: Exception) {
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

    private fun buildPromptPreview(userGoal: String): String {
        val goal = userGoal
            .trim()
            .replace(Regex("\\s+"), " ")
            .ifBlank { "日常均衡饮食" }
            .take(46)
        return "图片餐食 + 目标「$goal」：识别主要菜品，判断是否适合，并返回短标签和可执行建议。"
    }
}

enum class AnalysisStage(val title: String, val detail: String) {
    CheckingSettings("准备分析", "第 1/4 步：确认 API Key、模型和饮食目标"),
    PreparingImage("处理餐食图片", "第 2/4 步：限制图片宽高并保持清晰度"),
    RequestingAi("AI 正在分析", "第 3/4 步：识别菜品并生成建议，这一步通常最久"),
    ParsingResult("整理分析结果", "第 4/4 步：提取标签、建议和展示内容"),
}

data class AnalysisOutput(
    val result: MealAnalysisResult,
    val aiResultJson: String,
    val originalImagePath: String,
    val compressedImagePath: String,
    val userGoalSnapshot: String,
)
