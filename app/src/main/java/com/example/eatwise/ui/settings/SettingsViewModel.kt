package com.example.eatwise.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.eatwise.core.i18n.AppLanguage
import com.example.eatwise.core.i18n.MealLanguageText
import com.example.eatwise.core.network.ApiException
import com.example.eatwise.core.network.LlmConfig
import com.example.eatwise.core.network.OpenAiCompatibleClient
import com.example.eatwise.data.repository.SettingsRepository
import com.example.eatwise.domain.model.AppSettings
import com.example.eatwise.ui.i18n.AppStrings
import com.example.eatwise.ui.i18n.allPresetPrompts
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class SettingsUiState(
    val baseUrl: String = AppSettings.DEFAULT_BASE_URL,
    val modelName: String = "",
    val apiKey: String = "",
    val userGoal: String = AppSettings.DEFAULT_USER_GOAL,
    val language: AppLanguage = AppLanguage.default,
    val isSaving: Boolean = false,
    val isSavingGoal: Boolean = false,
    val isTesting: Boolean = false,
    val message: String? = null,
)

class SettingsViewModel(
    private val settingsRepository: SettingsRepository,
    private val openAiClient: OpenAiCompatibleClient,
) : ViewModel() {
    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()
    private var userGoalSaveJob: Job? = null
    private var keepLocalConfig = false
    private var keepLocalUserGoal = false

    init {
        viewModelScope.launch {
            settingsRepository.settings.collect { settings ->
                _uiState.update {
                    it.copy(
                        baseUrl = if (keepLocalConfig) it.baseUrl else settings.baseUrl,
                        modelName = if (keepLocalConfig) it.modelName else settings.modelName,
                        apiKey = if (keepLocalConfig) it.apiKey else settings.apiKey,
                        userGoal = if (keepLocalUserGoal) it.userGoal else settings.userGoal,
                        language = settings.language,
                    )
                }
            }
        }
    }

    fun updateBaseUrl(value: String) {
        keepLocalConfig = true
        _uiState.update { it.copy(baseUrl = value) }
    }

    fun updateModelName(value: String) {
        keepLocalConfig = true
        _uiState.update { it.copy(modelName = value) }
    }

    fun updateApiKey(value: String) {
        keepLocalConfig = true
        _uiState.update { it.copy(apiKey = value) }
    }

    fun updateUserGoal(value: String) {
        keepLocalUserGoal = true
        _uiState.update { it.copy(userGoal = value) }
        scheduleUserGoalSave(value, notify = false)
    }

    fun selectUserGoalPreset(value: String) {
        keepLocalUserGoal = true
        _uiState.update { it.copy(userGoal = value) }
        scheduleUserGoalSave(value, notify = true, delayMillis = 0)
    }

    fun selectLanguage(language: AppLanguage) {
        val current = uiState.value
        val shouldReplaceGoal = current.userGoal.trim() in allPresetPrompts()
        val nextGoal = if (shouldReplaceGoal) MealLanguageText.defaultUserGoal(language) else current.userGoal
        keepLocalUserGoal = false
        _uiState.update { it.copy(language = language, userGoal = nextGoal, message = null) }
        viewModelScope.launch {
            try {
                settingsRepository.saveLanguage(language, if (shouldReplaceGoal) nextGoal else null)
                _uiState.update { it.copy(message = AppStrings.of(language).settingsMessages.languageSaved) }
            } catch (error: CancellationException) {
                throw error
            } catch (_: Exception) {
                _uiState.update { it.copy(message = AppStrings.of(language).settingsMessages.saveFailed) }
            }
        }
    }

    fun testConnection() {
        val state = uiState.value
        val modelName = normalizeSingleLine(state.modelName)
        val messages = AppStrings.of(state.language).settingsMessages
        when {
            state.apiKey.isBlank() -> {
                _uiState.update { it.copy(message = messages.missingApiKey) }
                return
            }
            modelName.isBlank() -> {
                _uiState.update { it.copy(message = messages.missingModel) }
                return
            }
            state.baseUrl.isBlank() -> {
                _uiState.update { it.copy(message = messages.missingBaseUrl) }
                return
            }
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isTesting = true) }
            try {
                openAiClient.testConnection(
                    LlmConfig(
                        baseUrl = state.baseUrl.trim(),
                        modelName = modelName,
                        apiKey = state.apiKey.trim(),
                    ),
                    state.language,
                )
                _uiState.update { it.copy(isTesting = false, modelName = modelName, message = messages.testOk) }
            } catch (error: CancellationException) {
                throw error
            } catch (error: Exception) {
                _uiState.update {
                    it.copy(
                        isTesting = false,
                        message = (error as? ApiException)?.message ?: messages.testFailed,
                    )
                }
            }
        }
    }

    fun save() {
        userGoalSaveJob?.cancel()
        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true, isSavingGoal = false) }
            try {
                val state = uiState.value
                val messages = AppStrings.of(state.language).settingsMessages
                val userGoal = normalizedUserGoal(state.userGoal, state.language)
                settingsRepository.save(
                    AppSettings(
                        baseUrl = state.baseUrl.trim(),
                        modelName = normalizeSingleLine(state.modelName),
                        apiKey = state.apiKey.trim(),
                        userGoal = userGoal,
                        language = state.language,
                    ),
                )
                keepLocalConfig = false
                keepLocalUserGoal = false
                _uiState.update { it.copy(isSaving = false, isSavingGoal = false, userGoal = userGoal, message = messages.saveOk) }
            } catch (error: CancellationException) {
                throw error
            } catch (_: Exception) {
                val messages = AppStrings.of(uiState.value.language).settingsMessages
                _uiState.update { it.copy(isSaving = false, isSavingGoal = false, message = messages.saveFailed) }
            }
        }
    }

    fun clearMessage() = _uiState.update { it.copy(message = null) }

    private fun scheduleUserGoalSave(
        value: String,
        notify: Boolean,
        delayMillis: Long = 700,
    ) {
        userGoalSaveJob?.cancel()
        userGoalSaveJob = viewModelScope.launch {
            if (delayMillis > 0) delay(delayMillis)
            val language = uiState.value.language
            val messages = AppStrings.of(language).settingsMessages
            val userGoal = normalizedUserGoal(value, language)
            _uiState.update { it.copy(isSavingGoal = true) }
            try {
                settingsRepository.saveUserGoal(userGoal)
                val isCurrentGoal = normalizedUserGoal(uiState.value.userGoal, language) == userGoal
                if (isCurrentGoal) keepLocalUserGoal = false
                _uiState.update {
                    it.copy(
                        userGoal = if (isCurrentGoal) userGoal else it.userGoal,
                        isSavingGoal = false,
                        message = if (notify) messages.goalSaved else it.message,
                    )
                }
            } catch (error: CancellationException) {
                throw error
            } catch (_: Exception) {
                _uiState.update { it.copy(isSavingGoal = false, message = messages.goalSaveFailed) }
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
}
