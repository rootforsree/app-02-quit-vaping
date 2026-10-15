package com.sreecharan.quitvaping.ui.onboarding

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.sreecharan.quitvaping.ui.theme.AppDimens
import com.sreecharan.quitvaping.ui.theme.AppTypography
import com.sreecharan.quitvaping.ui.theme.LocalAppColors
import com.sreecharan.quitvaping.ui.theme.rememberHapticTick
import kotlinx.coroutines.launch

const val ONBOARDING_PAGE_COUNT = 7

/**
 * Thin stateful route. Owns the ViewModel and the pager state; pages stay
 * stateless. Screen 7 ("You're ready") navigates to the paywall stub via
 * [onFinishOnboarding]; the payments agent replaces the stub.
 */
@Composable
fun OnboardingRoute(
    onFinishOnboarding: () -> Unit,
    viewModel: OnboardingViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    OnboardingScreen(
        state = state,
        onQuitDateChange = viewModel::onQuitDateChange,
        onPlanModeChange = viewModel::onPlanModeChange,
        onDeviceTypeChange = viewModel::onDeviceTypeChange,
        onUnitsPerWeekChange = viewModel::onUnitsPerWeekChange,
        onCostPerUnitChange = viewModel::onCostPerUnitChange,
        onPuffsPerDayChange = viewModel::onPuffsPerDayChange,
        onNicotineChange = viewModel::onNicotineChange,
        onTriggerToggle = viewModel::onTriggerToggle,
        onPledgeTimeChange = viewModel::onPledgeTimeChange,
        onMilestoneCelebrationsChange = viewModel::onMilestoneCelebrationsChange,
        onSkip = { viewModel.onSkip(onFinishOnboarding) },
        onComplete = { viewModel.onComplete(onFinishOnboarding) },
    )
}

@Composable
fun OnboardingScreen(
    state: OnboardingUiState,
    onQuitDateChange: (Long) -> Unit,
    onPlanModeChange: (Boolean) -> Unit,
    onDeviceTypeChange: (com.sreecharan.quitvaping.data.model.DeviceType) -> Unit,
    onUnitsPerWeekChange: (Int) -> Unit,
    onCostPerUnitChange: (Int) -> Unit,
    onPuffsPerDayChange: (Int) -> Unit,
    onNicotineChange: (Float) -> Unit,
    onTriggerToggle: (String) -> Unit,
    onPledgeTimeChange: (Int, Int) -> Unit,
    onMilestoneCelebrationsChange: (Boolean) -> Unit,
    onSkip: () -> Unit,
    onComplete: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = LocalAppColors.current
    val tick = rememberHapticTick()
    val pagerState = rememberPagerState(pageCount = { ONBOARDING_PAGE_COUNT })
    val scope = rememberCoroutineScope()
    val isLastPage = pagerState.currentPage == ONBOARDING_PAGE_COUNT - 1

    fun goTo(page: Int) {
        scope.launch {
            tick()
            pagerState.animateScrollToPage(page)
        }
    }

    Column(
        modifier =
            modifier
                .fillMaxSize()
                .background(colors.groupedBackground)
                .windowInsetsPadding(WindowInsets.statusBars)
                .windowInsetsPadding(WindowInsets.navigationBars),
    ) {
        // Top bar: progress dots + skip.
        Row(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(
                        horizontal = AppDimens.screenMargin,
                        vertical = AppDimens.spacing12,
                    ),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            PageDots(
                pageCount = ONBOARDING_PAGE_COUNT,
                currentPage = pagerState.currentPage,
                modifier = Modifier.weight(1f),
            )
            if (!isLastPage) {
                TextButton(onClick = onSkip) {
                    Text(
                        text = "Skip",
                        style = AppTypography.body,
                        color = colors.accent,
                    )
                }
            }
        }

        // Pages.
        HorizontalPager(
            state = pagerState,
            modifier = Modifier.weight(1f),
            userScrollEnabled = true,
        ) { page ->
            OnboardingPage(
                page = page,
                state = state,
                onQuitDateChange = onQuitDateChange,
                onPlanModeChange = onPlanModeChange,
                onDeviceTypeChange = onDeviceTypeChange,
                onUnitsPerWeekChange = onUnitsPerWeekChange,
                onCostPerUnitChange = onCostPerUnitChange,
                onPuffsPerDayChange = onPuffsPerDayChange,
                onNicotineChange = onNicotineChange,
                onTriggerToggle = onTriggerToggle,
                onPledgeTimeChange = onPledgeTimeChange,
                onMilestoneCelebrationsChange = onMilestoneCelebrationsChange,
            )
        }

        // Bottom nav buttons.
        Row(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(
                        horizontal = AppDimens.screenMargin,
                        vertical = AppDimens.spacing16,
                    ),
            horizontalArrangement = Arrangement.spacedBy(AppDimens.spacing12),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            if (pagerState.currentPage > 0) {
                TextButton(onClick = { goTo(pagerState.currentPage - 1) }) {
                    Text(
                        text = "Back",
                        style = AppTypography.body,
                        color = colors.accent,
                    )
                }
            } else {
                Spacer(modifier = Modifier.weight(1f))
            }
            Spacer(modifier = Modifier.weight(1f))
            Button(
                onClick = {
                    if (isLastPage) {
                        onComplete()
                    } else {
                        goTo(pagerState.currentPage + 1)
                    }
                },
                enabled = !state.isSaving,
                colors =
                    ButtonDefaults.buttonColors(
                        containerColor = colors.accent,
                    ),
            ) {
                Text(
                    text =
                        when (pagerState.currentPage) {
                            0 -> "Get Started"
                            ONBOARDING_PAGE_COUNT - 1 -> "Continue"
                            else -> "Next"
                        },
                    style = AppTypography.headline,
                )
            }
        }
    }
}

