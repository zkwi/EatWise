package com.example.eatwise.data.repository

import com.example.eatwise.core.i18n.AppLanguage
import com.example.eatwise.core.storage.SettingsStore
import com.example.eatwise.domain.model.AppSettings
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first

class SettingsRepository(
    private val settingsStore: SettingsStore,
) {
    val settings: Flow<AppSettings> = settingsStore.settings

    suspend fun current(): AppSettings = settings.first()

    suspend fun save(settings: AppSettings) = settingsStore.save(settings)

    suspend fun saveUserGoal(userGoal: String) = settingsStore.saveUserGoal(userGoal)

    suspend fun saveLanguage(language: AppLanguage, userGoal: String? = null) =
        settingsStore.saveLanguage(language, userGoal)
}
