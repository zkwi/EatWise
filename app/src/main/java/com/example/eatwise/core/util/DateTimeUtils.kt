package com.example.eatwise.core.util

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object DateTimeUtils {
    private val fullFormat = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.CHINA)
    private val shortFormat = SimpleDateFormat("MM-dd HH:mm", Locale.CHINA)

    fun formatFull(millis: Long): String = fullFormat.format(Date(millis))

    fun formatShort(millis: Long): String = shortFormat.format(Date(millis))
}
