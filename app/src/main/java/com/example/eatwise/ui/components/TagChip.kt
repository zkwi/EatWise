package com.example.eatwise.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.padding
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

@Composable
fun TagChip(text: String, modifier: Modifier = Modifier) {
    val warning = text.contains("高") || text.contains("偏高") || text.contains("油") || text.contains("热量")
    val poor = text.contains("不适合") || text.contains("高脂")
    val container = when {
        poor -> RedSoft
        warning -> OrangeSoft
        else -> GreenSoft
    }
    val content = when {
        poor -> RedPrimary
        warning -> OrangePrimary
        else -> GreenDeep
    }
    Text(
        text = text,
        color = content,
        fontSize = 14.sp,
        fontWeight = FontWeight.SemiBold,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
        modifier = modifier
            .clip(RoundedCornerShape(50))
            .background(container)
            .padding(horizontal = 12.dp, vertical = 7.dp),
    )
}

@Composable
fun GoalBadge(level: String?, modifier: Modifier = Modifier) {
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
    Text(
        text = label,
        color = content,
        fontSize = 14.sp,
        fontWeight = FontWeight.SemiBold,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
        modifier = modifier
            .clip(RoundedCornerShape(50))
            .background(container)
            .padding(horizontal = 12.dp, vertical = 6.dp),
    )
}
