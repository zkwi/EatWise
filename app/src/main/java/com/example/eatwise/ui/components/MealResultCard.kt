package com.example.eatwise.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Restaurant
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.eatwise.core.i18n.AppLanguage
import com.example.eatwise.core.i18n.MealLanguageText
import com.example.eatwise.domain.model.Ingredient
import com.example.eatwise.domain.model.MealAnalysisResult
import com.example.eatwise.ui.i18n.AppStrings
import com.example.eatwise.ui.i18n.LocalAppLanguage
import com.example.eatwise.ui.i18n.LocalAppStrings
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
    val strings = LocalAppStrings.current
    val language = LocalAppLanguage.current
    val displayMealName = result.mealName.trim().ifBlank { strings.resultFallbackMealName }
    val displayAdvice = MealLanguageText.displayAdvice(result.eatingAdvice, language)
    val adviceStyle = adviceStyle(displayAdvice, result.goalMatch.level, strings)
    val overallScore = overallScore(result.eatingAdvice, result.goalMatch.level)
    val primaryTags = result.tags.take(4)

    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
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
                Column(
                    modifier = Modifier.fillMaxWidth().padding(end = 48.dp),
                    verticalArrangement = Arrangement.spacedBy(5.dp),
                ) {
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                        EatingAdviceChip(adviceStyle)
                        OverallRatingChip(overallScore, adviceStyle)
                    }
                    ExpandableText(
                        text = displayAdvice,
                        fontSize = 19.sp,
                        lineHeight = 23.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = adviceStyle.content,
                        collapsedMaxLines = 1,
                    )
                    ExpandableText(
                        text = displayMealName,
                        fontSize = 16.sp,
                        lineHeight = 20.sp,
                        fontWeight = FontWeight.ExtraBold,
                        collapsedMaxLines = 1,
                    )
                    if (primaryTags.isNotEmpty()) {
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(5.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp),
                        ) {
                            primaryTags.forEach { TagChip(it, compact = true) }
                        }
                    }
                    ExpandableText(
                        text = result.summary,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 12.sp,
                        lineHeight = 16.sp,
                        collapsedMaxLines = 2,
                    )
                }
            }
        }

        SoftCard {
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Text(strings.suitability, fontWeight = FontWeight.ExtraBold, fontSize = 16.sp, lineHeight = 20.sp)
                Spacer(Modifier.weight(1f))
                GoalBadge(result.goalMatch.level)
            }
            ExpandableText(
                text = result.goalMatch.reason.ifBlank { strings.unknownSuitability },
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 13.sp,
                lineHeight = 18.sp,
                collapsedMaxLines = 2,
            )
        }

        if (result.suggestions.isNotEmpty()) {
            SoftCard {
                Text(strings.adjustments, fontWeight = FontWeight.ExtraBold, fontSize = 16.sp, lineHeight = 20.sp)
                result.suggestions.take(3).forEach {
                    SuggestionRow(it, result.goalMatch.level)
                }
            }
        }

        if (result.ingredients.isNotEmpty()) {
            SoftCard {
                val dishes = dishAdvices(result)
                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    Text(strings.dishAdvice, fontWeight = FontWeight.ExtraBold, fontSize = 16.sp, lineHeight = 20.sp)
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
                Text(strings.keyTips, fontWeight = FontWeight.ExtraBold, fontSize = 16.sp, lineHeight = 20.sp)
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
private fun SuggestionRow(text: String, goalLevel: String) {
    val strings = LocalAppStrings.current
    val action = suggestionAction(text, goalLevel, strings)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(Color(0xFFFAFBFA))
            .padding(horizontal = 9.dp, vertical = 7.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = suggestionRowVerticalAlignment(),
    ) {
        SuggestionActionChip(action)
        ExpandableText(
            text = text,
            modifier = Modifier.weight(1f),
            fontSize = 14.sp,
            lineHeight = 18.sp,
            collapsedMaxLines = 3,
        )
    }
}

@Composable
private fun DishAdviceRow(dish: DishAdvice) {
    val language = LocalAppLanguage.current
    Column(
        Modifier
            .fillMaxWidth()
            .padding(vertical = 1.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Text(
                text = dish.title,
                fontWeight = FontWeight.SemiBold,
                fontSize = 14.sp,
                lineHeight = 20.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.width(dishTitleColumnWidth(language, dish.title)),
            )
            FlowRow(
                modifier = Modifier.weight(1f),
                horizontalArrangement = Arrangement.spacedBy(5.dp),
                verticalArrangement = Arrangement.spacedBy(3.dp),
                itemVerticalAlignment = Alignment.CenterVertically,
            ) {
                dish.hints.forEach { hint -> IngredientHintChip(hint) }
            }
        }
        ExpandableText(
            text = dish.advice,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontSize = 12.sp,
            lineHeight = 15.sp,
            collapsedMaxLines = 2,
        )
    }
}

@Composable
private fun ExpandableText(
    text: String,
    modifier: Modifier = Modifier,
    collapsedMaxLines: Int,
    color: Color = Color.Unspecified,
    fontSize: androidx.compose.ui.unit.TextUnit = androidx.compose.ui.unit.TextUnit.Unspecified,
    lineHeight: androidx.compose.ui.unit.TextUnit = androidx.compose.ui.unit.TextUnit.Unspecified,
    fontWeight: FontWeight? = null,
) {
    var expanded by remember(text) { mutableStateOf(false) }
    var canExpand by remember(text) { mutableStateOf(false) }
    val cleanText = text.trim()

    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(2.dp)) {
        Text(
            text = cleanText,
            color = color,
            fontSize = fontSize,
            lineHeight = lineHeight,
            fontWeight = fontWeight,
            maxLines = if (expanded) Int.MAX_VALUE else collapsedMaxLines,
            overflow = if (expanded) TextOverflow.Clip else TextOverflow.Ellipsis,
            onTextLayout = { result ->
                if (!expanded) canExpand = result.hasVisualOverflow
            },
        )
        if (canExpand) {
            val strings = LocalAppStrings.current
            Text(
                text = if (expanded) strings.collapse else strings.expand,
                color = GreenPrimary,
                fontSize = 12.sp,
                lineHeight = 16.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.clickable { expanded = !expanded },
            )
        }
    }
}

internal data class IngredientGroup(val title: String, val items: List<Ingredient>)
internal data class IngredientHint(val label: String, val container: Color, val content: Color)
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
        overflow = TextOverflow.Ellipsis,
        modifier = Modifier
            .clip(RoundedCornerShape(50))
            .background(Color.White.copy(alpha = 0.78f))
            .padding(horizontal = 8.dp, vertical = 3.dp),
    )
}

