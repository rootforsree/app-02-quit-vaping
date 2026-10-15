package com.sreecharan.quitvaping.data.local

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * One row per calendar day: the morning pledge and the optional evening review.
 * dateEpochDay is days since epoch (UTC) so one pledge exists per day.
 */
@Entity(
    tableName = "check_ins",
    indices = [Index(value = ["dateEpochDay"], unique = true)],
)
data class CheckInEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val dateEpochDay: Long,
    val pledgedAtEpochMs: Long,
    val eveningReview: String?,
    /** 1-5, how the day went. Null until the evening review is filled in. */
    val eveningRating: Int?,
)
