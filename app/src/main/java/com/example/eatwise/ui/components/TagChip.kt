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
    val label = compactLabel(text)
    val style = tagStyle(text)
    val fontSize = if (compact) 10.sp else 11.sp
    val minHeight = if (compact) 22.dp else 24.dp
    val maxWidth = if (compact) 74.dp else 82.dp
    val horizontalPadding = if (compact) 7.dp else 8.dp
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
            .padding(horizontal = horizontalPadding, vertical = 3.dp),
    )
}

private data class TagStyle(val container: Color, val content: Color)

private fun tagStyle(text: String): TagStyle {
    val clean = text.trim()
    return when {
        clean.hasAny("不适合", "不太适合", "油炸", "油脂高", "糖偏高", "钠偏高", "重口味", "高脂", "重油") ->
            TagStyle(RedSoft, RedPrimary)
        clean.hasAny("轻负担", "蛋白足", "有蔬菜", "少糖", "清淡", "推荐", "适合", "友好") ->
            TagStyle(GreenSoft, GreenDeep)
        clean.hasAny("负担高", "碳水多", "蔬菜少", "蛋白少", "控脂", "减脂", "谨慎", "关注", "甜品", "快餐") ->
            TagStyle(YellowSoft, YellowPrimary)
        else ->
            TagStyle(Color(0xFFF0F2F5), Color(0xFF6D7484))
    }
}

private fun compactLabel(text: String): String {
    val clean = text.trim().replace(Regex("\\s+"), "")
    val semanticShortLabel = when (clean) {
        "需要严格控量" -> "严控量"
        "只能尝一小口", "只能尝一两口解馋" -> "浅尝"
        "可以适量多吃" -> "可多吃"
        "可以常吃" -> "推荐"
        else -> null
    }
    if (semanticShortLabel != null) return semanticShortLabel
    return if (clean.length <= 6) clean else clean.take(6)
}

private fun String.hasAny(vararg keywords: String): Boolean =
    keywords.any { contains(it, ignoreCase = true) }

@Composable
fun GoalBadge(level: String?, modifier: Modifier = Modifier, compact: Boolean = false) {
    val label = goalLabel(level)
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
    val minHeight = if (compact) 22.dp else 24.dp
    val horizontalPadding = if (compact) 7.dp else 8.dp
    Text(
        text = label,
        color = content,
        fontSize = fontSize,
        fontWeight = FontWeight.Bold,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
        modifier = modifier
            .heightIn(min = minHeight)
            .clip(RoundedCornerShape(50))
            .background(container)
            .padding(horizontal = horizontalPadding, vertical = 3.dp),
    )
}