@Composable
private fun SuggestionActionChip(action: SuggestionAction) {
    val language = LocalAppLanguage.current
    Text(
        text = action.label,
        color = action.content,
        fontSize = 12.sp,
        fontWeight = FontWeight.Bold,
        maxLines = 1,
        textAlign = TextAlign.Center,
        overflow = TextOverflow.Ellipsis,
        modifier = Modifier
            .width(suggestionActionChipWidth(language))
            .heightIn(min = 28.dp)
            .clip(RoundedCornerShape(50))
            .background(action.container)
            .padding(horizontal = 8.dp, vertical = 4.dp),
    )
}

@Composable
private fun IngredientHintChip(hint: IngredientHint) {
    val language = LocalAppLanguage.current
    Text(
        text = MealLanguageText.displayTag(hint.label, language),
        color = hint.content,
        fontSize = 11.sp,
        fontWeight = FontWeight.SemiBold,
        maxLines = 1,
        textAlign = TextAlign.Center,
        overflow = TextOverflow.Ellipsis,
        modifier = Modifier
            .heightIn(min = 22.dp)
            .widthIn(min = ingredientHintMinWidth(language), max = ingredientHintMaxWidth(language))
            .clip(RoundedCornerShape(50))
            .background(hint.container)
            .padding(horizontal = 6.dp, vertical = 2.dp),
    )
}

@Composable
private fun dishAdvices(result: MealAnalysisResult): List<DishAdvice> {
    val strings = LocalAppStrings.current
    val language = LocalAppLanguage.current
    return ingredientGroups(result.ingredients, strings.other).map { group ->
        val title = group.title.ifBlank { result.mealName.trim().ifBlank { strings.thisDish } }
        val hints = dishHints(group).ifEmpty {
            listOf(IngredientHint("适量吃", Color(0xFFF0F2F5), Color(0xFF6D7484)))
        }
        DishAdvice(title, hints, dishAdviceText(hints, language))
    }
}

