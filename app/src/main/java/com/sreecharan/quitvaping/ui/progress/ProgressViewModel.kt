package com.sreecharan.quitvaping.ui.progress

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sreecharan.quitvaping.data.datastore.OnboardingRepository
import com.sreecharan.quitvaping.data.model.Milestone
import com.sreecharan.quitvaping.data.model.QuitStats
import com.sreecharan.quitvaping.data.repository.MilestoneCatalog
import com.sreecharan.quitvaping.data.repository.QuitRepository
import com.sreecharan.quitvaping.di.IoDispatcher
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale
import javax.inject.Inject

@HiltViewModel
class ProgressViewModel
    @Inject
    constructor(
        private val quitRepository: QuitRepository,
        onboardingRepository: OnboardingRepository,
        @IoDispatcher private val ioDispatcher: CoroutineDispatcher,
    ) : ViewModel() {
        val uiState: StateFlow<ProgressUiState> =
            combine(
                quitRepository.observeStats(),
                quitRepository.observeMilestones(),
                onboardingRepository.onboardingData,
            ) { stats, milestones, onboarding ->
                val quitMs = onboarding.quitDateEpochMs
                val weeklySpendCents = onboarding.unitsPerWeek * onboarding.costPerUnitCents
                buildState(stats, milestones, weeklySpendCents, quitMs)
            }.flowOn(ioDispatcher)
                .stateIn(
                    scope = viewModelScope,
                    started = SharingStarted.WhileSubscribed(5_000),
                    initialValue = ProgressUiState(),
                )

        init {
            viewModelScope.launch(ioDispatcher) {
                quitRepository.refreshMilestones(System.currentTimeMillis())
            }
        }

        private fun buildState(
            stats: QuitStats,
            milestones: List<Milestone>,
            weeklySpendCents: Int,
            quitMs: Long,
        ): ProgressUiState {
            val offsets = MilestoneCatalog.definitions.associate { it.key to it.offsetMs }
            val firstPending = milestones.indexOfFirst { !it.achieved }
            val uiMilestones =
                milestones.mapIndexed { index, milestone ->
                    MilestoneUi(
                        key = milestone.key,
                        title = milestone.title,
                        description = milestone.description,
                        offsetLabel = offsets[milestone.key]?.let(::offsetLabel) ?: "",
                        achieved = milestone.achieved,
                        isCurrent = index == firstPending,
                    )
                }
            val percent =
                if (stats.daysQuit > 0) {
                    ((stats.totalCleanDays * 100) / stats.daysQuit).toInt().coerceIn(0, 100)
                } else {
                    0
                }
            val elapsedWeeks = (stats.daysQuit / DAYS_PER_WEEK).coerceAtMost(CHART_WEEKS.toLong())
            val bars =
                (1..CHART_WEEKS).map { week ->
                    weeklySpendCents.toLong() * minOf(week.toLong(), elapsedWeeks)
                }
            val projectionDate =
                Instant
                    .ofEpochMilli(quitMs)
                    .atZone(ZoneId.systemDefault())
                    .toLocalDate()
                    .plusYears(1)
                    .format(DateTimeFormatter.ofPattern("MMM d, yyyy", Locale.getDefault()))
            return ProgressUiState(
                isLoading = false,
                currentStreakDays = stats.currentStreakDays,
                totalCleanDays = stats.totalCleanDays,
                cleanDayPercent = percent,
                moneySavedCents = stats.moneySavedCents,
                projectedYearlyCents = weeklySpendCents.toLong() * WEEKS_PER_YEAR,
                projectionDateLabel = projectionDate,
                weeklyBars = bars.toImmutableList(),
                milestones = uiMilestones.toImmutableList(),
                cravingsBeaten = stats.cravingsBeaten,
            )
        }

        companion object {
            private const val DAYS_PER_WEEK = 7L
            private const val WEEKS_PER_YEAR = 52L
            private const val CHART_WEEKS = 12

            private fun offsetLabel(offsetMs: Long): String {
                val minutes = offsetMs / 60_000L
                return when {
                    minutes < 60 -> "$minutes min"
                    minutes < 24 * 60 -> "${minutes / 60} hr"
                    minutes < 365 * 24 * 60 -> "${minutes / (24 * 60)} days"
                    else -> "${minutes / (365 * 24 * 60)} yr"
                }
            }
        }
    }
