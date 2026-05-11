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
    val isQueued: Boolean = false,
    val queuePosition: Int? = null,
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
    private val pendingQueue = ArrayDeque<String>()
    private var activeImagePath: String? = null
    private val _tasks = MutableStateFlow<List<AnalysisTaskState>>(emptyList())
    private val _latestTask = MutableStateFlow<AnalysisTaskState?>(null)
    val tasks: StateFlow<List<AnalysisTaskState>> = _tasks.asStateFlow()
    val latestTask: StateFlow<AnalysisTaskState?> = _latestTask.asStateFlow()

    fun observe(imagePath: String): StateFlow<AnalysisTaskState> =
        synchronized(lock) { stateForLocked(imagePath).asStateFlow() }

    fun start(imagePath: String, restart: Boolean = false) {
        if (imagePath.isBlank()) return
        synchronized(lock) {
            val state = stateForLocked(imagePath)
            val current = state.value
            val currentJob = jobs[imagePath]
            if (
                !restart &&
                (
                    currentJob?.isActive == true ||
                        current.isQueued ||
                        current.result != null ||
                        current.errorMessage != null ||
                        current.saveMessage?.let(MealLanguageText::isSaveFailure) == true
                    )
            ) {
                updateLatestLocked(current)
                emitTasksLocked()
                return
            }

            if (restart) {
                pendingQueue.remove(imagePath)
                if (currentJob?.isActive == true) {
                    currentJob.cancel()
                }
                state.value = AnalysisTaskState(imagePath = imagePath)
            }

            enqueueLocked(imagePath)
        }
    }

    private fun enqueueLocked(imagePath: String) {
        if (activeImagePath == null) {
            startJobLocked(imagePath)
        } else if (!pendingQueue.contains(imagePath)) {
            pendingQueue.addLast(imagePath)
            refreshQueuePositionsLocked()
        }
    }

    private fun startJobLocked(imagePath: String) {
        val state = stateForLocked(imagePath)
        activeImagePath = imagePath
        jobs[imagePath] = scope.launch { runAnalysis(imagePath, state) }
        state.value = state.value.copy(isQueued = false, queuePosition = null)
        updateLatestLocked(state.value)
        emitTasksLocked()
    }

    private suspend fun runAnalysis(
        imagePath: String,
        state: MutableStateFlow<AnalysisTaskState>,
    ) {
        try {
            updateState(state) {
                it.copy(
                    imagePath = imagePath,
                    isQueued = false,
                    queuePosition = null,
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
                if (activeImagePath == imagePath) activeImagePath = null
                startNextQueuedLocked()
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

    private fun startNextQueuedLocked() {
        val next = if (pendingQueue.isNotEmpty()) pendingQueue.removeFirst() else null
        refreshQueuePositionsLocked()
        if (next != null) startJobLocked(next)
    }

    private fun refreshQueuePositionsLocked() {
        pendingQueue.forEachIndexed { index, imagePath ->
            taskStates[imagePath]?.let { state ->
                state.value = state.value.copy(
                    isQueued = true,
                    queuePosition = index + 1,
                    isAnalyzing = false,
                    errorMessage = null,
                    saveMessage = null,
                )
                updateLatestLocked(state.value)
            }
        }
        emitTasksLocked()
    }

    private fun stateForLocked(imagePath: String): MutableStateFlow<AnalysisTaskState> =
        taskStates.getOrPut(imagePath) {
            MutableStateFlow(AnalysisTaskState(imagePath = imagePath))
        }

    private fun updateState(
        state: MutableStateFlow<AnalysisTaskState>,
        block: (AnalysisTaskState) -> AnalysisTaskState,
    ) {
        synchronized(lock) {
            state.update(block)
            updateLatestLocked(state.value)
            emitTasksLocked()
        }
    }

    private fun updateLatestLocked(state: AnalysisTaskState) {
        _latestTask.value = state
    }

    private fun emitTasksLocked() {
        _tasks.value = taskStates.values.map { it.value }.filter { it.imagePath.isNotBlank() }
    }
}
