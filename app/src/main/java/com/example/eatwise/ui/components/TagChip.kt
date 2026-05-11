package com.example.eatwise.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.eatwise.core.i18n.MealLanguageText
import com.example.eatwise.ui.i18n.LocalAppLanguage
import com.example.eatwise.ui.i18n.LocalAppStrings
import com.example.eatwise.ui.theme.GreenDeep
import com.example.eatwise.ui.theme.GreenSoft
import com.example.eatwise.ui.theme.OrangePrimary
import com.example.eatwise.ui.theme.OrangeSoft
import com.example.eatwise.ui.theme.RedPrimary
import com.example.eatwise.ui.theme.RedSoft
import com.example.eatwise.ui.theme.YellowPrimary
import com.example.eatwise.ui.theme.YellowSoft

@Composable
fun TagChip(text: String, modifier: Modifier = Modifier, compact: Boolean = false) {
    val language = LocalAppLanguage.current
    val label = compactLabel(text, language)
    val style = tagStyle(text)
    val fontSize = if (compact) 10.sp else 11.sp
    val minHeight = if (compact) 20.dp else 23.dp
    val maxWidth = if (compact) 70.dp else 88.dp
    val horizontalPadding = if (compact) 6.dp else 8.dp
    Text(
        text = label,
        color = style.content,
        fontSize = fontSize,
        fontWeight = FontWeight.Bold,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
        modifier = modifier
            .heightIn(min = minHeight)
            .widthIn(max = maxWidth)
            .clip(RoundedCornerShape(50))
            .background(style.container)
            .padding(horizontal = horizontalPadding, vertical = if (compact) 2.dp else 3.dp),
    )
}

private data class TagStyle(val container: Color, val content: Color)

private fun tagStyle(text: String): TagStyle {
    val clean = text.trim()
    return when {
        MealLanguageText.isRiskTag(clean) || clean.hasAny("不适合", "不太适合", "控量", "浅尝", "油炸", "油脂高", "糖偏高", "钠偏高", "重口味", "高脂", "重油") ->
            TagStyle(RedSoft, RedPrimary)
        MealLanguageText.isPositiveTag(clean) || clean.hasAny("轻负担", "蛋白足", "有蔬菜", "少糖", "清淡", "推荐", "适合", "友好") ->
            TagStyle(GreenSoft, GreenDeep)
        MealLanguageText.isWarningTag(clean) || clean.hasAny("负担高", "碳水多", "蔬菜少", "蛋白少", "控脂", "减脂", "谨慎", "关注", "甜品", "快餐") ->
            TagStyle(YellowSoft, YellowPrimary)
        else ->
            TagStyle(Color(0xFFF0F2F5), Color(0xFF6D7484))
    }
}

private fun compactLabel(text: String, language: com.example.eatwise.core.i18n.AppLanguage): String {
    val translated = MealLanguageText.compactTag(text, language)
    if (language != com.example.eatwise.core.i18n.AppLanguage.ZhHans || !translated.equals(text.trim(), ignoreCase = true)) {
        return translated
    }
    val clean = text.trim().replace(Regex("\\s+"), "")
    if (clean.contains("控脂")) return "控脂"
    val semanticShortLabel = when (clean) {
        "可以适量吃" -> "适量吃"
        "需要严格控量" -> "控量"
        "严控量", "需要控量" -> "控量"
        "只能尝一小口", "只能尝一两口解馋", "浅尝即可" -> "浅尝"
        "可以适量多吃" -> "可多吃"
        "可以常吃" -> "推荐"
        "不太适合" -> "不适合"
        "主食碳水" -> "主食"
        "蛋白食材" -> "有蛋白"
        "高脂注意", "油脂注意" -> "油脂高"
        "油盐偏高" -> "油盐高"
        else -> null
    }
    if (semanticShortLabel != null) return semanticShortLabel
    return if (clean.length <= 6) clean else clean.take(6)
}

private fun String.hasAny(vararg keywords: String): Boolean =
    keywords.any { contains(it, ignoreCase = true) }

@Composable
fun GoalBadge(level: String?, modifier: Modifier = Modifier, compact: Boolean = false) {
    val strings = LocalAppStrings.current
    val label = when (level) {
        "good" -> strings.goalGood
        "partial" -> strings.goalPartial
        "poor" -> strings.goalPoor
        else -> strings.goalUnknown
    }
    val container: Color
    val content: Color
    when (level) {
        "good" -> {
            container = GreenSoft
            content = GreenDeep
        }
        "poor" -> {
            container = RedSoft
            content = RedPrimary
        }
        "partial" -> {
            container = OrangeSoft
            content = OrangePrimary
        }
        else -> {
            container = Color(0xFFF0F2F5)
            content = Color(0xFF6D7484)
        }
    }
    val fontSize = if (compact) 10.sp else 11.sp
    val minHeight = if (compact) 20.dp else 23.dp
    val horizontalPadding = if (compact) 6.dp else 8.dp
    Text(
        text = label,
        color = content,
        fontSize = fontSize,
        fontWeight = FontWeight.Bold,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
        modifier = modifier
            .heightIn(min = minHeight)
            .widthIn(max = if (compact) 74.dp else 104.dp)
            .clip(RoundedCornerShape(50))
            .background(container)
            .padding(horizontal = horizontalPadding, vertical = if (compact) 2.dp else 3.dp),
    )
}
