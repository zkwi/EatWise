package com.example.eatwise.domain.usecase

import com.example.eatwise.core.i18n.MealLanguageText
import com.example.eatwise.core.util.AppResult
import com.example.eatwise.domain.model.MealAnalysisResult
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.io.File

data class AnalysisTaskState(
    val imagePath: String = "",
    val isAnalyzing: Boolean = false,
    val analysisStage: AnalysisStage = AnalysisStage.CheckingSettings,
    val promptPreview: String = "",
    val modelOutput: String = "",
    val result: MealAnalysisResult? = null,
    val errorMessage: String? = null,
    val isSaving: Boolean = false,
    val savedRecordId: String? = null,
    val saveMessage: String? = null,
)

class AnalysisTaskManager(
    private val analyzeMealUseCase: AnalyzeMealUseCase,
    private val saveMealRecordUseCase: SaveMealRecordUseCase,
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val lock = Any()
    private val taskStates = mutableMapOf<String, MutableStateFlow<AnalysisTaskState>>()
    private val jobs = mutableMapOf<String, Job>()
    private val _latestTask = MutableStateFlow<AnalysisTaskState?>(null)
    val latestTask: StateFlow<AnalysisTaskState?> = _latestTask.asStateFlow()

    fun observe(imagePath: String): StateFlow<AnalysisTaskState> = stateFor(imagePath).asStateFlow()

    fun start(imagePath: String, restart: Boolean = false) {
        val state = stateFor(imagePath)
        synchronized(lock) {
            val current = state.value
            val currentJob = jobs[imagePath]
            if (!restart && (currentJob?.isActive == true || current.result != null || current.errorMessage != null)) {
                updateLatest(current)
                return
            }
            currentJob?.cancel()
            jobs[imagePath] = scope.launch { runAnalysis(imagePath, state) }
        }
    }

    private suspend fun runAnalysis(
        imagePath: String,
        state: MutableStateFlow<AnalysisTaskState>,
    ) {
        try {
            updateState(state) {
                it.copy(
                    imagePath = imagePath,
                    isAnalyzing = true,
                    analysisStage = AnalysisStage.CheckingSettings,
                    promptPreview = "",
                    modelOutput = "",
                    result = null,
                    errorMessage = null,
                    isSaving = false,
                    savedRecordId = null,
                    saveMessage = null,
                )
            }

            when (
                val result = analyzeMealUseCase(
                    originalImage = File(imagePath),
                    onStageChanged = { stage ->
                        updateState(state) { it.copy(analysisStage = stage) }
                    },
                    onPromptReady = { prompt ->
                        updateState(state) { it.copy(promptPreview = prompt) }
                    },
                    onModelOutputChanged = { output ->
                        updateState(state) { it.copy(modelOutput = output) }
                    },
                )
            ) {
                is AppResult.Success -> {
                    updateState(state) {
                        it.copy(isAnalyzing = false, result = result.value.result, errorMessage = null)
                    }
                    saveOutput(state, result.value)
                }
                is AppResult.Failure -> {
                    updateState(state) { it.copy(isAnalyzing = false, errorMessage = result.message) }
                }
            }

        } finally {
            val finishedJob = currentCoroutineContext()[Job]
            synchronized(lock) {
                if (jobs[imagePath] == finishedJob) jobs.remove(imagePath)
            }
        }
    }

    private suspend fun saveOutput(
        state: MutableStateFlow<AnalysisTaskState>,
        output: AnalysisOutput,
    ) {
        updateState(state) { it.copy(isSaving = true, saveMessage = MealLanguageText.savingRecord(output.language)) }
        try {
            val id = saveMealRecordUseCase(output)
            updateState(state) {
                it.copy(isSaving = false, savedRecordId = id, saveMessage = MealLanguageText.savedRecord(output.language))
            }
        } catch (error: CancellationException) {
            throw error
        } catch (_: Exception) {
            updateState(state) {
                it.copy(isSaving = false, saveMessage = MealLanguageText.saveFailed(output.language))
            }
        }
    }

    private fun stateFor(imagePath: String): MutableStateFlow<AnalysisTaskState> =
        synchronized(lock) {
            taskStates.getOrPut(imagePath) {
                MutableStateFlow(AnalysisTaskState(imagePath = imagePath))
            }
        }

    private fun updateState(
        state: MutableStateFlow<AnalysisTaskState>,
        block: (AnalysisTaskState) -> AnalysisTaskState,
    ) {
        state.update(block)
        updateLatest(state.value)
    }

    private fun updateLatest(state: AnalysisTaskState) {
        _latestTask.value = state
    }
}
