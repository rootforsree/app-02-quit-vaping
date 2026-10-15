package com.sreecharan.quitvaping.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * A slip/relapse log entry. Never resets progress on its own — the repository
 * computes streak vs cumulative clean days from these rows.
 */
@Entity(tableName = "slips")
data class SlipEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val timestampEpochMs: Long,
    val trigger: String?,
    val notes: String?,
)
