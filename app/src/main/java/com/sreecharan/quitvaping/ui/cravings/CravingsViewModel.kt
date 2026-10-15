package com.sreecharan.quitvaping.ui.cravings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sreecharan.quitvaping.data.model.Craving
import com.sreecharan.quitvaping.data.repository.CravingRepository
import com.sreecharan.quitvaping.data.repository.QuitRepository
import com.sreecharan.quitvaping.di.IoDispatcher
import com.sreecharan.quitvaping.notifications.QuitNotifications
import com.sreecharan.quitvaping.ui.components.formatTimeAgo
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.LocalTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale
import javax.inject.Inject

@HiltViewModel
class CravingsViewModel
    @Inject
    constructor(
        private val cravingRepository: CravingRepository,
        private val quitRepository: QuitRepository,
        private val quitNotifications: QuitNotifications,
        @IoDispatcher private val ioDispatcher: CoroutineDispatcher,
    ) : ViewModel() {
        private val _sos = MutableStateFlow<SosUiState>(SosUiState.Hidden)
        val sos: StateFlow<SosUiState> = _sos.asStateFlow()

        private val _logIntensity = MutableStateFlow(DEFAULT_INTENSITY)
        val logIntensity: StateFlow<Int> = _logIntensity.asStateFlow()

        private val _logTrigger = MutableStateFlow<String?>(null)
        val logTrigger: StateFlow<String?> = _logTrigger.asStateFlow()

        private var sosJob: Job? = null

        val uiState: StateFlow<CravingsUiState> =
            combine(
                cravingRepository.observeRecentCravings(HISTORY_LIMIT),
                cravingRepository.observeCravingsBeatenCount(),
            ) { recent, beaten ->
                buildState(recent, beaten)
            }.flowOn(ioDispatcher)
                .stateIn(
                    scope = viewModelScope,
                    started = SharingStarted.WhileSubscribed(5_000),
                    initialValue = CravingsUiState(),
                )

        fun onSosOpened() {
            _logIntensity.value = DEFAULT_INTENSITY
            _logTrigger.value = null
            _sos.value = SosUiState.ModeSelect
        }

        fun onSosDismissed() {
            sosJob?.cancel()
            _sos.value = SosUiState.Hidden
        }

        fun onStartRiding() {
            sosJob?.cancel()
            sosJob =
                viewModelScope.launch {
                    for (remaining in RIDE_SECONDS - 1 downTo 0) {
                        _sos.value = SosUiState.Riding(remaining, ridePhase(remaining))
                        delay(SECOND_MS)
                    }
                    _sos.value = SosUiState.LogTrigger
                }
            _sos.value = SosUiState.Riding(RIDE_SECONDS, ridePhase(RIDE_SECONDS))
        }

        fun onStartBreathing() {
            sosJob?.cancel()
            sosJob =
                viewModelScope.launch {
                    val total = BREATH_ROUNDS * BREATH_ROUND_SECONDS
                    for (elapsed in 0 until total) {
                        val round = elapsed / BREATH_ROUND_SECONDS + 1
                        val inRound = elapsed % BREATH_ROUND_SECONDS
                        val phase =
                            when {
                                inRound < INHALE_SECONDS -> BreathPhase.INHALE
                                inRound < INHALE_SECONDS + HOLD_SECONDS -> BreathPhase.HOLD
                                else -> BreathPhase.EXHALE
                            }
                        _sos.value = SosUiState.Breathing(round, phase)
                        delay(SECOND_MS)
                    }
                    _sos.value = SosUiState.LogTrigger
                }
            _sos.value = SosUiState.Breathing(1, BreathPhase.INHALE)
        }

        fun onEndEarly() {
            sosJob?.cancel()
            _sos.value = SosUiState.LogTrigger
        }

        fun onLogIntensityChanged(intensity: Int) {
            _logIntensity.value = intensity.coerceIn(MIN_INTENSITY, MAX_INTENSITY)
        }

        fun onLogTriggerChanged(trigger: String?) {
            _logTrigger.value = trigger
        }

        fun onBeatIt() {
            val intensity = _logIntensity.value
            val trigger = _logTrigger.value
            viewModelScope.launch(ioDispatcher) {
                cravingRepository.logCraving(
                    timestampEpochMs = System.currentTimeMillis(),
                    intensity = intensity,
                    trigger = trigger ?: TRIGGER_UNKNOWN,
                    resisted = true,
                    notes = null,
                )
                _sos.value = SosUiState.DoneResisted
            }
        }

        fun onSlipped() {
            val trigger = _logTrigger.value
            viewModelScope.launch(ioDispatcher) {
                cravingRepository.logSlip(
                    timestampEpochMs = System.currentTimeMillis(),
                    trigger = trigger,
                    notes = null,
                )
                val stats = quitRepository.observeStats().first()
                quitNotifications.postLapseCompassion(stats.totalCleanDays.toLong())
                _sos.value = SosUiState.DoneSlipped(stats.totalCleanDays)
            }
        }

        private fun buildState(recent: List<Craving>, beaten: Int): CravingsUiState {
            val now = System.currentTimeMillis()
            val history =
                recent.map { craving ->
                    CravingHistoryItem(
                        id = craving.id,
                        trigger = craving.trigger,
                        intensity = craving.intensity,
                        resisted = craving.resisted,
                        timeAgo = formatTimeAgo(craving.timestampEpochMs, now),
                    )
                }
            val triggers = recent.map { it.trigger }.filter { it != TRIGGER_UNKNOWN }
            val topTrigger = triggers.groupingBy { it }.eachCount().maxByOrNull { it.value }?.key
            val peakHour =
                recent
                    .map { hourOf(it.timestampEpochMs) }
                    .groupingBy { it }
                    .eachCount()
                    .maxByOrNull { it.value }
                    ?.key
            return CravingsUiState(
                isLoading = false,
                cravingsBeaten = beaten,
                topTrigger = topTrigger,
                peakHourLabel = peakHour?.let { hourLabel(it) },
                history = history.toImmutableList(),
            )
        }

        private fun ridePhase(secondsLeft: Int): Int =
            when {
                secondsLeft > RIDE_PHASE_3 -> 0
                secondsLeft > RIDE_PHASE_2 -> 1
                secondsLeft > RIDE_PHASE_1 -> 2
                else -> 3
            }

        private fun hourOf(epochMs: Long): Int =
            Instant
                .ofEpochMilli(epochMs)
                .atZone(ZoneId.systemDefault())
                .hour

        private fun hourLabel(hour: Int): String =
            LocalTime.of(hour, 0).format(DateTimeFormatter.ofPattern("h a", Locale.getDefault()))

        companion object {
            private const val HISTORY_LIMIT = 50
            private const val SECOND_MS = 1_000L
            private const val RIDE_SECONDS = 300
            private const val RIDE_PHASE_3 = 240
            private const val RIDE_PHASE_2 = 150
            private const val RIDE_PHASE_1 = 90
            private const val BREATH_ROUNDS = 5
            private const val INHALE_SECONDS = 8
            private const val HOLD_SECONDS = 4
            private const val BREATH_ROUND_SECONDS = 20
            private const val DEFAULT_INTENSITY = 5
            private const val MIN_INTENSITY = 1
            private const val MAX_INTENSITY = 10
            private const val TRIGGER_UNKNOWN = "Unknown"
        }
    }
