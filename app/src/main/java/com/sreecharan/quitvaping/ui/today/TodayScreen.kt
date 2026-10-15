package com.sreecharan.quitvaping.ui.today

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.outlined.Savings
import androidx.compose.material.icons.outlined.Spa
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.sreecharan.quitvaping.R
import com.sreecharan.quitvaping.ui.components.EmptyState
import com.sreecharan.quitvaping.ui.components.GroupCard
import com.sreecharan.quitvaping.ui.components.InsetGroupedList
import com.sreecharan.quitvaping.ui.components.SectionHeader
import com.sreecharan.quitvaping.ui.components.formatCount
import com.sreecharan.quitvaping.ui.components.formatMoney
import com.sreecharan.quitvaping.ui.theme.AppColors
import com.sreecharan.quitvaping.ui.theme.AppDimens
import com.sreecharan.quitvaping.ui.theme.AppShapes
import com.sreecharan.quitvaping.ui.theme.AppTypography
import com.sreecharan.quitvaping.ui.theme.LocalAppColors
import com.sreecharan.quitvaping.ui.theme.rememberHapticTick

/**
 * Thin stateful route: owns the ViewModel and forwards events. Content
 * below stays stateless and never sees the ViewModel.
 */
@Composable
fun TodayRoute(
    onOpenCravings: () -> Unit,
    onOpenOnboarding: () -> Unit,
    viewModel: TodayViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    TodayScreenContent(
        state = state,
        onPledge = viewModel::onPledge,
        onEveningReview = viewModel::onEveningReview,
        onOpenCravings = onOpenCravings,
        onOpenOnboarding = onOpenOnboarding,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
@Suppress("LongMethod")
fun TodayScreenContent(
    state: TodayUiState,
    onPledge: () -> Unit,
    onEveningReview: (Int) -> Unit,
    onOpenCravings: () -> Unit,
    onOpenOnboarding: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = LocalAppColors.current
    val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior()
    Scaffold(
        modifier = modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = { TodayTopBar(scrollBehavior = scrollBehavior, colors = colors) },
        containerColor = colors.groupedBackground,
    ) { padding ->
        if (!state.hasProfile && !state.isLoading) {
            EmptyState(
                message = stringResource(R.string.today_no_plan_message),
                actionLabel = stringResource(R.string.today_no_plan_action),
                onAction = onOpenOnboarding,
                icon = Icons.Outlined.Spa,
                modifier =
                    Modifier.padding(
                        top = padding.calculateTopPadding(),
                        bottom = padding.calculateBottomPadding() + AppDimens.tabBarClearance,
                    ),
            )
        } else {
            InsetGroupedList(
                contentPadding =
                    PaddingValues(
                        top = padding.calculateTopPadding(),
                        bottom = padding.calculateBottomPadding() + AppDimens.tabBarClearance,
                    ),
            ) {
                item(key = "hero", contentType = "hero") {
                    HeroCounter(state = state, colors = colors)
                }
                item(key = "stats", contentType = "stats") {
                    StatsRow(state = state, colors = colors)
                }
                item(key = "goal-header", contentType = "header") {
                    SectionHeader(title = stringResource(R.string.today_goal_title))
                }
                item(key = "goal", contentType = "card") {
                    SavingsGoalCard(state = state, colors = colors)
                }
                item(key = "pledge-header", contentType = "header") {
                    SectionHeader(title = stringResource(R.string.today_pledge_title))
                }
                item(key = "pledge", contentType = "card") {
                    PledgeCard(
                        state = state,
                        onPledge = onPledge,
                        onEveningReview = onEveningReview,
                        colors = colors,
                    )
                }
                item(key = "milestone-header", contentType = "header") {
                    SectionHeader(title = stringResource(R.string.today_next_milestone))
                }
                item(key = "milestone", contentType = "card") {
                    NextMilestoneCard(state = state, colors = colors)
                }
                item(key = "sos", contentType = "sos") {
                    SosEntryCard(onOpenCravings = onOpenCravings, colors = colors)
                }
            }
        }
    }
}

@Composable
private fun HeroCounter(state: TodayUiState, colors: AppColors) {
    val ticking =
        "%02d:%02d:%02d".format(state.hoursPart, state.minutesPart, state.secondsPart)
    Card(
        shape = AppShapes.card,
        colors = CardDefaults.cardColors(containerColor = colors.accent),
    ) {
        Column(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(
                        horizontal = AppDimens.spacing16,
                        vertical = AppDimens.spacing24,
                    ),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = formatCount(state.daysQuit),
                style = AppTypography.largeTitle.copy(fontSize = 72.sp, lineHeight = 76.sp),
                color = Color.White,
                textAlign = TextAlign.Center,
            )
            Text(
                text = stringResource(R.string.today_days_vape_free),
                style = AppTypography.headline,
                color = Color.White.copy(alpha = 0.9f),
            )
            Spacer(modifier = Modifier.height(AppDimens.spacing8))
            Text(
                text = ticking,
                style = AppTypography.title2.copy(fontSize = 28.sp),
                color = Color.White.copy(alpha = 0.85f),
                textAlign = TextAlign.Center,
            )
            if (state.currentStreakDays != state.totalCleanDays) {
                Spacer(modifier = Modifier.height(AppDimens.spacing4))
                Text(
                    text =
                        stringResource(
                            R.string.today_clean_days,
                            state.totalCleanDays,
                        ),
                    style = AppTypography.footnote,
                    color = Color.White.copy(alpha = 0.8f),
                )
            }
        }
    }
}

@Composable
private fun StatsRow(state: TodayUiState, colors: AppColors) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(AppDimens.spacing12),
    ) {
        StatCard(
            value = formatMoney(state.moneySavedCents),
            label = stringResource(R.string.stat_money_saved),
            modifier = Modifier.weight(1f),
            colors = colors,
        )
        StatCard(
            value = formatCount(state.puffsAvoided),
            label = stringResource(R.string.stat_puffs_avoided),
            modifier = Modifier.weight(1f),
            colors = colors,
        )
        StatCard(
            value = state.cravingsBeaten.toString(),
            label = stringResource(R.string.stat_cravings_beaten),
            modifier = Modifier.weight(1f),
            colors = colors,
        )
    }
}

