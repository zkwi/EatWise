package com.example.eatwise.ui.components

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.material.icons.rounded.Restaurant
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.eatwise.domain.model.Ingredient
import com.example.eatwise.domain.model.MealAnalysisResult
import com.example.eatwise.ui.theme.GreenDeep
import com.example.eatwise.ui.theme.GreenPrimary
import com.example.eatwise.ui.theme.GreenSoft
import com.example.eatwise.ui.theme.LineSoft
import com.example.eatwise.ui.theme.OrangePrimary
import com.example.eatwise.ui.theme.OrangeSoft
import com.example.eatwise.ui.theme.RedPrimary
import com.example.eatwise.ui.theme.RedSoft
import com.example.eatwise.ui.theme.YellowPrimary
import com.example.eatwise.ui.theme.YellowSoft

@Composable
fun MealResultCard(result: MealAnalysisResult, modifier: Modifier = Modifier) {
    val displayMealName = compactMealName(result.mealName)
    val adviceStyle = adviceStyle(result.eatingAdvice, result.goalMatch.level)

    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(22.dp),
            colors = CardDefaults.cardColors(containerColor = adviceStyle.container),
            border = BorderStroke(1.dp, adviceStyle.content.copy(alpha = 0.18f)),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Brush.linearGradient(listOf(Color.White.copy(alpha = 0.76f), adviceStyle.container)))
                    .padding(12.dp),
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .size(58.dp)
                        .background(Color.White.copy(alpha = 0.64f), CircleShape),
                ) {
                    Icon(
                        Icons.Rounded.Restaurant,
                        contentDescription = null,
                        tint = adviceStyle.content.copy(alpha = 0.26f),
                        modifier = Modifier.size(30.dp),
                    )
                }
                Column(verticalArrangement = Arrangement.spacedBy(7.dp)) {
                    EatingAdviceChip(adviceStyle)
                    Text(
                        result.eatingAdvice,
                        fontSize = 24.sp,
                        lineHeight = 28.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = adviceStyle.content,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Text(
                        displayMealName,
                        fontSize = 18.sp,
                        lineHeight = 22.sp,
                        fontWeight = FontWeight.ExtraBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Text(
                        result.summary,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }
        }

        SoftCard {
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Text("健康判断", fontWeight = FontWeight.ExtraBold, fontSize = 17.sp)
                Spacer(Modifier.weight(1f))
                GoalBadge(result.goalMatch.level)
            }
            Text(
                result.goalMatch.reason.ifBlank { "AI 未给出明确判断。" },
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
        }

        if (result.suggestions.isNotEmpty()) {
            SoftCard {
                Text("建议", fontWeight = FontWeight.ExtraBold, fontSize = 17.sp)
                result.suggestions.take(3).forEach {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Rounded.CheckCircle,
                            contentDescription = null,
                            tint = GreenPrimary,
                            modifier = Modifier.size(22.dp),
                        )
                        SuggestionActionChip(suggestionAction(it, result.goalMatch.level))
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
                val groups = ingredientGroups(result.ingredients)
                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    Text("食材拆分", fontWeight = FontWeight.ExtraBold, fontSize = 17.sp)
                }
                groups.forEachIndexed { groupIndex, group ->
                    if (group.title.isNotBlank()) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier
                                    .size(24.dp)
                                    .background(GreenSoft, CircleShape),
                            ) {
                                Icon(
                                    Icons.Rounded.Restaurant,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(14.dp),
                                )
                            }
                            Text(
                                group.title,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 15.sp,
                                lineHeight = 20.sp,
                            )
                        }
                    }
                    group.items.take(8).forEachIndexed { index, ingredient ->
                        IngredientRow(ingredient)
                        if (index != group.items.lastIndex.coerceAtMost(7)) {
                            HorizontalDivider(color = Color(0xFFF0F1F2))
                        }
                    }
                }
            }
        }

        if (result.tags.isNotEmpty()) {
            SoftCard {
                Text("重点提示", fontWeight = FontWeight.ExtraBold, fontSize = 17.sp)
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
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
    Row(
        Modifier
            .fillMaxWidth()
            .padding(vertical = 5.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(6.dp)
                .background(GreenSoft, CircleShape),
        )
        Column(
            modifier = Modifier
                .padding(start = 10.dp)
                .weight(1f),
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Text(
                ingredient.name.ifBlank { "食材" },
                fontWeight = FontWeight.SemiBold,
                fontSize = 14.sp,
                lineHeight = 18.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            val hints = ingredientHints(ingredient)
            if (hints.isNotEmpty()) {
                FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    hints.forEach { hint ->
                        IngredientHintChip(hint)
                    }
                }
            }
        }
    }
}

private data class IngredientGroup(val title: String, val items: List<Ingredient>)
private data class IngredientHint(val label: String, val container: Color, val content: Color)
private data class SuggestionAction(val label: String, val container: Color, val content: Color)
private data class AdviceStyle(val container: Color, val content: Color, val label: String)

@Composable
private fun EatingAdviceChip(style: AdviceStyle) {
    Text(
        text = style.label,
        color = style.content,
        fontSize = 12.sp,
        fontWeight = FontWeight.Bold,
        maxLines = 1,
        modifier = Modifier
            .clip(RoundedCornerShape(50))
            .background(Color.White.copy(alpha = 0.78f))
            .padding(horizontal = 9.dp, vertical = 4.dp),
    )
}

@Composable
private fun SuggestionActionChip(action: SuggestionAction) {
    Text(
        text = action.label,
        color = action.content,
        fontSize = 12.sp,
        fontWeight = FontWeight.Bold,
        maxLines = 1,
        modifier = Modifier
            .clip(RoundedCornerShape(50))
            .background(action.container)
            .padding(horizontal = 9.dp, vertical = 5.dp),
    )
}

@Composable
private fun IngredientHintChip(hint: IngredientHint) {
    Text(
        text = hint.label,
        color = hint.content,
        fontSize = 11.sp,
        fontWeight = FontWeight.SemiBold,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
        modifier = Modifier
            .clip(RoundedCornerShape(50))
            .background(hint.container)
            .padding(horizontal = 8.dp, vertical = 4.dp),
    )
}

private fun ingredientHints(ingredient: Ingredient): List<IngredientHint> {
    val text = "${ingredient.dish}${ingredient.name}"
    val riskHints = buildList {
        if (text.hasAny("油炸", "炸物", "煎炸", "炸")) {
            add(IngredientHint("油炸", RedSoft, RedPrimary))
        }
        if (text.hasAny("红油", "重油", "肥肉", "五花", "黄油", "奶油", "芝麻酱", "沙拉酱") ||
            text.hasAny("油", "酱") && !text.hasAny("油麦菜")
        ) {
            add(IngredientHint("油脂高", YellowSoft, YellowPrimary))
        }
        if (text.hasAny("重口味", "麻辣", "重辣", "红油", "花椒", "辣椒", "腌", "卤", "咸")) {
            add(IngredientHint("重口味", RedSoft, RedPrimary))
        }
        if (text.hasAny("糖", "甜", "蜜", "奶茶", "饮料")) {
            add(IngredientHint("糖偏高", RedSoft, RedPrimary))
        }
    }
    val roleHint = ingredientRoleHint(ingredient)
    return (riskHints.ifEmpty { listOfNotNull(roleHint) })
        .distinctBy { it.label }
        .take(2)
}

private fun ingredientRoleHint(ingredient: Ingredient): IngredientHint? {
    val text = "${ingredient.dish}${ingredient.name}"
    return when {
        text.hasAny("米", "饭", "面", "粉", "饼", "馒头", "面包", "薯", "土豆") ->
            IngredientHint("主食碳水", YellowSoft, YellowPrimary)
        text.hasAny("鸡", "牛", "猪", "鱼", "虾", "蛋", "豆腐", "豆", "肉", "奶") ->
            IngredientHint("蛋白食材", GreenSoft, GreenDeep)
        text.hasAny("菜", "青", "叶", "瓜", "番茄", "西红柿", "菌", "菇", "萝卜", "椒") ->
            IngredientHint("蔬菜纤维", GreenSoft, GreenDeep)
        else ->
            null
    }
}

private fun String.hasAny(vararg keywords: String): Boolean = keywords.any { contains(it, ignoreCase = true) }

private fun suggestionAction(text: String, goalLevel: String): SuggestionAction {
    val clean = text.trim()
    return when {
        clean.hasAny("尝一小口", "浅尝") ->
            SuggestionAction("浅尝", RedSoft, RedPrimary)
        clean.hasAny("严格控量", "控量") ->
            SuggestionAction("控量", RedSoft, RedPrimary)
        clean.hasAny("避免", "避开", "不要", "别吃", "不建议", "少食用") ->
            SuggestionAction("避开", RedSoft, RedPrimary)
        clean.hasAny("减少", "少", "控制", "一半", "半份", "减半", "七分", "几口", "油", "盐", "糖", "炸") ->
            SuggestionAction("少吃点", OrangeSoft, OrangePrimary)
        clean.hasAny("增加", "补充", "加", "蔬菜", "纤维", "蛋白", "喝水") ->
            SuggestionAction("补一点", GreenSoft, GreenDeep)
        clean.hasAny("适量多吃", "可以多") ->
            SuggestionAction("可多吃", GreenSoft, GreenDeep)
        clean.hasAny("搭配", "替换", "改成", "选择", "下餐") ->
            SuggestionAction("换搭配", GreenSoft, GreenDeep)
        goalLevel == "good" ->
            SuggestionAction("放心吃", GreenSoft, GreenDeep)
        goalLevel == "poor" ->
            SuggestionAction("浅尝", RedSoft, RedPrimary)
        else ->
            SuggestionAction("适量吃", Color(0xFFF0F2F5), Color(0xFF6D7484))
    }
}

private fun compactSuggestion(text: String): String {
    val clean = text.trim()
        .removePrefix("建议")
        .removePrefix("可以")
        .removePrefix("尽量")
        .removePrefix("如果")
        .replace("尽量减少", "少")
        .replace("下一餐选择", "下餐选")
        .replace("清淡饮食", "清淡餐")
        .replace("蒸煮类食物", "蒸煮")
        .replace("建议下一餐", "下餐")
        .replace("增加一份", "加")
        .replace("以补充", "补")
        .replace("以降低", "降")
        .trim('，', '。', '、', ' ')
    return if (clean.length <= 16) clean else clean.take(16)
}

private fun adviceStyle(advice: String, level: String?): AdviceStyle = when {
    advice.hasAny("尝", "严格") || level == "poor" -> AdviceStyle(RedSoft, RedPrimary, "食用建议")
    advice.hasAny("适量多吃") || level == "good" -> AdviceStyle(GreenSoft, GreenDeep, "食用建议")
    else -> AdviceStyle(YellowSoft, YellowPrimary, "食用建议")
}

private fun compactMealName(name: String): String {
    val clean = name.trim()
    val withoutDetail = clean
        .substringBefore("（")
        .substringBefore("(")
        .trim()
    return withoutDetail.ifBlank { clean }.let {
        if (it.length <= 10) it else it.take(10)
    }
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
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, LineSoft.copy(alpha = 0.62f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    ) {
        Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp), content = content)
    }
}

fun goalLabel(level: String?) = when (level) {
    "good" -> "适合"
    "partial" -> "部分适合"
    "poor" -> "不太适合"
    else -> "无法判断"
}
