package com.sreecharan.quitvaping.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sreecharan.quitvaping.backup.BackupManager
import com.sreecharan.quitvaping.data.billing.BillingRepository
import com.sreecharan.quitvaping.data.billing.EntitlementState
import com.sreecharan.quitvaping.data.datastore.OnboardingData
import com.sreecharan.quitvaping.data.datastore.OnboardingRepository
import com.sreecharan.quitvaping.data.datastore.SettingsRepository
import com.sreecharan.quitvaping.data.model.DeviceType
import com.sreecharan.quitvaping.data.model.QuitProfile
import com.sreecharan.quitvaping.data.model.ThemeMode
import com.sreecharan.quitvaping.data.reminders.ReminderSettingsRepository
import com.sreecharan.quitvaping.data.repository.QuitRepository
import com.sreecharan.quitvaping.di.IoDispatcher
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel
    @Inject
    constructor(
        private val settingsRepository: SettingsRepository,
        private val onboardingRepository: OnboardingRepository,
        private val reminderSettingsRepository: ReminderSettingsRepository,
        private val quitRepository: QuitRepository,
        private val billingRepository: BillingRepository,
        private val backupManager: BackupManager,
        @IoDispatcher private val ioDispatcher: CoroutineDispatcher,
    ) : ViewModel() {
        private val oneShot = MutableStateFlow(OneShot())

        val uiState: StateFlow<SettingsUiState> =
            combine(
                settingsRepository.settings,
                onboardingRepository.onboardingData,
                reminderSettingsRepository.settings,
                billingRepository.entitlement,
                oneShot,
            ) { settings, onboarding, reminders, entitlement, shot ->
                val (premium, loading, sub) = entitlementUi(entitlement)
                SettingsUiState(
                    themeMode = settings.themeMode,
                    hapticsEnabled = settings.hapticsEnabled,
                    quitDateEpochMs = onboarding.quitDateEpochMs,
                    deviceType = onboarding.deviceType,
                    weeklySpendCents = onboarding.unitsPerWeek * onboarding.costPerUnitCents,
                    puffsPerDay = onboarding.puffsPerDay,
                    pledgeEnabled = reminders.pledgeEnabled,
                    pledgeHour = reminders.pledgeHour,
                    pledgeMinute = reminders.pledgeMinute,
                    milestoneCelebrations = onboarding.milestoneCelebrations,
                    eveningEnabled = reminders.eveningReflectionEnabled,
                    entitlementLoading = loading,
                    isPremium = premium,
                    subscriptionSubtitle = sub,
                    isExporting = shot.isExporting,
                    shareUri = shot.shareUri,
                    message = shot.message,
                    showResetConfirm = shot.showResetConfirm,
                )
            }.flowOn(ioDispatcher)
                .stateIn(
                    scope = viewModelScope,
                    started = SharingStarted.WhileSubscribed(5_000),
                    initialValue = SettingsUiState(),
                )

        fun onThemeModeSelected(mode: ThemeMode) {
            viewModelScope.launch(ioDispatcher) {
                settingsRepository.setThemeMode(mode)
            }
        }

        fun onHapticsToggled(enabled: Boolean) {
            viewModelScope.launch(ioDispatcher) {
                settingsRepository.setHapticsEnabled(enabled)
            }
        }

        fun onQuitDateChanged(quitDateEpochMs: Long) {
            viewModelScope.launch(ioDispatcher) {
                val current = onboardingRepository.onboardingData.first()
                val updated = current.copy(quitDateEpochMs = quitDateEpochMs)
                onboardingRepository.saveOnboarding(updated)
                syncRoomProfile(updated)
                quitRepository.refreshMilestones(System.currentTimeMillis())
            }
        }

        fun onWeeklySpendChanged(weeklySpendCents: Int) {
            viewModelScope.launch(ioDispatcher) {
                val current = onboardingRepository.onboardingData.first()
                // Keep weekly spend exact: one unit per week at the new price.
                val updated =
                    current.copy(
                        unitsPerWeek = 1,
                        costPerUnitCents = weeklySpendCents.coerceAtLeast(0),
                    )
                onboardingRepository.saveOnboarding(updated)
                syncRoomProfile(updated)
            }
        }

        fun onPuffsPerDayChanged(puffsPerDay: Int) {
            viewModelScope.launch(ioDispatcher) {
                val current = onboardingRepository.onboardingData.first()
                val updated = current.copy(puffsPerDay = puffsPerDay)
                onboardingRepository.saveOnboarding(updated)
                syncRoomProfile(updated)
            }
        }

        fun onDeviceTypeChanged(deviceType: DeviceType) {
            viewModelScope.launch(ioDispatcher) {
                val current = onboardingRepository.onboardingData.first()
                val updated = current.copy(deviceType = deviceType)
                onboardingRepository.saveOnboarding(updated)
                syncRoomProfile(updated)
            }
        }

        fun onPledgeToggled(enabled: Boolean) {
            viewModelScope.launch(ioDispatcher) {
                val reminders = reminderSettingsRepository.settings.first()
                reminderSettingsRepository.setPledge(enabled, reminders.pledgeHour, reminders.pledgeMinute)
            }
        }

        fun onPledgeTimeChanged(hour: Int, minute: Int) {
            viewModelScope.launch(ioDispatcher) {
                val reminders = reminderSettingsRepository.settings.first()
                reminderSettingsRepository.setPledge(reminders.pledgeEnabled, hour, minute)
                val current = onboardingRepository.onboardingData.first()
                onboardingRepository.saveOnboarding(current.copy(pledgeHour = hour, pledgeMinute = minute))
            }
        }

        fun onMilestoneCelebrationsChanged(enabled: Boolean) {
            viewModelScope.launch(ioDispatcher) {
                val current = onboardingRepository.onboardingData.first()
                onboardingRepository.saveOnboarding(current.copy(milestoneCelebrations = enabled))
            }
        }

        fun onEveningToggled(enabled: Boolean) {
            viewModelScope.launch(ioDispatcher) {
                val reminders = reminderSettingsRepository.settings.first()
                reminderSettingsRepository.setEveningReflection(
                    enabled,
                    reminders.eveningHour,
                    reminders.eveningMinute,
                )
            }
        }

        fun onExportData() {
            viewModelScope.launch(ioDispatcher) {
                oneShot.update { it.copy(isExporting = true) }
                runCatching { backupManager.export() }
                    .onSuccess { uri -> oneShot.update { it.copy(isExporting = false, shareUri = uri) } }
                    .onFailure { oneShot.update { it.copy(isExporting = false, message = it.message) } }
            }
        }

        fun onShareConsumed() {
            oneShot.update { it.copy(shareUri = null) }
        }

        fun onMessageConsumed() {
            oneShot.update { it.copy(message = null) }
        }

        fun onRestorePurchases() {
            viewModelScope.launch(ioDispatcher) {
                val found = runCatching { billingRepository.restore() }.getOrDefault(false)
                oneShot.update { it.copy(message = if (found) RESTORE_FOUND else RESTORE_EMPTY) }
            }
        }

        fun onResetClicked() {
            oneShot.update { it.copy(showResetConfirm = true) }
        }

        fun onResetDismissed() {
            oneShot.update { it.copy(showResetConfirm = false) }
        }

        fun onResetConfirmed(onComplete: () -> Unit) {
            viewModelScope.launch(ioDispatcher) {
                quitRepository.resetAll()
                oneShot.update { it.copy(showResetConfirm = false) }
                onComplete()
            }
        }

        private suspend fun syncRoomProfile(onboarding: OnboardingData) {
            val existing = quitRepository.observeProfile().first()
            quitRepository.saveProfile(
                QuitProfile(
                    quitDateEpochMs = onboarding.quitDateEpochMs,
                    deviceType = onboarding.deviceType,
                    nicotineMgPerMl = onboarding.nicotineMgPerMl.toDouble(),
                    puffsPerDay = onboarding.puffsPerDay,
                    weeklySpendCents = onboarding.unitsPerWeek * onboarding.costPerUnitCents,
                    costPerUnitCents = onboarding.costPerUnitCents,
                    triggers = onboarding.triggers.toList(),
                ),
            )
            if (existing == null) {
                quitRepository.refreshMilestones(System.currentTimeMillis())
            }
        }

        private fun entitlementUi(entitlement: EntitlementState): Triple<Boolean, Boolean, String?> =
            when (entitlement) {
                EntitlementState.Loading -> Triple(false, true, null)
                EntitlementState.Unavailable -> Triple(false, false, null)
                is EntitlementState.Ready ->
                    Triple(
                        entitlement.isPremium,
                        false,
                        entitlement.status?.let { status ->
                            if (status.isInTrial) TRIAL_SUB else null
                        },
                    )
            }

        private data class OneShot(
            val isExporting: Boolean = false,
            val shareUri: android.net.Uri? = null,
            val message: String? = null,
            val showResetConfirm: Boolean = false,
        )

        companion object {
            private const val RESTORE_FOUND = "Purchases restored."
            private const val RESTORE_EMPTY = "No purchases found on this account."
            private const val TRIAL_SUB = "Trial active"
        }
    }