@Composable
private fun StatCard(value: String, label: String, modifier: Modifier, colors: AppColors) {
    GroupCard(modifier = modifier) {
        Column(
            modifier = Modifier.padding(AppDimens.spacing12),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = value,
                style = AppTypography.headline,
                color = colors.label,
                maxLines = 1,
            )
            Text(
                text = label,
                style = AppTypography.caption,
                color = colors.secondaryLabel,
                textAlign = TextAlign.Center,
                maxLines = 2,
                minLines = 2,
            )
        }
    }
}

@Composable
private fun SavingsGoalCard(state: TodayUiState, colors: AppColors) {
    val progress =
        if (state.savingsGoalCents > 0) {
            (state.moneySavedCents.toFloat() / state.savingsGoalCents).coerceIn(0f, 1f)
        } else {
            0f
        }
    GroupCard {
        Column(
            modifier = Modifier.padding(AppDimens.spacing16),
            verticalArrangement = Arrangement.spacedBy(AppDimens.spacing8),
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(AppDimens.spacing8),
            ) {
                Icon(
                    imageVector = Icons.Outlined.Savings,
                    contentDescription = null,
                    tint = colors.success,
                    modifier = Modifier.size(AppDimens.iconSize),
                )
                Text(
                    text = stringResource(R.string.today_goal_title),
                    style = AppTypography.headline,
                    color = colors.label,
                )
            }
            Text(
                text =
                    stringResource(
                        R.string.today_goal_progress,
                        formatMoney(state.moneySavedCents),
                        formatMoney(state.savingsGoalCents),
                        (progress * 100).toInt(),
                    ),
                style = AppTypography.subheadline,
                color = colors.secondaryLabel,
            )
            LinearProgressIndicator(
                progress = { progress },
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(AppShapes.capsule),
                color = colors.success,
                trackColor = colors.secondaryLabel.copy(alpha = 0.15f),
            )
        }
    }
}

@Composable
@Suppress("LongMethod")
private fun PledgeCard(
    state: TodayUiState,
    onPledge: () -> Unit,
    onEveningReview: (Int) -> Unit,
    colors: AppColors,
) {
    val tick = rememberHapticTick(medium = true)
    var showReview by remember { mutableStateOf(false) }
    GroupCard {
        Column(
            modifier = Modifier.padding(AppDimens.spacing16),
            verticalArrangement = Arrangement.spacedBy(AppDimens.spacing12),
        ) {
            if (state.pledgedToday) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(AppDimens.spacing12),
                ) {
                    Icon(
                        imageVector = Icons.Filled.CheckCircle,
                        contentDescription = null,
                        tint = colors.success,
                        modifier = Modifier.size(AppDimens.iconSize),
                    )
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = stringResource(R.string.today_pledged_title),
                            style = AppTypography.headline,
                            color = colors.label,
                        )
                        Text(
                            text = stringResource(R.string.today_pledged_sub),
                            style = AppTypography.footnote,
                            color = colors.secondaryLabel,
                        )
                    }
                }
                TextButton(onClick = { showReview = true }) {
                    Text(
                        text = stringResource(R.string.today_evening_review),
                        style = AppTypography.subheadline,
                        color = colors.accent,
                    )
                }
            } else {
                Text(
                    text = stringResource(R.string.today_pledge_prompt),
                    style = AppTypography.body,
                    color = colors.label,
                )
                Button(
                    onClick = {
                        tick()
                        onPledge()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = colors.accent),
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text(
                        text = stringResource(R.string.today_pledge_action),
                        style = AppTypography.headline,
                        color = Color.White,
                    )
                }
            }
            if (state.pledgeStreakDays > 1) {
                Text(
                    text = stringResource(R.string.today_pledge_streak, state.pledgeStreakDays),
                    style = AppTypography.caption,
                    color = colors.secondaryLabel,
                )
            }
        }
    }
    if (showReview) {
        AppRatingDialog(
            onDismiss = { showReview = false },
            onRate = { rating ->
                showReview = false
                onEveningReview(rating)
            },
        )
    }
}

