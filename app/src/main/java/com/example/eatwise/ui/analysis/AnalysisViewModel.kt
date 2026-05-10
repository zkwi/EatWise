package com.example.eatwise.ui.analysis

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.eatwise.core.util.AppResult
import com.example.eatwise.domain.model.MealAnalysisResult
import com.example.eatwise.domain.usecase.AnalysisOutput
import com.example.eatwise.domain.usecase.AnalyzeMealUseCase
import com.example.eatwise.domain.usecase.SaveMealRecordUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.io.File

data class AnalysisUiState(
    val imagePath: String = "",
    val isAnalyzing: Boolean = false,
    val result: MealAnalysisResult? = null,
    val errorMessage: String? = null,
    val isSaving: Boolean = false,
    val savedRecordId: String? = null,
)

class AnalysisViewModel(
    imagePath: String,
    private val analyzeMealUseCase: AnalyzeMealUseCase,
    private val saveMealRecordUseCase: SaveMealRecordUseCase,
) : ViewModel() {
    private var output: AnalysisOutput? = null
    private val _uiState = MutableStateFlow(AnalysisUiState(imagePath = imagePath))
    val uiState: StateFlow<AnalysisUiState> = _uiState.asStateFlow()

    init {
        analyze()
    }

    fun analyze() {
        val path = uiState.value.imagePath
        viewModelScope.launch {
            _uiState.update { it.copy(isAnalyzing = true, errorMessage = null, result = null, savedRecordId = null) }
            when (val result = analyzeMealUseCase(File(path))) {
                is AppResult.Success -> {
                    output = result.value
                    _uiState.update {
                        it.copy(isAnalyzing = false, result = result.value.result, errorMessage = null)
                    }
                }
                is AppResult.Failure -> {
                    _uiState.update { it.copy(isAnalyzing = false, errorMessage = result.message) }
                }
            }
        }
    }

    fun save() {
        val currentOutput = output ?: return
        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true) }
            runCatching { saveMealRecordUseCase(currentOutput) }
                .onSuccess { id ->
                    _uiState.update { it.copy(isSaving = false, savedRecordId = id) }
                }
                .onFailure {
                    _uiState.update { it.copy(isSaving = false, errorMessage = "保存失败，请重试。") }
                }
        }
    }
}
