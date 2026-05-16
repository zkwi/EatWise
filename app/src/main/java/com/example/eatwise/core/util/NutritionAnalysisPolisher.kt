package com.example.eatwise.core.util

import com.example.eatwise.core.i18n.AppLanguage
import com.example.eatwise.domain.model.NutritionAnalysisResult
import com.example.eatwise.domain.model.NutritionItem
import java.util.Locale

object NutritionAnalysisPolisher {
    fun polish(result: NutritionAnalysisResult, language: AppLanguage = AppLanguage.ZhHans): NutritionAnalysisResult {
        val unknown = localizedUnknown(language)
        val calorieRange = normalizeEstimate(result.calorieRange, unknown)
        val items = result.items
            .map { item ->
                val label = compactSentence(item.label.ifBlank { localizedNutritionLabel(language) }, maxLabelLength(language))
                val estimate = normalizeEstimate(item.estimate, unknown)
                val note = compactSentence(cleanNote(item.note), maxNoteLength(language))
                val normalizedLabel = normalizeNutritionLabel(label, estimate, note, language)
                item.copy(
                    label = normalizedLabel,
                    level = normalizeLevel(item.level),
                    estimate = normalizeSemanticEstimate(normalizedLabel, estimate, unknown),
                    note = note,
                )
            }
            .filter { it.label.isNotBlank() || it.estimate.isNotBlank() || it.note.isNotBlank() }
            .distinctBy { it.label.trim().lowercase(Locale.ROOT) }
            .take(5)

        val suggestions = result.suggestions
            .map { compactSuggestion(it, language) }
            .filter { it.isNotBlank() }
            .distinct()
            .take(2)
            .ifEmpty { fallbackSuggestions(result, items, language) }

        return result.copy(
            mealName = result.mealName.ifBlank { localizedMealName(language) },
            calorieRange = calorieRange,
            calorieEquivalent = if (calorieRange == unknown) {
                ""
            } else {
                compactSentence(result.calorieEquivalent, maxEquivalentLength(language))
            },
            basis = compactSentence(cleanNote(result.basis), maxBasisLength(language)),
            items = items,
            suggestions = suggestions,
            disclaimer = localizedDisclaimer(language),
        )
    }

    private fun compactSuggestion(text: String, language: AppLanguage): String {
        val raw = text.trim().trim('，', '。', '、', '.', ' ')
        if (raw.isBlank()) return ""

        suggestionByKeyword(raw, language)?.let { return it }

        val clean = raw
            .removeReasonLead()
            .removePrefix("建议")
            .removePrefix("建議")
            .removePrefix("可以")
            .removePrefix("尽量")
            .removePrefix("盡量")
            .removePrefix("Please ")
            .removePrefix("Try to ")
            .removePrefix("Consider ")
            .replace("食用时", "")
            .replace("食用時", "")
            .replace("大量补充", "加")
            .replace("大量補充", "加")
            .replace("避免食用", "少吃")
            .replace("避免吃", "少吃")
            .replace("不要食用", "少吃")
            .replace("不要吃", "少吃")
            .replace("完全避免", "少吃")
            .trim('，', '。', '、', '.', ' ')

        return compactSentence(clean, maxSuggestionLength(language))
    }

