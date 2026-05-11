package com.example.eatwise.ui.home

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CameraAlt
import androidx.compose.material.icons.rounded.ChevronRight
import androidx.compose.material.icons.rounded.Image
import androidx.compose.material.icons.rounded.RestaurantMenu
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.example.eatwise.R
import com.example.eatwise.core.i18n.MealLanguageText
import com.example.eatwise.core.util.DateTimeUtils
import com.example.eatwise.domain.model.MealRecord
import com.example.eatwise.domain.usecase.AnalysisTaskState
import com.example.eatwise.ui.components.GoalBadge
import com.example.eatwise.ui.components.TagChip
import com.example.eatwise.ui.i18n.LocalAppLanguage
import com.example.eatwise.ui.i18n.LocalAppStrings
import com.example.eatwise.ui.theme.GreenDeep
import com.example.eatwise.ui.theme.GreenPale
import com.example.eatwise.ui.theme.GreenPrimary
import com.example.eatwise.ui.theme.GreenSoft
import com.example.eatwise.ui.theme.LineSoft
import java.io.File

@Composable
fun HomeScreen(
    viewModel: HomeViewModel,
    onOpenSettings: () -> Unit,
    onOpenHistory: () -> Unit,
    onOpenCamera: () -> Unit,
    onAnalyze: (String) -> Unit,
    onOpenDetail: (String) -> Unit,
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val strings = LocalAppStrings.current
    val snackbarHostState = remember { SnackbarHostState() }
    val photoPicker = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        if (uri != null) viewModel.importImage(uri, strings.imageReadFailed, onAnalyze)
    }

    LaunchedEffect(state.errorMessage) {
        state.errorMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearError()
        }
    }

    Box(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                        Text(
                            strings.homeTitle,
                            style = MaterialTheme.typography.displaySmall.copy(fontSize = 23.sp, lineHeight = 27.sp),
                        )
                        Text(
                            strings.homeSubtitle,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 13.sp,
                            lineHeight = 18.sp,
                        )
                    }
                    IconButton(
                        onClick = onOpenSettings,
                        modifier = Modifier
                            .size(40.dp)
                            .background(Color.White, CircleShape),
                    ) {
                        Icon(Icons.Rounded.Settings, contentDescription = strings.settings, tint = MaterialTheme.colorScheme.primary)
                    }
                }
            }

            item {
                StartMealCard(
                    onOpenCamera = onOpenCamera,
                    onPickImage = { photoPicker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)) },
                )
            }

            state.backgroundAnalysis?.takeIf { it.shouldShowOnHome() }?.let { task ->
                item {
                    BackgroundAnalysisCard(task, onClick = { onAnalyze(task.imagePath) })
                }
            }

            if (state.recentRecords.isEmpty()) {
                item {
                    SampleMealsSection(
                        samples = strings.sampleMeals.mapNotNull { meal ->
                            sampleImageRes[meal.key]?.let { imageRes ->
                                SampleMeal(meal.key, meal.title, meal.label, imageRes)
                            }
                        },
                        onSampleClick = { sample ->
                            viewModel.importSampleImage(sample.imageRes, sample.key, strings.sampleImageReadFailed, onAnalyze)
                        },
                    )
                }
            }

            item {
                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    Text(strings.recentAnalysis, fontWeight = FontWeight.ExtraBold, fontSize = 19.sp, lineHeight = 23.sp)
                    Spacer(Modifier.weight(1f))
                    TextButton(onClick = onOpenHistory) {
                        Text(strings.viewAll, color = GreenDeep, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        Icon(Icons.Rounded.ChevronRight, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    }
                }
            }

            if (state.recentRecords.isEmpty()) {
                item {
                    EmptyCard(strings.emptyHome)
                }
            } else {
                items(state.recentRecords.take(4)) { record ->
                    RecentRecordCard(record, onClick = { onOpenDetail(record.id) })
                }
            }

            item { Spacer(Modifier.height(12.dp)) }
        }

        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier.align(Alignment.BottomCenter).padding(horizontal = 20.dp, vertical = 12.dp),
        )
    }
}

private val sampleImageRes = mapOf(
    "spicy_shrimp" to R.drawable.sample_spicy_shrimp,
    "corn_dessert" to R.drawable.sample_corn_dessert,
    "dumpling_set" to R.drawable.sample_dumpling_set,
    "shared_feast" to R.drawable.sample_shared_feast,
    "burger" to R.drawable.sample_burger,
)

private data class SampleMeal(
    val key: String,
    val title: String,
    val label: String,
    val imageRes: Int,
)

@Composable
private fun SampleMealsSection(samples: List<SampleMeal>, onSampleClick: (SampleMeal) -> Unit) {
    val strings = LocalAppStrings.current
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(strings.sampleTitle, fontWeight = FontWeight.ExtraBold, fontSize = 19.sp, lineHeight = 23.sp)
        Text(strings.sampleSubtitle, color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 13.sp)
        LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            items(samples) { sample ->
                SampleMealCard(sample, onClick = { onSampleClick(sample) })
            }
        }
    }
}

