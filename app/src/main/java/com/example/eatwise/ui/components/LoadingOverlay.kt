package com.example.eatwise.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.eatwise.ui.theme.GreenSoft
import com.example.eatwise.ui.theme.LineSoft
import kotlinx.coroutines.delay

private val loadingTips = listOf(
    "正在保留图片细节，只压缩到适合上传的尺寸。",
    "AI 识别菜品和建议通常是最久的一步。",
    "你可以先回首页，分析会在后台继续进行。",
    "结果完成后会自动保存到饮食记录。",
)

@Composable
fun LoadingOverlay(
    text: String,
    modifier: Modifier = Modifier,
    detail: String = "正在识别食材、判断健康度和匹配目标",
    tips: List<String> = loadingTips,
    progress: Float? = null,
) {
    var tipIndex by remember(tips) { mutableStateOf(0) }

    LaunchedEffect(tips) {
        if (tips.isEmpty()) return@LaunchedEffect
        tipIndex = 0
        while (tips.size > 1) {
            delay(3200)
            tipIndex = (tipIndex + 1) % tips.size
        }
    }

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, LineSoft.copy(alpha = 0.62f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 18.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            CircularProgressIndicator(
                modifier = Modifier.size(32.dp),
                strokeWidth = 3.dp,
                trackColor = GreenSoft,
            )
            Text(
                text,
                modifier = Modifier.fillMaxWidth(),
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                lineHeight = 20.sp,
                textAlign = TextAlign.Center,
            )
            Text(
                detail,
                modifier = Modifier.fillMaxWidth(),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 13.sp,
                lineHeight = 17.sp,
                textAlign = TextAlign.Center,
            )
            val progressModifier = Modifier
                .fillMaxWidth()
                .padding(top = 6.dp)
                .height(5.dp)
                .clip(RoundedCornerShape(50))
            if (progress != null) {
                Box(progressModifier.background(GreenSoft)) {
                    Box(
                        Modifier
                            .fillMaxWidth(progress.coerceIn(0f, 1f))
                            .height(5.dp)
                            .background(MaterialTheme.colorScheme.primary),
                    )
                }
            } else {
                LinearProgressIndicator(
                    modifier = progressModifier,
                    color = MaterialTheme.colorScheme.primary,
                    trackColor = GreenSoft,
                )
            }
            Text(
                "可先返回首页，分析会在后台继续完成。",
                modifier = Modifier.fillMaxWidth(),
                color = MaterialTheme.colorScheme.primary,
                fontSize = 12.sp,
                lineHeight = 16.sp,
                fontWeight = FontWeight.SemiBold,
                textAlign = TextAlign.Center,
            )
            tips.getOrNull(tipIndex)?.let { tip ->
                Text(
                    "小提示：$tip",
                    modifier = Modifier.fillMaxWidth(),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 12.sp,
                    lineHeight = 16.sp,
                    textAlign = TextAlign.Center,
                )
            }
        }
    }
}
