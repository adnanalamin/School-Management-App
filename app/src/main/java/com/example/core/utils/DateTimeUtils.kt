package com.example.core.utils

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * DateTime formatting and academic session utilities.
 */
object DateTimeUtils {
    private val DISPLAY_DATE_FORMAT = SimpleDateFormat("dd MMM yyyy", Locale.US)
    private val ISO_DATE_FORMAT = SimpleDateFormat("yyyy-MM-dd", Locale.US)
    private val TIME_FORMAT = SimpleDateFormat("hh:mm a", Locale.US)

    fun formatDate(timestampMs: Long): String {
        return DISPLAY_DATE_FORMAT.format(Date(timestampMs))
    }

    fun formatIsoDate(timestampMs: Long): String {
        return ISO_DATE_FORMAT.format(Date(timestampMs))
    }

    fun formatTime(timestampMs: Long): String {
        return TIME_FORMAT.format(Date(timestampMs))
    }

    fun getCurrentAcademicSession(): String {
        val calendar = java.util.Calendar.getInstance()
        val year = calendar.get(java.util.Calendar.YEAR)
        val month = calendar.get(java.util.Calendar.MONTH) // 0-indexed
        return if (month >= 5) {
            "$year-${year + 1}"
        } else {
            "${year - 1}-$year"
        }
    }
}
