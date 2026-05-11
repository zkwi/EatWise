package com.example.eatwise.ui.components

import androidx.compose.ui.unit.dp
import androidx.compose.ui.Alignment
import com.example.eatwise.core.i18n.AppLanguage
import org.junit.Assert.assertEquals
import org.junit.Test

class MealResultLayoutTest {
    @Test
    fun suggestionRowsCenterChipAndTextVertically() {
        assertEquals(Alignment.CenterVertically, suggestionRowVerticalAlignment())
    }

    @Test
    fun suggestionActionChipsUseStableWidth() {
        assertEquals(62.dp, suggestionActionChipWidth(AppLanguage.ZhHans))
        assertEquals(76.dp, suggestionActionChipWidth(AppLanguage.En))
        assertEquals(80.dp, suggestionActionChipWidth(AppLanguage.Ja))
    }

    @Test
    fun dishTitleColumnKeepsHintStartAligned() {
        assertEquals(82.dp, dishTitleColumnWidth(AppLanguage.ZhHans))
        assertEquals(122.dp, dishTitleColumnWidth(AppLanguage.En))
        assertEquals(96.dp, dishTitleColumnWidth(AppLanguage.Ja))
        assertEquals(122.dp, dishTitleColumnWidth(AppLanguage.ZhHans, "Steamed basket"))
    }

    @Test
    fun dishHintChipsHaveMinimumWidthForShortLabels() {
        assertEquals(52.dp, ingredientHintMinWidth(AppLanguage.ZhHans))
        assertEquals(70.dp, ingredientHintMinWidth(AppLanguage.En))
        assertEquals(70.dp, ingredientHintMinWidth(AppLanguage.Ja))
    }
}