@Composable
private fun SampleMealCard(sample: SampleMeal, onClick: () -> Unit) {
    Card(
        onClick = onClick,
        modifier = Modifier.width(150.dp),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, LineSoft.copy(alpha = 0.58f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
    ) {
        Column {
            AsyncImage(
                model = sample.imageRes,
                contentDescription = sample.title,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(96.dp),
            )
            Column(Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(5.dp)) {
                Text(sample.title, fontWeight = FontWeight.ExtraBold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                TagChip(sample.label)
            }
        }
    }
}

@Composable
private fun StartMealCard(onOpenCamera: () -> Unit, onPickImage: () -> Unit) {
    val strings = LocalAppStrings.current
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = GreenPale),
        border = BorderStroke(1.dp, Color(0xFFDDEBD8)),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    ) {
        Box(
            modifier = Modifier
                .background(Brush.linearGradient(listOf(Color(0xFFF9FDF7), Color(0xFFEEF9E7))))
                .padding(12.dp),
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .size(48.dp)
                    .background(Color.White.copy(alpha = 0.68f), CircleShape),
            ) {
                Icon(
                    Icons.Rounded.RestaurantMenu,
                    contentDescription = null,
                    tint = GreenPrimary.copy(alpha = 0.34f),
                    modifier = Modifier.size(26.dp),
                )
            }
            Column(verticalArrangement = Arrangement.spacedBy(7.dp), modifier = Modifier.fillMaxWidth()) {
                Text(strings.startMealTitle, fontSize = 20.sp, lineHeight = 24.sp, fontWeight = FontWeight.ExtraBold, color = GreenDeep)
                Text(
                    strings.startMealSubtitle,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 13.sp,
                    lineHeight = 18.sp,
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                    Button(
                        onClick = onOpenCamera,
                        modifier = Modifier.weight(1f).height(40.dp),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = GreenPrimary),
                        elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp),
                    ) {
                        Icon(Icons.Rounded.CameraAlt, contentDescription = null)
                        Spacer(Modifier.size(6.dp))
                        Text(strings.cameraAnalyze, fontSize = 14.sp, fontWeight = FontWeight.ExtraBold, maxLines = 1)
                    }
                    OutlinedButton(
                        onClick = onPickImage,
                        modifier = Modifier.weight(1f).height(40.dp),
                        shape = RoundedCornerShape(14.dp),
                        border = BorderStroke(1.5.dp, GreenPrimary),
                        colors = ButtonDefaults.outlinedButtonColors(containerColor = Color.White),
                    ) {
                        Icon(Icons.Rounded.Image, contentDescription = null, tint = GreenPrimary)
                        Spacer(Modifier.size(6.dp))
                        Text(strings.importPhoto, fontSize = 14.sp, fontWeight = FontWeight.ExtraBold, color = GreenPrimary, maxLines = 1)
                    }
                }
            }
        }
    }
}

@Composable
private fun BackgroundAnalysisCard(task: AnalysisTaskState, onClick: () -> Unit) {
    val strings = LocalAppStrings.current
    val language = LocalAppLanguage.current
    val saveFailed = task.saveMessage?.let(MealLanguageText::isSaveFailure) == true
    val hasError = task.errorMessage != null || saveFailed
    val title = when {
        task.isSaving -> strings.backgroundSavingTitle
        hasError -> strings.backgroundFailedTitle
        else -> strings.backgroundAnalyzingTitle
    }
    val detail = when {
        task.isSaving -> task.saveMessage ?: MealLanguageText.savingRecord(language)
        task.errorMessage != null -> task.errorMessage
        saveFailed -> task.saveMessage.orEmpty()
        else -> "${MealLanguageText.analysisStageTitle(task.analysisStage.ordinal, language)}：${MealLanguageText.analysisStageDetail(task.analysisStage.ordinal, language)}"
    }

    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, if (hasError) MaterialTheme.colorScheme.error.copy(alpha = 0.28f) else LineSoft.copy(alpha = 0.62f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            if (hasError) {
                Icon(Icons.Rounded.Refresh, contentDescription = null, tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(24.dp))
            } else {
                CircularProgressIndicator(modifier = Modifier.size(24.dp), strokeWidth = 2.5.dp, trackColor = GreenSoft)
            }
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                Text(title, fontWeight = FontWeight.ExtraBold, fontSize = 15.sp, lineHeight = 19.sp)
                Text(
                    detail,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 12.sp,
                    lineHeight = 16.sp,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            Icon(Icons.Rounded.ChevronRight, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
        }
    }
}

private fun AnalysisTaskState.shouldShowOnHome(): Boolean =
    isAnalyzing || isSaving || errorMessage != null || saveMessage?.let(MealLanguageText::isSaveFailure) == true

@Composable
private fun RecentRecordCard(record: MealRecord, onClick: () -> Unit) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, LineSoft.copy(alpha = 0.62f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
    ) {
        Row(Modifier.padding(9.dp), verticalAlignment = Alignment.CenterVertically) {
            AsyncImage(
                model = File(record.thumbnailPath ?: record.imagePath),
                contentDescription = record.mealName,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(width = 94.dp, height = 70.dp)
                    .aspectRatio(1.35f)
                    .clip(RoundedCornerShape(14.dp)),
            )
            Column(Modifier.weight(1f).padding(start = 9.dp), verticalArrangement = Arrangement.spacedBy(5.dp)) {
                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        record.mealName,
                        modifier = Modifier.weight(1f),
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 16.sp,
                        lineHeight = 20.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Text(
                        DateTimeUtils.formatListTime(record.createdAt),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 13.sp,
                        lineHeight = 17.sp,
                        maxLines = 1,
                    )
                }
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(3.dp),
                ) {
                    TagChip(record.eatingAdvice, compact = true)
                    GoalBadge(record.goalMatchLevel, compact = true)
                    record.tags.take(3).forEach { TagChip(it, compact = true) }
                }
            }
        }
    }
}

@Composable
private fun EmptyCard(text: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, LineSoft.copy(alpha = 0.62f)),
    ) {
        Row(Modifier.padding(22.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            IconBubble(Icons.Rounded.RestaurantMenu, GreenSoft, MaterialTheme.colorScheme.primary)
            Text(text, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun IconBubble(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    container: Color,
    content: Color,
) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .size(48.dp)
            .background(container, CircleShape),
    ) {
        Icon(icon, contentDescription = null, tint = content, modifier = Modifier.size(25.dp))
    }
}