internal fun dishHints(group: IngredientGroup): List<IngredientHint> {
    val text = buildString {
        append(group.title)
        group.items.forEach {
            append(it.dish)
            append(it.name)
        }
    }
    val hints = mutableListOf<IngredientHint>()
    fun addHint(label: String, container: Color, content: Color) {
        if (hints.none { it.label == label }) hints += IngredientHint(label, container, content)
    }

    val lightCooking = text.hasAny(
        "清蒸",
        "蒸",
        "白灼",
        "清炖",
        "清燉",
        "清炒",
        "steamed",
        "boiled",
        "simmered",
        "蒸し",
        "茹で",
    ) || (text.hasAny("炖", "燉") && text.hasAny("菜", "蔬菜", "vegetable"))
    val panOrRoast = text.hasAny("煎", "烤", "烧烤", "燒烤", "pan-fried", "grilled", "roasted", "焼き")
    val redBraised = text.hasAny("红烧", "紅燒", "卤", "滷", "braised", "teriyaki", "照り焼き")

    if (text.hasAny("油炸", "炸物", "煎炸", "炸", "fried", "deep-fried", "揚げ物", "フライ", "天ぷら")) {
        addHint("油炸", RedSoft, RedPrimary)
    }
    if (
        text.hasAny(
            "红油",
            "紅油",
            "重油",
            "肥肉",
            "五花",
            "黄油",
            "奶油",
            "芝麻酱",
            "沙拉酱",
            "greasy",
            "oily",
            "fatty",
            "butter",
            "cream",
            "mayo",
            "油多め",
            "脂身",
            "バター",
            "クリーム",
            "マヨ",
        ) ||
        panOrRoast ||
        text.hasAny("油", "酱", "醬") && !text.hasAny("油麦菜")
    ) {
        addHint("油脂高", YellowSoft, YellowPrimary)
    }
    if (text.hasAny(
            "重口味",
            "麻辣",
            "重辣",
            "红油",
            "紅油",
            "红烧",
            "紅燒",
            "花椒",
            "辣椒",
            "腌",
            "卤",
            "咸",
            "heavy flavor",
            "strong flavor",
            "spicy",
            "salty",
            "sauce",
            "味濃い",
            "辛い",
            "塩分",
            "ソース",
            "たれ",
        ) ||
        redBraised
    ) {
        addHint("重口味", RedSoft, RedPrimary)
    }
    if (text.hasSugarRisk()) {
        addHint("糖偏高", RedSoft, RedPrimary)
    }

    val hasStaple = text.hasAny(
        "米",
        "饭",
        "飯",
        "面",
        "麵",
        "粉",
        "饼",
        "餅",
        "馒头",
        "饅頭",
        "面包",
        "麵包",
        "玉米",
        "红薯",
        "紅薯",
        "地瓜",
        "薯",
        "土豆",
        "rice",
        "noodle",
        "pasta",
        "bread",
        "potato",
        "dumpling",
        "ご飯",
        "麺",
        "パン",
        "いも",
        "じゃがいも",
    )
    val hasProtein = text.hasAny(
        "鸡",
        "雞",
        "牛",
        "猪",
        "豬",
        "鱼",
        "魚",
        "虾",
        "蝦",
        "蛋",
        "豆腐",
        "豆",
        "肉",
        "奶",
        "chicken",
        "beef",
        "pork",
        "fish",
        "shrimp",
        "egg",
        "tofu",
        "bean",
        "meat",
        "milk",
        "protein",
        "鶏",
        "牛",
        "豚",
        "魚",
        "えび",
        "卵",
        "肉",
        "たんぱく",
    )
    val hasVegetable = text.hasAny(
        "菜",
        "青",
        "叶",
        "葉",
        "瓜",
        "番茄",
        "西红柿",
        "西紅柿",
        "菌",
        "菇",
        "萝卜",
        "蘿蔔",
        "椒",
        "海带",
        "海帶",
        "vegetable",
        "salad",
        "tomato",
        "mushroom",
        "broccoli",
        "leafy",
        "fiber",
        "野菜",
        "サラダ",
        "トマト",
        "きのこ",
        "葉物",
    )

    if (hasStaple) addHint("主食", YellowSoft, YellowPrimary)
    if (hasProtein) addHint("有蛋白", GreenSoft, GreenDeep)
    if (hasVegetable) addHint("有蔬菜", GreenSoft, GreenDeep)
    if (lightCooking) addHint("清淡", GreenSoft, GreenDeep)

    val hasRisk = hints.hasRiskHint()
    when {
        hasVegetable && !hasRisk -> addHint("可多吃", GreenSoft, GreenDeep)
        hasStaple -> addHint("适量吃", Color(0xFFF0F2F5), Color(0xFF6D7484))
        hasProtein && !hasRisk -> addHint("适量吃", Color(0xFFF0F2F5), Color(0xFF6D7484))
    }

    return hints.take(3)
}

