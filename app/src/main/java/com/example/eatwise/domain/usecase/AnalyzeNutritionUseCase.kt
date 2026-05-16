package com.example.eatwise.domain.usecase

import com.example.eatwise.core.i18n.AppLanguage
import com.example.eatwise.core.i18n.MealLanguageText
import com.example.eatwise.core.network.ApiException
import com.example.eatwise.core.network.LlmConfig
import com.example.eatwise.core.network.OpenAiCompatibleClient
import com.example.eatwise.core.util.AppResult
import com.example.eatwise.core.util.ImageCompressor
import com.example.eatwise.core.util.JsonUtils
import com.example.eatwise.core.util.NutritionAnalysisPolisher
import com.example.eatwise.data.repository.SettingsRepository
import com.example.eatwise.domain.model.NutritionAnalysisResult
import kotlinx.coroutines.CancellationException
import java.io.File

class AnalyzeNutritionUseCase(
    private val settingsRepository: SettingsRepository,
    private val client: OpenAiCompatibleClient,
    private val imageCompressor: ImageCompressor,
) {
    suspend operator fun invoke(
        originalImage: File,
        onStageChanged: (AnalysisStage) -> Unit = {},
        onPromptReady: (String) -> Unit = {},
        onModelOutputChanged: (String) -> Unit = {},
    ): AppResult<NutritionAnalysisOutput> {
        onStageChanged(AnalysisStage.CheckingSettings)
        val settings = settingsRepository.current()
        if (settings.apiKey.isBlank()) return AppResult.Failure(MealLanguageText.missingApiKey(settings.language))
        val modelName = normalizeModelName(settings.modelName)
        if (modelName.isBlank()) return AppResult.Failure(MealLanguageText.missingModel(settings.language))
        onPromptReady(MealLanguageText.nutritionPromptPreview(settings.userGoal, settings.language))

        onStageChanged(AnalysisStage.PreparingImage)
        val compressed = try {
            imageCompressor.compress(originalImage)
        } catch (error: CancellationException) {
            throw error
        } catch (error: Exception) {
            return AppResult.Failure(MealLanguageText.imageFailed(settings.language), error)
        }

        onStageChanged(AnalysisStage.RequestingAi)
        val config = LlmConfig(settings.baseUrl, modelName, settings.apiKey)
        val rawContent = try {
            client.analyzeNutrition(config, settings.userGoal, settings.language, compressed, onModelOutputChanged)
        } catch (error: ApiException) {
            return AppResult.Failure(error.message, error)
        } catch (error: CancellationException) {
            throw error
        } catch (error: Exception) {
            return AppResult.Failure(MealLanguageText.requestFailed(settings.language), error)
        }

        onStageChanged(AnalysisStage.ParsingResult)
        val extractedJson = JsonUtils.extractJson(rawContent)
        val result = try {
            NutritionAnalysisPolisher.polish(JsonUtils.parseNutritionAnalysis(rawContent), settings.language)
        } catch (error: CancellationException) {
            throw error
        } catch (error: Exception) {
            return AppResult.Failure(MealLanguageText.parseFailed(settings.language), error)
        }

        return AppResult.Success(
            NutritionAnalysisOutput(
                result = result,
                aiResultJson = extractedJson,
                originalImagePath = originalImage.absolutePath,
                compressedImagePath = compressed.absolutePath,
                userGoalSnapshot = settings.userGoal,
                language = settings.language,
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

data class NutritionAnalysisOutput(
    val result: NutritionAnalysisResult,
    val aiResultJson: String,
    val originalImagePath: String,
    val compressedImagePath: String,
    val userGoalSnapshot: String,
    val language: AppLanguage,
)
