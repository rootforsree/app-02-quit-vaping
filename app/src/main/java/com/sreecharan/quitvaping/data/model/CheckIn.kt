package com.sreecharan.quitvaping.data.model

import androidx.compose.runtime.Immutable

@Immutable
data class CheckIn(
    val id: Long,
    /** Days since epoch (UTC). */
    val dateEpochDay: Long,
    val pledgedAtEpochMs: Long,
    val eveningReview: String?,
    /** 1-5. */
    val eveningRating: Int?,
)
