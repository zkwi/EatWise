package com.example.eatwise.domain.model

data class MealRecord(
    val id: String,
    val mealName: String,
    val summary: String,
    val imagePath: String,
    val thumbnailPath: String?,
    val eatingAdvice: String,
    val goalMatchLevel: String?,
    val goalMatchReason: String?,
    val tags: List<String>,
    val suggestions: List<String>,
    val ingredients: List<Ingredient>,
    val aiResultJson: String,
    val nutrition: NutritionAnalysisResult?,
    val nutritionAiResultJson: String?,
    val userGoalSnapshot: String,
    val note: String?,
    val isFavorite: Boolean,
    val createdAt: Long,
    val updatedAt: Long,
)