    private fun suggestionByKeyword(text: String, language: AppLanguage): String? = when {
        text.hasAny("面衣", "外壳", "外殼", "breading", "coating", "衣") &&
            text.hasAny("虾", "蝦", "shrimp", "えび", "海老", "炸") ->
            localizedSuggestion(language, "去掉炸虾面衣", "去掉炸蝦麵衣", "Remove shrimp breading", "えびの衣を外す")
        text.hasAny("盘底积油", "盤底積油", "积油", "積油", "pooled oil", "油だまり") ->
            localizedSuggestion(language, "避开盘底积油", "避開盤底積油", "Avoid pooled oil", "底の油を避ける")
        text.hasAny("蔬菜", "纤维", "纖維", "vegetable", "fiber", "野菜", "食物繊維") &&
            text.hasAny("缺", "不足", "增加", "補充", "补充", "加", "add", "more", "足す", "追加") ->
            localizedSuggestion(language, "下餐加一份蔬菜", "下餐加一份蔬菜", "Add vegetables next meal", "次は野菜を一品追加")
        text.hasAny("清蒸鱼", "清蒸魚", "豆腐", "steamed fish", "tofu", "蒸し魚", "豆腐") ->
            localizedSuggestion(language, "下餐选清淡蛋白", "下餐選清淡蛋白", "Choose lean protein next", "次は軽めのたんぱく質")
        text.hasAny("米饭", "米飯", "面条", "麵", "主食", "碳水", "rice", "noodle", "staple", "carb", "ご飯", "麺", "炭水化物") &&
            text.hasAny("少", "减少", "減少", "控制", "reduce", "less", "smaller", "控え") ->
            localizedSuggestion(language, "主食少吃几口", "主食少吃幾口", "Take fewer staple bites", "主食を少し減らす")
        text.hasAny("油炸", "炸物", "煎炸", "fried", "揚げ物") ->
            localizedSuggestion(language, "油炸少吃几口", "油炸少吃幾口", "Keep fried food small", "揚げ物は数口まで")
        text.hasAny("红油", "紅油", "重油", "油腻", "油膩", "greasy", "oily", "oil-heavy", "油多め") ->
            localizedSuggestion(language, "重油部分少夹", "重油部分少夾", "Take less oily parts", "油の多い部分は少なめ")
        text.hasAny("糖", "甜饮", "甜飲", "甜品", "dessert", "sweet drink", "sugar", "甘い") ->
            localizedSuggestion(language, "甜饮甜品少点", "甜飲甜品少點", "Keep sweets small", "甘い物は少なめに")
        text.hasAny("盐", "鹽", "钠", "鈉", "咸", "salty", "sodium", "塩分") ->
            localizedSuggestion(language, "重口味少几口", "重口味少幾口", "Keep salty bites small", "濃い味は控えめに")
        text.hasAny("汤", "湯", "汤汁", "湯汁", "broth", "soup", "汁") ->
            localizedSuggestion(language, "汤汁少喝几口", "湯汁少喝幾口", "Drink less broth", "汁は控えめに")
        text.hasAny("酱", "醬", "蘸料", "sauce", "dressing", "ソース", "たれ") ->
            localizedSuggestion(language, "酱料少放", "醬料少放", "Use less sauce", "ソースは少なめに")
        else -> null
    }

    private fun fallbackSuggestions(
        result: NutritionAnalysisResult,
        items: List<NutritionItem>,
        language: AppLanguage,
    ): List<String> {
        val text = buildString {
            append(result.mealName)
            append(result.basis)
            append(items.joinToString("") { "${it.label}${it.level}${it.note}" })
        }
        return listOfNotNull(
            if (text.hasAny("油炸", "炸", "油脂", "fat", "oil", "揚げ")) {
                localizedSuggestion(language, "油炸重油少几口", "油炸重油少幾口", "Keep oily food small", "揚げ物や油は控えめに")
            } else {
                null
            },
            if (text.hasAny("蔬菜", "纤维", "纖維", "vegetable", "fiber", "野菜")) {
                localizedSuggestion(language, "加一份蔬菜", "加一份蔬菜", "Add a vegetable serving", "野菜を一品追加")
            } else {
                null
            },
        ).take(2).ifEmpty {
            listOf(localizedSuggestion(language, "吃到七八分饱", "吃到七八分飽", "Stop when comfortably full", "腹八分目で止める"))
        }
    }

    private fun normalizeEstimate(value: String, unknown: String): String {
        val clean = value.trim()
            .replace(Regex("""\s*[-–—]\s*"""), "-")
            .replace(Regex("""\s+(kcal|g|mg|克|千卡|大卡)""", RegexOption.IGNORE_CASE), " $1")
            .trim()
        if (clean.isBlank()) return unknown
        if (looksLikeDecimalEstimate(clean)) return unknown
        if (looksLikeExactNumber(clean)) return unknown
        return clean
    }

    private fun normalizeNutritionLabel(
        label: String,
        estimate: String,
        note: String,
        language: AppLanguage,
    ): String {
        if (isCombinedVegetableFiberLabel(label)) {
            return if (looksLikeGramEstimate(estimate) || note.hasAny("纤维", "纖維", "fiber", "食物繊維")) {
                localizedDietaryFiberLabel(language)
            } else {
                localizedVegetableAmountLabel(language)
            }
        }
        if (isSodiumOrSaltLabel(label)) return normalizeSodiumSaltLabel(label, estimate, note, language)
        if (isCarbOrStapleLabel(label)) return normalizeCarbStapleLabel(label, estimate, note, language)
        if (isFatOrOilLabel(label)) return normalizeFatOilLabel(label, estimate, note, language)
        return label
    }

