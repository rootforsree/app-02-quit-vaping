package com.sreecharan.quitvaping.screenshots

import com.github.takahirom.roborazzi.ExperimentalRoborazziApi
import com.github.takahirom.roborazzi.RoborazziOptions
import com.github.takahirom.roborazzi.RoborazziTaskType
import com.github.takahirom.roborazzi.captureRoboImage
import com.sreecharan.quitvaping.ui.progress.MilestoneUi
import com.sreecharan.quitvaping.ui.progress.ProgressScreenContent
import com.sreecharan.quitvaping.ui.progress.ProgressUiState
import com.sreecharan.quitvaping.ui.theme.AppTheme
import kotlinx.collections.immutable.persistentListOf
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.GraphicsMode

/**
 * Roborazzi golden tests for the Progress tab.
 * Deterministic fixture with a mix of achieved and pending milestones.
 */
@OptIn(ExperimentalRoborazziApi::class)
@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class ProgressScreenshotTest {
    @Test
    fun progress_light() {
        capture(darkTheme = false)
    }

    @Test
    fun progress_dark() {
        capture(darkTheme = true)
    }

    private fun capture(darkTheme: Boolean) {
        val milestones =
            persistentListOf(
                MilestoneUi(
                    key = "m20min",
                    title = "20 minutes",
                    description = "Heart rate drops",
                    offsetLabel = "20 min",
                    achieved = true,
                    isCurrent = false,
                ),
                MilestoneUi(
                    key = "m12hr",
                    title = "12 hours",
                    description = "Carbon monoxide normalizes",
                    offsetLabel = "12 hr",
                    achieved = true,
                    isCurrent = false,
                ),
                MilestoneUi(
                    key = "m1month",
                    title = "1 month",
                    description = "Lung function keeps improving",
                    offsetLabel = "1 mo",
                    achieved = false,
                    isCurrent = true,
                ),
                MilestoneUi(
                    key = "m1yr",
                    title = "1 year",
                    description = "Heart disease risk halved",
                    offsetLabel = "1 yr",
                    achieved = false,
                    isCurrent = false,
                ),
            )
        val state =
            ProgressUiState(
                isLoading = false,
                currentStreakDays = 42,
                totalCleanDays = 45,
                cleanDayPercent = 94,
                moneySavedCents = 12_600,
                projectedYearlyCents = 109_500,
                projectionDateLabel = "Jan 1",
                weeklyBars =
                    persistentListOf(
                        900, 1_800, 2_700, 3_600, 4_500, 5_400,
                        6_300, 7_200, 8_100, 9_000, 9_900, 10_800,
                    ),
                milestones = milestones,
                cravingsBeaten = 17,
            )
        captureRoboImage(
            roborazziOptions = RoborazziOptions(taskType = RoborazziTaskType.Record),
        ) {
            AppTheme(darkTheme = darkTheme, hapticsEnabled = false) {
                ProgressScreenContent(state = state)
            }
        }
    }
}
