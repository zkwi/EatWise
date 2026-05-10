package com.example.eatwise

import com.example.eatwise.core.util.JsonUtils
import com.example.eatwise.core.util.MealAnalysisPolisher
import org.junit.Assert.assertEquals
import org.junit.Test

class JsonUtilsTest {
    @Test
    fun parseNormalJson() {
        val result = JsonUtils.parseMealAnalysis(sampleJson)
        assertEquals("番茄鸡蛋面配烧烤", result.mealName)
        assertEquals("partial", result.goalMatch.level)
        assertEquals(128.3, result.macros.proteinG ?: 0.0, 0.01)
    }

    @Test
    fun extractJsonFromMarkdownBlock() {
        val raw = "```json\n$sampleJson\n```"
        val result = JsonUtils.parseMealAnalysis(raw)
        assertEquals(1596.0, result.totalKcal ?: 0.0, 0.01)
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

        assertEquals(listOf("蛋白足", "热量高", "油脂高", "控脂关注", "碳水多"), result.tags)
        assertEquals(listOf("烧烤少吃半份", "汤汁少喝几口", "少吃高油食物"), result.suggestions)
    }

    private val sampleJson = """
        {
          "meal_name": "番茄鸡蛋面配烧烤",
          "summary": "这餐蛋白质充足，但总热量和油脂偏高。",
          "total_kcal": 1596,
          "confidence": 0.72,
          "macros": {
            "protein_g": 128.3,
            "carbs_g": 109.9,
            "fat_g": 69.7
          },
          "goal_match": {
            "level": "partial",
            "score": 6,
            "reason": "符合高蛋白需求，但脂肪和总热量偏高。"
          },
          "ingredients": [
            {
              "dish": "番茄鸡蛋面",
              "name": "面条",
              "amount": "约300克",
              "kcal": 330
            }
          ],
          "suggestions": [
            "如果目标是减重，可以减少一半烧烤肉类。",
            "建议少喝汤底，减少额外油盐摄入。"
          ],
          "tags": ["高蛋白", "热量偏高", "油脂偏高"],
          "disclaimer": "以上是基于图片的粗略估算，仅供饮食记录参考。"
        }
    """.trimIndent()
}
