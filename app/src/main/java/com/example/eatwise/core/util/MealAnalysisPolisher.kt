package com.example.eatwise.core.util

import com.example.eatwise.core.i18n.AppLanguage
import com.example.eatwise.core.i18n.MealLanguageText
import com.example.eatwise.domain.model.MealAnalysisResult
import java.util.Locale

object MealAnalysisPolisher {
    fun polish(result: MealAnalysisResult, language: AppLanguage = AppLanguage.ZhHans): MealAnalysisResult {
        if (language != AppLanguage.ZhHans) return polishLocalized(result, language)

        val suggestions = result.suggestions
            .map(::normalizeSuggestion)
            .filter { it.isNotBlank() }
            .distinct()
            .take(3)
            .ifEmpty { listOf(defaultLocalizedSuggestion(language)) }
            .ifEmpty { fallbackSuggestions(result) }

        return result.copy(
            mealName = localizedMealName(result.mealName, language),
            summary = compactSentence(result.summary, 58),
            goalMatch = result.goalMatch.copy(
                reason = compactSentence(result.goalMatch.reason, 52),
            ),
            eatingAdvice = normalizeEatingAdvice(result.eatingAdvice, result.goalMatch.level, analysisText(result)),
            suggestions = suggestions,
            tags = normalizedTags(result),
            disclaimer = MealLanguageText.disclaimer(language),
        )
    }

    private fun localizedMealName(value: String, language: AppLanguage): String {
        val clean = value.trim()
        if (clean.isNotBlank() && clean !in setOf("未命名餐食", "Unnamed meal")) return clean
        return when (language) {
            AppLanguage.ZhHans -> "餐食分析"
            AppLanguage.ZhHant -> "餐食分析"
            AppLanguage.En -> "Meal analysis"
            AppLanguage.Ja -> "食事分析"
        }
    }

    private fun defaultLocalizedSuggestion(language: AppLanguage): String = when (language) {
        AppLanguage.ZhHans -> "吃到七八分饱"
        AppLanguage.ZhHant -> "吃到七八分飽"
        AppLanguage.En -> "Stop when comfortably full"
        AppLanguage.Ja -> "腹八分目で止める"
    }

    private fun polishLocalized(result: MealAnalysisResult, language: AppLanguage): MealAnalysisResult {
        val suggestions = result.suggestions
            .map {
                localizedSuggestionByKeyword(it, language)
                    .ifBlank { compactSentence(it.trim(), if (language == AppLanguage.En) 90 else 42) }
            }
            .filter { it.isNotBlank() }
            .filterNot { it.isMeaninglessSuggestion() }
            .distinct()
            .take(3)
            .ifEmpty { listOf(defaultLocalizedSuggestion(language)) }

        val tags = result.tags
            .map { MealLanguageText.compactTag(it, language) }
            .filter { it.isNotBlank() }
            .filterNot { it.isMeaninglessTag() }
            .distinct()
            .take(4)
            .ifEmpty { listOf(MealLanguageText.displayTag("适量吃", language)) }

        return result.copy(
            mealName = localizedMealName(result.mealName, language),
            summary = compactSentence(result.summary, if (language == AppLanguage.En) 112 else 58),
            goalMatch = result.goalMatch.copy(
                reason = compactSentence(result.goalMatch.reason, if (language == AppLanguage.En) 104 else 52),
            ),
            eatingAdvice = MealLanguageText.displayAdvice(result.eatingAdvice.ifBlank { MealLanguageText.displayAdvice("可以适量吃", language) }, language),
            suggestions = suggestions,
            tags = tags,
            disclaimer = MealLanguageText.disclaimer(language),
        )
    }

    private fun normalizedTags(result: MealAnalysisResult): List<String> {
        val tags = result.tags.map(::normalizeTag) + derivedTags(result)
        return resolveTagConflicts(tags)
            .filter { it.isNotBlank() }
            .filterNot { it.isMeaninglessTag() }
            .distinct()
            .take(4)
    }