@Composable
@Suppress("LongMethod")
private fun NextMilestoneCard(state: TodayUiState, colors: AppColors) {
    val title = state.nextMilestoneTitle
    GroupCard {
        Column(
            modifier = Modifier.padding(AppDimens.spacing16),
            verticalArrangement = Arrangement.spacedBy(AppDimens.spacing8),
        ) {
            if (title == null) {
                Text(
                    text = stringResource(R.string.today_all_milestones),
                    style = AppTypography.headline,
                    color = colors.label,
                )
                Text(
                    text = stringResource(R.string.today_all_milestones_sub),
                    style = AppTypography.subheadline,
                    color = colors.secondaryLabel,
                )
            } else {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(AppDimens.spacing8),
                ) {
                    Icon(
                        imageVector = Icons.Filled.Favorite,
                        contentDescription = null,
                        tint = colors.accent,
                        modifier = Modifier.size(AppDimens.iconSize),
                    )
                    Text(
                        text = title,
                        style = AppTypography.headline,
                        color = colors.label,
                    )
                }
                state.nextMilestoneDescription?.let {
                    Text(
                        text = it,
                        style = AppTypography.subheadline,
                        color = colors.secondaryLabel,
                    )
                }
                LinearProgressIndicator(
                    progress = { state.nextMilestoneProgress },
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .clip(AppShapes.capsule),
                    color = colors.accent,
                    trackColor = colors.secondaryLabel.copy(alpha = 0.15f),
                )
                Text(
                    text =
                        if (state.nextMilestoneInDays > 0) {
                            stringResource(
                                R.string.today_milestone_eta,
                                state.nextMilestoneInDays,
                            )
                        } else {
                            stringResource(R.string.today_milestone_eta_soon)
                        },
                    style = AppTypography.caption,
                    color = colors.secondaryLabel,
                )
            }
        }
    }
}

@Composable
private fun SosEntryCard(onOpenCravings: () -> Unit, colors: AppColors) {
    val tick = rememberHapticTick(medium = true)
    Card(
        shape = AppShapes.card,
        colors = CardDefaults.cardColors(containerColor = colors.cardSurface),
        onClick = {
            tick()
            onOpenCravings()
        },
    ) {
        Row(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(AppDimens.spacing16),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(AppDimens.spacing12),
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = stringResource(R.string.today_sos_title),
                    style = AppTypography.headline,
                    color = colors.label,
                )
                Text(
                    text = stringResource(R.string.today_sos_sub),
                    style = AppTypography.subheadline,
                    color = colors.secondaryLabel,
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TodayTopBar(scrollBehavior: TopAppBarScrollBehavior, colors: AppColors) {
    LargeTopAppBar(
        title = {
            Text(
                text = stringResource(R.string.tab_today),
                style = AppTypography.largeTitle,
            )
        },
        scrollBehavior = scrollBehavior,
        colors =
            TopAppBarDefaults.topAppBarColors(
                containerColor = colors.groupedBackground,
                scrolledContainerColor = colors.groupedBackground,
            ),
    )
}

/**
 * Small inline rating dialog for the evening review. Kept here (not in
 * components) because it is Today-specific copy.
 */
@Composable
private fun AppRatingDialog(onDismiss: () -> Unit, onRate: (Int) -> Unit) {
    var selected by remember { mutableIntStateOf(0) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = stringResource(R.string.today_review_title),
                style = AppTypography.headline,
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(AppDimens.spacing8)) {
                Text(
                    text = stringResource(R.string.today_review_prompt),
                    style = AppTypography.body,
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                ) {
                    (1..5).forEach { rating ->
                        TextButton(onClick = { selected = rating }) {
                            Text(
                                text = rating.toString(),
                                style = AppTypography.title2,
                                color =
                                    if (rating == selected) {
                                        LocalAppColors.current.accent
                                    } else {
                                        LocalAppColors.current.secondaryLabel
                                    },
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = { onRate(selected.coerceAtLeast(1)) },
                enabled = selected > 0,
            ) {
                Text(text = stringResource(R.string.action_done))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(text = stringResource(R.string.action_cancel))
            }
        },
    )
}
