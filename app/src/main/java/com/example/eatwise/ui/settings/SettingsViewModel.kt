package com.example.eatwise.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.eatwise.core.network.ApiException
import com.example.eatwise.core.network.LlmConfig
import com.example.eatwise.core.network.OpenAiCompatibleClient
import com.example.eatwise.data.repository.SettingsRepository
import com.example.eatwise.domain.model.AppSettings
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class SettingsUiState(
    val baseUrl: String = "https://openrouter.ai/api/v1",
    val modelName: String = "",
    val apiKey: String = "",
    val userGoal: String = "我想保持饮食均衡，尽量吃得健康一些。",
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
            runCatching {
                openAiClient.testConnection(
                    LlmConfig(
                        baseUrl = state.baseUrl.trim(),
                        modelName = modelName,
                        apiKey = state.apiKey.trim(),
                    ),
                )
            }.onSuccess {
                _uiState.update { it.copy(isTesting = false, modelName = modelName, message = "模型连接正常。") }
            }.onFailure { error ->
                _uiState.update {
                    it.copy(
                        isTesting = false,
                        message = (error as? ApiException)?.message ?: "测试连接失败，请检查网络或 API 配置。",
                    )
                }
            }
        }
    }

    fun save() {
        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true) }
            runCatching {
                settingsRepository.save(
                    AppSettings(
                        baseUrl = uiState.value.baseUrl,
                        modelName = uiState.value.modelName,
                        apiKey = uiState.value.apiKey,
                        userGoal = uiState.value.userGoal,
                    ),
                )
            }.onSuccess {
                _uiState.update { it.copy(isSaving = false, message = "配置已保存") }
            }.onFailure {
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
