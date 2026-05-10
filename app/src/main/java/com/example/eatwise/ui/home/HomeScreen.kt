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
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import com.example.eatwise.core.util.DateTimeUtils
import com.example.eatwise.domain.model.MealRecord
import com.example.eatwise.ui.components.GoalBadge
import com.example.eatwise.ui.components.TagChip
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
    val snackbarHostState = remember { SnackbarHostState() }
    val photoPicker = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        if (uri != null) viewModel.importImage(uri, onAnalyze)
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
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(
                            "今天这餐怎么样？",
                            style = MaterialTheme.typography.displaySmall.copy(fontSize = 28.sp, lineHeight = 34.sp),
                        )
                        Text(
                            "记录每一餐，了解你的饮食习惯。",
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            style = MaterialTheme.typography.bodyLarge,
                        )
                    }
                    IconButton(
                        onClick = onOpenSettings,
                        modifier = Modifier
                            .size(48.dp)
                            .background(Color.White, CircleShape),
                    ) {
                        Icon(Icons.Rounded.Settings, contentDescription = "设置", tint = MaterialTheme.colorScheme.primary)
                    }
                }
            }

            item {
                StartMealCard(
                    onOpenCamera = onOpenCamera,
                    onPickImage = { photoPicker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)) },
                )
            }

            if (state.recentRecords.isEmpty()) {
                item {
                    SampleMealsSection(
                        samples = sampleMeals,
                        onSampleClick = { sample ->
                            viewModel.importSampleImage(sample.imageRes, sample.key, onAnalyze)
                        },
                    )
                }
            }

            item {
                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    Text("最近分析", style = MaterialTheme.typography.titleLarge)
                    Spacer(Modifier.weight(1f))
                    TextButton(onClick = onOpenHistory) {
                        Text("查看全部", color = GreenDeep, fontWeight = FontWeight.Bold)
                        Icon(Icons.Rounded.ChevronRight, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    }
                }
            }

            if (state.recentRecords.isEmpty()) {
                item {
                    EmptyCard("还没有记录。分析成功后会自动保存到这里。")
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

private val sampleMeals = listOf(
    SampleMeal("spicy_shrimp", "辣味虾拼饭", "重口味", R.drawable.sample_spicy_shrimp),
    SampleMeal("corn_dessert", "玉米甜品冰", "甜品", R.drawable.sample_corn_dessert),
    SampleMeal("dumpling_set", "煎饺滑蛋套餐", "复合餐", R.drawable.sample_dumpling_set),
    SampleMeal("shared_feast", "多人聚餐", "多菜品", R.drawable.sample_shared_feast),
    SampleMeal("burger", "双层汉堡", "快餐", R.drawable.sample_burger),
)

private data class SampleMeal(
    val key: String,
    val title: String,
    val label: String,
    val imageRes: Int,
)

@Composable
private fun SampleMealsSection(samples: List<SampleMeal>, onSampleClick: (SampleMeal) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text("没有照片时先试试", style = MaterialTheme.typography.titleLarge)
        Text("用内置图片体验完整分析流程。", color = MaterialTheme.colorScheme.onSurfaceVariant)
        LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
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
        modifier = Modifier.width(164.dp),
        shape = RoundedCornerShape(22.dp),
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
                    .height(112.dp),
            )
            Column(Modifier.padding(13.dp), verticalArrangement = Arrangement.spacedBy(7.dp)) {
                Text(sample.title, fontWeight = FontWeight.ExtraBold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                TagChip(sample.label)
            }
        }
    }
}

@Composable
private fun StartMealCard(onOpenCamera: () -> Unit, onPickImage: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = GreenPale),
        border = BorderStroke(1.dp, Color(0xFFDDEBD8)),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    ) {
        Box(
            modifier = Modifier
                .background(Brush.linearGradient(listOf(Color(0xFFF9FDF7), Color(0xFFEEF9E7))))
                .padding(16.dp),
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .size(78.dp)
                    .background(Color.White.copy(alpha = 0.68f), CircleShape),
            ) {
                Icon(
                    Icons.Rounded.RestaurantMenu,
                    contentDescription = null,
                    tint = GreenPrimary.copy(alpha = 0.34f),
                    modifier = Modifier.size(40.dp),
                )
            }
            Column(verticalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                Text("记录一餐", style = MaterialTheme.typography.headlineSmall, color = GreenDeep)
                Text("尽量拍清主食、配菜和饮料，结果会更稳定。", color = MaterialTheme.colorScheme.onSurfaceVariant)
                Button(
                    onClick = onOpenCamera,
                    modifier = Modifier.fillMaxWidth().height(50.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = GreenPrimary),
                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp),
                ) {
                    Icon(Icons.Rounded.CameraAlt, contentDescription = null)
                    Spacer(Modifier.size(10.dp))
                    Text("拍今天这一餐", fontSize = 16.sp, fontWeight = FontWeight.ExtraBold)
                }
                OutlinedButton(
                    onClick = onPickImage,
                    modifier = Modifier.fillMaxWidth().height(50.dp),
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(1.5.dp, GreenPrimary),
                    colors = ButtonDefaults.outlinedButtonColors(containerColor = Color.White),
                ) {
                    Icon(Icons.Rounded.Image, contentDescription = null, tint = GreenPrimary)
                    Spacer(Modifier.size(10.dp))
                    Text("导入已有照片", fontSize = 16.sp, fontWeight = FontWeight.ExtraBold, color = GreenPrimary)
                }
            }
        }
    }
}

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
        Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            AsyncImage(
                model = File(record.thumbnailPath ?: record.imagePath),
                contentDescription = record.mealName,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(width = 112.dp, height = 86.dp)
                    .aspectRatio(1.35f)
                    .clip(RoundedCornerShape(18.dp)),
            )
            Column(Modifier.weight(1f).padding(horizontal = 12.dp), verticalArrangement = Arrangement.spacedBy(7.dp)) {
                Text(record.mealName, fontWeight = FontWeight.ExtraBold, fontSize = 18.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    TagChip(record.eatingAdvice)
                    GoalBadge(record.goalMatchLevel)
                }
            }
            Text(DateTimeUtils.formatShort(record.createdAt), color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 14.sp)
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