    private fun normalizeTag(tag: String): String {
        val clean = tag.trim()
            .trim('，', '。', '、', ' ', '#')
            .removePrefix("标签：")
            .removePrefix("标签")

        if (clean.isMeaninglessTag()) return ""

        return when {
            clean.hasAny("蛋白") && clean.hasAny("高", "足", "充足") -> "蛋白足"
            clean.hasAny("蛋白") && clean.hasAny("低", "少", "不足") -> "蛋白少"
            clean.hasAny("蛋白来源", "常规蛋白") -> ""
            clean.hasAny("热量", "能量", "卡路里") && clean.hasAny("高", "多", "偏高") -> "负担高"
            clean.hasAny("热量", "能量", "卡路里") && clean.hasAny("低", "轻") -> "轻负担"
            clean.hasAny("油炸", "炸物", "煎炸") -> "油炸"
            clean.hasAny("重口味", "口味重", "重辣", "麻辣", "红油") -> "重口味"
            clean.hasAny("油盐") && clean.hasAny("高", "偏高", "重") -> "油盐高"
            clean.hasAny("油脂调味", "重油", "油腻") -> "油脂高"
            clean.hasAny("高脂注意", "油脂注意") -> "油脂高"
            clean.hasAny("油", "脂肪", "脂") && clean.hasAny("高", "多", "重", "偏高") -> "油脂高"
            clean.hasAny("糖", "甜") && clean.hasAny("低", "少") -> "少糖"
            clean.hasAny("糖", "甜", "奶茶", "饮料") -> "糖偏高"
            clean.hasAny("盐", "钠", "咸", "腌") -> "钠偏高"
            clean.hasAny("碳水", "主食", "米饭", "面") && clean.hasAny("高", "多", "偏高") -> "碳水多"
            clean.hasAny("蔬菜", "纤维") && clean.hasAny("少", "低", "不足", "缺") -> "蔬菜少"
            clean.hasAny("蔬菜", "纤维") -> "有蔬菜"
            clean.hasAny("胆固醇", "血脂", "高脂", "控脂") -> "少油控脂"
            clean.hasAny("严格控量", "需要控量", "少量", "浅尝") -> "控量"
            clean.hasAny("减脂", "减重", "减肥") && clean.hasAny("友好", "适合") -> "减脂友好"
            clean.hasAny("减脂", "减重", "减肥") -> "减脂谨慎"
            else -> compactSentence(clean.replace("偏高", "高").replace("较高", "高"), 6)
        }
    }

    private fun resolveTagConflicts(tags: List<String>): List<String> {
        val distinctTags = tags.filter { it.isNotBlank() }.distinct()
        val riskTags = setOf("负担高", "油脂高", "油炸", "糖偏高", "钠偏高", "油盐高", "蔬菜少", "重口味", "少油控脂", "减脂谨慎", "控量")
        val hasRisk = distinctTags.any { it in riskTags }
        return distinctTags
            .filterNot { hasRisk && it == "轻负担" }
            .filterNot { "油炸" in distinctTags && it == "油脂高" }
            .filterNot { "油盐高" in distinctTags && it in setOf("油脂高", "钠偏高") }
            .filterNot { "钠偏高" in distinctTags && it == "重口味" }
    }

    private fun derivedTags(result: MealAnalysisResult): List<String> {
        val text = analysisText(result)
        val tags = mutableListOf<String>()
        if (text.hasAny("红油", "重油", "油腻", "肥肉", "五花")) tags += "油脂高"
        if (text.hasAny("油炸", "炸", "煎炸")) tags += "油炸"
        if (text.hasAny("重口味", "口味重", "麻辣", "红油", "花椒", "辣椒")) tags += "重口味"
        if (text.hasAny("盐", "钠", "咸", "汤底", "腌")) tags += "钠偏高"
        if (text.hasAny("糖", "甜", "奶茶", "饮料")) tags += "糖偏高"
        if (text.hasAny("蔬菜少", "蔬菜不足", "少蔬菜", "缺少蔬菜")) tags += "蔬菜少"
        if (text.hasAny("胆固醇", "血脂", "控脂")) tags += "少油控脂"
        return tags
    }

    private fun normalizeEatingAdvice(rawAdvice: String, goalLevel: String, text: String): String {
        val clean = rawAdvice.trim()
        return when {
            text.hasAny("油炸", "重油", "红油", "肥肉", "甜品", "奶茶", "高糖") -> "需要严格控量"
            clean.hasAny("尝", "一小口", "浅尝") -> "只能尝一小口"
            clean.hasAny("严格", "控量", "少吃", "少碰") -> "需要严格控量"
            clean.hasAny("多吃", "多一点", "放心") -> "可以适量多吃"
            clean.hasAny("适量", "正常") -> "可以适量吃"
            goalLevel == "poor" -> "只能尝一小口"
            goalLevel == "good" -> "可以适量多吃"
            else -> "可以适量吃"
        }
    }

