package com.sreecharan.quitvaping.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * A single craving event, logged from the SOS flow or the Cravings tab.
 * resisted = true means the user beat it (micro-win); false means it led to a slip.
 */
@Entity(tableName = "cravings")
data class CravingEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val timestampEpochMs: Long,
    /** 1-10. */
    val intensity: Int,
    val trigger: String,
    val resisted: Boolean,
    val notes: String?,
)
