package com.example.eatwise.ui.analysis

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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.History
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.example.eatwise.ui.components.AppTopBar
import com.example.eatwise.ui.components.ErrorCard
import com.example.eatwise.ui.components.LoadingOverlay
import com.example.eatwise.ui.components.MealResultCard
import java.io.File

@Composable
fun AnalysisScreen(
    viewModel: AnalysisViewModel,
    onBack: () -> Unit,
    onSaved: (String) -> Unit,
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(androidx.compose.material3.MaterialTheme.colorScheme.background),
    ) {
        AppTopBar("分析结果", onBack)
        LazyColumn(
            modifier = Modifier.padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            item {
                AsyncImage(
                    model = File(state.imagePath),
                    contentDescription = "餐食图片",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(1.65f)
                        .clip(RoundedCornerShape(22.dp)),
                )
            }
            if (state.isAnalyzing) {
                item { LoadingOverlay("AI 正在分析餐食...") }
            }
            state.errorMessage?.let { message ->
                item {
                    ErrorCard(message)
                    Spacer(Modifier.height(8.dp))
                    Button(onClick = viewModel::analyze, modifier = Modifier.fillMaxWidth()) {
                        Icon(Icons.Rounded.Refresh, contentDescription = null)
                        Text("重新分析")
                    }
                }
            }
            state.result?.let { result ->
                item { MealResultCard(result) }
                state.saveMessage?.let { message ->
                    item {
                        val saveFailed = message.contains("失败")
                        val saveDone = state.savedRecordId != null
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            Icon(
                                if (saveDone) Icons.Rounded.CheckCircle else Icons.Rounded.Refresh,
                                contentDescription = null,
                                tint = if (saveFailed) {
                                    androidx.compose.material3.MaterialTheme.colorScheme.error
                                } else {
                                    androidx.compose.material3.MaterialTheme.colorScheme.primary
                                },
                            )
                            Text(
                                message,
                                fontWeight = FontWeight.SemiBold,
                                color = if (saveFailed) {
                                    androidx.compose.material3.MaterialTheme.colorScheme.error
                                } else {
                                    androidx.compose.material3.MaterialTheme.colorScheme.onSurface
                                },
                            )
                        }
                    }
                }
                item {
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
                        OutlinedButton(
                            onClick = { state.savedRecordId?.let(onSaved) },
                            enabled = state.savedRecordId != null && !state.isSaving,
                            modifier = Modifier.weight(1f).height(52.dp),
                            shape = RoundedCornerShape(18.dp),
                        ) {
                            Icon(Icons.Rounded.History, contentDescription = null)
                            Text(if (state.isSaving) "保存中" else "查看记录", fontWeight = FontWeight.Bold)
                        }
                        Button(
                            onClick = viewModel::analyze,
                            enabled = !state.isAnalyzing,
                            modifier = Modifier.weight(1f).height(52.dp),
                            shape = RoundedCornerShape(18.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = androidx.compose.material3.MaterialTheme.colorScheme.primary),
                        ) {
                            Icon(Icons.Rounded.Refresh, contentDescription = null)
                            Text("重新分析", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
            item { Spacer(Modifier.height(18.dp)) }
        }
    }
}
