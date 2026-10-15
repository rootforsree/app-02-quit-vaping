package com.sreecharan.quitvaping.ui.today

import androidx.compose.runtime.Immutable

/**
 * Everything the Today tab renders. Money is in cents. The seconds tick
 * live; money accrues per clean day.
 */
@Immutable
data class TodayUiState(
    val isLoading: Boolean = true,
    val hasProfile: Boolean = false,
    val daysQuit: Long = 0,
    val hoursPart: Int = 0,
    val minutesPart: Int = 0,
    val secondsPart: Int = 0,
    val moneySavedCents: Long = 0,
    val savingsGoalCents: Long = 0,
    val puffsAvoided: Long = 0,
    val cravingsBeaten: Int = 0,
    val pledgedToday: Boolean = false,
    val pledgeStreakDays: Int = 0,
    val currentStreakDays: Long = 0,
    val totalCleanDays: Long = 0,
    val nextMilestoneTitle: String? = null,
    val nextMilestoneDescription: String? = null,
    val nextMilestoneProgress: Float = 0f,
    val nextMilestoneInDays: Long = 0,
)