    private fun normalizeSuggestion(text: String): String {
        val raw = text.trim()
        val keywordSuggestion = suggestionByKeyword(raw)
        if (keywordSuggestion.isNotBlank()) return keywordSuggestion

        val clean = raw
            .removePrefix("建议")
            .removePrefix("可以")
            .removePrefix("尽量")
            .replace("完全避免", "少吃")
            .replace("不要食用", "少吃")
            .replace("不要吃", "少吃")
            .replace("避免食用", "少吃")
            .replace("避免", "少")
            .replace("减少一半", "减半")
            .replace("份量", "分量")
            .replace("下一餐选择", "下餐选")
            .replace("下一餐", "下餐")
            .replace("额外", "")
            .trim('，', '。', '、', ' ')

        return compactSentence(clean, 18)
    }

    private fun suggestionByKeyword(text: String): String = when {
        text.hasAny("烧烤") && text.hasAny("一半", "半份", "减半", "减少") -> "烧烤少吃半份"
        text.hasAny("汤", "汤底", "汤汁") -> "汤汁少喝几口"
        text.hasAny("酱", "蘸料", "沙拉酱") -> "酱料少放"
        text.hasAny("油炸", "炸物", "煎炸") -> "油炸少吃几口"
        text.hasAny("重油", "油腻") -> "重油菜少吃几口"
        text.hasAny("完全避免", "禁止", "不要吃", "避免") && text.hasAny("油", "脂", "炸") -> "少吃高油食物"
        text.hasAny("米饭", "面条", "主食", "碳水") && text.hasAny("减少", "控制", "少", "半") -> "主食少吃几口"
        text.hasAny("甜饮", "含糖饮料") -> "甜饮别叠加"
        text.hasAny("蔬菜", "纤维") && text.hasAny("增加", "加", "补充", "搭配") -> "加一份蔬菜"
        text.hasAny("先吃") && text.hasAny("蔬菜", "蛋白") -> "先吃蔬菜蛋白"
        text.hasAny("蛋白") && text.hasAny("增加", "加", "补充", "不足") -> "加点蛋白质"
        text.hasAny("甜", "糖", "饮料", "奶茶") -> "甜饮甜品少点"
        text.hasAny("盐", "钠", "咸") -> "汤汁酱料少点"
        text.hasAny("下餐", "下一餐") && text.hasAny("清淡") -> "下餐清淡一点"
        text.hasAny("频率", "常吃", "经常") -> "这类少安排"
        text.hasAny("分量", "份量") && text.hasAny("减少", "控制", "偏高", "过高") -> "这餐少吃几口"
        else -> ""
    }

    private fun fallbackSuggestions(result: MealAnalysisResult): List<String> {
        val text = analysisText(result)
        return listOfNotNull(
            if (text.hasAny("油", "脂", "炸")) "油炸重油少几口" else null,
            if (text.hasAny("盐", "钠", "咸", "汤")) "汤汁酱料少点" else null,
            if (text.hasAny("蔬菜少", "蔬菜不足", "缺少蔬菜")) "加一份蔬菜" else null,
        ).take(3).ifEmpty { listOf("吃到七八分饱") }
    }

