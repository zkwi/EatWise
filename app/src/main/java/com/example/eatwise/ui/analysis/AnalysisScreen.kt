package com.example.eatwise.ui.analysis

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.eatwise.core.i18n.MealLanguageText
import com.example.eatwise.domain.usecase.AnalysisTaskState
import com.example.eatwise.ui.components.AppTopBar
import com.example.eatwise.ui.components.ErrorCard
import com.example.eatwise.ui.components.LoadingOverlay
import com.example.eatwise.ui.components.MealImageCard
import com.example.eatwise.ui.components.MealResultCard
import com.example.eatwise.ui.components.NutritionResultCard
import com.example.eatwise.ui.components.ResultTab
import com.example.eatwise.ui.components.ResultTabSwitcher
import com.example.eatwise.ui.components.SwipeableResultPane
import com.example.eatwise.ui.components.resolvedResultTab
import com.example.eatwise.ui.i18n.LocalAppLanguage
import com.example.eatwise.ui.i18n.LocalAppStrings
import com.example.eatwise.ui.theme.GreenPrimary

@Composable
fun AnalysisScreen(
    viewModel: AnalysisViewModel,
    onBack: () -> Unit,
    onOpenSettings: () -> Unit,
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val strings = LocalAppStrings.current
    val language = LocalAppLanguage.current
    val adviceAvailable = state.isAnalyzing || state.errorMessage != null || state.result != null
    val nutritionAvailable = state.isNutritionAnalyzing || state.nutritionErrorMessage != null || state.nutritionResult != null
    val hasResultActions = state.result != null ||
        state.nutritionResult != null ||
        state.errorMessage != null ||
        state.nutritionErrorMessage != null
    var selectedTab by rememberSaveable(state.imagePath) { mutableStateOf(ResultTab.Advice) }
    val activeTab = resolvedResultTab(selectedTab, adviceAvailable, nutritionAvailable)

    fun selectResultTab(tab: ResultTab) {
        val canSelect = (tab == ResultTab.Advice && adviceAvailable) ||
            (tab == ResultTab.Nutrition && nutritionAvailable)
        if (canSelect && tab != selectedTab) {
            selectedTab = tab
        }
    }

    LaunchedEffect(activeTab) {
        if (selectedTab != activeTab) selectedTab = activeTab
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
    ) {
        AppTopBar(if (state.isAnalyzing || state.isNutritionAnalyzing || state.isQueued) strings.analyzing else strings.analysisResult, onBack)
        if (!state.isQueued && (adviceAvailable || nutritionAvailable)) {
            ResultTabSwitcher(
                selectedTab = activeTab,
                onSelectedTab = ::selectResultTab,
                adviceLabel = strings.adviceCardTitle,
                nutritionLabel = strings.nutritionCardTitle,
                adviceEnabled = adviceAvailable,
                nutritionEnabled = nutritionAvailable,
                modifier = Modifier
                    .padding(horizontal = 16.dp)
                    .padding(bottom = 8.dp),
            )
        }
        when {
            state.isQueued -> QueuedAnalysisContent(state.imagePath, Modifier.weight(1f))
            adviceAvailable || nutritionAvailable -> {
                SwipeableResultPane(
                    selectedTab = activeTab,
                    adviceAvailable = adviceAvailable,
                    nutritionAvailable = nutritionAvailable,
                    onSelectedTab = ::selectResultTab,
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 16.dp),
                ) { tab ->
                    AnalysisResultPage(
                        state = state,
                        tab = tab,
                        hasResultActions = hasResultActions,
                        onBack = onBack,
                        onAnalyze = viewModel::analyze,
                        onRetryMealAdvice = viewModel::retryMealAdvice,
                        onRetryNutrition = viewModel::retryNutrition,
                        onOpenSettings = onOpenSettings,
                    )
                }
            }
            else -> AnalysisImageOnlyContent(state.imagePath, Modifier.weight(1f))
        }
    }
}

