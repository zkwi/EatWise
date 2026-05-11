package com.example.eatwise

import com.example.eatwise.core.util.DateTimeUtils
import org.junit.Assert.assertEquals
import org.junit.Test
import java.util.Calendar

class DateTimeUtilsTest {
    @Test
    fun formatListTimeShowsTimeForToday() {
        val now = at(2026, Calendar.MAY, 11, 10, 30)
        val createdAt = at(2026, Calendar.MAY, 11, 6, 47)

        assertEquals("06:47", DateTimeUtils.formatListTime(createdAt, now))
    }

    @Test
    fun formatListTimeShowsDateForOtherDays() {
        val now = at(2026, Calendar.MAY, 11, 10, 30)
        val createdAt = at(2026, Calendar.MAY, 10, 22, 15)

        assertEquals("05-10", DateTimeUtils.formatListTime(createdAt, now))
    }

    private fun at(year: Int, month: Int, day: Int, hour: Int, minute: Int): Long =
        Calendar.getInstance().apply {
            set(Calendar.YEAR, year)
            set(Calendar.MONTH, month)
            set(Calendar.DAY_OF_MONTH, day)
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis
}
