package com.example.eatwise.ui.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Cloud
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.Key
import androidx.compose.material.icons.rounded.SettingsSuggest
import androidx.compose.material.icons.rounded.Visibility
import androidx.compose.material.icons.rounded.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.eatwise.domain.model.AppSettings
import com.example.eatwise.ui.theme.GreenDeep
import com.example.eatwise.ui.theme.GreenPale
import com.example.eatwise.ui.theme.GreenPrimary
import com.example.eatwise.ui.theme.GreenSoft
import com.example.eatwise.ui.theme.RedPrimary
import com.example.eatwise.ui.theme.RedSoft

@Composable
fun SettingsScreen(viewModel: SettingsViewModel) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    var showKey by remember { mutableStateOf(false) }
    val fieldColors = OutlinedTextFieldDefaults.colors(
        focusedBorderColor = GreenPrimary,
        unfocusedBorderColor = Color(0xFFE1E5EA),
        focusedContainerColor = Color(0xFFFCFDFC),
        unfocusedContainerColor = Color(0xFFFCFDFC),
        cursorColor = GreenPrimary,
    )

    LaunchedEffect(state.message) {
        state.message?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearMessage()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
    ) {
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
                        .padding(top = 36.dp, bottom = 2.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text("模型与目标", fontSize = 36.sp, lineHeight = 40.sp, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.weight(1f))
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .size(58.dp)
                            .background(Color.White, CircleShape),
                    ) {
                        Icon(Icons.Rounded.Info, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    }
                }
            }

            item {
                AiConfigHero(isConfigured = state.apiKey.isNotBlank() && state.modelName.isNotBlank())
            }

            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                ) {
                    Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                        SectionTitle(Icons.Rounded.Key, "模型连接")
                        OutlinedTextField(
                            value = state.baseUrl,
                            onValueChange = viewModel::updateBaseUrl,
                            label = { Text("Base URL") },
                            placeholder = { Text("https://openrouter.ai/api/v1") },
                            singleLine = true,
                            shape = RoundedCornerShape(14.dp),
                            colors = fieldColors,
                            modifier = Modifier.fillMaxWidth(),
                        )
                        OutlinedTextField(
                            value = state.apiKey,
                            onValueChange = viewModel::updateApiKey,
                            label = { Text("API Key") },
                            placeholder = { Text("只保存在本机") },
                            visualTransformation = if (showKey) VisualTransformation.None else PasswordVisualTransformation(),
                            trailingIcon = {
                                IconButton(onClick = { showKey = !showKey }) {
                                    Icon(
                                        if (showKey) Icons.Rounded.VisibilityOff else Icons.Rounded.Visibility,
                                        contentDescription = "显示或隐藏 API Key",
                                    )
                                }
                            },
                            singleLine = true,
                            shape = RoundedCornerShape(14.dp),
                            colors = fieldColors,
                            modifier = Modifier.fillMaxWidth(),
                        )
                        OutlinedTextField(
                            value = state.modelName,
                            onValueChange = viewModel::updateModelName,
                            label = { Text("模型名称") },
                            placeholder = { Text("例如 google/gemini-3.1-flash-lite") },
                            singleLine = true,
                            shape = RoundedCornerShape(14.dp),
                            colors = fieldColors,
                            modifier = Modifier.fillMaxWidth(),
                        )
                    }
                }
            }

            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                ) {
                    Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                        SectionTitle(Icons.Rounded.SettingsSuggest, "饮食目标")
                        Text("先选一个常见目标，再按自己的情况微调。", color = MaterialTheme.colorScheme.onSurfaceVariant)
                        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            goalPresets.forEach { preset ->
                                GoalPresetChip(
                                    preset = preset,
                                    selected = state.userGoal.trim() == preset.prompt,
                                    onClick = { viewModel.updateUserGoal(preset.prompt) },
                                )
                            }
                        }
                        OutlinedTextField(
                            value = state.userGoal,
                            onValueChange = viewModel::updateUserGoal,
                            label = { Text("我的饮食目标") },
                            minLines = 4,
                            shape = RoundedCornerShape(14.dp),
                            colors = fieldColors,
                            modifier = Modifier.fillMaxWidth(),
                        )
                        Text("热量估算默认以 kcal 展示。", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }

            item { Spacer(Modifier.height(112.dp)) }
        }

        Card(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .imePadding()
                .padding(horizontal = 20.dp, vertical = 12.dp),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
        ) {
            Row(
                Modifier.padding(12.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                Button(
                    onClick = viewModel::save,
                    enabled = !state.isSaving && !state.isTesting,
                    modifier = Modifier.weight(1f).height(56.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                ) {
                    Text(if (state.isSaving) "保存中..." else "保存设置", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                }
                OutlinedButton(
                    onClick = viewModel::testConnection,
                    enabled = !state.isSaving && !state.isTesting,
                    modifier = Modifier.weight(1f).height(56.dp),
                    shape = RoundedCornerShape(16.dp),
                ) {
                    Text(if (state.isTesting) "测试中..." else "测试连接", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        SnackbarHost(hostState = snackbarHostState, modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 104.dp))
    }
}

private val goalPresets = listOf(
    GoalPreset(
        title = "均衡饮食",
        detail = "日常均衡",
        prompt = AppSettings.DEFAULT_USER_GOAL,
    ),
    GoalPreset(
        title = "减脂控卡",
        detail = "控热量",
        prompt = "我正在减脂，希望控制总热量，优先选择高蛋白、少油、少糖、饱腹感强的食物，但不极端节食。",
    ),
    GoalPreset(
        title = "控糖稳糖",
        detail = "稳血糖",
        prompt = "我想控制血糖波动，尽量减少含糖饮料、甜品和精制碳水，优先选择蔬菜、优质蛋白和低负担主食。",
    ),
    GoalPreset(
        title = "控脂护心",
        detail = "控胆固醇",
        prompt = "我想控制血脂和胆固醇，少吃油炸、高脂肪和重油食物，优先选择清淡烹饪、鱼类、豆制品和蔬菜。",
    ),
    GoalPreset(
        title = "控盐清淡",
        detail = "少钠",
        prompt = "我想控制盐分摄入，尽量避免过咸、重口味、腌制和汤底过浓的食物，优先选择清淡搭配。",
    ),
    GoalPreset(
        title = "增肌高蛋白",
        detail = "补蛋白",
        prompt = "我想增肌或保持肌肉量，希望每餐有足够蛋白质，同时控制过多油脂和空热量。",
    ),
)

private data class GoalPreset(
    val title: String,
    val detail: String,
    val prompt: String,
)

@Composable
private fun GoalPresetChip(preset: GoalPreset, selected: Boolean, onClick: () -> Unit) {
    Card(
        onClick = onClick,
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = if (selected) GreenSoft else Color(0xFFF5F6F7)),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
    ) {
        Column(Modifier.padding(horizontal = 13.dp, vertical = 10.dp), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(
                preset.title,
                color = if (selected) GreenDeep else MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
            )
            Text(
                preset.detail,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 12.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

@Composable
private fun AiConfigHero(isConfigured: Boolean) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(26.dp),
        colors = CardDefaults.cardColors(containerColor = GreenPale),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    ) {
        Row(
            modifier = Modifier
                .background(Brush.linearGradient(listOf(GreenPale, Color(0xFFFFF8C8), GreenSoft)))
                .padding(22.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("连接 AI 模型", fontSize = 28.sp, fontWeight = FontWeight.Bold, color = GreenDeep)
                Text("填好 Key 和模型名称后，就可以拍照分析。", color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text(
                    if (isConfigured) "已就绪，可以分析" else "还缺 Key 或模型名称",
                    color = if (isConfigured) GreenDeep else RedPrimary,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier
                        .background(if (isConfigured) GreenSoft else RedSoft, RoundedCornerShape(50))
                        .padding(horizontal = 13.dp, vertical = 8.dp),
                )
            }
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(104.dp)
                    .background(Color.White.copy(alpha = 0.45f), CircleShape),
            ) {
                Icon(Icons.Rounded.Cloud, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(58.dp))
            }
        }
    }
}

@Composable
private fun SectionTitle(icon: androidx.compose.ui.graphics.vector.ImageVector, title: String) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
        Text(title, fontWeight = FontWeight.Bold, fontSize = 20.sp)
    }
}
