package com.sreecharan.quitvaping.data.model

import androidx.compose.runtime.Immutable

@Immutable
data class Milestone(
    val key: String,
    val title: String,
    val description: String,
    val achievedAtEpochMs: Long?,
) {
    val achieved: Boolean get() = achievedAtEpochMs != null
}