    private fun normalizeSemanticEstimate(label: String, estimate: String, unknown: String): String =
        if (looksLikeGramEstimate(estimate) && shouldAvoidGramEstimate(label)) {
            unknown
        } else {
            estimate
        }

    private fun shouldAvoidGramEstimate(label: String): Boolean =
        (isVegetableAmountLabel(label) && !isFiberLabel(label)) ||
            (isStapleLabel(label) && !isCarbLabel(label)) ||
            (isOilLabel(label) && !isFatLabel(label))

    private fun normalizeSodiumSaltLabel(
        label: String,
        estimate: String,
        note: String,
        language: AppLanguage,
    ): String {
        val sodiumCue = looksLikeMilligramEstimate(estimate) || note.hasAny("钠", "鈉", "sodium", "ナトリウム")
        val saltCue = looksLikeGramEstimate(estimate) || note.hasAny("盐", "鹽", "salt", "塩分", "咸")
        return when {
            sodiumCue && !saltCue -> localizedSodiumLabel(language)
            saltCue && !sodiumCue -> localizedSaltLabel(language)
            looksLikeMilligramEstimate(estimate) -> localizedSodiumLabel(language)
            looksLikeGramEstimate(estimate) -> localizedSaltLabel(language)
            isSodiumLabel(label) && !isSaltLabel(label) -> localizedSodiumLabel(language)
            else -> localizedSaltLabel(language)
        }
    }

    private fun normalizeCarbStapleLabel(
        label: String,
        estimate: String,
        note: String,
        language: AppLanguage,
    ): String {
        val carbCue = isCarbLabel(label) || note.hasAny("碳水", "carb", "carbohydrate", "炭水化物")
        val stapleCue = isStapleLabel(label)
        return when {
            carbCue && (!stapleCue || looksLikeGramEstimate(estimate)) -> localizedCarbohydrateLabel(language)
            looksLikeGramEstimate(estimate) && !stapleCue -> localizedCarbohydrateLabel(language)
            else -> localizedStapleAmountLabel(language)
        }
    }

    private fun normalizeFatOilLabel(
        label: String,
        estimate: String,
        note: String,
        language: AppLanguage,
    ): String {
        val fatCue = isFatLabel(label) || note.hasAny("脂肪", "fat", "lipid", "脂質")
        val oilCue = isOilLabel(label)
        return when {
            fatCue && (!oilCue || looksLikeGramEstimate(estimate)) -> localizedFatLabel(language)
            looksLikeGramEstimate(estimate) && !oilCue -> localizedFatLabel(language)
            else -> localizedOilAmountLabel(language)
        }
    }

    private fun looksLikeExactNumber(value: String): Boolean {
        val clean = value.replace(",", "")
        val hasRange = clean.contains("-") || clean.contains("~") || clean.contains("到") || clean.contains("至")
        if (hasRange) return false
        return Regex("""(?i)\d+(?:\.\d+)?\s*(kcal|g|mg|克|千卡|大卡)""").containsMatchIn(clean)
    }

    private fun looksLikeDecimalEstimate(value: String): Boolean =
        Regex("""\d+\.\d+""").containsMatchIn(value)

    private fun looksLikeGramEstimate(value: String): Boolean =
        Regex("""(?i)(?:^|[^\p{L}])\d+(?:\.\d+)?(?:\s*[-~到至]\s*\d+(?:\.\d+)?)?\s*(?:g\b|克)""")
            .containsMatchIn(value)

    private fun looksLikeMilligramEstimate(value: String): Boolean =
        Regex("""(?i)(?:^|[^\p{L}])\d[\d,]*(?:\.\d+)?(?:\s*[-~到至]\s*\d[\d,]*(?:\.\d+)?)?\s*(?:mg\b|毫克)""")
            .containsMatchIn(value)

