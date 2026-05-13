package com.example.eatwise

import com.example.eatwise.core.i18n.AppLanguage
import com.example.eatwise.core.i18n.MealLanguageText
import com.example.eatwise.core.network.OpenAiCompatibleClient
import com.example.eatwise.core.util.JsonUtils
import com.example.eatwise.core.util.MealAnalysisPolisher
import com.example.eatwise.domain.model.GoalMatch
import com.example.eatwise.domain.model.Ingredient
import com.example.eatwise.domain.model.MealAnalysisResult
import okhttp3.OkHttpClient
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
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
        val parsed = JsonUtils.parseMealAnalysis(sampleJson)
        val result = MealAnalysisPolisher.polish(
            parsed.copy(
                suggestions = listOf(
                    "如果目标是减重，可以减少一半烧烤肉类。",
                    "建议少喝汤底，减少额外油盐摄入。",
                    "完全避免任何高脂食物。",
                ),
                ingredients = parsed.ingredients + Ingredient(dish = "番茄鸡蛋面", name = "汤底"),
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

        assertEquals(listOf("这类少安排", "先吃蔬菜蛋白"), result.suggestions)
        assertEquals(listOf("控量", "油盐高"), result.tags)
    }

    @Test
    fun polishDropsSuggestionsForFoodsNotPresentInMeal() {
        val result = MealAnalysisPolisher.polish(
            MealAnalysisResult(
                mealName = "家常清淡多菜餐",
                summary = "包含蒸粗粮、乌鸡、花菜和木耳，整体清淡。",
                eatingAdvice = "可以适量吃",
                goalMatch = GoalMatch(level = "good", reason = "蔬菜和蛋白较多，主食适量即可。"),
                ingredients = listOf(
                    Ingredient(dish = "蒸笼", name = "玉米"),
                    Ingredient(dish = "乌鸡", name = "鸡肉"),
                    Ingredient(dish = "花菜", name = "花菜"),
                    Ingredient(dish = "木耳", name = "木耳"),
                ),
                suggestions = listOf("甜饮甜品少点", "主食少吃几口", "汤汁少喝几口"),
                tags = listOf("有蔬菜", "主食", "轻负担"),
            ),
        )

        assertEquals(listOf("主食少吃几口"), result.suggestions)
    }

    @Test
    fun polishKeepsSweetDrinkSuggestionWhenVisible() {
        val result = MealAnalysisPolisher.polish(
            MealAnalysisResult(
                mealName = "炸物配奶茶",
                summary = "有炸物和奶茶，甜饮会增加负担。",
                ingredients = listOf(
                    Ingredient(dish = "饮品", name = "奶茶"),
                    Ingredient(dish = "炸物", name = "炸鸡"),
                ),
                suggestions = listOf("甜饮别叠加"),
            ),
        )

        assertEquals(listOf("甜饮别叠加"), result.suggestions)
    }

    @Test
    fun polishDropsStapleOrFriedAdviceWhenNotVisible() {
        val result = MealAnalysisPolisher.polish(
            MealAnalysisResult(
                mealName = "清炒花菜木耳",
                summary = "花菜和木耳为主，整体清淡。",
                ingredients = listOf(
                    Ingredient(dish = "花菜", name = "花菜"),
                    Ingredient(dish = "木耳", name = "木耳"),
                ),
                suggestions = listOf("主食少吃几口", "油炸少吃几口", "下餐清淡一点"),
            ),
        )

        assertEquals(listOf("下餐清淡一点"), result.suggestions)
    }

    @Test
    fun polishKeepsEnoughContextForDetailText() {
        val result = MealAnalysisPolisher.polish(
            MealAnalysisResult(
                mealName = "家庭聚餐",
                summary = "包含蒸粗粮、绿叶菜、鱼肉和红烧肉，整体种类丰富，有清淡部分，也有油脂和酱汁需要少量注意。",
                goalMatch = GoalMatch(
                    level = "partial",
                    reason = "食材多样且有蛋白和蔬菜，适合做主餐基础，但红烧肉和煎鱼让油盐负担明显偏高。",
                ),
            ),
        )

        assertEquals("包含蒸粗粮、绿叶菜、鱼肉和红烧肉，整体种类丰富，有清淡部分，也有油脂和酱汁需要少量注意", result.summary)
        assertEquals("食材多样且有蛋白和蔬菜，适合做主餐基础，但红烧肉和煎鱼让油盐负担明显偏高", result.goalMatch.reason)
    }

    @Test
    fun polishKeepsOverallAdviceBalancedForMixedSharedMeals() {
        val result = MealAnalysisPolisher.polish(
            MealAnalysisResult(
                mealName = "家庭聚餐",
                summary = "一桌菜包含清蒸鱼、绿叶菜、豆腐和五花肉，有清淡部分也有油脂负担。",
                eatingAdvice = "可以适量吃",
                goalMatch = GoalMatch(level = "partial", reason = "整体有蔬菜和蛋白，但五花肉需要少吃。"),
                ingredients = listOf(
                    Ingredient(dish = "清蒸鱼", name = "鱼肉"),
                    Ingredient(dish = "绿叶菜", name = "青菜"),
                    Ingredient(dish = "红烧肉", name = "五花肉"),
                ),
                suggestions = listOf("先吃绿叶菜和清蒸鱼，红烧肉少吃几口。"),
                tags = listOf("有蔬菜", "蛋白足", "油脂高"),
            ),
        )

        assertEquals("可以适量吃", result.eatingAdvice)
    }

    @Test
    fun polishStillLimitsSingleHeavyDish() {
        val result = MealAnalysisPolisher.polish(
            MealAnalysisResult(
                mealName = "红油肥肉",
                summary = "红油和肥肉明显，整体油脂负担高。",
                eatingAdvice = "可以适量吃",
                goalMatch = GoalMatch(level = "partial", reason = "油脂偏高。"),
                ingredients = listOf(Ingredient(dish = "红油肥肉", name = "肥肉")),
            ),
        )

        assertEquals("需要严格控量", result.eatingAdvice)
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
                ingredients = listOf(Ingredient(dish = "Fried chicken", name = "sauce")),
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

    @Test
    fun promptVersionTracksVisibleSuggestionPromptUpdate() {
        assertEquals(15, OpenAiCompatibleClient.promptVersion)
    }

    @Test
    fun mealPromptRequiresOverallJudgmentForMultiDishMeals() {
        val prompt = OpenAiCompatibleClient::class.java
            .getDeclaredMethod("userPrompt", String::class.java, AppLanguage::class.java)
            .apply { isAccessible = true }
            .invoke(OpenAiCompatibleClient(OkHttpClient()), "少油控脂", AppLanguage.ZhHans) as String

        assertTrue(prompt.contains("must judge the whole plate"))
        assertTrue(prompt.contains("Do not base the overall advice or 1-5 score on only one single dish"))
        assertTrue(prompt.contains("which healthier dishes the user can eat more or eat first"))
        assertTrue(prompt.contains("Keep ingredients grouped by dish"))
        assertTrue(prompt.contains("Every food-specific suggestion must point to a dish or ingredient already in that array"))
        assertTrue(prompt.contains("Avoid generic category advice when a specific dish name is available"))
        assertTrue(prompt.contains("Do not mention desserts, sweet drinks, soup, broth, sauce"))
        assertTrue(prompt.contains("do not say to reduce desserts or sweet drinks when no dessert or drink is visible"))
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
