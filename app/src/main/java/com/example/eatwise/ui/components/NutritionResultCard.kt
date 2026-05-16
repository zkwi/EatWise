package com.example.eatwise.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.eatwise.domain.model.NutritionAnalysisResult
import com.example.eatwise.domain.model.NutritionItem
import com.example.eatwise.ui.i18n.AppStrings
import com.example.eatwise.ui.i18n.LocalAppStrings
import com.example.eatwise.ui.theme.GreenDeep
import com.example.eatwise.ui.theme.GreenSoft
import com.example.eatwise.ui.theme.LineSoft
import com.example.eatwise.ui.theme.OrangePrimary
import com.example.eatwise.ui.theme.OrangeSoft
import com.example.eatwise.ui.theme.YellowPrimary
import com.example.eatwise.ui.theme.YellowSoft

@Composable
fun NutritionResultCard(result: NutritionAnalysisResult, modifier: Modifier = Modifier) {
    val strings = LocalAppStrings.current
    val calorieItem = nutritionCalorieItem(result.items)
    val overviewItems = nutritionOverviewItems(result.items)
    val detailItems = nutritionDetailItems(result.items)

    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(10.dp)) {
        nutritionSections(result).forEach { section ->
            when (section) {
                NutritionSection.Hero -> NutritionHeroCard(result, strings, calorieItem)
                NutritionSection.Actions -> NutritionSoftCard {
                    SectionTitle(strings.nutritionActions)
                    result.suggestions.take(2).forEachIndexed { index, suggestion ->
                        NutritionActionRow(index + 1, suggestion)
                    }
                }
                NutritionSection.Overview -> NutritionSoftCard {
                    SectionTitle(strings.nutritionDetails)
                    NutritionOverviewGrid(overviewItems, strings)
                }
                NutritionSection.Basis -> NutritionSoftCard {
                    SectionTitle(strings.nutritionBasis)
                    Text(
                        result.basis,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 13.sp,
                        lineHeight = 19.sp,
                    )
                }
                NutritionSection.Notes -> NutritionSoftCard {
                    SectionTitle(strings.nutritionNotes)
                    detailItems.forEach { NutritionItemRow(it, strings) }
                }
            }
        }

        Text(
            result.disclaimer,
            modifier = Modifier.padding(horizontal = 4.dp),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontSize = 12.sp,
            lineHeight = 16.sp,
        )
    }
}

@Composable
private fun NutritionSoftCard(content: @Composable () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, LineSoft.copy(alpha = 0.62f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
    ) {
        Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            content()
        }
    }
}

