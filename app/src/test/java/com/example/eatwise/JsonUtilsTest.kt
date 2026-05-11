package com.example.eatwise

import com.example.eatwise.core.i18n.AppLanguage
import com.example.eatwise.core.i18n.MealLanguageText
import com.example.eatwise.core.util.JsonUtils
import com.example.eatwise.core.util.MealAnalysisPolisher
import com.example.eatwise.domain.model.GoalMatch
import com.example.eatwise.domain.model.Ingredient
import com.example.eatwise.domain.model.MealAnalysisResult
import org.junit.Assert.assertEquals
import org.junit.Test
import java.util.Locale

class JsonUtilsTest {
    @Test
    fun parseNormalJson() {
        val result = JsonUtils.parseMealAnalysis(sampleJson)
        assertEquals("番茄鸡蛋面配烧烤", result.mealName)
        assertEquals("partial", result.goalMatch.level)
        assertEquals("需要严格控量", result.eatingAdvice)
    }

    @Test
    fun extractJsonFromMarkdownBlock() {
        val raw = "```json\n$sampleJson\n```"
        val result = JsonUtils.parseMealAnalysis(raw)
        assertEquals("以上仅基于图片做饮食参考，不替代专业建议。", result.disclaimer)
    }

    @Test
    fun parseIngredientDishForMixedMeals() {
        val result = JsonUtils.parseMealAnalysis(sampleJson)
        assertEquals("番茄鸡蛋面", result.ingredients.first().dish)
    }

    @Test
    fun parseLegacyIngredientWithoutDish() {
        val legacyJson = sampleJson.replace("\"dish\": \"番茄鸡蛋面\",\n", "")
        val result = JsonUtils.parseMealAnalysis(legacyJson)
        assertEquals("", result.ingredients.first().dish)
    }

    @Test
    fun polishTagsAndSuggestionsForMobileCards() {
        val result = MealAnalysisPolisher.polish(
            JsonUtils.parseMealAnalysis(sampleJson).copy(
                suggestions = listOf(
                    "如果目标是减重，可以减少一半烧烤肉类。",
                    "建议少喝汤底，减少额外油盐摄入。",
                    "完全避免任何高脂食物。",
                ),
                tags = listOf("蛋白质充足", "热量偏高", "油脂偏高", "高胆固醇风险"),
            ),
        )

        assertEquals(listOf("蛋白足", "负担高", "油脂高", "少油控脂"), result.tags)
        assertEquals(listOf("烧烤少吃半份", "汤汁少喝几口", "少吃高油食物"), result.suggestions)
    }

    @Test
    fun polishTagsRemovesMeaninglessAndConflictingLabels() {
        val result = MealAnalysisPolisher.polish(
            MealAnalysisResult(
                mealName = "红油鸡肉饭",
                summary = "红油较多，口味偏重。",
                eatingAdvice = "可以适量吃",
                goalMatch = GoalMatch(level = "partial", reason = "控脂目标下不太合适"),
                ingredients = listOf(Ingredient(dish = "红油鸡肉", name = "红油调料")),
                tags = listOf("常规食材", "常规分量", "轻负担", "红油调味", "控脂关注"),
            ),
        )

        assertEquals(listOf("重口味", "少油控脂", "油脂高"), result.tags)
        assertEquals("需要严格控量", result.eatingAdvice)
    }

    @Test
    fun polishSuggestionsIntoActionableChecklist() {
        val result = MealAnalysisPolisher.polish(
            MealAnalysisResult(
                mealName = "聚餐拼盘",
                suggestions = listOf(
                    "需要注意控制频率。",
                    "建议先吃蔬菜和蛋白。",
                    "含糖饮料不要叠加。",
                ),
                tags = listOf("高脂注意", "需要控量", "油盐偏高"),
            ),
        )

        assertEquals(listOf("这类少安排", "先吃蔬菜蛋白", "甜饮别叠加"), result.suggestions)
        assertEquals(listOf("控量", "油盐高"), result.tags)
    }

