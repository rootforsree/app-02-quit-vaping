package com.sreecharan.quitvaping.ui.components

import java.text.NumberFormat
import java.util.Locale
import java.util.concurrent.TimeUnit

private val usdFormat: NumberFormat = NumberFormat.getCurrencyInstance(Locale.US)

/** "$1,284.50" for 128450 cents. */
fun formatMoney(cents: Long): String = usdFormat.format(cents / 100.0)

/** Int overload for settings sliders. */
fun formatMoneyCents(cents: Int): String = formatMoney(cents.toLong())

/** "8:00 AM" for a 24h hour/minute pair. */
fun formatTimeLabel(hour: Int, minute: Int): String {
    val suffix = if (hour < 12) "AM" else "PM"
    val hour12 = when (hour % 12) {
        0 -> 12
        else -> hour % 12
    }
    return "$hour12:%02d $suffix".format(minute)
}

/** "$1.2k" style compact form for tight spaces. */
fun formatMoneyCompact(cents: Long): String {
    val dollars = cents / 100.0
    return when {
        dollars >= 1_000_000 -> "$%.1fM".format(dollars / 1_000_000)
        dollars >= 10_000 -> "$%.0fk".format(dollars / 1_000)
        dollars >= 1_000 -> "$%.1fk".format(dollars / 1_000)
        else -> usdFormat.format(dollars)
    }
}

/** "47" for large counts; keeps the hero number clean. */
fun formatCount(count: Long): String =
    when {
        count >= 1_000_000 -> "%.1fM".format(count / 1_000_000.0)
        count >= 10_000 -> "%.0fk".format(count / 1_000.0)
        else -> "%,d".format(count)
    }

/** "3h 12m ago" style relative time for history rows. */
fun formatTimeAgo(timestampMs: Long, nowMs: Long = System.currentTimeMillis()): String {
    val minutes = TimeUnit.MILLISECONDS.toMinutes((nowMs - timestampMs).coerceAtLeast(0))
    return when {
        minutes < 1 -> "just now"
        minutes < 60 -> "${minutes}m ago"
        minutes < 24 * 60 -> "${minutes / 60}h ago"
        else -> "${minutes / (24 * 60)}d ago"
    }
}
