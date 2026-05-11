package com.example.eatwise.domain.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class MealAnalysisResult(
    @SerialName("meal_name") val mealName: String = "未命名餐食",
    val summary: String = "",
    @SerialName("eating_advice") val eatingAdvice: String = "可以适量吃",
    @SerialName("goal_match") val goalMatch: GoalMatch = GoalMatch(),
    val ingredients: List<Ingredient> = emptyList(),
    val suggestions: List<String> = emptyList(),
    val tags: List<String> = emptyList(),
    val disclaimer: String = "以上仅基于图片做饮食参考，不替代专业建议。",
)

@Serializable
data class GoalMatch(
    val level: String = "unknown",
    val reason: String = "",
)

@Serializable
data class Ingredient(
    val dish: String = "",
    val name: String = "",
)
