package com.example.eatwise.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
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
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.eatwise.ui.theme.GreenSoft
import com.example.eatwise.ui.theme.LineSoft
import com.example.eatwise.ui.i18n.LocalAppStrings
import kotlinx.coroutines.delay

@Composable
fun LoadingOverlay(
    text: String,
    modifier: Modifier = Modifier,
    detail: String = "",
    tips: List<String>? = null,
    progress: Float? = null,
    promptPreview: String = "",
    modelOutput: String = "",
) {
    val strings = LocalAppStrings.current
    val displayTips = tips ?: strings.loadingTips
    var tipIndex by remember(displayTips) { mutableIntStateOf(0) }

    LaunchedEffect(displayTips) {
        if (displayTips.isEmpty()) return@LaunchedEffect
        tipIndex = 0
        while (displayTips.size > 1) {
            delay(3200)
            tipIndex = (tipIndex + 1) % displayTips.size
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
                .padding(horizontal = 14.dp, vertical = 14.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(7.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                CircularProgressIndicator(
                    modifier = Modifier.size(28.dp),
                    strokeWidth = 3.dp,
                    trackColor = GreenSoft,
                )
                Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text(
                        text,
                        modifier = Modifier.fillMaxWidth(),
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        lineHeight = 20.sp,
                    )
                    Text(
                        detail,
                        modifier = Modifier.fillMaxWidth(),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 12.sp,
                        lineHeight = 16.sp,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }
            val progressModifier = Modifier
                .fillMaxWidth()
                .padding(top = 2.dp)
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
                strings.loadingBackHint,
                modifier = Modifier.fillMaxWidth(),
                color = MaterialTheme.colorScheme.primary,
                fontSize = 12.sp,
                lineHeight = 16.sp,
                fontWeight = FontWeight.SemiBold,
                textAlign = TextAlign.Center,
            )
            if (promptPreview.isNotBlank() || modelOutput.isNotBlank()) {
                GenerationPreviewPanel(
                    promptPreview = promptPreview,
                    modelOutput = modelOutput,
                )
            }
            displayTips.getOrNull(tipIndex)?.let { tip ->
                Text(
                    "${strings.tipPrefix}$tip",
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

@Composable
private fun GenerationPreviewPanel(
    promptPreview: String,
    modelOutput: String,
) {
    val strings = LocalAppStrings.current
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.06f))
            .border(1.dp, LineSoft.copy(alpha = 0.56f), RoundedCornerShape(14.dp))
            .padding(9.dp),
        verticalArrangement = Arrangement.spacedBy(5.dp),
    ) {
        PreviewBlock(
            title = strings.promptPreviewTitle,
            text = promptPreview.ifBlank { strings.promptPreviewPlaceholder },
            maxLines = 2,
        )
        Box(Modifier.fillMaxWidth().height(1.dp).background(LineSoft.copy(alpha = 0.5f)))
        PreviewBlock(
            title = strings.modelOutputTitle,
            text = modelOutput.toStreamingPreview(),
            maxLines = 7,
            monospace = true,
            showLiveBadge = modelOutput.isNotBlank(),
        )
    }
}

@Composable
private fun PreviewBlock(
    title: String,
    text: String,
    maxLines: Int,
    monospace: Boolean = false,
    showLiveBadge: Boolean = false,
) {
    val strings = LocalAppStrings.current
    Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                title,
                color = MaterialTheme.colorScheme.primary,
                fontSize = 12.sp,
                lineHeight = 15.sp,
                fontWeight = FontWeight.ExtraBold,
            )
            if (showLiveBadge) {
                Box(Modifier.width(6.dp))
                Text(
                    strings.analyzingBadge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 10.sp,
                    lineHeight = 12.sp,
                    maxLines = 1,
                )
            }
        }
        Text(
            text,
            modifier = Modifier.fillMaxWidth().heightIn(min = 18.dp),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontSize = if (monospace) 11.sp else 12.sp,
            lineHeight = if (monospace) 15.sp else 16.sp,
            fontFamily = if (monospace) FontFamily.Monospace else FontFamily.Default,
            maxLines = maxLines,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

@Composable
private fun String.toStreamingPreview(): String {
    val strings = LocalAppStrings.current
    val normalized = lines().joinToString("\n") { it.trimEnd() }.trim()
    if (normalized.isBlank()) return strings.streamingPlaceholder
    return if (normalized.length > 560) "...${normalized.takeLast(560)}" else normalized
}