    private fun isCombinedVegetableFiberLabel(label: String): Boolean {
        val compact = label.replace(Regex("""\s+"""), "").replace('／', '/')
        return compact.hasAny(
            "蔬菜/纤维",
            "蔬菜/纖維",
            "蔬菜纤维",
            "蔬菜纖維",
            "蔬菜和纤维",
            "蔬菜和纖維",
            "蔬菜与纤维",
            "蔬菜與纖維",
            "vegetable/fiber",
            "vegetables/fiber",
            "vegetable+fiber",
            "vegetables+fiber",
            "vegetable&fiber",
            "vegetables&fiber",
            "vegetableandfiber",
            "vegetablesandfiber",
            "vegetablefiber",
            "vegetablesfiber",
            "野菜/食物繊維",
            "野菜と食物繊維",
        )
    }

    private fun isVegetableAmountLabel(label: String): Boolean =
        label.hasAny("蔬菜", "青菜", "蔬菜量", "蔬菜份", "vegetable", "vegetables", "野菜")

    private fun isFiberLabel(label: String): Boolean =
        label.hasAny("纤维", "纖維", "fiber", "食物繊維")

    private fun isSodiumOrSaltLabel(label: String): Boolean =
        isSodiumLabel(label) || isSaltLabel(label)

    private fun isSodiumLabel(label: String): Boolean =
        label.hasAny("钠", "鈉", "sodium", "ナトリウム")

    private fun isSaltLabel(label: String): Boolean =
        label.hasAny("盐", "鹽", "salt", "塩分")

    private fun isCarbOrStapleLabel(label: String): Boolean =
        isCarbLabel(label) || isStapleLabel(label)

    private fun isCarbLabel(label: String): Boolean =
        label.hasAny("碳水", "carb", "carbohydrate", "炭水化物")

    private fun isStapleLabel(label: String): Boolean =
        label.hasAny("主食", "staple", "staples")

    private fun isFatOrOilLabel(label: String): Boolean =
        isFatLabel(label) || isOilLabel(label)

    private fun isFatLabel(label: String): Boolean =
        label.hasAny("脂肪", "fat", "lipid", "脂質")

    private fun isOilLabel(label: String): Boolean =
        label.hasAny("油脂", "油量", "油", "oil", "oily")

    private fun normalizeLevel(level: String): String = when (level.trim().lowercase(Locale.ROOT)) {
        "low", "moderate", "high", "unknown" -> level.trim().lowercase(Locale.ROOT)
        else -> "unknown"
    }

    private fun cleanNote(text: String): String =
        text.trim()
            .removeReasonLead()
            .removePrefix("因为")
            .removePrefix("因為")
            .removePrefix("由于")
            .removePrefix("由於")
            .removePrefix("Because ")
            .removePrefix("Because")
            .trim('，', '。', '、', '.', ' ')

    private fun String.removeReasonLead(): String {
        val clean = trim()
        val cut = listOf('，', '。', '；', ';', ',')
            .map { clean.indexOf(it) }
            .filter { it in 2..28 }
            .minOrNull()
        val firstClause = if (cut != null) clean.take(cut) else clean
        val isReasonLead = firstClause.hasAny("这餐", "此餐", "本餐", "該餐", "该餐", "建议", "建議", "because", "goal", "目标", "目標", "違背", "违背")
        return if (isReasonLead && cut != null) clean.drop(cut + 1).trim() else clean
    }

    private fun compactSentence(text: String, maxLength: Int): String {
        val clean = text.trim().trim('，', '。', '、', '.', ' ')
        if (clean.length <= maxLength) return clean
        val head = clean.take(maxLength)
        val breakAt = listOf('。', '；', ';', '，', ',', '、')
            .map { head.lastIndexOf(it) }
            .filter { it >= maxLength / 2 }
            .maxOrNull()
        return (if (breakAt != null) head.take(breakAt) else head).trim('，', '。', '、', '.', ' ')
    }

    private fun localizedMealName(language: AppLanguage): String = when (language) {
        AppLanguage.ZhHans -> "营养估算"
        AppLanguage.ZhHant -> "營養估算"
        AppLanguage.En -> "Nutrition estimate"
        AppLanguage.Ja -> "栄養推定"
    }

    private fun localizedNutritionLabel(language: AppLanguage): String = when (language) {
        AppLanguage.ZhHans -> "营养结构"
        AppLanguage.ZhHant -> "營養結構"
        AppLanguage.En -> "Nutrition"
        AppLanguage.Ja -> "栄養構成"
    }

