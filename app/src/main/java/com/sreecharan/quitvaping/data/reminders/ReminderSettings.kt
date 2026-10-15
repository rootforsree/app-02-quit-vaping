package com.sreecharan.quitvaping.data.reminders

/**
 * Everything the reminder system needs, in one place.
 *
 * Owned by the notifications module and stored in the Preferences DataStore
 * file "quit_prefs" (see [ReminderSettingsRepository]). The onboarding flow
 * writes quit date + spend here; Settings edits the reminder prefs.
 */
data class ReminderSettings(
    /** Epoch millis of the quit moment. Null until onboarding completes. */
    val quitDateEpochMillis: Long?,
    /** Morning pledge reminder. */
    val pledgeEnabled: Boolean,
    val pledgeHour: Int,
    val pledgeMinute: Int,
    /** Up to 3 daily high-risk times, each as (hour, minute). */
    val highRiskTimes: List<Pair<Int, Int>>,
    /** Optional evening reflection. */
    val eveningReflectionEnabled: Boolean,
    val eveningHour: Int,
    val eveningMinute: Int,
    /** Weekly vape spend in cents, for the money-saved counter. */
    val weeklySpendCents: Long,
    /** Money thresholds already celebrated (cents), so they fire once. */
    val firedMoneyThresholds: Set<Long>,
    /** Milestone ids already celebrated, so they fire once. */
    val firedMilestones: Set<String>,
) {
    companion object {
        val DEFAULT =
            ReminderSettings(
                quitDateEpochMillis = null,
                pledgeEnabled = true,
                pledgeHour = 8,
                pledgeMinute = 0,
                highRiskTimes = emptyList(),
                eveningReflectionEnabled = false,
                eveningHour = 21,
                eveningMinute = 0,
                weeklySpendCents = 0L,
                firedMoneyThresholds = emptySet(),
                firedMilestones = emptySet(),
            )
    }
}
