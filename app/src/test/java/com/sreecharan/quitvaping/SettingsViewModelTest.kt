package com.sreecharan.quitvaping

import app.cash.turbine.test
import com.sreecharan.quitvaping.data.datastore.AppSettings
import com.sreecharan.quitvaping.data.datastore.SettingsRepository
import com.sreecharan.quitvaping.data.model.ThemeMode
import com.sreecharan.quitvaping.ui.settings.SettingsUiState
import com.sreecharan.quitvaping.ui.settings.SettingsViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

private class FakeSettingsRepository : SettingsRepository {
    private val state = MutableStateFlow(AppSettings(ThemeMode.SYSTEM, hapticsEnabled = true))

    override val settings: Flow<AppSettings> = state

    override suspend fun setThemeMode(mode: ThemeMode) {
        state.value = state.value.copy(themeMode = mode)
    }

    override suspend fun setHapticsEnabled(enabled: Boolean) {
        state.value = state.value.copy(hapticsEnabled = enabled)
    }
}

@OptIn(ExperimentalCoroutinesApi::class)
class SettingsViewModelTest {
    private val testDispatcher = StandardTestDispatcher()

    @BeforeEach
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
    }

    @AfterEach
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `emits default settings`() =
        runTest(testDispatcher) {
            val viewModel = SettingsViewModel(FakeSettingsRepository())

            viewModel.uiState.test {
                assertEquals(SettingsUiState(), awaitItem())
                cancelAndIgnoreRemainingEvents()
            }
        }

    @Test
    fun `selecting dark updates theme`() =
        runTest(testDispatcher) {
            val viewModel = SettingsViewModel(FakeSettingsRepository())

            viewModel.uiState.test {
                assertEquals(ThemeMode.SYSTEM, awaitItem().themeMode)

                viewModel.onThemeModeSelected(ThemeMode.DARK)

                assertEquals(ThemeMode.DARK, awaitItem().themeMode)
                cancelAndIgnoreRemainingEvents()
            }
        }

    @Test
    fun `toggling haptics updates flag`() =
        runTest(testDispatcher) {
            val viewModel = SettingsViewModel(FakeSettingsRepository())

            viewModel.uiState.test {
                assertTrue(awaitItem().hapticsEnabled)

                viewModel.onHapticsToggled(false)

                assertFalse(awaitItem().hapticsEnabled)
                cancelAndIgnoreRemainingEvents()
            }
        }
}
