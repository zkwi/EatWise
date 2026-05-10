package com.example.eatwise.ui.home

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
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
import com.example.eatwise.ui.theme.GreenDeep
import com.example.eatwise.ui.theme.GreenPale
import com.example.eatwise.ui.theme.GreenSoft
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
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp),
        ) {
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 30.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text("今天这餐怎么样？", fontSize = 34.sp, lineHeight = 38.sp, fontWeight = FontWeight.Bold)
                        Text("拍照或导入图片，先记录，再看怎么调整。", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    IconButton(
                        onClick = onOpenSettings,
                        modifier = Modifier
                            .size(58.dp)
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

            item {
                SampleMealsSection(
                    samples = sampleMeals,
                    onSampleClick = { sample ->
                        viewModel.importSampleImage(sample.imageRes, sample.key, onAnalyze)
                    },
                )
            }

            item {
                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    Text("最近分析", fontSize = 24.sp, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.weight(1f))
                    TextButton(onClick = onOpenHistory) {
                        Text("查看全部", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.SemiBold)
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
        Text("没有照片时先试试", fontSize = 20.sp, fontWeight = FontWeight.Bold)
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
        modifier = Modifier.width(156.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    ) {
        Column {
            AsyncImage(
                model = sample.imageRes,
                contentDescription = sample.title,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(104.dp),
            )
            Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(sample.title, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text(
                    sample.label,
                    color = MaterialTheme.colorScheme.primary,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier
                        .background(GreenSoft, RoundedCornerShape(50))
                        .padding(horizontal = 9.dp, vertical = 4.dp),
                )
            }
        }
    }
}

@Composable
private fun StartMealCard(onOpenCamera: () -> Unit, onPickImage: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(26.dp),
        colors = CardDefaults.cardColors(containerColor = GreenPale),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    ) {
        Box(
            modifier = Modifier
                .background(Brush.linearGradient(listOf(Color(0xFFE2F8C9), Color(0xFFFFF7C8), GreenSoft)))
                .padding(22.dp),
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .size(106.dp)
                    .background(Color.White.copy(alpha = 0.42f), CircleShape),
            ) {
                Icon(
                    Icons.Rounded.RestaurantMenu,
                    contentDescription = null,
                    tint = GreenDeep.copy(alpha = 0.34f),
                    modifier = Modifier.size(54.dp),
                )
            }
            Column(verticalArrangement = Arrangement.spacedBy(14.dp), modifier = Modifier.fillMaxWidth()) {
                Text("记录一餐", fontSize = 28.sp, lineHeight = 32.sp, fontWeight = FontWeight.Bold, color = GreenDeep)
                Text("尽量拍清主食、配菜和饮料，结果会更稳定。", color = GreenDeep.copy(alpha = 0.72f))
                Button(
                    onClick = onOpenCamera,
                    modifier = Modifier.fillMaxWidth().height(58.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp),
                ) {
                    Icon(Icons.Rounded.CameraAlt, contentDescription = null)
                    Spacer(Modifier.size(10.dp))
                    Text("拍今天这一餐", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                }
                OutlinedButton(
                    onClick = onPickImage,
                    modifier = Modifier.fillMaxWidth().height(58.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.outlinedButtonColors(containerColor = Color.White),
                ) {
                    Icon(Icons.Rounded.Image, contentDescription = null)
                    Spacer(Modifier.size(10.dp))
                    Text("导入已有照片", fontSize = 18.sp, fontWeight = FontWeight.Bold)
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
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    ) {
        Row(Modifier.padding(10.dp), verticalAlignment = Alignment.CenterVertically) {
            AsyncImage(
                model = File(record.thumbnailPath ?: record.imagePath),
                contentDescription = record.mealName,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(width = 104.dp, height = 76.dp)
                    .aspectRatio(1.35f)
                    .clip(RoundedCornerShape(16.dp)),
            )
            Column(Modifier.weight(1f).padding(horizontal = 12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(record.mealName, fontWeight = FontWeight.Bold, fontSize = 17.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text("${record.totalKcal?.let { "%.0f".format(it) } ?: "--"} kcal", color = MaterialTheme.colorScheme.onSurfaceVariant)
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
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
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
