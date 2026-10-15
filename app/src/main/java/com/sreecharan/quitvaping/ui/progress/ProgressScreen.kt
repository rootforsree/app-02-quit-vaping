package com.sreecharan.quitvaping.ui.progress

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.outlined.Circle
import androidx.compose.material.icons.outlined.EmojiEvents
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.sreecharan.quitvaping.R
import com.sreecharan.quitvaping.ui.components.GroupCard
import com.sreecharan.quitvaping.ui.components.InsetGroupedList
import com.sreecharan.quitvaping.ui.components.RowDivider
import com.sreecharan.quitvaping.ui.components.SectionHeader
import com.sreecharan.quitvaping.ui.components.SkeletonList
import com.sreecharan.quitvaping.ui.components.formatCount
import com.sreecharan.quitvaping.ui.components.formatMoney
import com.sreecharan.quitvaping.ui.theme.AppColors
import com.sreecharan.quitvaping.ui.theme.AppDimens
import com.sreecharan.quitvaping.ui.theme.AppShapes
import com.sreecharan.quitvaping.ui.theme.AppTypography
import com.sreecharan.quitvaping.ui.theme.LocalAppColors

@Composable
fun ProgressRoute(viewModel: ProgressViewModel = hiltViewModel()) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    ProgressScreenContent(state = state)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProgressScreenContent(state: ProgressUiState, modifier: Modifier = Modifier) {
    val colors = LocalAppColors.current
    val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior()
    Scaffold(
        modifier = modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = { ProgressTopBar(scrollBehavior = scrollBehavior, colors = colors) },
        containerColor = colors.groupedBackground,
    ) { padding ->
        if (state.isLoading) {
            SkeletonList(
                contentPadding =
                    PaddingValues(
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
                item(key = "stats", contentType = "stats") {
                    StreakStatsRow(state = state, colors = colors)
                }
                item(key = "money-header", contentType = "header") {
                    SectionHeader(title = stringResource(R.string.progress_money_title))
                }
                item(key = "money", contentType = "card") {
                    MoneyCard(state = state, colors = colors)
                }
                item(key = "health-header", contentType = "header") {
                    SectionHeader(title = stringResource(R.string.progress_health_title))
                }
                item(key = "health", contentType = "card") {
                    HealthTimelineCard(state = state, colors = colors)
                }
                item(key = "badges-header", contentType = "header") {
                    SectionHeader(title = stringResource(R.string.progress_badges_title))
                }
                state.milestones.chunked(BADGES_PER_ROW).forEachIndexed { rowIndex, row ->
                    item(key = "badges-$rowIndex", contentType = "badges") {
                        BadgeRow(milestones = row, colors = colors)
                    }
                }
            }
        }
    }
}

@Composable
private fun StreakStatsRow(state: ProgressUiState, colors: AppColors) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(AppDimens.spacing12),
    ) {
        StreakStatCard(
            value = formatCount(state.currentStreakDays),
            label = stringResource(R.string.progress_current_streak),
            modifier = Modifier.weight(1f),
            colors = colors,
        )
        StreakStatCard(
            value = formatCount(state.totalCleanDays),
            label = stringResource(R.string.progress_total_clean),
            modifier = Modifier.weight(1f),
            colors = colors,
        )
        StreakStatCard(
            value = stringResource(R.string.progress_percent, state.cleanDayPercent),
            label = stringResource(R.string.progress_clean_rate),
            modifier = Modifier.weight(1f),
            colors = colors,
        )
    }
}

