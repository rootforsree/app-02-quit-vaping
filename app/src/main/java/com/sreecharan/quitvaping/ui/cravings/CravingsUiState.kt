package com.sreecharan.quitvaping.ui.cravings

import androidx.compose.runtime.Immutable
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf

@Immutable
data class CravingHistoryItem(
    val id: Long,
    val trigger: String,
    val intensity: Int,
    val resisted: Boolean,
    /** Preformatted, e.g. "3h ago". */
    val timeAgo: String,
)

enum class BreathPhase {
    INHALE,
    HOLD,
    EXHALE,
}

/**
 * The SOS flow is a small state machine. It lives in the ViewModel so a
 * rotation mid-craving does not lose the timer.
 */
sealed interface SosUiState {
    data object Hidden : SosUiState

    data object ModeSelect : SosUiState

    data class Riding(
        val secondsLeft: Int,
        val phaseIndex: Int,
    ) : SosUiState

    data class Breathing(
        /** 1..5. */
        val round: Int,
        val phase: BreathPhase,
    ) : SosUiState

    data object LogTrigger : SosUiState

    data object DoneResisted : SosUiState

    data class DoneSlipped(
        val totalCleanDays: Long,
    ) : SosUiState
}

@Immutable
data class CravingsUiState(
    val isLoading: Boolean = true,
    val cravingsBeaten: Int = 0,
    val topTrigger: String? = null,
    /** e.g. "3 PM". Null until there is enough history. */
    val peakHourLabel: String? = null,
    val history: ImmutableList<CravingHistoryItem> = persistentListOf(),
)