    @Test
    fun polishEnglishResultKeepsLocalizedTagsReadable() {
        val result = MealAnalysisPolisher.polish(
            MealAnalysisResult(
                mealName = "Shared dinner",
                eatingAdvice = "Needs portion control",
                goalMatch = GoalMatch(level = "partial", reason = "Heavy sauces need smaller portions."),
                suggestions = listOf("Eat vegetables and protein first.", "Use less sauce."),
                tags = listOf("High oil", "Portion control", "Has vegetables"),
                disclaimer = "",
            ),
            AppLanguage.En,
        )

        assertEquals("Needs portion control", result.eatingAdvice)
        assertEquals(listOf("High oil", "Portion control", "Vegetables"), result.tags)
        assertEquals("This is image-based dietary guidance only and does not replace professional advice.", result.disclaimer)
    }

    @Test
    fun systemLocaleMapsToSupportedInitialLanguage() {
        assertEquals(AppLanguage.ZhHans, AppLanguage.fromLocale(Locale.forLanguageTag("zh-Hans-CN")))
        assertEquals(AppLanguage.ZhHant, AppLanguage.fromLocale(Locale.forLanguageTag("zh-Hant-TW")))
        assertEquals(AppLanguage.ZhHant, AppLanguage.fromLocale(Locale.forLanguageTag("zh-HK")))
        assertEquals(AppLanguage.En, AppLanguage.fromLocale(Locale.US))
        assertEquals(AppLanguage.Ja, AppLanguage.fromLocale(Locale.JAPAN))
    }

    @Test
    fun tagAliasesCoverCommonModelOutputs() {
        assertEquals("Heavy flavor", MealLanguageText.displayTag("Heavy seasoning", AppLanguage.En))
        assertEquals("High oil", MealLanguageText.displayTag("oily", AppLanguage.En))
        assertEquals("Vegetables", MealLanguageText.displayTag("Has vegetables", AppLanguage.En))
        assertEquals("塩分控えめ", MealLanguageText.displayTag("Low sodium", AppLanguage.Ja))
        assertEquals("Lower sodium", MealLanguageText.displayTag("low-sodium", AppLanguage.En))
        assertEquals("High sugar", MealLanguageText.displayTag("糖質高め", AppLanguage.En))
        assertEquals("High sugar", MealLanguageText.displayTag("high-sugar", AppLanguage.En))
    }

    @Test
    fun localizedSuggestionsAreActionableWhenModelUsesLooseWording() {
        val result = MealAnalysisPolisher.polish(
            MealAnalysisResult(
                mealName = "Fried meal",
                suggestions = listOf("Control frequency.", "Use less sauce.", "Add more vegetables."),
                tags = listOf("Light burden", "Low sodium"),
            ),
            AppLanguage.En,
        )

        assertEquals(listOf("Have this less often", "Use less sauce", "Add a serving of vegetables"), result.suggestions)
        assertEquals(listOf("Light choice", "Lower sodium"), result.tags)
    }

    @Test
    fun localizedPolishUsesCurrentLanguageForFallbackText() {
        val result = MealAnalysisPolisher.polish(MealAnalysisResult(), AppLanguage.En)

        assertEquals("Meal analysis", result.mealName)
        assertEquals("Moderate amount is fine", result.eatingAdvice)
        assertEquals(
            "This is image-based dietary guidance only and does not replace professional advice.",
            result.disclaimer,
        )
    }

    private val sampleJson = """
        {
          "meal_name": "番茄鸡蛋面配烧烤",
          "summary": "这餐蛋白质充足，但油脂偏高。",
          "eating_advice": "需要严格控量",
          "goal_match": {
            "level": "partial",
            "reason": "符合高蛋白需求，但油脂偏高。"
          },
          "ingredients": [
            {
              "dish": "番茄鸡蛋面",
              "name": "面条"
            }
          ],
          "suggestions": [
            "如果目标是减重，可以减少一半烧烤肉类。",
            "建议少喝汤底，减少额外油盐摄入。"
          ],
          "tags": ["高蛋白", "热量偏高", "油脂偏高"],
          "disclaimer": "以上仅基于图片做饮食参考，不替代专业建议。"
        }
    """.trimIndent()
}
