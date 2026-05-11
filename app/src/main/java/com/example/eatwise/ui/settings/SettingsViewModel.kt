package com.example.eatwise.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.eatwise.core.network.ApiException
import com.example.eatwise.core.network.LlmConfig
import com.example.eatwise.core.network.OpenAiCompatibleClient
import com.example.eatwise.data.repository.SettingsRepository
import com.example.eatwise.domain.model.AppSettings
import kotlinx.coroutines.CancellationException
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
    val isSaving: Boolean = false,
    val isTesting: Boolean = false,
    val message: String? = null,
)

class SettingsViewModel(
    private val settingsRepository: SettingsRepository,
    private val openAiClient: OpenAiCompatibleClient,
) : ViewModel() {
    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            settingsRepository.settings.collect { settings ->
                _uiState.update {
                    it.copy(
                        baseUrl = settings.baseUrl,
                        modelName = settings.modelName,
                        apiKey = settings.apiKey,
                        userGoal = settings.userGoal,
                    )
                }
            }
        }
    }

    fun updateBaseUrl(value: String) = _uiState.update { it.copy(baseUrl = value) }
    fun updateModelName(value: String) = _uiState.update { it.copy(modelName = value) }
    fun updateApiKey(value: String) = _uiState.update { it.copy(apiKey = value) }
    fun updateUserGoal(value: String) = _uiState.update { it.copy(userGoal = value) }

    fun testConnection() {
        val state = uiState.value
        val modelName = normalizeSingleLine(state.modelName)
        when {
            state.apiKey.isBlank() -> {
                _uiState.update { it.copy(message = "请先填写 API Key。") }
                return
            }
            modelName.isBlank() -> {
                _uiState.update { it.copy(message = "请先填写模型名称。") }
                return
            }
            state.baseUrl.isBlank() -> {
                _uiState.update { it.copy(message = "请先填写 API Base URL。") }
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
                )
                _uiState.update { it.copy(isTesting = false, modelName = modelName, message = "连接正常，模型支持图片输入。") }
            } catch (error: CancellationException) {
                throw error
            } catch (error: Exception) {
                _uiState.update {
                    it.copy(
                        isTesting = false,
                        message = (error as? ApiException)?.message ?: "连接失败，请检查网络或 API 配置。",
                    )
                }
            }
        }
    }

    fun save() {
        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true) }
            try {
                val state = uiState.value
                settingsRepository.save(
                    AppSettings(
                        baseUrl = state.baseUrl.trim(),
                        modelName = normalizeSingleLine(state.modelName),
                        apiKey = state.apiKey.trim(),
                        userGoal = state.userGoal.trim().ifBlank { AppSettings.DEFAULT_USER_GOAL },
                    ),
                )
                _uiState.update { it.copy(isSaving = false, message = "设置已保存。") }
            } catch (error: CancellationException) {
                throw error
            } catch (_: Exception) {
                _uiState.update { it.copy(isSaving = false, message = "保存失败，请重试。") }
            }
        }
    }

    fun clearMessage() = _uiState.update { it.copy(message = null) }

    private fun normalizeSingleLine(value: String): String =
        value
            .lines()
            .map { it.trim() }
            .firstOrNull { it.isNotBlank() }
            .orEmpty()
}
