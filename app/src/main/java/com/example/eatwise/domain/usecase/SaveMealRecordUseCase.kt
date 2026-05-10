package com.example.eatwise.domain.usecase

import com.example.eatwise.data.repository.MealRepository

class SaveMealRecordUseCase(
    private val mealRepository: MealRepository,
) {
    suspend operator fun invoke(output: AnalysisOutput): String =
        mealRepository.saveAnalysis(
            result = output.result,
            imagePath = output.originalImagePath,
            compressedPath = output.compressedImagePath,
            aiResultJson = output.aiResultJson,
            userGoalSnapshot = output.userGoalSnapshot,
        )
}
