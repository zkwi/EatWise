package com.example.eatwise.core.util

import com.example.eatwise.domain.model.MealAnalysisResult

object MealAnalysisPolisher {
    fun polish(result: MealAnalysisResult): MealAnalysisResult {
        val suggestions = result.suggestions
            .map(::normalizeSuggestion)
            .filter { it.isNotBlank() }
            .distinct()
            .take(3)
            .ifEmpty { fallbackSuggestions(result) }

        return result.copy(
            summary = compactSentence(result.summary, 42),
            goalMatch = result.goalMatch.copy(
                score = result.goalMatch.score?.coerceIn(0, 10),
                reason = compactSentence(result.goalMatch.reason, 36),
            ),
            suggestions = suggestions,
            tags = normalizedTags(result),
        )
    }

    private fun normalizedTags(result: MealAnalysisResult): List<String> {
        val tags = result.tags.map(::normalizeTag) + derivedTags(result)
        return tags
            .filter { it.isNotBlank() }
            .distinct()
            .take(5)
            .ifEmpty { listOf("粗略估算") }
    }

    private fun normalizeTag(tag: String): String {
        val clean = tag.trim()
            .trim('，', '。', '、', ' ', '#')
            .removePrefix("标签：")
            .removePrefix("标签")

        return when {
            clean.hasAny("蛋白") && clean.hasAny("高", "足", "充足") -> "蛋白足"
            clean.hasAny("蛋白") && clean.hasAny("低", "少", "不足") -> "蛋白少"
            clean.hasAny("热量", "能量", "卡路里") && clean.hasAny("高", "多", "偏高") -> "热量高"
            clean.hasAny("热量", "能量", "卡路里") && clean.hasAny("低", "轻") -> "轻负担"
            clean.hasAny("油炸", "炸物", "煎炸") -> "油炸"
            clean.hasAny("油", "脂肪", "脂") && clean.hasAny("高", "多", "重", "偏高") -> "油脂高"
            clean.hasAny("糖", "甜") && clean.hasAny("低", "少") -> "少糖"
            clean.hasAny("糖", "甜", "奶茶", "饮料") -> "糖偏高"
            clean.hasAny("盐", "钠", "咸", "腌") -> "钠偏高"
            clean.hasAny("碳水", "主食", "米饭", "面") && clean.hasAny("高", "多", "偏高") -> "碳水多"
            clean.hasAny("蔬菜", "纤维") && clean.hasAny("少", "低", "不足", "缺") -> "蔬菜少"
            clean.hasAny("蔬菜", "纤维") -> "有蔬菜"
            clean.hasAny("胆固醇", "血脂", "高脂") -> "控脂关注"
            clean.hasAny("减脂", "减重", "减肥") -> "减脂关注"
            else -> compactSentence(clean.replace("偏高", "高").replace("较高", "高"), 6)
        }
    }

    private fun derivedTags(result: MealAnalysisResult): List<String> {
        val text = analysisText(result)
        val tags = mutableListOf<String>()
        result.totalKcal?.let {
            when {
                it >= 850 -> tags += "热量高"
                it <= 450 -> tags += "轻负担"
            }
        }
        result.macros.proteinG?.let {
            when {
                it >= 35 -> tags += "蛋白足"
                it <= 12 -> tags += "蛋白少"
            }
        }
        if ((result.macros.carbsG ?: 0.0) >= 100) tags += "碳水多"
        if ((result.macros.fatG ?: 0.0) >= 35) tags += "油脂高"
        if (text.hasAny("油炸", "炸", "煎炸")) tags += "油炸"
        if (text.hasAny("盐", "钠", "咸", "汤底", "腌")) tags += "钠偏高"
        if (text.hasAny("糖", "甜", "奶茶", "饮料")) tags += "糖偏高"
        if (text.hasAny("蔬菜少", "蔬菜不足", "少蔬菜", "缺少蔬菜")) tags += "蔬菜少"
        return tags
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
        text.hasAny("完全避免", "禁止", "不要吃", "避免") && text.hasAny("油", "脂", "炸") -> "少吃高油食物"
        text.hasAny("米饭", "面条", "主食", "碳水") && text.hasAny("减少", "控制", "少", "半") -> "主食少吃几口"
        text.hasAny("蔬菜", "纤维") && text.hasAny("增加", "加", "补充", "搭配") -> "加一份蔬菜"
        text.hasAny("蛋白") && text.hasAny("增加", "加", "补充", "不足") -> "加点蛋白质"
        text.hasAny("甜", "糖", "饮料", "奶茶") -> "甜饮甜品少点"
        text.hasAny("盐", "钠", "咸") -> "汤汁酱料少点"
        text.hasAny("热量", "总量", "分量", "份量") && text.hasAny("减少", "控制", "偏高", "过高") -> "这餐少吃几口"
        else -> ""
    }

    private fun fallbackSuggestions(result: MealAnalysisResult): List<String> {
        val text = analysisText(result)
        return listOfNotNull(
            if (text.hasAny("油", "脂", "炸")) "油炸重油少点" else null,
            if (text.hasAny("盐", "钠", "咸", "汤")) "汤汁酱料少点" else null,
            if (text.hasAny("蔬菜少", "蔬菜不足", "缺少蔬菜")) "加一份蔬菜" else null,
            if ((result.macros.carbsG ?: 0.0) >= 100) "主食少吃几口" else null,
        ).take(3).ifEmpty { listOf("吃到七八分饱") }
    }

    private fun analysisText(result: MealAnalysisResult): String =
        buildString {
            append(result.mealName)
            append(result.summary)
            append(result.goalMatch.reason)
            append(result.tags.joinToString(""))
            append(result.ingredients.joinToString("") { "${it.dish}${it.name}${it.amount}" })
        }

    private fun compactSentence(text: String, maxLength: Int): String {
        val clean = text.trim().trim('，', '。', '、', ' ')
        return if (clean.length <= maxLength) clean else clean.take(maxLength)
    }

    private fun String.hasAny(vararg keywords: String): Boolean =
        keywords.any { contains(it, ignoreCase = true) }
}
