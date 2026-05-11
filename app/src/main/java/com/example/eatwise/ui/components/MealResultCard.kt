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
    val overallScore = overallScore(result.eatingAdvice, result.goalMatch.level)
    val primaryTags = result.tags.take(4)

    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(7.dp)) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = adviceStyle.container),
            border = BorderStroke(1.dp, adviceStyle.content.copy(alpha = 0.18f)),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Brush.linearGradient(listOf(Color.White.copy(alpha = 0.76f), adviceStyle.container)))
                    .padding(9.dp),
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .size(42.dp)
                        .background(Color.White.copy(alpha = 0.64f), CircleShape),
                ) {
                    Icon(
                        Icons.Rounded.Restaurant,
                        contentDescription = null,
                        tint = adviceStyle.content.copy(alpha = 0.26f),
                        modifier = Modifier.size(23.dp),
                    )
                }
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                        EatingAdviceChip(adviceStyle)
                        OverallRatingChip(overallScore, adviceStyle)
                    }
                    Text(
                        result.eatingAdvice,
                        fontSize = 19.sp,
                        lineHeight = 23.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = adviceStyle.content,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Text(
                        displayMealName,
                        fontSize = 16.sp,
                        lineHeight = 20.sp,
                        fontWeight = FontWeight.ExtraBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    if (primaryTags.isNotEmpty()) {
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(5.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp),
                        ) {
                            primaryTags.forEach { TagChip(it, compact = true) }
                        }
                    }
                    Text(
                        summaryForCard(result.summary),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 12.sp,
                        lineHeight = 16.sp,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }
        }

        SoftCard {
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Text("健康判断", fontWeight = FontWeight.ExtraBold, fontSize = 16.sp, lineHeight = 20.sp)
                Spacer(Modifier.weight(1f))
                GoalBadge(result.goalMatch.level)
            }
            Text(
                result.goalMatch.reason.ifBlank { "AI 未给出明确判断。" },
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 13.sp,
                lineHeight = 18.sp,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
        }

        if (result.suggestions.isNotEmpty()) {
            SoftCard {
                Text("建议", fontWeight = FontWeight.ExtraBold, fontSize = 16.sp, lineHeight = 20.sp)
                result.suggestions.take(3).forEach {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Rounded.CheckCircle,
                            contentDescription = null,
                            tint = GreenPrimary,
                            modifier = Modifier.size(19.dp),
                        )
                        SuggestionActionChip(suggestionAction(it, result.goalMatch.level))
                        Text(
                            compactSuggestion(it),
                            modifier = Modifier.weight(1f),
                            fontSize = 14.sp,
                            lineHeight = 18.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                }
            }
        }

        if (result.ingredients.isNotEmpty()) {
            SoftCard {
                val dishes = dishAdvices(result)
                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    Text("菜品建议", fontWeight = FontWeight.ExtraBold, fontSize = 16.sp, lineHeight = 20.sp)
                }
                dishes.take(6).forEachIndexed { index, dish ->
                    DishAdviceRow(dish)
                    if (index != dishes.lastIndex.coerceAtMost(5)) {
                        HorizontalDivider(color = Color(0xFFF0F1F2))
                    }
                }
            }
        }

        if (result.tags.size > primaryTags.size) {
            SoftCard {
                Text("重点提示", fontWeight = FontWeight.ExtraBold, fontSize = 16.sp, lineHeight = 20.sp)
                FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    result.tags.drop(primaryTags.size).take(5).forEach { TagChip(it) }
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
private fun DishAdviceRow(dish: DishAdvice) {
    Column(
        Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Text(
            dish.title,
            fontWeight = FontWeight.SemiBold,
            fontSize = 14.sp,
            lineHeight = 18.sp,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            dish.hints.forEach { hint -> IngredientHintChip(hint) }
        }
        Text(
            dish.advice,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontSize = 12.sp,
            lineHeight = 16.sp,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

private data class IngredientGroup(val title: String, val items: List<Ingredient>)
private data class IngredientHint(val label: String, val container: Color, val content: Color)
private data class DishAdvice(val title: String, val hints: List<IngredientHint>, val advice: String)
private data class SuggestionAction(val label: String, val container: Color, val content: Color)
private data class AdviceStyle(val container: Color, val content: Color, val label: String)

@Composable
private fun OverallRatingChip(score: Int, style: AdviceStyle) {
    Text(
        text = "${starText(score)} $score/5",
        color = style.content,
        fontSize = 12.sp,
        fontWeight = FontWeight.Bold,
        maxLines = 1,
        modifier = Modifier
            .clip(RoundedCornerShape(50))
            .background(Color.White.copy(alpha = 0.78f))
            .padding(horizontal = 8.dp, vertical = 3.dp),
    )
}

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
            .padding(horizontal = 8.dp, vertical = 3.dp),
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
            .padding(horizontal = 8.dp, vertical = 4.dp),
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
            .padding(horizontal = 7.dp, vertical = 3.dp),
    )
}

private fun dishAdvices(result: MealAnalysisResult): List<DishAdvice> =
    ingredientGroups(result.ingredients).map { group ->
        val title = group.title.ifBlank { compactMealName(result.mealName).ifBlank { "这道菜" } }
        val hints = dishHints(group).ifEmpty {
            listOf(IngredientHint("适量", Color(0xFFF0F2F5), Color(0xFF6D7484)))
        }
        DishAdvice(title, hints, dishAdviceText(hints))
    }

private fun dishHints(group: IngredientGroup): List<IngredientHint> {
    val text = buildString {
        append(group.title)
        group.items.forEach {
            append(it.dish)
            append(it.name)
        }
    }
    return buildList {
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
        if (text.hasAny("米", "饭", "面", "粉", "饼", "馒头", "面包", "薯", "土豆")) {
            add(IngredientHint("主食碳水", YellowSoft, YellowPrimary))
        }
        if (text.hasAny("鸡", "牛", "猪", "鱼", "虾", "蛋", "豆腐", "豆", "肉", "奶")) {
            add(IngredientHint("蛋白食材", GreenSoft, GreenDeep))
        }
        if (text.hasAny("菜", "青", "叶", "瓜", "番茄", "西红柿", "菌", "菇", "萝卜", "椒", "海带")) {
            add(IngredientHint("蔬菜纤维", GreenSoft, GreenDeep))
        }
    }
        .distinctBy { it.label }
        .take(3)
}

private fun dishAdviceText(hints: List<IngredientHint>): String {
    val labels = hints.map { it.label }
    return when {
        "油炸" in labels -> "油炸部分少量尝即可，搭配清淡蔬菜更稳。"
        "油脂高" in labels -> "油脂偏高，控制份量，少蘸酱汁更合适。"
        "重口味" in labels -> "口味偏重，少喝汤汁酱汁，下一餐清淡一点。"
        "糖偏高" in labels -> "甜度偏高，适合浅尝，别和含糖饮料叠加。"
        "蔬菜纤维" in labels && "蛋白食材" in labels -> "蛋白和蔬菜搭配不错，可以作为这餐主力。"
        "蛋白食材" in labels -> "补蛋白可以，注意份量和烹调油盐。"
        "蔬菜纤维" in labels -> "蔬菜纤维不错，可以多吃一点平衡这餐。"
        "主食碳水" in labels -> "主食适量就好，搭配蛋白和蔬菜更稳。"
        else -> "按正常份量吃，留意整体油盐和饱腹感。"
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

private fun summaryForCard(text: String): String {
    val clean = text.trim()
    val endsCleanly = clean.lastOrNull() in setOf('。', '！', '？', '.', '!', '?')
    if (clean.length <= 48) return if (endsCleanly) clean else "$clean..."
    return clean.take(48).trimEnd('，', '、', ' ') + "..."
}

private fun adviceStyle(advice: String, level: String?): AdviceStyle = when {
    advice.hasAny("尝", "严格") || level == "poor" -> AdviceStyle(RedSoft, RedPrimary, "食用建议")
    advice.hasAny("适量多吃") || level == "good" -> AdviceStyle(GreenSoft, GreenDeep, "食用建议")
    else -> AdviceStyle(YellowSoft, YellowPrimary, "食用建议")
}

private fun overallScore(advice: String, level: String?): Int = when {
    level == "good" || advice.hasAny("适量多吃", "可以常吃", "推荐") -> 5
    level == "poor" || advice.hasAny("尝一小口", "只能尝", "不建议") -> 1
    advice.hasAny("严格控量", "控量") -> 2
    level == "partial" || advice.hasAny("适量") -> 3
    else -> 3
}

private fun starText(score: Int): String {
    val safeScore = score.coerceIn(1, 5)
    return "★".repeat(safeScore) + "☆".repeat(5 - safeScore)
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
        Column(Modifier.padding(9.dp), verticalArrangement = Arrangement.spacedBy(5.dp), content = content)
    }
}

fun goalLabel(level: String?) = when (level) {
    "good" -> "适合"
    "partial" -> "部分适合"
    "poor" -> "不太适合"
    else -> "无法判断"
}
