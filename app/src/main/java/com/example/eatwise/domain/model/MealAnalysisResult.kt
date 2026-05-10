package com.example.eatwise.domain.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class MealAnalysisResult(
    @SerialName("meal_name") val mealName: String = "未命名餐食",
    val summary: String = "",
    @SerialName("total_kcal") val totalKcal: Double? = null,
    val confidence: Double? = null,
    val macros: Macros = Macros(),
    @SerialName("goal_match") val goalMatch: GoalMatch = GoalMatch(),
    val ingredients: List<Ingredient> = emptyList(),
    val suggestions: List<String> = emptyList(),
    val tags: List<String> = emptyList(),
    val disclaimer: String = "以上是基于图片的粗略估算，仅供饮食记录参考。",
)

@Serializable
data class Macros(
    @SerialName("protein_g") val proteinG: Double? = null,
    @SerialName("carbs_g") val carbsG: Double? = null,
    @SerialName("fat_g") val fatG: Double? = null,
)

@Serializable
data class GoalMatch(
    val level: String = "unknown",
    val score: Int? = null,
    val reason: String = "",
)

@Serializable
data class Ingredient(
    val dish: String = "",
    val name: String = "",
    val amount: String = "",
    val kcal: Double? = null,
)
