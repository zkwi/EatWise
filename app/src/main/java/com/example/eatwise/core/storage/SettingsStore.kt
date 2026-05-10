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
            baseUrl = preferences[Keys.baseUrl] ?: "https://openrouter.ai/api/v1",
            modelName = preferences[Keys.modelName] ?: "",
            apiKey = preferences[Keys.apiKey] ?: "",
            userGoal = preferences[Keys.userGoal] ?: "我想保持饮食均衡，尽量吃得健康一些。",
        )
    }

    suspend fun save(settings: AppSettings) {
        context.settingsDataStore.edit { preferences ->
            preferences[Keys.baseUrl] = settings.baseUrl.trim()
            preferences[Keys.modelName] = settings.modelName.trim()
            preferences[Keys.apiKey] = settings.apiKey.trim()
            preferences[Keys.userGoal] = settings.userGoal.trim()
        }
    }

    private object Keys {
        val baseUrl = stringPreferencesKey("base_url")
        val modelName = stringPreferencesKey("model_name")
        val apiKey = stringPreferencesKey("api_key")
        val userGoal = stringPreferencesKey("user_goal")
    }
}
