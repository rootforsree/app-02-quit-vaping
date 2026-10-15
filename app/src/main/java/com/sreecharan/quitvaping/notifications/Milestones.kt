package com.sreecharan.quitvaping.notifications

import java.time.Duration

/**
 * Science-backed health recovery milestones (CDC / VA / ACS timelines).
 * Kept short on purpose: users complain about padded filler achievements.
 */
data class Milestone(
    val id: String,
    val after: Duration,
    val title: String,
    val message: String,
)

object Milestones {
    val ALL: List<Milestone> =
        listOf(
            Milestone(
                id = "m_20min",
                after = Duration.ofMinutes(20),
                title = "20 minutes clean",
                message = "Your heart rate and blood pressure are already dropping.",
            ),
            Milestone(
                id = "m_12h",
                after = Duration.ofHours(12),
                title = "12 hours clean",
                message = "Carbon monoxide is dropping and oxygen is normalizing.",
            ),
            Milestone(
                id = "m_24h",
                after = Duration.ofHours(24),
                title = "1 full day",
                message = "Nicotine is nearly cleared from your blood. Huge.",
            ),
            Milestone(
                id = "m_72h",
                after = Duration.ofHours(72),
                title = "3 days: nicotine-free",
                message = "Nicotine is fully out of your body. The hardest window is behind you.",
            ),
            Milestone(
                id = "m_1w",
                after = Duration.ofDays(7),
                title = "1 week clean",
                message = "Cravings are getting shorter and less frequent. Keep going.",
            ),
            Milestone(
                id = "m_2w",
                after = Duration.ofDays(14),
                title = "2 weeks clean",
                message = "Your lungs are clearing. Circulation is improving every day.",
            ),
            Milestone(
                id = "m_1mo",
                after = Duration.ofDays(30),
                title = "1 month clean",
                message = "Your lung function is measurably better. Walking feels easier.",
            ),
            Milestone(
                id = "m_3mo",
                after = Duration.ofDays(90),
                title = "3 months clean",
                message = "Cilia are regrowing in your lungs. Your body is healing.",
            ),
            Milestone(
                id = "m_6mo",
                after = Duration.ofDays(180),
                title = "6 months clean",
                message = "Coughing and shortness of breath are fading. Infection risk is down.",
            ),
            Milestone(
                id = "m_1y",
                after = Duration.ofDays(365),
                title = "1 year clean",
                message = "Your coronary heart disease risk is now HALVED. Incredible.",
            ),
            Milestone(
                id = "m_5y",
                after = Duration.ofDays(365 * 5),
                title = "5 years clean",
                message = "Stroke risk dropping. Mouth and throat cancer risk halved.",
            ),
            Milestone(
                id = "m_10y",
                after = Duration.ofDays(365 * 10),
                title = "10 years clean",
                message = "Your lung cancer death risk is about half what it was. Legend.",
            ),
        )

    /** Milestones whose time has passed and that have not fired yet. */
    fun due(
        quitEpochMillis: Long,
        firedIds: Set<String>,
        nowEpochMillis: Long = System.currentTimeMillis(),
    ): List<Milestone> {
        val elapsed = QuitDate.elapsedSince(quitEpochMillis, nowEpochMillis)
        return ALL.filter { it.id !in firedIds && elapsed >= it.after }
    }
}
