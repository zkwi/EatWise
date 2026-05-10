package com.example.eatwise

import com.example.eatwise.core.util.JsonUtils
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