private fun dishAdviceText(hints: List<IngredientHint>, language: AppLanguage): String {
    val labels = hints.map { it.label }
    return when (language) {
        AppLanguage.ZhHans -> when {
            "油炸" in labels -> "油炸部分少量尝即可，搭配清淡蔬菜更稳。"
            "油脂高" in labels && "有蛋白" in labels -> "蛋白能补上，但油脂也偏高；优先吃瘦的部分，肥肉和油多处少一点。"
            "油脂高" in labels -> "油脂偏高，控制分量，油多的部分少夹。"
            "重口味" in labels -> "口味偏重，重口味部分少夹，下一餐清淡一点。"
            "糖偏高" in labels -> "甜度偏高，适合浅尝，别叠加其他甜食。"
            "有蔬菜" in labels && "清淡" in labels -> "这道菜是这餐的平衡项，可以多夹一些。"
            "有蔬菜" in labels && "有蛋白" in labels -> "蛋白质和蔬菜搭配不错，可以作为这餐主力。"
            "有蛋白" in labels -> "蛋白质够用，注意分量和烹调油盐。"
            "有蔬菜" in labels -> "蔬菜够用，可以多吃一点平衡这餐。"
            "主食" in labels && "清淡" in labels -> "主食做得清淡，适量吃即可；搭配蛋白和蔬菜更稳。"
            "主食" in labels -> "主食适量就好，搭配蛋白质和蔬菜更稳。"
            else -> "按正常分量吃，留意整体油盐和饱腹感。"
        }
        AppLanguage.ZhHant -> when {
            "油炸" in labels -> "油炸部分少量嚐即可，搭配清淡蔬菜更穩。"
            "油脂高" in labels && "有蛋白" in labels -> "蛋白質能補上，但油脂也偏高；優先吃瘦的部分，肥肉和油多處少一點。"
            "油脂高" in labels -> "油脂偏高，控制份量，油多的部分少夾。"
            "重口味" in labels -> "口味偏重，重口味部分少夾，下一餐清淡一點。"
            "糖偏高" in labels -> "甜度偏高，適合淺嚐，別疊加其他甜食。"
            "有蔬菜" in labels && "清淡" in labels -> "這道菜是這餐的平衡項，可以多夾一些。"
            "有蔬菜" in labels && "有蛋白" in labels -> "蛋白質和蔬菜搭配不錯，可以作為這餐主力。"
            "有蛋白" in labels -> "蛋白質夠用，注意份量和烹調油鹽。"
            "有蔬菜" in labels -> "蔬菜夠用，可以多吃一點平衡這餐。"
            "主食" in labels && "清淡" in labels -> "主食做得清淡，適量吃即可；搭配蛋白質和蔬菜更穩。"
            "主食" in labels -> "主食適量就好，搭配蛋白質和蔬菜更穩。"
            else -> "按正常份量吃，留意整體油鹽和飽足感。"
        }
        AppLanguage.En -> when {
            "油炸" in labels -> "Keep fried parts to a small taste and pair with lighter vegetables."
            "油脂高" in labels && "有蛋白" in labels -> "Protein is useful, but oil is high; prioritize leaner pieces and keep oily parts small."
            "油脂高" in labels -> "Oil is on the high side; control portions and take less of the oily parts."
            "重口味" in labels -> "Seasoning is heavy; take a smaller share and keep the next meal lighter."
            "糖偏高" in labels -> "Sweetness is high; taste a little and avoid adding other sweets."
            "有蔬菜" in labels && "清淡" in labels -> "This helps balance the meal, so it can be a larger share."
            "有蔬菜" in labels && "有蛋白" in labels -> "Protein and vegetables are a solid base for this meal."
            "有蛋白" in labels -> "Protein is covered; watch portions and cooking oil or salt."
            "有蔬菜" in labels -> "Vegetables help balance this meal; they can be the larger part."
            "主食" in labels && "清淡" in labels -> "The staple is simply cooked; keep it moderate and pair with protein or vegetables."
            "主食" in labels -> "Keep staples moderate and pair them with protein and vegetables."
            else -> "Eat a normal portion and watch overall oil, salt, and fullness."
        }
        AppLanguage.Ja -> when {
            "油炸" in labels -> "揚げ物は少し味見程度にして、あっさりした野菜を合わせると安定します。"
            "油脂高" in labels && "有蛋白" in labels -> "たんぱく質は取れますが油も多めです。脂身や油の多い部分は控えめにしましょう。"
            "油脂高" in labels -> "油が多めなので、量を控えめにして油の多い部分は少なめにしましょう。"
            "重口味" in labels -> "味が濃いめです。濃い味の部分は控え、次の食事は軽めにしましょう。"
            "糖偏高" in labels -> "甘さが強めです。少しだけにして他の甘い物は足さないのが無難です。"
            "有蔬菜" in labels && "清淡" in labels -> "この食事のバランス役にできます。少し多めに食べてもよいです。"
            "有蔬菜" in labels && "有蛋白" in labels -> "たんぱく質と野菜の組み合わせがあり、この食事の軸にできます。"
            "有蛋白" in labels -> "たんぱく質は取れます。量と調理の油塩に注意しましょう。"
            "有蔬菜" in labels -> "野菜があるので、この食事のバランス役にできます。"
            "主食" in labels && "清淡" in labels -> "主食はシンプルな調理です。適量にして、たんぱく質や野菜と合わせると安定します。"
            "主食" in labels -> "主食は適量にし、たんぱく質と野菜を合わせると安定します。"
            else -> "普通量を目安に、油塩と満腹感を見ながら食べましょう。"
        }
    }
}

