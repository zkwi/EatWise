package com.example.eatwise.ui.components

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class MealImageCardTest {
    @Test
    fun previewDismissDragTracksVerticalMotionWhenImageIsNotZoomed() {
        assertEquals(
            42f,
            previewDismissDragOffset(currentOffset = 12f, dragAmount = 30f, scale = 1f),
            0.001f,
        )
        assertEquals(
            -18f,
            previewDismissDragOffset(currentOffset = 12f, dragAmount = -30f, scale = 1f),
            0.001f,
        )
    }

    @Test
    fun previewDismissDragIsIgnoredWhenImageIsZoomed() {
        assertEquals(
            0f,
            previewDismissDragOffset(currentOffset = 48f, dragAmount = 80f, scale = 1.15f),
            0.001f,
        )
    }

    @Test
    fun previewDismissRequiresEnoughVerticalDragAndUnzoomedImage() {
        assertTrue(shouldDismissPreviewByDrag(dragOffset = 130f, threshold = 120f, scale = 1f))
        assertTrue(shouldDismissPreviewByDrag(dragOffset = -130f, threshold = 120f, scale = 1f))
        assertFalse(shouldDismissPreviewByDrag(dragOffset = 110f, threshold = 120f, scale = 1f))
        assertFalse(shouldDismissPreviewByDrag(dragOffset = -110f, threshold = 120f, scale = 1f))
        assertFalse(shouldDismissPreviewByDrag(dragOffset = 160f, threshold = 120f, scale = 1.15f))
    }
}
