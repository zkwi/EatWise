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
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowRight
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Restaurant
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.eatwise.domain.model.Ingredient
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
                    Text(
                        result.mealName,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Text(
                        result.summary,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                    )
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
            Text(
                result.goalMatch.reason.ifBlank { "AI 未给出明确判断。" },
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
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
                        Text(
                            compactSuggestion(it),
                            modifier = Modifier.weight(1f),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                }
            }
        }

        if (result.ingredients.isNotEmpty()) {
            SoftCard {
                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    Text("多食材明细", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                    Spacer(Modifier.weight(1f))
                    Text(
                        result.totalKcal?.let { "总计 %.0f kcal".format(it) } ?: "总计 -- kcal",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                ingredientGroups(result.ingredients).forEachIndexed { groupIndex, group ->
                    if (group.title.isNotBlank()) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier
                                    .size(28.dp)
                                    .background(GreenSoft, CircleShape),
                            ) {
                                Icon(
                                    Icons.Rounded.Restaurant,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(16.dp),
                                )
                            }
                            Text(group.title, fontWeight = FontWeight.SemiBold)
                        }
                    }
                    group.items.take(8).forEachIndexed { index, ingredient ->
                        IngredientRow(ingredient)
                        if (index != group.items.lastIndex.coerceAtMost(7)) {
                            HorizontalDivider(color = Color(0xFFF0F1F2))
                        }
                    }
                    if (groupIndex != ingredientGroups(result.ingredients).lastIndex) {
                        Spacer(Modifier.height(2.dp))
                    }
                }
            }
        }

        if (result.tags.isNotEmpty()) {
            SoftCard {
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("标签", fontWeight = FontWeight.Bold, modifier = Modifier.padding(end = 4.dp, top = 6.dp))
                    result.tags.take(5).forEach { TagChip(it) }
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
private fun IngredientRow(ingredient: Ingredient) {
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(8.dp)
                .background(GreenSoft, CircleShape),
        )
        Text(
            ingredient.name.ifBlank { "食材" },
            modifier = Modifier
                .padding(start = 10.dp)
                .weight(1f),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        Text(
            ingredient.amount,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.widthIn(max = 88.dp),
        )
        Text(
            ingredient.kcal?.let { "  %.0f".format(it) } ?: "  --",
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1,
        )
        Text(" kcal", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 12.sp)
        Icon(Icons.AutoMirrored.Rounded.KeyboardArrowRight, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

private data class IngredientGroup(val title: String, val items: List<Ingredient>)

private fun compactSuggestion(text: String): String {
    val clean = text.trim()
        .removePrefix("建议")
        .removePrefix("可以")
        .replace("尽量减少", "少")
        .replace("下一餐选择", "下餐选")
        .replace("清淡饮食", "清淡餐")
        .replace("蒸煮类食物", "蒸煮")
        .replace("建议下一餐", "下餐")
        .replace("增加一份", "加")
        .replace("以补充", "补")
        .replace("以降低", "降")
        .trim('，', '。', '、', ' ')
    return if (clean.length <= 14) clean else clean.take(14)
}

private fun ingredientGroups(ingredients: List<Ingredient>): List<IngredientGroup> {
    val grouped = ingredients.groupBy { it.dish.trim() }
    if (grouped.size <= 1 && grouped.keys.firstOrNull().isNullOrBlank()) {
        return listOf(IngredientGroup("", ingredients))
    }
    return grouped.map { (dish, items) ->
        IngredientGroup(dish.ifBlank { "其他" }, items)
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
