package com.sreecharan.quitvaping.data.model

import androidx.compose.runtime.Immutable

/**
 * Aggregated quit stats for the Today tab. All money in cents.
 * Relapse-forgiving: [currentStreakDays] restarts on a slip, but
 * [totalCleanDays] counts every clean day so progress is never erased.
 */
@Immutable
data class QuitStats(
    val daysQuit: Long,
    val currentStreakDays: Long,
    val totalCleanDays: Long,
    val moneySavedCents: Long,
    val puffsAvoided: Long,
    val cravingsBeaten: Int,
    val pledgeStreakDays: Int,
)
