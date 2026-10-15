package com.sreecharan.quitvaping.notifications

import java.time.Duration
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.temporal.ChronoUnit

/** Shared quit-date math. The quit day itself is Day 1. */
object QuitDate {
    /** Days clean, quit day = 1. Returns 0 if [quitEpochMillis] is null or in the future. */
    fun daysClean(quitEpochMillis: Long?, nowEpochMillis: Long = System.currentTimeMillis()): Long {
        if (quitEpochMillis == null) return 0
        val zone = ZoneId.systemDefault()
        val quitDay = Instant.ofEpochMilli(quitEpochMillis).atZone(zone).toLocalDate()
        val today = Instant.ofEpochMilli(nowEpochMillis).atZone(zone).toLocalDate()
        val between = ChronoUnit.DAYS.between(quitDay, today)
        return if (between < 0) 0 else between + 1
    }

    /** Money saved in cents: weekly spend prorated by days clean. */
    fun moneySavedCents(weeklySpendCents: Long, daysClean: Long): Long {
        if (weeklySpendCents <= 0 || daysClean <= 0) return 0
        return weeklySpendCents * daysClean / 7
    }

    fun formatMoney(cents: Long): String {
        val dollars = cents / 100
        val remainder = (cents % 100).toString().padStart(2, '0')
        return "$$dollars.$remainder"
    }

    /** Duration since quit, for milestone checks. */
    fun elapsedSince(quitEpochMillis: Long, nowEpochMillis: Long = System.currentTimeMillis()): Duration =
        Duration.ofMillis((nowEpochMillis - quitEpochMillis).coerceAtLeast(0))

    fun todayMidnightEpochMillis(): Long =
        LocalDate.now().atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
}
