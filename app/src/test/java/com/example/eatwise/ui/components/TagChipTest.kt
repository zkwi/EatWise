package com.example.eatwise.ui.components

import androidx.compose.ui.unit.dp
import com.example.eatwise.core.i18n.AppLanguage
import org.junit.Assert.assertEquals
import org.junit.Test

class TagChipTest {
    @Test
    fun englishCompactTagsGetMoreWidthForReadableLabels() {
        assertEquals(102.dp, tagMaxWidth(AppLanguage.En, compact = true))
        assertEquals(70.dp, tagMaxWidth(AppLanguage.ZhHans, compact = true))
    }
}
