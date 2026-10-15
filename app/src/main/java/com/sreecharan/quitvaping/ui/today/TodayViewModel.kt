package com.sreecharan.quitvaping.ui.today

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sreecharan.quitvaping.data.datastore.OnboardingData
import com.sreecharan.quitvaping.data.datastore.OnboardingRepository
import com.sreecharan.quitvaping.data.model.Milestone
import com.sreecharan.quitvaping.data.model.QuitProfile
import com.sreecharan.quitvaping.data.model.QuitStats
import com.sreecharan.quitvaping.data.repository.MilestoneCatalog
import com.sreecharan.quitvaping.data.repository.QuitRepository
import com.sreecharan.quitvaping.di.IoDispatcher
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.ZoneOffset
import javax.inject.Inject

@HiltViewModel
class TodayViewModel
    @Inject
    constructor(
        private val quitRepository: QuitRepository,
        private val onboardingRepository: OnboardingRepository,
        @IoDispatcher private val ioDispatcher: CoroutineDispatcher,
    ) : ViewModel() {
        private val profileSynced = MutableStateFlow(false)

        private val ticker: Flow<Long> =
            flow {
                while (true) {
                    emit(System.currentTimeMillis())
                    delay(TICK_MS)
                }
            }

        val uiState: StateFlow<TodayUiState> =
            combine(
                quitRepository.observeStats(),
                quitRepository.observeProfile(),
                quitRepository.observeMilestones(),
                onboardingRepository.onboardingData,
                quitRepository.observeCheckInForDay(todayEpochDay()),
                ticker,
            ) { stats, profile, milestones, onboarding, checkIn, nowMs ->
                if (profile == null && onboarding.onboardingCompleted && !profileSynced.value) {
                    syncProfileFromOnboarding(onboarding)
                }
                val quitMs = profile?.quitDateEpochMs ?: onboarding.quitDateEpochMs
                val elapsed = (nowMs - quitMs).coerceAtLeast(0)
                val weeklySpend = profile?.weeklySpendCents ?: onboarding.weeklySpend()
                buildState(stats, checkIn != null, milestones, nowMs, quitMs, elapsed, weeklySpend)
            }.flowOn(ioDispatcher)
                .stateIn(
                    scope = viewModelScope,
                    started = SharingStarted.WhileSubscribed(5_000),
                    initialValue = TodayUiState(),
                )

        init {
            viewModelScope.launch(ioDispatcher) {
                quitRepository.refreshMilestones(System.currentTimeMillis())
            }
        }

        fun onPledge() {
            viewModelScope.launch(ioDispatcher) {
                quitRepository.recordPledge(todayEpochDay(), System.currentTimeMillis())
            }
        }

        fun onEveningReview(rating: Int) {
            viewModelScope.launch(ioDispatcher) {
                quitRepository.recordEveningReview(todayEpochDay(), "", rating)
            }
        }

        private fun syncProfileFromOnboarding(onboarding: OnboardingData) {
            profileSynced.value = true
            viewModelScope.launch(ioDispatcher) {
                if (quitRepository.observeProfile().first() != null) return@launch
                quitRepository.saveProfile(
                    QuitProfile(
                        quitDateEpochMs = onboarding.quitDateEpochMs,
                        deviceType = onboarding.deviceType,
                        nicotineMgPerMl = onboarding.nicotineMgPerMl.toDouble(),
                        puffsPerDay = onboarding.puffsPerDay,
                        weeklySpendCents = onboarding.weeklySpend(),
                        costPerUnitCents = onboarding.costPerUnitCents,
                        triggers = onboarding.triggers.toList(),
                    ),
                )
            }
        }

        private fun buildState(
            stats: QuitStats,
            pledgedToday: Boolean,
            milestones: List<Milestone>,
            nowMs: Long,
            quitMs: Long,
            elapsed: Long,
            weeklySpendCents: Int,
        ): TodayUiState {
            val offsets = MilestoneCatalog.definitions.associate { it.key to it.offsetMs }
            val next = milestones.firstOrNull { !it.achieved }
            val prevOffset =
                milestones
                    .filter { it.achieved }
                    .mapNotNull { offsets[it.key] }
                    .maxOrNull() ?: 0L
            val nextOffset = next?.let { offsets[it.key] } ?: 0L
            val progress =
                if (nextOffset > prevOffset) {
                    ((nowMs - quitMs - prevOffset).toFloat() / (nextOffset - prevOffset)).coerceIn(0f, 1f)
                } else {
                    1f
                }
            val goal = weeklySpendCents.toLong() * GOAL_DAYS / DAYS_PER_WEEK
            return TodayUiState(
                isLoading = false,
                hasProfile = true,
                daysQuit = elapsed / MILLIS_PER_DAY,
                hoursPart = ((elapsed % MILLIS_PER_DAY) / MILLIS_PER_HOUR).toInt(),
                minutesPart = ((elapsed % MILLIS_PER_HOUR) / MILLIS_PER_MINUTE).toInt(),
                secondsPart = ((elapsed % MILLIS_PER_MINUTE) / MILLIS_PER_SECOND).toInt(),
                moneySavedCents = stats.moneySavedCents,
                savingsGoalCents = goal,
                puffsAvoided = stats.puffsAvoided,
                cravingsBeaten = stats.cravingsBeaten,
                pledgedToday = pledgedToday,
                pledgeStreakDays = stats.pledgeStreakDays,
                currentStreakDays = stats.currentStreakDays,
                totalCleanDays = stats.totalCleanDays,
                nextMilestoneTitle = next?.title,
                nextMilestoneDescription = next?.description,
                nextMilestoneProgress = progress,
                nextMilestoneInDays =
                    if (nextOffset > 0) {
                        maxOf(0, (quitMs + nextOffset - nowMs) / MILLIS_PER_DAY)
                    } else {
                        0
                    },
            )
        }

        companion object {
            private const val TICK_MS = 1_000L
            private const val MILLIS_PER_SECOND = 1_000L
            private const val MILLIS_PER_MINUTE = 60 * MILLIS_PER_SECOND
            private const val MILLIS_PER_HOUR = 60 * MILLIS_PER_MINUTE
            private const val MILLIS_PER_DAY = 24 * MILLIS_PER_HOUR
            private const val DAYS_PER_WEEK = 7L
            private const val GOAL_DAYS = 90L

            private fun todayEpochDay(): Long = LocalDate.now(ZoneOffset.UTC).toEpochDay()

            private fun OnboardingData.weeklySpend(): Int = unitsPerWeek * costPerUnitCents
        }
    }
