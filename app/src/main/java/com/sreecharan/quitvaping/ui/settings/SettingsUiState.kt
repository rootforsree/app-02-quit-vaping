package com.sreecharan.quitvaping.ui.settings

import android.net.Uri
import androidx.compose.runtime.Immutable
import com.sreecharan.quitvaping.data.model.DeviceType
import com.sreecharan.quitvaping.data.model.ThemeMode

@Immutable
data class SettingsUiState(
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
    val hapticsEnabled: Boolean = true,
    // Quit plan (source: onboarding data).
    val quitDateEpochMs: Long? = null,
    val deviceType: DeviceType = DeviceType.DISPOSABLE,
    val weeklySpendCents: Int = 0,
    val puffsPerDay: Int = -1,
    // Notifications (source: reminder settings).
    val pledgeEnabled: Boolean = true,
    val pledgeHour: Int = 8,
    val pledgeMinute: Int = 0,
    val milestoneCelebrations: Boolean = true,
    val eveningEnabled: Boolean = false,
    // Subscription.
    val entitlementLoading: Boolean = true,
    val isPremium: Boolean = false,
    val subscriptionSubtitle: String? = null,
    // One-shot UI.
    val isExporting: Boolean = false,
    val shareUri: Uri? = null,
    val message: String? = null,
    val showResetConfirm: Boolean = false,
)
