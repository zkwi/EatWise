package com.example.eatwise.core.storage

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
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
            userGoal = preferences[Keys.userGoal] ?: AppSettings.DEFAULT_USER_GOAL,
        )
    }

    suspend fun save(settings: AppSettings) {
        context.settingsDataStore.edit { preferences ->
            preferences[Keys.baseUrl] = settings.baseUrl.trim()
            preferences[Keys.modelName] = normalizeSingleLine(settings.modelName)
            preferences[Keys.apiKey] = settings.apiKey.trim()
            preferences[Keys.userGoal] = normalizedUserGoal(settings.userGoal)
        }
    }

    suspend fun saveUserGoal(userGoal: String) {
        context.settingsDataStore.edit { preferences ->
            preferences[Keys.userGoal] = normalizedUserGoal(userGoal)
        }
    }

    private fun normalizeSingleLine(value: String): String =
        value
            .lines()
            .map { it.trim() }
            .firstOrNull { it.isNotBlank() }
            .orEmpty()

    private fun normalizedUserGoal(value: String): String =
        value.trim().ifBlank { AppSettings.DEFAULT_USER_GOAL }

    private object Keys {
        val baseUrl = stringPreferencesKey("base_url")
        val modelName = stringPreferencesKey("model_name")
        val apiKey = stringPreferencesKey("api_key")
        val userGoal = stringPreferencesKey("user_goal")
    }
}
