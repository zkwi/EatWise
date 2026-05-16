package com.example.eatwise.ui.components

import org.junit.Assert.assertEquals
import org.junit.Test

class ResultTabSwitcherTest {
    @Test
    fun pagerPagesKeepStableOrderAndSkipUnavailableTabs() {
        assertEquals(listOf(ResultTab.Advice, ResultTab.Nutrition), resultTabPages(true, true))
        assertEquals(listOf(ResultTab.Advice), resultTabPages(true, false))
        assertEquals(listOf(ResultTab.Nutrition), resultTabPages(false, true))
    }

    @Test
    fun selectedTabMapsToAvailablePagerPage() {
        assertEquals(0, resultTabPageIndex(ResultTab.Advice, adviceAvailable = true, nutritionAvailable = true))
        assertEquals(1, resultTabPageIndex(ResultTab.Nutrition, adviceAvailable = true, nutritionAvailable = true))
        assertEquals(0, resultTabPageIndex(ResultTab.Advice, adviceAvailable = false, nutritionAvailable = true))
        assertEquals(0, resultTabPageIndex(ResultTab.Nutrition, adviceAvailable = true, nutritionAvailable = false))
    }

    @Test
    fun indicatorIndexResolvesUnavailableTabBeforeAnimating() {
        assertEquals(0, resultTabIndicatorIndex(ResultTab.Advice, adviceEnabled = true, nutritionEnabled = true))
        assertEquals(1, resultTabIndicatorIndex(ResultTab.Nutrition, adviceEnabled = true, nutritionEnabled = true))
        assertEquals(0, resultTabIndicatorIndex(ResultTab.Nutrition, adviceEnabled = true, nutritionEnabled = false))
        assertEquals(1, resultTabIndicatorIndex(ResultTab.Advice, adviceEnabled = false, nutritionEnabled = true))
    }

    @Test
    fun pagerPageMapsBackToTab() {
        assertEquals(ResultTab.Advice, resultTabForPage(0, adviceAvailable = true, nutritionAvailable = true))
        assertEquals(ResultTab.Nutrition, resultTabForPage(1, adviceAvailable = true, nutritionAvailable = true))
        assertEquals(ResultTab.Nutrition, resultTabForPage(0, adviceAvailable = false, nutritionAvailable = true))
        assertEquals(ResultTab.Advice, resultTabForPage(4, adviceAvailable = true, nutritionAvailable = false))
    }
}