@Composable
private fun QueuedAnalysisContent(imagePath: String, modifier: Modifier = Modifier) {
    val strings = LocalAppStrings.current
    val listState = rememberLazyListState()
    LazyColumn(
        state = listState,
        modifier = modifier
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(bottom = 16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        item {
            MealImageCard(
                imagePath = imagePath,
                contentDescription = strings.imageToAnalyze,
            )
        }
        item {
            LoadingOverlay(
                text = strings.backgroundQueuedTitle,
                detail = strings.backgroundQueuedDetail,
                progress = 0f,
                tips = emptyList(),
            )
        }
        item { Spacer(Modifier.height(8.dp)) }
    }
}

@Composable
private fun AnalysisImageOnlyContent(imagePath: String, modifier: Modifier = Modifier) {
    val strings = LocalAppStrings.current
    val listState = rememberLazyListState()
    LazyColumn(
        state = listState,
        modifier = modifier
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(bottom = 16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        item {
            MealImageCard(
                imagePath = imagePath,
                contentDescription = strings.imageToAnalyze,
            )
        }
        item { Spacer(Modifier.height(8.dp)) }
    }
}

@Composable
private fun AnalysisResultPage(
    state: AnalysisTaskState,
    tab: ResultTab,
    hasResultActions: Boolean,
    onBack: () -> Unit,
    onAnalyze: () -> Unit,
    onRetryMealAdvice: () -> Unit,
    onRetryNutrition: () -> Unit,
    onOpenSettings: () -> Unit,
) {
    val strings = LocalAppStrings.current
    val listState = rememberLazyListState()
    LazyColumn(
        state = listState,
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        item {
            MealImageCard(
                imagePath = state.imagePath,
                contentDescription = strings.imageToAnalyze,
            )
        }
        item {
            when (tab) {
                ResultTab.Advice -> MealAdvicePane(
                    state = state,
                    onRetry = onRetryMealAdvice,
                    onOpenSettings = onOpenSettings,
                )
                ResultTab.Nutrition -> NutritionPane(
                    state = state,
                    onRetry = onRetryNutrition,
                    onOpenSettings = onOpenSettings,
                )
            }
        }
        if (hasResultActions) {
            item {
                AnalysisResultActions(
                    onBack = onBack,
                    onAnalyze = onAnalyze,
                    enabled = !state.isAnalyzing && !state.isNutritionAnalyzing,
                )
            }
        }
        item { Spacer(Modifier.height(8.dp)) }
    }
}

@Composable
private fun AnalysisResultActions(
    onBack: () -> Unit,
    onAnalyze: () -> Unit,
    enabled: Boolean,
) {
    val strings = LocalAppStrings.current
    Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
        OutlinedButton(
            onClick = onBack,
            modifier = Modifier.weight(1f).height(46.dp),
            shape = RoundedCornerShape(16.dp),
        ) {
            Icon(Icons.Rounded.Home, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(6.dp))
            Text(
                strings.home,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                maxLines = 1,
                overflow = TextOverflow.Clip,
            )
        }
        Button(
            onClick = onAnalyze,
            enabled = enabled,
            modifier = Modifier.weight(1f).height(46.dp),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(containerColor = GreenPrimary),
        ) {
            Icon(Icons.Rounded.Refresh, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(6.dp))
            Text(
                strings.reanalyze,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                maxLines = 1,
                overflow = TextOverflow.Clip,
            )
        }
    }
}

@Composable
private fun MealAdvicePane(
    state: AnalysisTaskState,
    onRetry: () -> Unit,
    onOpenSettings: () -> Unit,
) {
    val language = LocalAppLanguage.current
    when {
        state.isAnalyzing -> LoadingOverlay(
            text = MealLanguageText.analysisStepText(state.analysisStage.ordinal, language),
            detail = MealLanguageText.analysisStageDetail(state.analysisStage.ordinal, language),
            progress = (state.analysisStage.ordinal + 1) / 4f,
            promptPreview = state.promptPreview,
            modelOutput = state.modelOutput,
        )
        state.errorMessage != null -> AnalysisErrorActions(
            message = state.errorMessage,
            onRetry = onRetry,
            onOpenSettings = onOpenSettings,
        )
        state.result != null -> Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            MealResultCard(state.result)
            state.saveMessage?.let { SaveStatusRow(it, state.savedRecordId != null) }
        }
    }
}

