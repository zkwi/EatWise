package com.example.eatwise.ui.home

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.eatwise.core.i18n.MealLanguageText
import com.example.eatwise.core.storage.ImageStorage
import com.example.eatwise.data.repository.MealRepository
import com.example.eatwise.domain.model.MealRecord
import com.example.eatwise.domain.usecase.AnalysisTaskManager
import com.example.eatwise.domain.usecase.AnalysisTaskState
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class HomeUiState(
    val recentRecords: List<MealRecord> = emptyList(),
    val backgroundAnalyses: List<AnalysisTaskState> = emptyList(),
    val isLoading: Boolean = true,
    val errorMessage: String? = null,
)

class HomeViewModel(
    private val mealRepository: MealRepository,
    private val imageStorage: ImageStorage,
    private val analysisTaskManager: AnalysisTaskManager,
) : ViewModel() {
    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            mealRepository.observeRecent(5).collect { records ->
                _uiState.update { it.copy(recentRecords = records, isLoading = false) }
            }
        }
        viewModelScope.launch {
            analysisTaskManager.tasks.collect { tasks ->
                _uiState.update { it.copy(backgroundAnalyses = tasks.filter { task -> task.shouldShowOnHome() }) }
            }
        }
    }

    fun importImage(uri: Uri, errorMessage: String, onReady: (String) -> Unit) {
        importImages(listOf(uri), errorMessage, onReady)
    }

    fun importImages(uris: List<Uri>, errorMessage: String, onReady: (String) -> Unit) {
        if (uris.isEmpty()) return
        viewModelScope.launch {
            try {
                val imagePaths = uris.map { uri -> imageStorage.copyToPrivateStorage(uri).absolutePath }
                imagePaths.forEach(analysisTaskManager::start)
                onReady(imagePaths.first())
            } catch (error: CancellationException) {
                throw error
            } catch (error: Exception) {
                _uiState.update { it.copy(errorMessage = errorMessage) }
            }
        }
    }

    fun importSampleImage(resourceId: Int, name: String, errorMessage: String, onReady: (String) -> Unit) {
        viewModelScope.launch {
            try {
                onReady(imageStorage.copyResourceToPrivateStorage(resourceId, name).absolutePath)
            } catch (error: CancellationException) {
                throw error
            } catch (error: Exception) {
                _uiState.update { it.copy(errorMessage = errorMessage) }
            }
        }
    }

    fun retryAnalysis(imagePath: String) {
        analysisTaskManager.start(imagePath, restart = true)
    }

    fun clearError() {
        _uiState.update { it.copy(errorMessage = null) }
    }
}

internal fun AnalysisTaskState.shouldShowOnHome(): Boolean =
    isQueued ||
        isAnalyzing ||
        isNutritionAnalyzing ||
        isSaving ||
        errorMessage != null ||
        nutritionErrorMessage != null ||
        saveMessage?.let(MealLanguageText::isSaveFailure) == true
