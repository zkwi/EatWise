package com.example.eatwise.domain.model

import com.example.eatwise.core.i18n.AppLanguage
import com.example.eatwise.core.i18n.MealLanguageText

data class AppSettings(
    val baseUrl: String = DEFAULT_BASE_URL,
    val modelName: String = "",
    val apiKey: String = "",
    val userGoal: String = DEFAULT_USER_GOAL,
    val language: AppLanguage = AppLanguage.default,
) {
    companion object {
        const val DEFAULT_BASE_URL = "https://openrouter.ai/api/v1"
        val DEFAULT_USER_GOAL = MealLanguageText.defaultUserGoal(AppLanguage.default)
    }
}