@Composable
private fun NutritionPane(
    state: AnalysisTaskState,
    onRetry: () -> Unit,
    onOpenSettings: () -> Unit,
) {
    val strings = LocalAppStrings.current
    val language = LocalAppLanguage.current
    when {
        state.isNutritionAnalyzing -> LoadingOverlay(
            text = strings.nutritionLoadingTitle,
            detail = MealLanguageText.analysisStageDetail(state.nutritionAnalysisStage.ordinal, language),
            progress = (state.nutritionAnalysisStage.ordinal + 1) / 4f,
            promptPreview = state.nutritionPromptPreview,
            modelOutput = state.nutritionModelOutput,
        )
        state.nutritionErrorMessage != null -> AnalysisErrorActions(
            message = state.nutritionErrorMessage,
            onRetry = onRetry,
            onOpenSettings = onOpenSettings,
        )
        state.nutritionResult != null -> NutritionResultCard(state.nutritionResult)
    }
}

@Composable
private fun SaveStatusRow(message: String, saveDone: Boolean) {
    val saveFailed = MealLanguageText.isSaveFailure(message)
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            if (saveDone) Icons.Rounded.CheckCircle else Icons.Rounded.Refresh,
            contentDescription = null,
            tint = if (saveFailed) {
                MaterialTheme.colorScheme.error
            } else {
                MaterialTheme.colorScheme.primary
            },
            modifier = Modifier.size(22.dp),
        )
        Text(
            message,
            fontWeight = FontWeight.SemiBold,
            color = if (saveFailed) {
                MaterialTheme.colorScheme.error
            } else {
                MaterialTheme.colorScheme.onSurface
            },
            fontSize = 15.sp,
            lineHeight = 20.sp,
        )
    }
}

@Composable
private fun AnalysisErrorActions(
    message: String,
    onRetry: () -> Unit,
    onOpenSettings: () -> Unit,
) {
    val strings = LocalAppStrings.current
    val showSettingsAction = analysisNeedsSettingsAction(message)
    ErrorCard(message)
    Spacer(Modifier.height(8.dp))
    if (showSettingsAction) {
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
            OutlinedButton(
                onClick = onRetry,
                modifier = Modifier.weight(1f).height(46.dp),
                shape = RoundedCornerShape(16.dp),
            ) {
                Icon(Icons.Rounded.Refresh, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(6.dp))
                Text(strings.retry, fontWeight = FontWeight.Bold)
            }
            Button(
                onClick = onOpenSettings,
                modifier = Modifier.weight(1f).height(46.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = GreenPrimary),
            ) {
                Icon(Icons.Rounded.Settings, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(6.dp))
                Text(strings.settings, fontWeight = FontWeight.Bold)
            }
        }
    } else {
        Button(onClick = onRetry, modifier = Modifier.fillMaxWidth()) {
            Icon(Icons.Rounded.Refresh, contentDescription = null)
            Text(strings.retry)
        }
    }
}

internal fun analysisNeedsSettingsAction(message: String): Boolean {
    val clean = message.trim()
    return clean.contains("API Key", ignoreCase = true) ||
        clean.contains("model name", ignoreCase = true) ||
        clean.contains("image-capable model", ignoreCase = true) ||
        clean.contains("Base URL", ignoreCase = true) ||
        clean.contains("request parameters", ignoreCase = true) ||
        clean.contains("设置") ||
        clean.contains("設定") ||
        clean.contains("支持图片") ||
        clean.contains("支援圖片") ||
        clean.contains("模型名称") ||
        clean.contains("模型名稱") ||
        clean.contains("モデル名") ||
        clean.contains("画像対応モデル") ||
        clean.contains("リクエストパラメータ") ||
        clean.contains("接続先")
}
