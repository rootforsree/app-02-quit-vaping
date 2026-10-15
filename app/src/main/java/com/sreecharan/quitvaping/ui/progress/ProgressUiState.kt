package com.sreecharan.quitvaping.ui.progress

import androidx.compose.runtime.Immutable
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf

@Immutable
data class MilestoneUi(
    val key: String,
    val title: String,
    val description: String,
    /** "20 min", "12 hr", "7 days", "1 yr" — from the catalog offset. */
    val offsetLabel: String,
    val achieved: Boolean,
    /** The first unachieved milestone: the one in progress now. */
    val isCurrent: Boolean,
)

@Immutable
data class ProgressUiState(
    val isLoading: Boolean = true,
    val currentStreakDays: Long = 0,
    val totalCleanDays: Long = 0,
    val cleanDayPercent: Int = 0,
    val moneySavedCents: Long = 0,
    val projectedYearlyCents: Long = 0,
    val projectionDateLabel: String = "",
    /** Cumulative savings at the end of each of the last 12 weeks, in cents. */
    val weeklyBars: ImmutableList<Long> = persistentListOf(),
    val milestones: ImmutableList<MilestoneUi> = persistentListOf(),
    val cravingsBeaten: Int = 0,
)