@Composable
private fun StreakStatCard(value: String, label: String, modifier: Modifier, colors: AppColors) {
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
private fun MoneyCard(state: ProgressUiState, colors: AppColors) {
    GroupCard {
        Column(
            modifier = Modifier.padding(AppDimens.spacing16),
            verticalArrangement = Arrangement.spacedBy(AppDimens.spacing12),
        ) {
            Text(
                text = formatMoney(state.moneySavedCents),
                style = AppTypography.largeTitle,
                color = colors.label,
            )
            Text(
                text =
                    stringResource(
                        R.string.progress_projection,
                        formatMoney(state.projectedYearlyCents),
                        state.projectionDateLabel,
                    ),
                style = AppTypography.subheadline,
                color = colors.secondaryLabel,
            )
            SavingsBarChart(bars = state.weeklyBars, colors = colors)
        }
    }
}

@Composable
private fun SavingsBarChart(bars: List<Long>, colors: AppColors, modifier: Modifier = Modifier) {
    val max = (bars.maxOrNull() ?: 0L).coerceAtLeast(1L)
    val barColor = colors.success
    val trackColor = colors.secondaryLabel.copy(alpha = 0.15f)
    Canvas(
        modifier =
            modifier
                .fillMaxWidth()
                .height(96.dp)
                .clip(AppShapes.card)
                .background(trackColor.copy(alpha = 0.35f))
                .padding(horizontal = 8.dp, vertical = 8.dp),
    ) {
        drawBars(bars = bars, max = max, barColor = barColor)
    }
}

private fun DrawScope.drawBars(
    bars: List<Long>,
    max: Long,
    barColor: androidx.compose.ui.graphics.Color,
) {
    if (bars.isEmpty()) return
    val gap = 4.dp.toPx()
    val barWidth = (size.width - gap * (bars.size - 1)) / bars.size
    bars.forEachIndexed { index, value ->
        val fraction = (value.toFloat() / max).coerceIn(0.02f, 1f)
        val barHeight = size.height * fraction
        drawRoundRect(
            color = barColor,
            topLeft = Offset(x = index * (barWidth + gap), y = size.height - barHeight),
            size = Size(width = barWidth, height = barHeight),
            cornerRadius = CornerRadius(3.dp.toPx()),
        )
    }
}

@Composable
private fun HealthTimelineCard(state: ProgressUiState, colors: AppColors) {
    GroupCard {
        Column {
            state.milestones.forEachIndexed { index, milestone ->
                if (index > 0) RowDivider()
                MilestoneRow(milestone = milestone, colors = colors)
            }
        }
    }
}

@Composable
private fun MilestoneRow(milestone: MilestoneUi, colors: AppColors) {
    Row(
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(
                    horizontal = AppDimens.rowHorizontalPadding,
                    vertical = AppDimens.spacing12,
                ),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(AppDimens.spacing12),
    ) {
        val iconTint =
            when {
                milestone.achieved -> colors.success
                milestone.isCurrent -> colors.accent
                else -> colors.secondaryLabel.copy(alpha = 0.4f)
            }
        Icon(
            imageVector =
                if (milestone.achieved) {
                    Icons.Filled.CheckCircle
                } else {
                    Icons.Outlined.Circle
                },
            contentDescription = null,
            tint = iconTint,
            modifier = Modifier.size(AppDimens.iconSize),
        )
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = milestone.title,
                style = AppTypography.headline,
                color =
                    if (milestone.achieved || milestone.isCurrent) {
                        colors.label
                    } else {
                        colors.secondaryLabel
                    },
            )
            Text(
                text = milestone.description,
                style = AppTypography.footnote,
                color = colors.secondaryLabel,
            )
        }
        Text(
            text = milestone.offsetLabel,
            style = AppTypography.caption,
            color = colors.secondaryLabel,
        )
    }
}

@Composable
private fun BadgeRow(milestones: List<MilestoneUi>, colors: AppColors) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(AppDimens.spacing12),
    ) {
        milestones.forEach { milestone ->
            BadgeCell(milestone = milestone, colors = colors, modifier = Modifier.weight(1f))
        }
        repeat(BADGES_PER_ROW - milestones.size) {
            Box(modifier = Modifier.weight(1f))
        }
    }
}

@Composable
private fun BadgeCell(milestone: MilestoneUi, colors: AppColors, modifier: Modifier = Modifier) {
    val earnedAlpha = if (milestone.achieved) 1f else 0.35f
    GroupCard(modifier = modifier.alpha(earnedAlpha)) {
        Column(
            modifier = Modifier.padding(AppDimens.spacing12),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(AppDimens.spacing4),
        ) {
            Icon(
                imageVector = Icons.Outlined.EmojiEvents,
                contentDescription = null,
                tint = if (milestone.achieved) colors.warning else colors.secondaryLabel,
                modifier = Modifier.size(32.dp),
            )
            Text(
                text = milestone.title,
                style = AppTypography.caption,
                color = colors.label,
                textAlign = TextAlign.Center,
                maxLines = 2,
                minLines = 2,
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ProgressTopBar(scrollBehavior: TopAppBarScrollBehavior, colors: AppColors) {
    LargeTopAppBar(
        title = {
            Text(
                text = stringResource(R.string.tab_progress),
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

private const val BADGES_PER_ROW = 3