    private fun localizedSuggestionByKeyword(text: String, language: AppLanguage): String {
        val raw = text.trim()
        return when {
            raw.hasAny("蔬菜", "蛋白", "vegetable", "protein", "野菜", "たんぱく") &&
                raw.hasAny("先", "first", "先に") ->
                localizedSuggestion(language, "先吃蔬菜蛋白", "先吃蔬菜蛋白", "Eat vegetables and protein first", "野菜とたんぱく質を先に")
            raw.hasAny("蔬菜", "vegetable", "fiber", "野菜") &&
                raw.hasAny("增加", "加", "补充", "補充", "add", "more", "足す", "追加") ->
                localizedSuggestion(language, "加一份蔬菜", "加一份蔬菜", "Add a serving of vegetables", "野菜を一品追加")
            raw.hasAny("蛋白", "protein", "たんぱく") &&
                raw.hasAny("增加", "加", "补充", "補充", "add", "more", "足す", "追加", "不足") ->
                localizedSuggestion(language, "加点蛋白质", "加點蛋白質", "Add some protein", "たんぱく質を少し追加")
            raw.hasAny("汤", "湯", "汤底", "汤汁", "soup", "broth", "汁") ->
                localizedSuggestion(language, "汤汁少喝几口", "湯汁少喝幾口", "Drink less broth", "汁は控えめに")
            raw.hasAny("酱", "醬", "蘸料", "sauce", "dressing", "ソース", "たれ") ->
                localizedSuggestion(language, "酱料少放", "醬料少放", "Use less sauce", "ソースは少なめに")
            raw.hasAny("米饭", "米飯", "面条", "麵", "主食", "碳水", "staple", "rice", "noodle", "carb", "主食", "炭水化物") &&
                raw.hasAny("减少", "減少", "控制", "少", "半", "reduce", "less", "smaller", "控え") ->
                localizedSuggestion(language, "主食少吃几口", "主食少吃幾口", "Take fewer staple bites", "主食を少し減らす")
            raw.hasAny("油炸", "炸物", "煎炸", "fried", "揚げ物") ->
                localizedSuggestion(language, "油炸少吃几口", "油炸少吃幾口", "Keep fried food to a few bites", "揚げ物は数口まで")
            raw.hasAny("重油", "油腻", "油膩", "greasy", "high oil", "油多め") ->
                localizedSuggestion(language, "重油菜少吃几口", "重油菜少吃幾口", "Eat less oily dishes", "油多めの料理は控えめに")
            raw.hasAny("甜饮", "甜飲", "含糖饮料", "含糖飲料", "奶茶", "sugary drink", "sweet drink", "甘い飲み物") ->
                localizedSuggestion(language, "甜饮别叠加", "甜飲別疊加", "Skip sugary drinks", "甘い飲み物は足さない")
            raw.hasAny("甜", "糖", "dessert", "sweet", "デザート", "甘い") ->
                localizedSuggestion(language, "甜饮甜品少点", "甜飲甜品少點", "Keep sweets small", "甘い物は少なめに")
            raw.hasAny("下餐", "下一餐", "next meal", "次の食事") && raw.hasAny("清淡", "lighter", "軽め") ->
                localizedSuggestion(language, "下餐清淡一点", "下餐清淡一點", "Keep the next meal lighter", "次の食事は軽めに")
            raw.hasAny("频率", "頻率", "常吃", "经常", "frequency", "often", "頻度") ->
                localizedSuggestion(language, "这类少安排", "這類少安排", "Have this less often", "このタイプは頻度を控える")
            raw.hasAny("分量", "份量", "portion", "amount", "量") &&
                raw.hasAny("减少", "減少", "控制", "少", "reduce", "control", "控え") ->
                localizedSuggestion(language, "这餐少吃几口", "這餐少吃幾口", "Eat a few bites less", "数口少なめに")
            else -> ""
        }
    }

    private fun localizedSuggestion(language: AppLanguage, zhHans: String, zhHant: String, en: String, ja: String): String =
        when (language) {
            AppLanguage.ZhHans -> zhHans
            AppLanguage.ZhHant -> zhHant
            AppLanguage.En -> en
            AppLanguage.Ja -> ja
        }

    private fun analysisText(result: MealAnalysisResult): String =
        buildString {
            append(result.mealName)
            append(result.summary)
            append(result.goalMatch.reason)
            append(result.eatingAdvice)
            append(result.tags.joinToString(""))
            append(result.ingredients.joinToString("") { "${it.dish}${it.name}" })
        }

    private fun compactSentence(text: String, maxLength: Int): String {
        val clean = text.trim().trim('，', '。', '、', ' ')
        return if (clean.length <= maxLength) clean else clean.take(maxLength)
    }

    private fun String.isMeaninglessTag(): Boolean {
        val clean = trim().replace(Regex("\\s+"), "")
        val lower = trim().lowercase(Locale.ROOT)
        if (clean.isBlank()) return true
        if (clean.contains("常规") || clean.contains("普通") || clean.contains("一般")) return true
        if (clean.contains("常規") || clean.contains("ふつう") || clean.contains("一般的")) return true
        if (lower.contains("regular") || lower.contains("ordinary") || lower.contains("rough estimate")) return true
        return clean in setOf("食材", "分量", "份量", "估算", "粗估", "粗略估算", "记录参考", "常见", "常見", "普通量", "概算")
    }

    private fun String.isMeaninglessSuggestion(): Boolean {
        val clean = trim()
        val lower = clean.lowercase(Locale.ROOT)
        if (clean.isBlank()) return true
        return lower in setOf("keep balanced", "eat healthy", "be mindful", "control frequency") ||
            clean in setOf("保持均衡", "注意频率", "注意頻率", "バランスを保つ", "頻度に注意")
    }

    private fun String.hasAny(vararg keywords: String): Boolean =
        keywords.any { contains(it, ignoreCase = true) }
}