private fun List<IngredientHint>.hasRiskHint(): Boolean =
    any { it.label in setOf("油炸", "油脂高", "重口味", "糖偏高") }

internal fun suggestionActionChipWidth(language: AppLanguage) = when (language) {
    AppLanguage.En -> 76.dp
    AppLanguage.Ja -> 80.dp
    else -> 62.dp
}

internal fun suggestionRowVerticalAlignment(): Alignment.Vertical = Alignment.CenterVertically

internal fun dishTitleColumnWidth(language: AppLanguage, title: String = "") = when {
    title.hasLatinText() || language == AppLanguage.En -> 122.dp
    language == AppLanguage.Ja -> 96.dp
    else -> 82.dp
}

internal fun ingredientHintMinWidth(language: AppLanguage) = when (language) {
    AppLanguage.En, AppLanguage.Ja -> 70.dp
    else -> 52.dp
}

private fun ingredientHintMaxWidth(language: AppLanguage) = when (language) {
    AppLanguage.En -> 106.dp
    AppLanguage.Ja -> 96.dp
    else -> 92.dp
}

private fun String.hasLatinText(): Boolean = any { it in 'A'..'Z' || it in 'a'..'z' }

private fun String.hasAny(vararg keywords: String): Boolean = keywords.any { contains(it, ignoreCase = true) }

private fun String.hasSugarRisk(): Boolean {
    val lower = lowercase()
    if (
        hasAny(
            "糖",
            "蜜",
            "奶茶",
            "饮料",
            "飲料",
            "甜点",
            "甜點",
            "甜品",
            "甜饮",
            "甜飲",
            "甜食",
            "sugar",
            "sugary",
            "dessert",
            "honey",
            "soda",
            "cake",
            "ice cream",
            "sweet drink",
            "sweets",
            "甘い飲み物",
            "デザート",
            "ケーキ",
            "アイス",
        )
    ) {
        return true
    }

    return lower.contains("sweet") && !lower.contains("sweet potato")
}

