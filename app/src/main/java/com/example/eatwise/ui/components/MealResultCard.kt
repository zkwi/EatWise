package com.example.eatwise.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.eatwise.domain.model.MealAnalysisResult
import com.example.eatwise.ui.theme.GreenSoft
import com.example.eatwise.ui.theme.OrangePrimary

@Composable
fun MealResultCard(result: MealAnalysisResult, modifier: Modifier = Modifier) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(14.dp)) {
        Card(
            colors = CardDefaults.cardColors(containerColor = GreenSoft),
            modifier = Modifier.fillMaxWidth(),
        ) {
            Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row {
                    Text(
                        text = result.totalKcal?.let { "%.0f".format(it) } ?: "--",
                        fontSize = 44.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                    )
                    Text(" kcal", modifier = Modifier.padding(top = 22.dp), color = MaterialTheme.colorScheme.primary)
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

        Card(modifier = Modifier.fillMaxWidth()) {
            Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(Modifier.fillMaxWidth()) {
                    Text("目标匹配", fontWeight = FontWeight.Bold)
                    Spacer(Modifier.weight(1f))
                    Text(goalLabel(result.goalMatch.level), color = OrangePrimary, fontWeight = FontWeight.Bold)
                    Text(result.goalMatch.score?.let { "  $it/10" } ?: "  --/10")
                }
                LinearProgressIndicator(
                    progress = { ((result.goalMatch.score ?: 0).coerceIn(0, 10)) / 10f },
                    modifier = Modifier.fillMaxWidth(),
                    color = OrangePrimary,
                )
                Text(result.goalMatch.reason.ifBlank { "AI 未给出明确判断。" })
            }
        }

        if (result.suggestions.isNotEmpty()) {
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("建议", fontWeight = FontWeight.Bold)
                    result.suggestions.take(3).forEach { Text("✓ $it") }
                }
            }
        }

        if (result.ingredients.isNotEmpty()) {
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("食材明细", fontWeight = FontWeight.Bold)
                    result.ingredients.forEach { ingredient ->
                        Row(Modifier.fillMaxWidth()) {
                            Text(ingredient.name.ifBlank { "食材" })
                            Spacer(Modifier.weight(1f))
                            Text(ingredient.amount)
                            Text("  ${ingredient.kcal?.let { "%.0f kcal".format(it) } ?: "--"}")
                        }
                    }
                }
            }
        }

        if (result.tags.isNotEmpty()) {
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                result.tags.forEach { TagChip(it) }
            }
        }

        Text(
            result.disclaimer,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            style = MaterialTheme.typography.bodySmall,
        )
    }
}

fun goalLabel(level: String?) = when (level) {
    "good" -> "适合"
    "partial" -> "部分适合"
    "poor" -> "不太适合"
    else -> "无法判断"
}