    private fun localizedVegetableAmountLabel(language: AppLanguage): String = when (language) {
        AppLanguage.ZhHans -> "蔬菜量"
        AppLanguage.ZhHant -> "蔬菜量"
        AppLanguage.En -> "Vegetable amount"
        AppLanguage.Ja -> "野菜量"
    }

    private fun localizedDietaryFiberLabel(language: AppLanguage): String = when (language) {
        AppLanguage.ZhHans -> "膳食纤维"
        AppLanguage.ZhHant -> "膳食纖維"
        AppLanguage.En -> "Dietary fiber"
        AppLanguage.Ja -> "食物繊維"
    }

    private fun localizedSodiumLabel(language: AppLanguage): String = when (language) {
        AppLanguage.ZhHans -> "钠"
        AppLanguage.ZhHant -> "鈉"
        AppLanguage.En -> "Sodium"
        AppLanguage.Ja -> "ナトリウム"
    }

    private fun localizedSaltLabel(language: AppLanguage): String = when (language) {
        AppLanguage.ZhHans -> "盐分"
        AppLanguage.ZhHant -> "鹽分"
        AppLanguage.En -> "Salt"
        AppLanguage.Ja -> "塩分"
    }

    private fun localizedCarbohydrateLabel(language: AppLanguage): String = when (language) {
        AppLanguage.ZhHans -> "碳水"
        AppLanguage.ZhHant -> "碳水"
        AppLanguage.En -> "Carbohydrates"
        AppLanguage.Ja -> "炭水化物"
    }

    private fun localizedStapleAmountLabel(language: AppLanguage): String = when (language) {
        AppLanguage.ZhHans -> "主食量"
        AppLanguage.ZhHant -> "主食量"
        AppLanguage.En -> "Staple amount"
        AppLanguage.Ja -> "主食量"
    }

    private fun localizedFatLabel(language: AppLanguage): String = when (language) {
        AppLanguage.ZhHans -> "脂肪"
        AppLanguage.ZhHant -> "脂肪"
        AppLanguage.En -> "Fat"
        AppLanguage.Ja -> "脂質"
    }

    private fun localizedOilAmountLabel(language: AppLanguage): String = when (language) {
        AppLanguage.ZhHans -> "油脂量"
        AppLanguage.ZhHant -> "油脂量"
        AppLanguage.En -> "Oil amount"
        AppLanguage.Ja -> "油量"
    }

    private fun localizedUnknown(language: AppLanguage): String = when (language) {
        AppLanguage.ZhHans -> "无法判断"
        AppLanguage.ZhHant -> "無法判斷"
        AppLanguage.En -> "Unable to judge"
        AppLanguage.Ja -> "判断できません"
    }

    private fun localizedDisclaimer(language: AppLanguage): String = when (language) {
        AppLanguage.ZhHans -> "热量和营养素克数是基于常见份量的粗略区间，不替代称重记录。"
        AppLanguage.ZhHant -> "熱量和營養素克數是基於常見份量的粗略區間，不替代稱重記錄。"
        AppLanguage.En -> "Calories and nutrient grams are rough ranges from common portions, not a weighed record."
        AppLanguage.Ja -> "カロリーと栄養素のグラム数は一般的な量からの大まかな範囲で、計量記録の代わりではありません。"
    }

    private fun localizedSuggestion(language: AppLanguage, zhHans: String, zhHant: String, en: String, ja: String): String =
        when (language) {
            AppLanguage.ZhHans -> zhHans
            AppLanguage.ZhHant -> zhHant
            AppLanguage.En -> en
            AppLanguage.Ja -> ja
        }

    private fun maxLabelLength(language: AppLanguage): Int = if (language == AppLanguage.En) 28 else 10

    private fun maxNoteLength(language: AppLanguage): Int = if (language == AppLanguage.En) 72 else 28

    private fun maxBasisLength(language: AppLanguage): Int = if (language == AppLanguage.En) 120 else 54

    private fun maxEquivalentLength(language: AppLanguage): Int = if (language == AppLanguage.En) 96 else 42

    private fun maxSuggestionLength(language: AppLanguage): Int = if (language == AppLanguage.En) 64 else 26

    private fun String.hasAny(vararg keywords: String): Boolean =
        keywords.any { contains(it, ignoreCase = true) }
}
