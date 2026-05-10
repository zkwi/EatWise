package com.example.eatwise.ui.detail

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
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.FavoriteBorder
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import com.example.eatwise.core.util.DateTimeUtils
import com.example.eatwise.domain.model.GoalMatch
import com.example.eatwise.domain.model.Macros
import com.example.eatwise.domain.model.MealAnalysisResult
import com.example.eatwise.ui.components.AppTopBar
import com.example.eatwise.ui.components.ErrorCard
import com.example.eatwise.ui.components.MealResultCard
import java.io.File

@Composable
fun MealDetailScreen(
    viewModel: MealDetailViewModel,
    onBack: () -> Unit,
    onDeleted: () -> Unit,
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
    ) {
        AppTopBar("记录详情", onBack)
        val record = state.record
        val errorMessage = state.errorMessage
        if (errorMessage != null) {
            ErrorCard(errorMessage, Modifier.padding(20.dp))
        } else if (record != null) {
            LazyColumn(
                modifier = Modifier.padding(horizontal = 20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                item {
                    AsyncImage(
                        model = File(record.imagePath),
                        contentDescription = record.mealName,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .fillMaxWidth()
                            .aspectRatio(1.6f)
                            .clip(RoundedCornerShape(20.dp)),
                    )
                }
                item {
                    MealResultCard(
                        MealAnalysisResult(
                            mealName = record.mealName,
                            summary = record.summary,
                            totalKcal = record.totalKcal,
                            macros = Macros(record.proteinG, record.carbsG, record.fatG),
                            goalMatch = GoalMatch(record.goalMatchLevel ?: "unknown", record.goalMatchScore, record.goalMatchReason.orEmpty()),
                            ingredients = record.ingredients,
                            suggestions = record.suggestions,
                            tags = record.tags,
                        ),
                    )
                }
                item {
                    Card(Modifier.fillMaxWidth()) {
                        Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text("目标快照", fontWeight = FontWeight.Bold)
                            Text(record.userGoalSnapshot)
                            Text("记录时间：${DateTimeUtils.formatFull(record.createdAt)}", color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
                item {
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
                        Button(onClick = viewModel::toggleFavorite, modifier = Modifier.weight(1f).height(52.dp)) {
                            Icon(if (record.isFavorite) Icons.Rounded.Favorite else Icons.Rounded.FavoriteBorder, contentDescription = null)
                            Text(if (record.isFavorite) "取消收藏" else "收藏")
                        }
                        Button(onClick = { viewModel.delete(onDeleted) }, modifier = Modifier.weight(1f).height(52.dp)) {
                            Icon(Icons.Rounded.Delete, contentDescription = null)
                            Text("删除")
                        }
                    }
                }
                item { Spacer(Modifier.height(18.dp)) }
            }
        }
    }
}
