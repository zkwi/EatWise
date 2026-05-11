package com.example.eatwise.ui.components

import com.example.eatwise.domain.model.Ingredient
import org.junit.Assert.assertEquals
import org.junit.Test

class DishAdviceLogicTest {
    @Test
    fun dishHintsUseMultipleDecisionLabelsForHeavyMeat() {
        val labels = dishHints(
            IngredientGroup(
                title = "红烧肉类",
                items = listOf(Ingredient(dish = "红烧肉类", name = "五花肉")),
            ),
        ).map { it.label }

        assertEquals(listOf("油脂高", "重口味", "有蛋白"), labels)
    }

    @Test
    fun dishHintsAddContextForLightVegetablesAndSteamedStaples() {
        val vegetables = dishHints(
            IngredientGroup(
                title = "清炒蔬菜",
                items = listOf(Ingredient(dish = "清炒蔬菜", name = "绿叶菜")),
            ),
        ).map { it.label }
        val staples = dishHints(
            IngredientGroup(
                title = "蒸笼",
                items = listOf(
                    Ingredient(dish = "蒸笼", name = "玉米"),
                    Ingredient(dish = "蒸笼", name = "红薯"),
                ),
            ),
        ).map { it.label }
        val englishStaples = dishHints(
            IngredientGroup(
                title = "Steamed basket",
                items = listOf(
                    Ingredient(dish = "Steamed basket", name = "corn"),
                    Ingredient(dish = "Steamed basket", name = "sweet potato"),
                ),
            ),
        ).map { it.label }

        assertEquals(listOf("有蔬菜", "清淡", "可多吃"), vegetables)
        assertEquals(listOf("主食", "清淡", "适量吃"), staples)
        assertEquals(listOf("主食", "清淡", "适量吃"), englishStaples)
    }
}
