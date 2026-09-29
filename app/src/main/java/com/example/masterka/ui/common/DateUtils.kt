package com.example.masterka.ui.common

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

/**
 * Форматирует timestamp в "29.09.2025"
 */
fun formatDate(timestamp: Long?): String {
    if (timestamp == null) return "—"
    val fmt = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())
    return fmt.format(Date(timestamp))
}

/**
 * Форматирует timestamp в "29.09.2025 14:30"
 */
fun formatDateTime(timestamp: Long?): String {
    if (timestamp == null) return "—"
    val fmt = SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault())
    return fmt.format(Date(timestamp))
}

/**
 * «Сегодня», «Вчера», «29.09.2025»
 */
fun formatRelativeDate(timestamp: Long?): String {
    if (timestamp == null) return "—"
    val now = Calendar.getInstance()
    val then = Calendar.getInstance().apply { timeInMillis = timestamp }

    val sameDay = now.get(Calendar.YEAR) == then.get(Calendar.YEAR) &&
            now.get(Calendar.DAY_OF_YEAR) == then.get(Calendar.DAY_OF_YEAR)
    if (sameDay) return "сегодня"

    val yesterday = Calendar.getInstance().apply {
        add(Calendar.DAY_OF_YEAR, -1)
    }
    val isYesterday = yesterday.get(Calendar.YEAR) == then.get(Calendar.YEAR) &&
            yesterday.get(Calendar.DAY_OF_YEAR) == then.get(Calendar.DAY_OF_YEAR)
    if (isYesterday) return "вчера"

    return formatDate(timestamp)
}

/**
 * Через месяц от текущего момента.
 */
fun oneMonthFromNow(): Long {
    val cal = Calendar.getInstance()
    cal.add(Calendar.MONTH, 1)
    return cal.timeInMillis
}

/**
 * Просрочено ли (returnBy < сейчас).
 */
fun isOverdue(returnBy: Long?): Boolean {
    if (returnBy == null) return false
    return returnBy < System.currentTimeMillis()
}