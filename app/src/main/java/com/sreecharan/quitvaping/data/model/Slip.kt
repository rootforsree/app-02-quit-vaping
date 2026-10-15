package com.sreecharan.quitvaping.data.model

import androidx.compose.runtime.Immutable

@Immutable
data class Slip(
    val id: Long,
    val timestampEpochMs: Long,
    val trigger: String?,
    val notes: String?,
)
