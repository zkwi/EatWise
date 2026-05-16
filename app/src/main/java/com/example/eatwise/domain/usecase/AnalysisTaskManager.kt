package com.example.eatwise.domain.usecase

import com.example.eatwise.core.i18n.MealLanguageText
import com.example.eatwise.core.util.AppResult
import com.example.eatwise.domain.model.MealAnalysisResult
import com.example.eatwise.domain.model.NutritionAnalysisResult
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
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.launch
import kotlinx.coroutines.supervisorScope
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
    val isNutritionAnalyzing: Boolean = false,
    val nutritionAnalysisStage: AnalysisStage = AnalysisStage.CheckingSettings,
    val nutritionPromptPreview: String = "",
    val nutritionModelOutput: String = "",
    val nutritionResult: NutritionAnalysisResult? = null,
    val nutritionErrorMessage: String? = null,
    val isSaving: Boolean = false,
    val savedRecordId: String? = null,
    val saveMessage: String? = null,
)

class AnalysisTaskManager(
    private val analyzeMealUseCase: AnalyzeMealUseCase,
    private val analyzeNutritionUseCase: AnalyzeNutritionUseCase,
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
                        current.isAnalyzing ||
                        current.isNutritionAnalyzing ||
                        current.result != null ||
                        current.nutritionResult != null ||
                        current.errorMessage != null ||
                        current.nutritionErrorMessage != null ||
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

    fun restartMeal(imagePath: String) {
        restartSingle(imagePath, runMeal = true, runNutrition = false)
    }

    fun restartNutrition(imagePath: String) {
        restartSingle(imagePath, runMeal = false, runNutrition = true)
    }

    private fun restartSingle(imagePath: String, runMeal: Boolean, runNutrition: Boolean) {
        if (imagePath.isBlank()) return
        synchronized(lock) {
            if (activeImagePath != null || jobs[imagePath]?.isActive == true) return
            startJobLocked(imagePath, runMeal = runMeal, runNutrition = runNutrition)
        }
    }

    private fun enqueueLocked(imagePath: String) {
        if (activeImagePath == null) {
            startJobLocked(imagePath, runMeal = true, runNutrition = true)
        } else if (!pendingQueue.contains(imagePath)) {
            pendingQueue.addLast(imagePath)
            refreshQueuePositionsLocked()
        }
    }

    private fun startJobLocked(imagePath: String, runMeal: Boolean, runNutrition: Boolean) {
        val state = stateForLocked(imagePath)
        activeImagePath = imagePath
        jobs[imagePath] = scope.launch { runAnalysis(imagePath, state, runMeal, runNutrition) }
        state.value = state.value.copy(isQueued = false, queuePosition = null)
        updateLatestLocked(state.value)
        emitTasksLocked()
    }

    private suspend fun runAnalysis(
        imagePath: String,
        state: MutableStateFlow<AnalysisTaskState>,
        runMeal: Boolean,
        runNutrition: Boolean,
    ) {
        try {
            updateState(state) {
                it.copy(
                    imagePath = imagePath,
                    isQueued = false,
                    queuePosition = null,
                    isAnalyzing = runMeal,
                    analysisStage = if (runMeal) AnalysisStage.CheckingSettings else it.analysisStage,
                    promptPreview = if (runMeal) "" else it.promptPreview,
                    modelOutput = if (runMeal) "" else it.modelOutput,
                    result = if (runMeal) null else it.result,
                    errorMessage = if (runMeal) null else it.errorMessage,
                    isNutritionAnalyzing = runNutrition,
                    nutritionAnalysisStage = if (runNutrition) AnalysisStage.CheckingSettings else it.nutritionAnalysisStage,
                    nutritionPromptPreview = if (runNutrition) "" else it.nutritionPromptPreview,
                    nutritionModelOutput = if (runNutrition) "" else it.nutritionModelOutput,
                    nutritionResult = if (runNutrition) null else it.nutritionResult,
                    nutritionErrorMessage = if (runNutrition) null else it.nutritionErrorMessage,
                    isSaving = if (runMeal) false else it.isSaving,
                    savedRecordId = if (runMeal) null else it.savedRecordId,
                    saveMessage = if (runMeal) null else it.saveMessage,
                )
            }

            var mealOutput: AnalysisOutput? = null
            var nutritionOutput: NutritionAnalysisOutput? = null
            supervisorScope {
                val mealJob = if (runMeal) launch {
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
                            mealOutput = result.value
                            updateState(state) {
                                it.copy(isAnalyzing = false, result = result.value.result, errorMessage = null)
                            }
                        }
                        is AppResult.Failure -> {
                            updateState(state) { it.copy(isAnalyzing = false, errorMessage = result.message) }
                        }
                    }
                } else null
                val nutritionJob = if (runNutrition) launch {
                    when (
                        val result = analyzeNutritionUseCase(
                            originalImage = File(imagePath),
                            onStageChanged = { stage ->
                                updateState(state) { it.copy(nutritionAnalysisStage = stage) }
                            },
                            onPromptReady = { prompt ->
                                updateState(state) { it.copy(nutritionPromptPreview = prompt) }
                            },
                            onModelOutputChanged = { output ->
                                updateState(state) { it.copy(nutritionModelOutput = output) }
                            },
                        )
                    ) {
                        is AppResult.Success -> {
                            nutritionOutput = result.value
                            updateState(state) {
                                it.copy(
                                    isNutritionAnalyzing = false,
                                    nutritionResult = result.value.result,
                                    nutritionErrorMessage = null,
                                )
                            }
                        }
                        is AppResult.Failure -> {
                            updateState(state) {
                                it.copy(isNutritionAnalyzing = false, nutritionErrorMessage = result.message)
                            }
                        }
                    }
                } else null
                listOfNotNull(mealJob, nutritionJob).joinAll()
            }

            val savedId = mealOutput?.let { saveOutput(state, it) } ?: state.value.savedRecordId
            if (savedId != null && nutritionOutput != null) {
                saveNutritionOutput(savedId, nutritionOutput)
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
    ): String? {
        updateState(state) { it.copy(isSaving = true, saveMessage = MealLanguageText.savingRecord(output.language)) }
        try {
            val id = saveMealRecordUseCase(output)
            updateState(state) {
                it.copy(isSaving = false, savedRecordId = id, saveMessage = MealLanguageText.savedRecord(output.language))
            }
            return id
        } catch (error: CancellationException) {
            throw error
        } catch (_: Exception) {
            updateState(state) {
                it.copy(isSaving = false, saveMessage = MealLanguageText.saveFailed(output.language))
            }
            return null
        }
    }

    private suspend fun saveNutritionOutput(
        recordId: String,
        output: NutritionAnalysisOutput,
    ) {
        try {
            saveMealRecordUseCase.saveNutrition(recordId, output)
        } catch (error: CancellationException) {
            throw error
        } catch (_: Exception) {
            // 营养卡片是补充信息，本地更新失败不影响主分析记录。
        }
    }

    private fun startNextQueuedLocked() {
        val next = if (pendingQueue.isNotEmpty()) pendingQueue.removeFirst() else null
        refreshQueuePositionsLocked()
        if (next != null) startJobLocked(next, runMeal = true, runNutrition = true)
    }

    private fun refreshQueuePositionsLocked() {
        pendingQueue.forEachIndexed { index, imagePath ->
            taskStates[imagePath]?.let { state ->
                state.value = state.value.copy(
                    isQueued = true,
                    queuePosition = index + 1,
                    isAnalyzing = false,
                    isNutritionAnalyzing = false,
                    errorMessage = null,
                    nutritionErrorMessage = null,
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
