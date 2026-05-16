package com.example.eatwise.ui.analysis

import com.example.eatwise.ui.components.ResultTab
import com.example.eatwise.ui.components.resolvedResultTab
import com.example.eatwise.ui.components.swipedResultTab
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class AnalysisScreenTest {
    @Test
    fun configErrorsShowSettingsShortcut() {
        assertTrue(analysisNeedsSettingsAction("Please enter an API key in Settings first."))
        assertTrue(analysisNeedsSettingsAction("请先在设置中填写模型名称。"))
        assertTrue(analysisNeedsSettingsAction("API Key 可能无效，请检查设置。"))
        assertTrue(analysisNeedsSettingsAction("这个模型可能看不了图片，请换一个支持图片输入的模型。"))
        assertTrue(analysisNeedsSettingsAction("Request parameters are incompatible. Check the Base URL and model name."))
    }

    @Test
    fun transientErrorsKeepRetryOnly() {
        assertFalse(analysisNeedsSettingsAction("The model returned no content. Please try again."))
        assertFalse(analysisNeedsSettingsAction("模型没有返回内容，请重试。"))
        assertFalse(analysisNeedsSettingsAction("图片处理失败，请重试。"))
    }

    @Test
    fun selectedTabFallsBackToAvailablePane() {
        assertEquals(
            ResultTab.Nutrition,
            resolvedResultTab(ResultTab.Advice, adviceAvailable = false, nutritionAvailable = true),
        )
        assertEquals(
            ResultTab.Advice,
            resolvedResultTab(ResultTab.Nutrition, adviceAvailable = true, nutritionAvailable = false),
        )
        assertEquals(
            ResultTab.Nutrition,
            resolvedResultTab(ResultTab.Nutrition, adviceAvailable = true, nutritionAvailable = true),
        )
    }

    @Test
    fun horizontalSwipeMovesBetweenAvailableTabs() {
        assertEquals(
            ResultTab.Nutrition,
            swipedResultTab(
                selected = ResultTab.Advice,
                dragDistance = -72f,
                adviceAvailable = true,
                nutritionAvailable = true,
            ),
        )
        assertEquals(
            ResultTab.Advice,
            swipedResultTab(
                selected = ResultTab.Nutrition,
                dragDistance = 72f,
                adviceAvailable = true,
                nutritionAvailable = true,
            ),
        )
        assertEquals(
            ResultTab.Advice,
            swipedResultTab(
                selected = ResultTab.Advice,
                dragDistance = -12f,
                adviceAvailable = true,
                nutritionAvailable = true,
            ),
        )
        assertEquals(
            ResultTab.Advice,
            swipedResultTab(
                selected = ResultTab.Advice,
                dragDistance = -72f,
                adviceAvailable = true,
                nutritionAvailable = false,
            ),
        )
    }
}
