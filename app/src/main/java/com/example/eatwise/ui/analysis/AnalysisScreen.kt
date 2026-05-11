package com.example.eatwise.ui.analysis

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.History
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.example.eatwise.core.i18n.MealLanguageText
import com.example.eatwise.ui.components.AppTopBar
import com.example.eatwise.ui.components.ErrorCard
import com.example.eatwise.ui.components.LoadingOverlay
import com.example.eatwise.ui.components.MealResultCard
import com.example.eatwise.ui.i18n.LocalAppLanguage
import com.example.eatwise.ui.i18n.LocalAppStrings
import com.example.eatwise.ui.theme.GreenPrimary
import com.example.eatwise.ui.theme.LineSoft
import java.io.File

@Composable
fun AnalysisScreen(
    viewModel: AnalysisViewModel,
    onBack: () -> Unit,
    onOpenSettings: () -> Unit,
    onSaved: (String) -> Unit,
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val strings = LocalAppStrings.current
    val language = LocalAppLanguage.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
    ) {
        AppTopBar(if (state.isAnalyzing || state.isQueued) strings.analyzing else strings.analysisResult, onBack)
        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    border = BorderStroke(1.dp, LineSoft.copy(alpha = 0.62f)),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                ) {
                    AsyncImage(
                        model = File(state.imagePath),
                        contentDescription = strings.imageToAnalyze,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .fillMaxWidth()
                            .aspectRatio(2.85f)
                            .clip(RoundedCornerShape(20.dp)),
                    )
                }
            }
            if (state.isQueued) {
                item {
                    LoadingOverlay(
                        text = strings.backgroundQueuedTitle,
                        detail = strings.backgroundQueuedDetail,
                        progress = 0f,
                        tips = emptyList(),
                    )
                }
            }
            if (state.isAnalyzing) {
                item {
                    LoadingOverlay(
                        text = MealLanguageText.analysisStepText(state.analysisStage.ordinal, language),
                        detail = MealLanguageText.analysisStageDetail(state.analysisStage.ordinal, language),
                        progress = (state.analysisStage.ordinal + 1) / 4f,
                        promptPreview = state.promptPreview,
                        modelOutput = state.modelOutput,
                    )
                }
            }
            state.errorMessage?.let { message ->
                item {
                    val showSettingsAction = analysisNeedsSettingsAction(message)
                    ErrorCard(message)
                    Spacer(Modifier.height(8.dp))
                    if (showSettingsAction) {
                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
                            OutlinedButton(
                                onClick = viewModel::analyze,
                                modifier = Modifier.weight(1f).height(44.dp),
                                shape = RoundedCornerShape(16.dp),
                            ) {
                                Icon(Icons.Rounded.Refresh, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(Modifier.width(6.dp))
                                Text(strings.retry, fontWeight = FontWeight.Bold)
                            }
                            Button(
                                onClick = onOpenSettings,
                                modifier = Modifier.weight(1f).height(44.dp),
                                shape = RoundedCornerShape(16.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = GreenPrimary),
                            ) {
                                Icon(Icons.Rounded.Settings, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(Modifier.width(6.dp))
                                Text(strings.settings, fontWeight = FontWeight.Bold)
                            }
                        }
                    } else {
                        Button(onClick = viewModel::analyze, modifier = Modifier.fillMaxWidth()) {
                            Icon(Icons.Rounded.Refresh, contentDescription = null)
                            Text(strings.retry)
                        }
                    }
                }
            }
            state.result?.let { result ->
                item { MealResultCard(result) }
                state.saveMessage?.let { message ->
                    item {
                        val saveFailed = MealLanguageText.isSaveFailure(message)
                        val saveDone = state.savedRecordId != null
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
                }
                item {
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
                        OutlinedButton(
                            onClick = { state.savedRecordId?.let(onSaved) },
                            enabled = state.savedRecordId != null && !state.isSaving,
                            modifier = Modifier.weight(1f).height(44.dp),
                            shape = RoundedCornerShape(16.dp),
                        ) {
                            Icon(Icons.Rounded.History, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(6.dp))
                            Text(
                                if (state.isSaving) strings.savingShort else strings.viewDetail,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                maxLines = 1,
                                overflow = TextOverflow.Clip,
                            )
                        }
                        Button(
                            onClick = viewModel::analyze,
                            enabled = !state.isAnalyzing,
                            modifier = Modifier.weight(1f).height(44.dp),
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
            }
            item { Spacer(Modifier.height(8.dp)) }
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
