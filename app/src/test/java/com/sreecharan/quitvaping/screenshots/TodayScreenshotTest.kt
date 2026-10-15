package com.sreecharan.quitvaping.screenshots

import com.github.takahirom.roborazzi.ExperimentalRoborazziApi
import com.github.takahirom.roborazzi.RoborazziOptions
import com.github.takahirom.roborazzi.RoborazziTaskType
import com.github.takahirom.roborazzi.captureRoboImage
import com.sreecharan.quitvaping.ui.theme.AppTheme
import com.sreecharan.quitvaping.ui.today.TodayScreenContent
import com.sreecharan.quitvaping.ui.today.TodayUiState
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.GraphicsMode

/**
 * Roborazzi golden tests for the Today tab.
 * Deterministic fixture; the live ticker is frozen by the fixed state.
 */
@OptIn(ExperimentalRoborazziApi::class)
@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class TodayScreenshotTest {
    @Test
    fun today_light() {
        capture(darkTheme = false)
    }

    @Test
    fun today_dark() {
        capture(darkTheme = true)
    }

    private fun capture(darkTheme: Boolean) {
        val state =
            TodayUiState(
                isLoading = false,
                hasProfile = true,
                daysQuit = 42,
                hoursPart = 7,
                minutesPart = 23,
                secondsPart = 11,
                moneySavedCents = 12_600,
                savingsGoalCents = 45_000,
                puffsAvoided = 840,
                cravingsBeaten = 17,
                pledgedToday = true,
                pledgeStreakDays = 12,
                currentStreakDays = 42,
                totalCleanDays = 45,
                nextMilestoneTitle = "1 month",
                nextMilestoneDescription = "Lung function keeps improving",
                nextMilestoneProgress = 0.7f,
                nextMilestoneInDays = 18,
            )
        captureRoboImage(
            roborazziOptions = RoborazziOptions(taskType = RoborazziTaskType.Record),
        ) {
            AppTheme(darkTheme = darkTheme, hapticsEnabled = false) {
                TodayScreenContent(
                    state = state,
                    onPledge = {},
                    onEveningReview = {},
                    onOpenCravings = {},
                    onOpenOnboarding = {},
                )
            }
        }
    }
}
