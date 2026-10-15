package com.sreecharan.quitvaping.data.model

import androidx.compose.runtime.Immutable

/** Vape device category, captured at onboarding. */
enum class DeviceType {
    DISPOSABLE,
    POD,
    REFILLABLE,
}

@Immutable
data class QuitProfile(
    val quitDateEpochMs: Long,
    val deviceType: DeviceType,
    val nicotineMgPerMl: Double,
    val puffsPerDay: Int,
    val weeklySpendCents: Int,
    val costPerUnitCents: Int,
    val triggers: List<String>,
)
