package com.example.eatwise.ui.home

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.eatwise.core.storage.ImageStorage
import com.example.eatwise.data.repository.MealRepository
import com.example.eatwise.domain.model.MealRecord
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class HomeUiState(
    val recentRecords: List<MealRecord> = emptyList(),
    val isLoading: Boolean = true,
    val errorMessage: String? = null,
)

class HomeViewModel(
    private val mealRepository: MealRepository,
    private val imageStorage: ImageStorage,
) : ViewModel() {
    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            mealRepository.observeRecent(5).collect { records ->
                _uiState.update { it.copy(recentRecords = records, isLoading = false) }
            }
        }
    }

    fun importImage(uri: Uri, onReady: (String) -> Unit) {
        viewModelScope.launch {
            runCatching { imageStorage.copyToPrivateStorage(uri) }
                .onSuccess { onReady(it.absolutePath) }
                .onFailure { error ->
                    _uiState.update { it.copy(errorMessage = error.message ?: "图片读取失败，请换一张图片。") }
                }
        }
    }

    fun clearError() {
        _uiState.update { it.copy(errorMessage = null) }
    }
}
