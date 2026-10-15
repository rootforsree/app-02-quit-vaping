package com.sreecharan.quitvaping.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * The user's quit plan. Single row (id = 1); onboarding writes it once,
 * Settings > Quit Plan edits it later.
 */
@Entity(tableName = "quit_profile")
data class QuitProfileEntity(
    @PrimaryKey val id: Int = 1,
    val quitDateEpochMs: Long,
    /** DISPOSABLE, POD or REFILLABLE — see [com.sreecharan.quitvaping.data.model.DeviceType]. */
    val deviceType: String,
    val nicotineMgPerMl: Double,
    val puffsPerDay: Int,
    val weeklySpendCents: Int,
    val costPerUnitCents: Int,
    /** JSON array of trigger strings, e.g. ["stress","social"]. */
    val triggersJson: String,
    val createdAtEpochMs: Long,
)
