package com.example.eatwise

import com.example.eatwise.core.util.JsonUtils
import com.example.eatwise.core.util.MealAnalysisPolisher
import com.example.eatwise.domain.model.GoalMatch
import com.example.eatwise.domain.model.Ingredient
import com.example.eatwise.domain.model.MealAnalysisResult
import org.junit.Assert.assertEquals
import org.junit.Test

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
        assertEquals("以上是基于图片的定性判断，仅供饮食记录参考。", result.disclaimer)
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

        assertEquals(listOf("蛋白足", "负担高", "油脂高", "控脂谨慎"), result.tags)
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

        assertEquals(listOf("重口味", "控脂谨慎", "油脂高"), result.tags)
        assertEquals("需要严格控量", result.eatingAdvice)
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
          "disclaimer": "以上是基于图片的定性判断，仅供饮食记录参考。"
        }
    """.trimIndent()
}
