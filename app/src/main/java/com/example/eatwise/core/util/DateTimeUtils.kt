package com.example.eatwise.core.util

import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

object DateTimeUtils {
    private val zone: ZoneId = ZoneId.systemDefault()
    private val fullFormat = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm", Locale.CHINA)
    private val shortFormat = DateTimeFormatter.ofPattern("MM-dd HH:mm", Locale.CHINA)
    private val timeFormat = DateTimeFormatter.ofPattern("HH:mm", Locale.CHINA)
    private val dateFormat = DateTimeFormatter.ofPattern("MM-dd", Locale.CHINA)

    fun formatFull(millis: Long): String = fullFormat.format(millis.toLocalDateTime())

    fun formatShort(millis: Long): String = shortFormat.format(millis.toLocalDateTime())

    fun formatListTime(millis: Long, now: Long = System.currentTimeMillis()): String =
        if (millis.toLocalDate() == now.toLocalDate()) {
            timeFormat.format(millis.toLocalDateTime())
        } else {
            dateFormat.format(millis.toLocalDateTime())
        }

    private fun Long.toLocalDateTime() = Instant.ofEpochMilli(this).atZone(zone).toLocalDateTime()

    private fun Long.toLocalDate() = Instant.ofEpochMilli(this).atZone(zone).toLocalDate()
}
