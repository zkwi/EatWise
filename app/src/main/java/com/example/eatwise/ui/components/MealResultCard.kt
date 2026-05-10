package com.example.eatwise.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.eatwise.domain.model.MealAnalysisResult
import com.example.eatwise.ui.theme.GreenDeep
import com.example.eatwise.ui.theme.GreenPale
import com.example.eatwise.ui.theme.GreenSoft
import com.example.eatwise.ui.theme.OrangePrimary

@Composable
fun MealResultCard(result: MealAnalysisResult, modifier: Modifier = Modifier) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(14.dp)) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = GreenPale),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Brush.linearGradient(listOf(GreenPale, Color(0xFFFFFEF0))))
                    .padding(18.dp),
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(verticalAlignment = Alignment.Bottom) {
                        Text(
                            text = result.totalKcal?.let { "%.0f".format(it) } ?: "--",
                            fontSize = 46.sp,
                            lineHeight = 48.sp,
                            fontWeight = FontWeight.Bold,
                            color = GreenDeep,
                        )
                        Text(" kcal", modifier = Modifier.padding(start = 6.dp, bottom = 7.dp), color = GreenDeep)
                    }
                    Text(result.mealName, fontSize = 22.sp, fontWeight = FontWeight.Bold)
                    Text(result.summary, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                        MacroChip("蛋白质", result.macros.proteinG, Modifier.weight(1f))
                        MacroChip("碳水", result.macros.carbsG, Modifier.weight(1f))
                        MacroChip("脂肪", result.macros.fatG, Modifier.weight(1f))
                    }
                }
            }
        }

        SoftCard {
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Text("目标匹配", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                Spacer(Modifier.weight(1f))
                GoalBadge(result.goalMatch.level)
                Text(
                    result.goalMatch.score?.let { "  $it/10" } ?: "  --/10",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.SemiBold,
                )
            }
            LinearProgressIndicator(
                progress = { ((result.goalMatch.score ?: 0).coerceIn(0, 10)) / 10f },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(9.dp),
                color = if (result.goalMatch.level == "good") GreenDeep else OrangePrimary,
                trackColor = Color(0xFFEDEDED),
            )
            Text(result.goalMatch.reason.ifBlank { "AI 未给出明确判断。" }, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }

        if (result.suggestions.isNotEmpty()) {
            SoftCard {
                Text("建议", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                result.suggestions.take(3).forEach {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.Top) {
                        Icon(
                            Icons.Rounded.CheckCircle,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp).padding(top = 2.dp),
                        )
                        Text(it)
                    }
                }
            }
        }

        if (result.ingredients.isNotEmpty()) {
            SoftCard {
                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    Text("食材明细", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                    Spacer(Modifier.weight(1f))
                    Text(
                        result.totalKcal?.let { "总计 %.0f kcal".format(it) } ?: "总计 -- kcal",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                result.ingredients.forEach { ingredient ->
                    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .background(GreenSoft, CircleShape),
                        )
                        Text(ingredient.name.ifBlank { "食材" }, modifier = Modifier.padding(start = 10.dp))
                        Spacer(Modifier.weight(1f))
                        Text(ingredient.amount, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(
                            "  ${ingredient.kcal?.let { "%.0f kcal".format(it) } ?: "--"}",
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }
        }

        if (result.tags.isNotEmpty()) {
            SoftCard {
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("标签", fontWeight = FontWeight.Bold, modifier = Modifier.padding(end = 4.dp, top = 6.dp))
                    result.tags.forEach { TagChip(it) }
                }
            }
        }

        Text(
            result.disclaimer,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            style = MaterialTheme.typography.bodySmall,
        )
    }
}

@Composable
private fun SoftCard(content: @Composable ColumnScope.() -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
    ) {
        Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(10.dp), content = content)
    }
}

fun goalLabel(level: String?) = when (level) {
    "good" -> "适合"
    "partial" -> "部分适合"
    "poor" -> "不太适合"
    else -> "无法判断"
}
