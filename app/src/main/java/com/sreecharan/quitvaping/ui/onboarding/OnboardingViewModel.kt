package com.sreecharan.quitvaping.ui.onboarding

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sreecharan.quitvaping.data.model.DeviceType
import com.sreecharan.quitvaping.data.datastore.OnboardingData
import com.sreecharan.quitvaping.data.datastore.OnboardingRepository
import com.sreecharan.quitvaping.data.reminders.ReminderSettingsRepository
import com.sreecharan.quitvaping.notifications.ReminderScheduler
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/** Trigger labels offered on screen 5. Kept as plain strings for Room/DataStore simplicity. */
val ONBOARDING_TRIGGERS =
    listOf(
        "Stress",
        "Boredom",
        "Social",
        "After meals",
        "Alcohol",
        "Habit",
        "Other",
    )

/** Puff-per-day quick picks on screen 4. -1 = "Not sure". */
val PUFF_OPTIONS =
    listOf(
        "Under 50" to 25,
        "50\u2013100" to 75,
        "100\u2013200" to 150,
        "200+" to 250,
        "Not sure" to -1,
    )

data class OnboardingUiState(
    val quitDateEpochMs: Long = 0L,
    val hasQuitDate: Boolean = true,
    val deviceType: DeviceType = DeviceType.DISPOSABLE,
    val unitsPerWeek: Int = 2,
    val costPerUnitCents: Int = 1500,
    val puffsPerDay: Int = -1,
    val nicotineMgPerMl: Float = 0f,
    val triggers: Set<String> = emptySet(),
    val pledgeHour: Int = 8,
    val pledgeMinute: Int = 0,
    val milestoneCelebrations: Boolean = true,
    val isSaving: Boolean = false,
) {
    /** Live yearly savings shown on screen 3 as the user types. */
    val yearlySavingsDollars: Double
        get() = unitsPerWeek * (costPerUnitCents / 100.0) * 52

    fun toData(): OnboardingData =
        OnboardingData(
            quitDateEpochMs = quitDateEpochMs,
            deviceType = deviceType,
            unitsPerWeek = unitsPerWeek,
            costPerUnitCents = costPerUnitCents,
            puffsPerDay = puffsPerDay,
            nicotineMgPerMl = nicotineMgPerMl,
            triggers = triggers,
            pledgeHour = pledgeHour,
            pledgeMinute = pledgeMinute,
            milestoneCelebrations = milestoneCelebrations,
            onboardingCompleted = true,
        )
}

@HiltViewModel
class OnboardingViewModel
    @Inject
    constructor(
        private val repository: OnboardingRepository,
        private val reminderSettingsRepository: ReminderSettingsRepository,
        private val reminderScheduler: ReminderScheduler,
    ) : ViewModel() {
        private val _uiState = MutableStateFlow(OnboardingUiState())
        val uiState: StateFlow<OnboardingUiState> = _uiState.asStateFlow()

        init {
            viewModelScope.launch {
                val existing = repository.onboardingData.first()
                _uiState.update {
                    it.copy(
                        quitDateEpochMs = existing.quitDateEpochMs,
                        deviceType = existing.deviceType,
                        unitsPerWeek = existing.unitsPerWeek,
                        costPerUnitCents = existing.costPerUnitCents,
                        puffsPerDay = existing.puffsPerDay,
                        nicotineMgPerMl = existing.nicotineMgPerMl,
                        triggers = existing.triggers,
                        pledgeHour = existing.pledgeHour,
                        pledgeMinute = existing.pledgeMinute,
                        milestoneCelebrations = existing.milestoneCelebrations,
                    )
                }
            }
        }

        fun onQuitDateChange(epochMs: Long) = _uiState.update { it.copy(quitDateEpochMs = epochMs, hasQuitDate = true) }

        fun onPlanModeChange(hasQuitDate: Boolean) = _uiState.update { it.copy(hasQuitDate = hasQuitDate) }

        fun onDeviceTypeChange(type: DeviceType) = _uiState.update { it.copy(deviceType = type) }

        fun onUnitsPerWeekChange(units: Int) = _uiState.update { it.copy(unitsPerWeek = units.coerceIn(1, 50)) }

        fun onCostPerUnitChange(cents: Int) = _uiState.update { it.copy(costPerUnitCents = cents.coerceIn(100, 10000)) }

        fun onPuffsPerDayChange(puffs: Int) = _uiState.update { it.copy(puffsPerDay = puffs) }

        fun onNicotineChange(mg: Float) = _uiState.update { it.copy(nicotineMgPerMl = mg.coerceIn(0f, 60f)) }

        fun onTriggerToggle(trigger: String) =
            _uiState.update { state ->
                val updated =
                    if (trigger in state.triggers) state.triggers - trigger else state.triggers + trigger
                state.copy(triggers = updated)
            }

        fun onPledgeTimeChange(
            hour: Int,
            minute: Int,
        ) = _uiState.update { it.copy(pledgeHour = hour, pledgeMinute = minute) }

        fun onMilestoneCelebrationsChange(enabled: Boolean) =
            _uiState.update { it.copy(milestoneCelebrations = enabled) }

        /** Persists everything and marks onboarding complete. Calls [onDone] on success. */
        fun onComplete(onDone: () -> Unit) {
            if (_uiState.value.isSaving) return
            _uiState.update { it.copy(isSaving = true) }
            viewModelScope.launch {
                val data = _uiState.value.toData()
                repository.saveOnboarding(data)
                // Wire up notifications: sync quit date/spend/pledge time, then schedule alarms
                reminderSettingsRepository.setQuitDate(data.quitDateEpochMs)
                reminderSettingsRepository.setWeeklySpendCents((data.unitsPerWeek * data.costPerUnitCents).toLong())
                reminderSettingsRepository.setPledge(true, data.pledgeHour, data.pledgeMinute)
                reminderScheduler.scheduleAll()
                onDone()
            }
        }

        /** Skip path: persist current answers (or defaults) and finish. */
        fun onSkip(onDone: () -> Unit) {
            viewModelScope.launch {
                repository.saveOnboarding(_uiState.value.toData())
                onDone()
            }
        }
    }
