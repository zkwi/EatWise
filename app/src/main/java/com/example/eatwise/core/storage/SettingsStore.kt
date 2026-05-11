package com.example.eatwise.core.storage

import android.content.Context
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import com.example.eatwise.core.i18n.AppLanguage
import com.example.eatwise.core.i18n.MealLanguageText
import com.example.eatwise.domain.model.AppSettings
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.settingsDataStore by preferencesDataStore("eatwise_settings")

class SettingsStore(
    private val context: Context,
) {
    val settings: Flow<AppSettings> = context.settingsDataStore.data.map { preferences ->
        AppSettings(
            baseUrl = preferences[Keys.baseUrl] ?: AppSettings.DEFAULT_BASE_URL,
            modelName = normalizeSingleLine(preferences[Keys.modelName] ?: ""),
            apiKey = preferences[Keys.apiKey] ?: "",
            language = AppLanguage.fromCode(preferences[Keys.language]),
            userGoal = preferences[Keys.userGoal] ?: MealLanguageText.defaultUserGoal(AppLanguage.fromCode(preferences[Keys.language])),
        )
    }

    suspend fun save(settings: AppSettings) {
        context.settingsDataStore.edit { preferences ->
            preferences[Keys.baseUrl] = settings.baseUrl.trim()
            preferences[Keys.modelName] = normalizeSingleLine(settings.modelName)
            preferences[Keys.apiKey] = settings.apiKey.trim()
            preferences[Keys.userGoal] = normalizedUserGoal(settings.userGoal, settings.language)
            preferences[Keys.language] = settings.language.code
        }
    }

    suspend fun saveUserGoal(userGoal: String) {
        context.settingsDataStore.edit { preferences ->
            preferences[Keys.userGoal] = normalizedUserGoal(userGoal, AppLanguage.fromCode(preferences[Keys.language]))
        }
    }

    suspend fun saveLanguage(language: AppLanguage, userGoal: String? = null) {
        context.settingsDataStore.edit { preferences ->
            preferences[Keys.language] = language.code
            if (userGoal != null) {
                preferences[Keys.userGoal] = normalizedUserGoal(userGoal, language)
            }
        }
    }

    private fun normalizeSingleLine(value: String): String =
        value
            .lines()
            .map { it.trim() }
            .firstOrNull { it.isNotBlank() }
            .orEmpty()

    private fun normalizedUserGoal(value: String, language: AppLanguage): String =
        value.trim().ifBlank { MealLanguageText.defaultUserGoal(language) }

    private object Keys {
        val baseUrl = stringPreferencesKey("base_url")
        val modelName = stringPreferencesKey("model_name")
        val apiKey = stringPreferencesKey("api_key")
        val userGoal = stringPreferencesKey("user_goal")
        val language = stringPreferencesKey("language")
    }
}
