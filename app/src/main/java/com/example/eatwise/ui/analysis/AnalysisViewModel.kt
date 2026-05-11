package com.example.eatwise.ui.analysis

import androidx.lifecycle.ViewModel
import com.example.eatwise.domain.usecase.AnalysisTaskManager
import com.example.eatwise.domain.usecase.AnalysisTaskState
import kotlinx.coroutines.flow.StateFlow

class AnalysisViewModel(
    imagePath: String,
    private val analysisTaskManager: AnalysisTaskManager,
) : ViewModel() {
    val uiState: StateFlow<AnalysisTaskState> = analysisTaskManager.observe(imagePath)

    init {
        analysisTaskManager.start(imagePath)
    }

    fun analyze() {
        analysisTaskManager.start(uiState.value.imagePath, restart = true)
    }
}
