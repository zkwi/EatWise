package com.example.eatwise.ui.detail

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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.FavoriteBorder
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.example.eatwise.core.util.DateTimeUtils
import com.example.eatwise.domain.model.GoalMatch
import com.example.eatwise.domain.model.MealAnalysisResult
import com.example.eatwise.ui.components.AppTopBar
import com.example.eatwise.ui.components.ErrorCard
import com.example.eatwise.ui.components.MealResultCard
import com.example.eatwise.ui.i18n.LocalAppStrings
import com.example.eatwise.ui.theme.GreenPrimary
import com.example.eatwise.ui.theme.LineSoft
import java.io.File

@Composable
fun MealDetailScreen(
    viewModel: MealDetailViewModel,
    onBack: () -> Unit,
    onDeleted: () -> Unit,
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val strings = LocalAppStrings.current
    var showDeleteDialog by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
    ) {
        AppTopBar(strings.detailTitle, onBack)
        val record = state.record
        if (state.recordMissing) {
            ErrorCard(strings.recordNotFound, Modifier.padding(20.dp))
        } else if (record != null) {
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
                            model = File(record.imagePath),
                            contentDescription = record.mealName,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .fillMaxWidth()
                                .aspectRatio(2.85f)
                                .clip(RoundedCornerShape(20.dp)),
                        )
                    }
                }
                item {
                    MealResultCard(
                        MealAnalysisResult(
                            mealName = record.mealName,
                            summary = record.summary,
                            eatingAdvice = record.eatingAdvice,
                            goalMatch = GoalMatch(
                                level = record.goalMatchLevel ?: "unknown",
                                reason = record.goalMatchReason.orEmpty(),
                            ),
                            ingredients = record.ingredients,
                            suggestions = record.suggestions,
                            tags = record.tags,
                        ),
                    )
                }
                item {
                    Card(
                        Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(18.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        border = BorderStroke(1.dp, LineSoft.copy(alpha = 0.62f)),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                    ) {
                        Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text(strings.goalDuringAnalysis, fontWeight = FontWeight.Bold)
                            Text(
                                record.userGoalSnapshot.ifBlank { strings.noGoalAtAnalysis },
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                            Text("${strings.recordTime}${DateTimeUtils.formatFull(record.createdAt)}", color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
                item {
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
                        Button(
                            onClick = viewModel::toggleFavorite,
                            modifier = Modifier.weight(1f).height(44.dp),
                            shape = RoundedCornerShape(16.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = GreenPrimary),
                        ) {
                            Icon(if (record.isFavorite) Icons.Rounded.Favorite else Icons.Rounded.FavoriteBorder, contentDescription = null)
                            Text(if (record.isFavorite) strings.unfavorite else strings.favoriteMeal)
                        }
                        Button(
                            onClick = { showDeleteDialog = true },
                            modifier = Modifier.weight(1f).height(44.dp),
                            shape = RoundedCornerShape(16.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                        ) {
                            Icon(Icons.Rounded.Delete, contentDescription = null)
                            Text(strings.deleteRecord)
                        }
                    }
                }
                item { Spacer(Modifier.height(28.dp)) }
            }
        }
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text(strings.deleteRecordTitle) },
            text = { Text(strings.deleteRecordText) },
            confirmButton = {
                Button(
                    onClick = {
                        showDeleteDialog = false
                        viewModel.delete(onDeleted)
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                ) {
                    Text(strings.delete)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text(strings.cancel)
                }
            },
        )
    }
}