@Composable
private fun PageDots(
    pageCount: Int,
    currentPage: Int,
    modifier: Modifier = Modifier,
) {
    val colors = LocalAppColors.current
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        repeat(pageCount) { index ->
            Box(
                modifier =
                    Modifier
                        .size(
                            width = if (index == currentPage) 24.dp else 8.dp,
                            height = 8.dp,
                        ).clip(CircleShape)
                        .background(
                            if (index == currentPage) {
                                colors.accent
                            } else {
                                colors.secondaryLabel.copy(alpha = 0.3f)
                            },
                        ),
            )
        }
    }
}

@Composable
private fun OnboardingPage(
    page: Int,
    state: OnboardingUiState,
    onQuitDateChange: (Long) -> Unit,
    onPlanModeChange: (Boolean) -> Unit,
    onDeviceTypeChange: (com.sreecharan.quitvaping.data.model.DeviceType) -> Unit,
    onUnitsPerWeekChange: (Int) -> Unit,
    onCostPerUnitChange: (Int) -> Unit,
    onPuffsPerDayChange: (Int) -> Unit,
    onNicotineChange: (Float) -> Unit,
    onTriggerToggle: (String) -> Unit,
    onPledgeTimeChange: (Int, Int) -> Unit,
    onMilestoneCelebrationsChange: (Boolean) -> Unit,
) {
    when (page) {
        0 -> WelcomePage()
        1 ->
            QuitDatePage(
                state = state,
                onQuitDateChange = onQuitDateChange,
                onPlanModeChange = onPlanModeChange,
            )
        2 ->
            SpendPage(
                state = state,
                onDeviceTypeChange = onDeviceTypeChange,
                onUnitsPerWeekChange = onUnitsPerWeekChange,
                onCostPerUnitChange = onCostPerUnitChange,
            )
        3 ->
            UsagePage(
                state = state,
                onPuffsPerDayChange = onPuffsPerDayChange,
                onNicotineChange = onNicotineChange,
            )
        4 -> TriggersPage(state = state, onTriggerToggle = onTriggerToggle)
        5 ->
            NotificationsPage(
                state = state,
                onPledgeTimeChange = onPledgeTimeChange,
                onMilestoneCelebrationsChange = onMilestoneCelebrationsChange,
            )
        else ->
            ReadyPage(
                state = state,
            )
    }
}
