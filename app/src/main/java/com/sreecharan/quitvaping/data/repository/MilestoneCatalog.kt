package com.sreecharan.quitvaping.data.repository

/**
 * Static health-recovery milestones, seeded into the milestones table when the
 * quit profile is saved. Timelines sourced from CDC / VA / ACS cessation data.
 * Offsets are milliseconds after the quit datetime.
 */
object MilestoneCatalog {
    data class Definition(
        val key: String,
        val title: String,
        val description: String,
        val offsetMs: Long,
    )

    private const val MIN = 60_000L
    private const val HOUR = 60 * MIN
    private const val DAY = 24 * HOUR

    val definitions: List<Definition> =
        listOf(
            Definition(
                key = "20min",
                title = "Heart rate drops",
                description = "Heart rate and blood pressure begin to drop back toward normal.",
                offsetMs = 20 * MIN,
            ),
            Definition(
                key = "12h",
                title = "Carbon monoxide clears",
                description = "Carbon monoxide levels drop and blood oxygen normalizes.",
                offsetMs = 12 * HOUR,
            ),
            Definition(
                key = "24h",
                title = "Nicotine nearly gone",
                description = "Nicotine is nearly cleared from your blood. Heart-attack risk starts declining.",
                offsetMs = 24 * HOUR,
            ),
            Definition(
                key = "48h",
                title = "Withdrawal peaks — hold on",
                description = "The hardest window. Taste and smell are already improving. This peak passes.",
                offsetMs = 48 * HOUR,
            ),
            Definition(
                key = "72h",
                title = "Nicotine-free body",
                description = "Nicotine is fully out of your body. Breathing feels easier.",
                offsetMs = 72 * HOUR,
            ),
            Definition(
                key = "7d",
                title = "Cravings ease",
                description = "Cravings are shorter and less frequent than week one.",
                offsetMs = 7 * DAY,
            ),
            Definition(
                key = "14d",
                title = "Circulation improves",
                description = "Circulation improves and lung cilia start regrowing.",
                offsetMs = 14 * DAY,
            ),
            Definition(
                key = "30d",
                title = "Lungs recovering",
                description = "Lung function is measurably better. Walking feels easier.",
                offsetMs = 30 * DAY,
            ),
            Definition(
                key = "90d",
                title = "Breathing easier",
                description = "Coughing and shortness of breath keep decreasing.",
                offsetMs = 90 * DAY,
            ),
            Definition(
                key = "180d",
                title = "Infection risk drops",
                description = "Lung infection risk drops as airways keep healing.",
                offsetMs = 180 * DAY,
            ),
            Definition(
                key = "365d",
                title = "Heart risk halved",
                description = "Risk of coronary heart disease is now half that of a vaper.",
                offsetMs = 365 * DAY,
            ),
            Definition(
                key = "1825d",
                title = "Stroke risk drops",
                description = "Stroke risk drops; mouth and throat cancer risk is halved.",
                offsetMs = 1825 * DAY,
            ),
            Definition(
                key = "3650d",
                title = "Lung cancer risk halved",
                description = "Risk of dying from lung cancer is about half that of a vaper.",
                offsetMs = 3650 * DAY,
            ),
        )
}