private fun suggestionAction(text: String, goalLevel: String, strings: AppStrings): SuggestionAction {
    val clean = text.trim()
    return when {
        clean.hasAny("尝一小口", "浅尝", "taste", "bite", "少し", "一口") ->
            SuggestionAction(strings.tasteOnly, RedSoft, RedPrimary)
        clean.hasAny("严格控量", "控量", "portion", "control", "量を", "量控え") ->
            SuggestionAction(strings.portionControl, RedSoft, RedPrimary)
        clean.hasAny("避免", "避开", "不要", "别吃", "不建议", "少食用", "avoid", "skip", "控え") ->
            SuggestionAction(strings.lessEat, RedSoft, RedPrimary)
        clean.hasAny("减少", "少", "控制", "一半", "半份", "减半", "七分", "几口", "油", "盐", "糖", "炸", "less", "reduce", "sauce", "soup", "fried", "sugar", "少なめ") ->
            SuggestionAction(strings.lessEat, OrangeSoft, OrangePrimary)
        clean.hasAny("增加", "补充", "加", "蔬菜", "纤维", "蛋白", "喝水", "add", "vegetable", "protein", "野菜", "たんぱく") ->
            SuggestionAction(strings.addSome, GreenSoft, GreenDeep)
        clean.hasAny("适量多吃", "可以多", "eat more", "多め") ->
            SuggestionAction(strings.eatMore, GreenSoft, GreenDeep)
        clean.hasAny("搭配", "替换", "改成", "选择", "下餐", "pair", "swap", "next meal", "合わせ", "次の") ->
            SuggestionAction(strings.pairBetter, GreenSoft, GreenDeep)
        goalLevel == "good" ->
            SuggestionAction(strings.normalEat, GreenSoft, GreenDeep)
        goalLevel == "poor" ->
            SuggestionAction(strings.tasteOnly, RedSoft, RedPrimary)
        else ->
            SuggestionAction(strings.moderate, Color(0xFFF0F2F5), Color(0xFF6D7484))
    }
}

private fun adviceStyle(advice: String, level: String?, strings: AppStrings): AdviceStyle = when {
    advice.hasAny("尝", "嚐", "严格", "嚴格", "taste", "portion control", "一口", "控える") || level == "poor" ->
        AdviceStyle(RedSoft, RedPrimary, strings.howToEat)
    advice.hasAny("适量多吃", "適量多吃", "eat a bit more", "多め") || level == "good" ->
        AdviceStyle(GreenSoft, GreenDeep, strings.howToEat)
    else -> AdviceStyle(YellowSoft, YellowPrimary, strings.howToEat)
}

private fun overallScore(advice: String, level: String?): Int = when {
    level == "good" || advice.hasAny("适量多吃", "適量多吃", "可以常吃", "推荐", "recommended", "bit more", "多め") -> 5
    level == "poor" || advice.hasAny("尝一小口", "嚐一小口", "只能尝", "不建议", "taste only", "一口だけ") -> 1
    advice.hasAny("严格控量", "嚴格控量", "控量", "portion control", "量をしっかり") -> 2
    level == "partial" || advice.hasAny("适量", "適量", "moderate", "適量なら") -> 3
    else -> 3
}

private fun starText(score: Int): String {
    val safeScore = score.coerceIn(1, 5)
    return "★".repeat(safeScore) + "☆".repeat(5 - safeScore)
}

private fun ingredientGroups(ingredients: List<Ingredient>, otherLabel: String): List<IngredientGroup> {
    val grouped = ingredients.groupBy { it.dish.trim() }
    if (grouped.size <= 1 && grouped.keys.firstOrNull().isNullOrBlank()) {
        return listOf(IngredientGroup("", ingredients))
    }
    return grouped.map { (dish, items) ->
        IngredientGroup(dish.ifBlank { otherLabel }, items)
    }
}

@Composable
private fun SoftCard(content: @Composable ColumnScope.() -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, LineSoft.copy(alpha = 0.62f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
    ) {
        Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp), content = content)
    }
}
