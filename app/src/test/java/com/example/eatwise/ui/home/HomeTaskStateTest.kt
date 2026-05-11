package com.example.eatwise.ui.home

import com.example.eatwise.domain.usecase.AnalysisTaskState
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class HomeTaskStateTest {
    @Test
    fun queuedAndFailedTasksStayVisibleOnHome() {
        assertTrue(AnalysisTaskState(imagePath = "queued.jpg", isQueued = true).shouldShowOnHome())
        assertTrue(AnalysisTaskState(imagePath = "running.jpg", isAnalyzing = true).shouldShowOnHome())
        assertTrue(AnalysisTaskState(imagePath = "failed.jpg", errorMessage = "failed").shouldShowOnHome())
        assertTrue(AnalysisTaskState(imagePath = "save-failed.jpg", saveMessage = "Save failed. Please try again.").shouldShowOnHome())
    }

    @Test
    fun completedTasksDoNotStayVisibleOnHome() {
        assertFalse(AnalysisTaskState(imagePath = "done.jpg").shouldShowOnHome())
    }
}
