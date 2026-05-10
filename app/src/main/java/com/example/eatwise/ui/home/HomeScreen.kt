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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
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

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = 20.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp),
    ) {
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 34.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text("今天吃了什么？", fontSize = 34.sp, lineHeight = 38.sp, fontWeight = FontWeight.Bold)
                    Text("拍一张照片，让 AI 帮你记录和分析。", color = MaterialTheme.colorScheme.onSurfaceVariant)
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
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Text("最近记录", fontSize = 24.sp, fontWeight = FontWeight.Bold)
                Spacer(Modifier.weight(1f))
                Text("查看全部", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.SemiBold)
                IconButton(onClick = onOpenHistory) {
                    Icon(Icons.Rounded.ChevronRight, contentDescription = "查看全部", tint = MaterialTheme.colorScheme.primary)
                }
            }
        }

        if (state.recentRecords.isEmpty()) {
            item {
                EmptyCard("还没有记录，先拍一餐试试。")
            }
        } else {
            items(state.recentRecords.take(4)) { record ->
                RecentRecordCard(record, onClick = { onOpenDetail(record.id) })
            }
        }

        item { Spacer(Modifier.height(12.dp)) }
    }

    SnackbarHost(hostState = snackbarHostState)
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
            Column(verticalArrangement = Arrangement.spacedBy(14.dp), modifier = Modifier.fillMaxWidth()) {
                Text("开始记录这一餐", fontSize = 28.sp, fontWeight = FontWeight.Bold, color = GreenDeep)
                Button(
                    onClick = onOpenCamera,
                    modifier = Modifier.fillMaxWidth().height(58.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                ) {
                    Icon(Icons.Rounded.CameraAlt, contentDescription = null)
                    Spacer(Modifier.size(10.dp))
                    Text("拍照分析", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                }
                OutlinedButton(
                    onClick = onPickImage,
                    modifier = Modifier.fillMaxWidth().height(58.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.outlinedButtonColors(containerColor = Color.White),
                ) {
                    Icon(Icons.Rounded.Image, contentDescription = null)
                    Spacer(Modifier.size(10.dp))
                    Text("从相册选择", fontSize = 18.sp, fontWeight = FontWeight.Bold)
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
                Text(record.mealName, fontWeight = FontWeight.Bold, fontSize = 17.sp, maxLines = 1)
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
