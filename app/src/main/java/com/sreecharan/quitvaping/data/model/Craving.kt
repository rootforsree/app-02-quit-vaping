package com.sreecharan.quitvaping.data.model

import androidx.compose.runtime.Immutable

@Immutable
data class Craving(
    val id: Long,
    val timestampEpochMs: Long,
    /** 1-10. */
    val intensity: Int,
    val trigger: String,
    val resisted: Boolean,
    val notes: String?,
)
