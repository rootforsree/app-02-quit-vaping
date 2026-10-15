package com.sreecharan.quitvaping.screenshots

import com.github.takahirom.roborazzi.ExperimentalRoborazziApi
import com.github.takahirom.roborazzi.RoborazziOptions
import com.github.takahirom.roborazzi.RoborazziTaskType
import com.github.takahirom.roborazzi.captureRoboImage
import com.sreecharan.quitvaping.ui.cravings.CravingHistoryItem
import com.sreecharan.quitvaping.ui.cravings.CravingsScreenContent
import com.sreecharan.quitvaping.ui.cravings.CravingsUiState
import com.sreecharan.quitvaping.ui.cravings.SosUiState
import com.sreecharan.quitvaping.ui.theme.AppTheme
import kotlinx.collections.immutable.persistentListOf
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.GraphicsMode

/**
 * Roborazzi golden tests for the Cravings tab.
 * SOS dialog stays hidden; only the main tab content is captured.
 */
@OptIn(ExperimentalRoborazziApi::class)
@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class CravingsScreenshotTest {
    @Test
    fun cravings_light() {
        capture(darkTheme = false)
    }

    @Test
    fun cravings_dark() {
        capture(darkTheme = true)
    }

    private fun capture(darkTheme: Boolean) {
        val state =
            CravingsUiState(
                isLoading = false,
                cravingsBeaten = 17,
                topTrigger = "Stress",
                peakHourLabel = "3 PM",
                history =
                    persistentListOf(
                        CravingHistoryItem(
                            id = 1,
                            trigger = "Stress",
                            intensity = 7,
                            resisted = true,
                            timeAgo = "2h ago",
                        ),
                        CravingHistoryItem(
                            id = 2,
                            trigger = "After meals",
                            intensity = 5,
                            resisted = true,
                            timeAgo = "1d ago",
                        ),
                        CravingHistoryItem(
                            id = 3,
                            trigger = "Social",
                            intensity = 8,
                            resisted = false,
                            timeAgo = "3d ago",
                        ),
                    ),
            )
        captureRoboImage(
            roborazziOptions = RoborazziOptions(taskType = RoborazziTaskType.Record),
        ) {
            AppTheme(darkTheme = darkTheme, hapticsEnabled = false) {
                CravingsScreenContent(
                    state = state,
                    sos = SosUiState.Hidden,
                    logIntensity = 5,
                    logTrigger = null,
                    onSosOpened = {},
                    onSosDismissed = {},
                    onStartRiding = {},
                    onStartBreathing = {},
                    onEndEarly = {},
                    onLogIntensityChanged = {},
                    onLogTriggerChanged = {},
                    onBeatIt = {},
                    onSlipped = {},
                )
            }
        }
    }
}