@Composable
private fun NutritionHeroCard(
    result: NutritionAnalysisResult,
    strings: AppStrings,
    calorieItem: NutritionItem?,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, LineSoft.copy(alpha = 0.62f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(Brush.linearGradient(listOf(Color(0xFFFFFCF7), Color.White)))
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                SectionTitle(strings.nutritionEstimate)
            }
            Text(
                result.calorieRange.ifBlank { strings.nutritionLevelUnknown },
                fontWeight = FontWeight.ExtraBold,
                fontSize = 26.sp,
                lineHeight = 32.sp,
                color = OrangePrimary,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
            val calorieEquivalent = nutritionCalorieEquivalent(result)
            if (calorieEquivalent.isNotBlank()) {
                Text(
                    calorieEquivalent,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(OrangeSoft.copy(alpha = 0.30f))
                        .padding(horizontal = 10.dp, vertical = 8.dp),
                    color = OrangePrimary,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    lineHeight = 18.sp,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            Text(
                result.mealName,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 13.sp,
                lineHeight = 18.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            val calorieNote = calorieItem?.note.orEmpty()
            if (calorieNote.isNotBlank()) {
                Text(
                    calorieNote,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(OrangeSoft.copy(alpha = 0.38f))
                        .padding(horizontal = 10.dp, vertical = 8.dp),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 12.sp,
                    lineHeight = 17.sp,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
    }
}

@Composable
private fun SectionTitle(text: String) {
    Text(
        text,
        fontWeight = FontWeight.ExtraBold,
        fontSize = 16.sp,
        lineHeight = 20.sp,
    )
}

@Composable
private fun NutritionOverviewGrid(items: List<NutritionItem>, strings: AppStrings) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        items.forEach { item ->
            NutritionOverviewTile(item, strings, Modifier.fillMaxWidth())
        }
    }
}

@Composable
private fun NutritionOverviewTile(item: NutritionItem, strings: AppStrings, modifier: Modifier = Modifier) {
    val style = nutritionLevelStyle(item.level)
    val header = nutritionDetailHeaderText(
        item = item,
        fallbackLabel = strings.nutritionDetails,
        unknownEstimate = strings.nutritionLevelUnknown,
        compactEstimate = true,
    )
    Row(
        modifier = modifier
            .heightIn(min = 56.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(style.container.copy(alpha = 0.48f))
            .padding(10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            buildAnnotatedString {
                append(header.label)
                append("  ")
                withStyle(SpanStyle(color = style.content, fontWeight = FontWeight.ExtraBold)) {
                    append(header.estimate)
                }
            },
            modifier = Modifier.weight(1f),
            fontWeight = FontWeight.Bold,
            fontSize = 14.sp,
            lineHeight = 18.sp,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        Spacer(Modifier.width(8.dp))
        LevelChip(item.level, strings, style)
    }
}

@Composable
private fun NutritionItemRow(item: NutritionItem, strings: AppStrings) {
    val style = nutritionLevelStyle(item.level)
    val header = nutritionDetailHeaderText(
        item = item,
        fallbackLabel = strings.nutritionDetails,
        unknownEstimate = strings.nutritionLevelUnknown,
        compactEstimate = true,
    )
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(style.container.copy(alpha = 0.30f))
            .padding(horizontal = 9.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.Top,
    ) {
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    buildAnnotatedString {
                        append(header.label)
                        append("  ")
                        withStyle(SpanStyle(color = style.content, fontWeight = FontWeight.ExtraBold)) {
                            append(header.estimate)
                        }
                    },
                    modifier = Modifier.weight(1f),
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    lineHeight = 18.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Spacer(Modifier.width(6.dp))
                LevelChip(item.level, strings, style)
            }
            if (item.note.isNotBlank()) {
                Text(
                    item.note,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 12.sp,
                    lineHeight = 16.sp,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
    }
}

@Composable
private fun NutritionActionRow(index: Int, text: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(GreenSoft)
            .padding(10.dp),
        horizontalArrangement = Arrangement.spacedBy(9.dp),
        verticalAlignment = Alignment.Top,
    ) {
        Box(
            modifier = Modifier
                .size(24.dp)
                .clip(CircleShape)
                .background(Color.White),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                index.toString(),
                color = GreenDeep,
                fontWeight = FontWeight.ExtraBold,
                fontSize = 12.sp,
                lineHeight = 14.sp,
            )
        }
        Text(
            text,
            modifier = Modifier.weight(1f),
            color = GreenDeep,
            fontWeight = FontWeight.SemiBold,
            fontSize = 12.sp,
            lineHeight = 17.sp,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

@Composable
private fun LevelChip(level: String, strings: AppStrings, style: NutritionStyle) {
    val text = when (level.trim().lowercase()) {
        "low" -> strings.nutritionLevelLow
        "moderate" -> strings.nutritionLevelModerate
        "high" -> strings.nutritionLevelHigh
        else -> strings.nutritionLevelUnknown
    }
    Text(
        text,
        modifier = Modifier
            .clip(RoundedCornerShape(999.dp))
            .background(style.container)
            .padding(horizontal = 7.dp, vertical = 3.dp),
        color = style.content,
        fontWeight = FontWeight.Bold,
        fontSize = 11.sp,
        lineHeight = 14.sp,
    )
}

private data class NutritionStyle(val container: Color, val content: Color)

enum class NutritionSection {
    Hero,
    Actions,
    Overview,
    Basis,
    Notes,
}

private fun nutritionLevelStyle(level: String): NutritionStyle = when (level.trim().lowercase()) {
    "low" -> NutritionStyle(YellowSoft, YellowPrimary)
    "moderate" -> NutritionStyle(GreenSoft, GreenDeep)
    "high" -> NutritionStyle(OrangeSoft, OrangePrimary)
    else -> NutritionStyle(Color(0xFFF0F2F5), Color(0xFF6D7484))
}

fun nutritionOverviewItems(items: List<NutritionItem>, limit: Int = 4): List<NutritionItem> {
    val nonCalorieItems = items.filterNot { isCalorieItem(it.label) }
    return nonCalorieItems.ifEmpty { items }.take(limit)
}

fun nutritionCalorieItem(items: List<NutritionItem>): NutritionItem? = items.firstOrNull { isCalorieItem(it.label) }

fun nutritionCalorieEquivalent(result: NutritionAnalysisResult): String = result.calorieEquivalent.trim()

fun nutritionDetailItems(items: List<NutritionItem>, limit: Int = 3): List<NutritionItem> =
    items.filter { it.note.isNotBlank() && !isCalorieItem(it.label) }.take(limit)

fun nutritionSections(result: NutritionAnalysisResult): List<NutritionSection> = buildList {
    add(NutritionSection.Hero)
    if (result.suggestions.isNotEmpty()) add(NutritionSection.Actions)
    if (nutritionOverviewItems(result.items).isNotEmpty()) add(NutritionSection.Overview)
    if (result.basis.isNotBlank()) add(NutritionSection.Basis)
    if (nutritionDetailItems(result.items).isNotEmpty()) add(NutritionSection.Notes)
}

data class NutritionDetailHeaderText(
    val label: String,
    val estimate: String,
)

fun nutritionDetailHeaderText(
    item: NutritionItem,
    fallbackLabel: String,
    unknownEstimate: String,
    compactEstimate: Boolean = false,
): NutritionDetailHeaderText = NutritionDetailHeaderText(
    label = item.label.ifBlank { fallbackLabel },
    estimate = if (compactEstimate) {
        compactNutritionEstimate(item.estimate, unknownEstimate)
    } else {
        item.estimate.ifBlank { unknownEstimate }
    },
)

fun compactNutritionEstimate(estimate: String, unknownEstimate: String): String =
    estimate.ifBlank { unknownEstimate }
        .trim()
        .replace(Regex("""\s*[-–—]\s*"""), "-")
        .replace(Regex("""\s+(kcal|g|mg|克|千卡|大卡)""", RegexOption.IGNORE_CASE), "$1")

private fun isCalorieItem(label: String): Boolean {
    val clean = label.trim().lowercase()
    return clean.contains("热量") ||
        clean.contains("熱量") ||
        clean.contains("能量") ||
        clean.contains("calorie") ||
        clean.contains("kcal") ||
        clean.contains("カロリー") ||
        clean.contains("エネルギー")
}
