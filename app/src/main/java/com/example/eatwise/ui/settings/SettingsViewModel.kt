package com.example.eatwise.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
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
    val message: String? = null,
)

class SettingsViewModel(
    private val settingsRepository: SettingsRepository,
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
}
