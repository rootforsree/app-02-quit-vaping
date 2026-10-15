package com.sreecharan.quitvaping.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Health-recovery milestones, seeded from [MilestoneCatalog] when the quit
 * profile is saved. achievedAtEpochMs is null until the milestone is reached.
 */
@Entity(tableName = "milestones")
data class MilestoneEntity(
    @PrimaryKey val key: String,
    val title: String,
    val description: String,
    val sortOrder: Int,
    val achievedAtEpochMs: Long?,
)
