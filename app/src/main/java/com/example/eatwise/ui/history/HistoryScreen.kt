package com.example.eatwise.ui.history

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.animation.core.animateFloatAsState
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
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.example.eatwise.core.util.DateTimeUtils
import com.example.eatwise.domain.model.MealRecord
import com.example.eatwise.ui.components.GoalBadge
import com.example.eatwise.ui.components.TagChip
import com.example.eatwise.ui.i18n.LocalAppStrings
import com.example.eatwise.ui.theme.GreenDeep
import com.example.eatwise.ui.theme.GreenSoft
import com.example.eatwise.ui.theme.LineSoft
import com.example.eatwise.ui.theme.RedPrimary
import java.io.File
import kotlin.math.roundToInt

@Composable
fun HistoryScreen(viewModel: HistoryViewModel, onOpenDetail: (String) -> Unit) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val strings = LocalAppStrings.current
    var pendingDelete by remember { mutableStateOf<MealRecord?>(null) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        item {
            Column(
                Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp, bottom = 2.dp),
                verticalArrangement = Arrangement.spacedBy(3.dp),
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        strings.historyTitle,
                        modifier = Modifier.weight(1f),
                        style = MaterialTheme.typography.displaySmall.copy(fontSize = 23.sp, lineHeight = 27.sp),
                    )
                }
                Text(
                    strings.historySubtitle,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 13.sp,
                    lineHeight = 18.sp,
                )
            }
        }
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FilterPill(strings.all, !state.favoriteFirst, onClick = {
                    if (state.favoriteFirst) viewModel.toggleFavoriteFirst()
                })
                FilterPill(strings.favoriteFirst, state.favoriteFirst, onClick = {
                    if (!state.favoriteFirst) viewModel.toggleFavoriteFirst()
                })
            }
        }
        if (state.records.isEmpty()) {
            item {
                Card(
                    Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    border = BorderStroke(1.dp, LineSoft.copy(alpha = 0.62f)),
                ) {
                    Text(
                        strings.emptyHistory,
                        modifier = Modifier.padding(24.dp),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        } else {
            items(state.records, key = { it.id }) { record ->
                HistoryRecordCard(
                    record = record,
                    onClick = { onOpenDetail(record.id) },
                    onFavorite = { viewModel.toggleFavorite(record) },
                    onDelete = { pendingDelete = record },
                )
            }
            item {
                Text(
                    strings.allRecordsShown,
                    modifier = Modifier.fillMaxWidth().padding(vertical = 10.dp),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 14.sp,
                    textAlign = TextAlign.Center,
                )
            }
        }
    }

    pendingDelete?.let { record ->
        AlertDialog(
            onDismissRequest = { pendingDelete = null },
            title = { Text(strings.deleteRecordTitle) },
            text = { Text(strings.deleteRecordText) },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.delete(record)
                        pendingDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                ) {
                    Text(strings.delete)
                }
            },
            dismissButton = {
                TextButton(onClick = { pendingDelete = null }) {
                    Text(strings.cancel)
                }
            },
        )
    }
}

@Composable
private fun FilterPill(text: String, selected: Boolean, onClick: () -> Unit) {
    Card(
        onClick = onClick,
        shape = RoundedCornerShape(50),
        colors = CardDefaults.cardColors(containerColor = if (selected) Color.White else Color(0xFFF0F2F0)),
        border = BorderStroke(1.dp, if (selected) GreenSoft else Color.Transparent),
        elevation = CardDefaults.cardElevation(defaultElevation = if (selected) 2.dp else 0.dp),
    ) {
        Text(
            text = text,
            color = if (selected) GreenDeep else MaterialTheme.colorScheme.onSurfaceVariant,
            fontWeight = FontWeight.Bold,
            fontSize = 13.sp,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 5.dp),
        )
    }
}

@Composable
private fun HistoryRecordCard(
    record: MealRecord,
    onClick: () -> Unit,
    onFavorite: () -> Unit,
    onDelete: () -> Unit,
) {
    val strings = LocalAppStrings.current
    val actionWidth = 104.dp
    val actionWidthPx = with(LocalDensity.current) { actionWidth.toPx() }
    var targetOffset by remember(record.id) { mutableFloatStateOf(0f) }
    val offsetX by animateFloatAsState(targetValue = targetOffset, label = "historyRecordOffset")

    Box(Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .fillMaxWidth()
                .height(94.dp)
                .clip(RoundedCornerShape(18.dp)),
            horizontalArrangement = Arrangement.End,
        ) {
            SwipeAction(
                label = if (record.isFavorite) strings.unfavorite else strings.favorite,
                icon = if (record.isFavorite) Icons.Rounded.Favorite else Icons.Rounded.FavoriteBorder,
                container = GreenDeep,
                onClick = {
                    onFavorite()
                    targetOffset = 0f
                },
            )
            SwipeAction(
                label = strings.delete,
                icon = Icons.Rounded.Delete,
                container = MaterialTheme.colorScheme.error,
                onClick = {
                    targetOffset = 0f
                    onDelete()
                },
            )
        }

        Card(
            onClick = onClick,
            modifier = Modifier
                .fillMaxWidth()
                .offset { IntOffset(offsetX.roundToInt(), 0) }
                .pointerInput(record.id) {
                    detectHorizontalDragGestures(
                        onHorizontalDrag = { _, dragAmount ->
                            targetOffset = (targetOffset + dragAmount).coerceIn(-actionWidthPx, 0f)
                        },
                        onDragEnd = {
                            targetOffset = if (targetOffset < -actionWidthPx * 0.45f) -actionWidthPx else 0f
                        },
                        onDragCancel = { targetOffset = 0f },
                    )
                },
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            border = BorderStroke(1.dp, LineSoft.copy(alpha = 0.62f)),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        ) {
            Box(Modifier.fillMaxWidth().padding(8.dp)) {
                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.Top) {
                    AsyncImage(
                        model = File(record.thumbnailPath ?: record.imagePath),
                        contentDescription = record.mealName,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .size(width = 78.dp, height = 78.dp)
                            .clip(RoundedCornerShape(13.dp)),
                    )
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .padding(start = 9.dp, top = 2.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp),
                    ) {
                        Text(
                            record.mealName,
                            modifier = Modifier.fillMaxWidth().padding(end = if (record.isFavorite) 76.dp else 50.dp),
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 15.sp,
                            lineHeight = 19.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                        FlowRow(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(5.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp),
                        ) {
                            TagChip(record.eatingAdvice, compact = true)
                            GoalBadge(record.goalMatchLevel, compact = true)
                            record.tags.take(3).forEach { TagChip(it, compact = true) }
                        }
                    }
                }
                Row(
                    modifier = Modifier.align(Alignment.TopEnd),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(3.dp),
                ) {
                    Text(
                        DateTimeUtils.formatListTime(record.createdAt),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 12.sp,
                        lineHeight = 16.sp,
                        maxLines = 1,
                    )
                    if (record.isFavorite) {
                        Icon(
                            Icons.Rounded.Favorite,
                            contentDescription = strings.favorited,
                            tint = RedPrimary,
                            modifier = Modifier.size(15.dp),
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SwipeAction(
    label: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    container: Color,
    onClick: () -> Unit,
) {
    Column(
        modifier = Modifier
            .width(52.dp)
            .fillMaxHeight()
            .background(container)
            .clickable(onClick = onClick),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Icon(icon, contentDescription = label, tint = Color.White, modifier = Modifier.size(18.dp))
        Text(label, color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
    }
}
