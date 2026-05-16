package com.example.eatwise.ui.components

import com.example.eatwise.domain.model.NutritionAnalysisResult
import com.example.eatwise.domain.model.NutritionItem
import org.junit.Assert.assertEquals
import org.junit.Test

class NutritionResultCardTest {
    @Test
    fun overviewItemsSkipCalorieWhenOtherItemsExist() {
        val items = listOf(
            NutritionItem(label = "热量", estimate = "约 800-1200 kcal"),
            NutritionItem(label = "脂肪", estimate = "约 40-65 g"),
            NutritionItem(label = "碳水化合物", estimate = "约 50-80 g"),
        )

        assertEquals(
            listOf("脂肪", "碳水化合物"),
            nutritionOverviewItems(items).map { it.label },
        )
    }

    @Test
    fun overviewItemsKeepCalorieWhenItIsTheOnlyItem() {
        val items = listOf(NutritionItem(label = "Calorie", estimate = "about 600-900 kcal"))

        assertEquals(listOf("Calorie"), nutritionOverviewItems(items).map { it.label })
    }

    @Test
    fun calorieItemFindsCalorieAcrossLanguages() {
        val items = listOf(
            NutritionItem(label = "脂肪", estimate = "约 40 g"),
            NutritionItem(label = "Calories", estimate = "about 800 kcal"),
        )

        assertEquals("Calories", nutritionCalorieItem(items)?.label)
    }

    @Test
    fun detailItemsSkipCalorieNotesShownInHero() {
        val items = listOf(
            NutritionItem(label = "热量", estimate = "约 800 kcal", note = "油炸增加热量。"),
            NutritionItem(label = "脂肪", estimate = "约 40 g", note = "油脂偏高。"),
            NutritionItem(label = "蛋白质", estimate = "约 30 g"),
        )

        assertEquals(listOf("脂肪"), nutritionDetailItems(items).map { it.label })
    }

    @Test
    fun detailItemsStayCompactForMobileCard() {
        val items = listOf(
            NutritionItem(label = "热量", estimate = "约 1000-1500 kcal", note = "整餐热量偏高。"),
            NutritionItem(label = "脂肪", estimate = "约 45-70 g", note = "油炸和酱汁较多。"),
            NutritionItem(label = "碳水化合物", estimate = "约 90-130 g", note = "主食份量较大。"),
            NutritionItem(label = "蛋白质", estimate = "约 30-45 g", note = "肉蛋提供蛋白。"),
            NutritionItem(label = "钠/盐分", estimate = "偏高", note = "调味较重。"),
        )

        assertEquals(listOf("脂肪", "碳水化合物", "蛋白质"), nutritionDetailItems(items).map { it.label })
    }

    @Test
    fun detailHeaderKeepsLabelAndEstimateTogether() {
        val item = NutritionItem(label = "脂肪", estimate = "约 40-65 g", note = "油脂偏高。")

        assertEquals(
            NutritionDetailHeaderText(label = "脂肪", estimate = "约 40-65 g"),
            nutritionDetailHeaderText(
                item = item,
                fallbackLabel = "营养结构",
                unknownEstimate = "未知",
            ),
        )
    }

    @Test
    fun compactEstimateKeepsUnitWithRange() {
        assertEquals("约 40-65g", compactNutritionEstimate("约 40 - 65 g", "未知"))
        assertEquals("约 1000-1500kcal", compactNutritionEstimate("约 1000 - 1500 kcal", "未知"))
    }

    @Test
    fun calorieEquivalentUsesDedicatedField() {
        val result = NutritionAnalysisResult(calorieEquivalent = "约相当于 4-6 碗米饭或 10-15 个苹果")

        assertEquals("约相当于 4-6 碗米饭或 10-15 个苹果", nutritionCalorieEquivalent(result))
    }

    @Test
    fun nutritionSectionsPutActionsBeforeDetails() {
        val result = NutritionAnalysisResult(
            basis = "按常见份量估算。",
            suggestions = listOf("先去掉油炸面衣。"),
            items = listOf(
                NutritionItem(label = "热量", estimate = "约 800-1200 kcal"),
                NutritionItem(label = "脂肪", estimate = "约 40-65 g", note = "油炸吸油。"),
                NutritionItem(label = "蛋白质", estimate = "约 30-45 g"),
            ),
        )

        assertEquals(
            listOf(NutritionSection.Hero, NutritionSection.Actions, NutritionSection.Overview, NutritionSection.Basis, NutritionSection.Notes),
            nutritionSections(result),
        )
    }
}
