package com.sreecharan.quitvaping.ui.cravings

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material.icons.filled.SelfImprovement
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.outlined.History
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.sreecharan.quitvaping.R
import com.sreecharan.quitvaping.ui.components.EmptyState
import com.sreecharan.quitvaping.ui.components.GroupCard
import com.sreecharan.quitvaping.ui.components.InsetGroupedList
import com.sreecharan.quitvaping.ui.components.RowDivider
import com.sreecharan.quitvaping.ui.components.SectionHeader
import com.sreecharan.quitvaping.ui.components.SkeletonList
import com.sreecharan.quitvaping.ui.theme.AppColors
import com.sreecharan.quitvaping.ui.theme.AppDimens
import com.sreecharan.quitvaping.ui.theme.AppShapes
import com.sreecharan.quitvaping.ui.theme.AppTypography
import com.sreecharan.quitvaping.ui.theme.LocalAppColors
import com.sreecharan.quitvaping.ui.theme.rememberHapticTick

@Composable
fun CravingsRoute(viewModel: CravingsViewModel = hiltViewModel()) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val sos by viewModel.sos.collectAsStateWithLifecycle()
    val logIntensity by viewModel.logIntensity.collectAsStateWithLifecycle()
    val logTrigger by viewModel.logTrigger.collectAsStateWithLifecycle()
    CravingsScreenContent(
        state = state,
        sos = sos,
        logIntensity = logIntensity,
        logTrigger = logTrigger,
        onSosOpened = viewModel::onSosOpened,
        onSosDismissed = viewModel::onSosDismissed,
        onStartRiding = viewModel::onStartRiding,
        onStartBreathing = viewModel::onStartBreathing,
        onEndEarly = viewModel::onEndEarly,
        onLogIntensityChanged = viewModel::onLogIntensityChanged,
        onLogTriggerChanged = viewModel::onLogTriggerChanged,
        onBeatIt = viewModel::onBeatIt,
        onSlipped = viewModel::onSlipped,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
@Suppress("LongMethod", "LongParameterList")
fun CravingsScreenContent(
    state: CravingsUiState,
    sos: SosUiState,
    logIntensity: Int,
    logTrigger: String?,
    onSosOpened: () -> Unit,
    onSosDismissed: () -> Unit,
    onStartRiding: () -> Unit,
    onStartBreathing: () -> Unit,
    onEndEarly: () -> Unit,
    onLogIntensityChanged: (Int) -> Unit,
    onLogTriggerChanged: (String?) -> Unit,
    onBeatIt: () -> Unit,
    onSlipped: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = LocalAppColors.current
    val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior()
    Scaffold(
        modifier = modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = { CravingsTopBar(scrollBehavior = scrollBehavior, colors = colors) },
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
                item(key = "sos", contentType = "sos") {
                    SosHeroCard(onSosOpened = onSosOpened, colors = colors)
                }
                item(key = "beaten", contentType = "card") {
                    BeatenCard(state = state, colors = colors)
                }
                if (state.topTrigger != null || state.peakHourLabel != null) {
                    item(key = "insight-header", contentType = "header") {
                        SectionHeader(title = stringResource(R.string.cravings_insight_title))
                    }
                    item(key = "insight", contentType = "card") {
                        InsightCard(state = state, colors = colors)
                    }
                }
                item(key = "history-header", contentType = "header") {
                    SectionHeader(title = stringResource(R.string.cravings_history_title))
                }
                if (state.history.isEmpty()) {
                    item(key = "history-empty", contentType = "empty") {
                        EmptyState(
                            message = stringResource(R.string.cravings_empty),
                            actionLabel = stringResource(R.string.cravings_sos_action),
                            onAction = onSosOpened,
                            icon = Icons.Outlined.History,
                        )
                    }
                } else {
                    item(key = "history", contentType = "card") {
                        HistoryCard(state = state, colors = colors)
                    }
                }
            }
        }
    }
    if (sos != SosUiState.Hidden) {
        SosDialog(
            sos = sos,
            logIntensity = logIntensity,
            logTrigger = logTrigger,
            onDismiss = onSosDismissed,
            onStartRiding = onStartRiding,
            onStartBreathing = onStartBreathing,
            onEndEarly = onEndEarly,
            onLogIntensityChanged = onLogIntensityChanged,
            onLogTriggerChanged = onLogTriggerChanged,
            onBeatIt = onBeatIt,
            onSlipped = onSlipped,
        )
    }
}

@Composable
private fun SosHeroCard(onSosOpened: () -> Unit, colors: AppColors) {
    val tick = rememberHapticTick(medium = true)
    Card(
        shape = AppShapes.card,
        colors = CardDefaults.cardColors(containerColor = colors.accent),
        onClick = {
            tick()
            onSosOpened()
        },
    ) {
        Column(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(AppDimens.spacing16),
            verticalArrangement = Arrangement.spacedBy(AppDimens.spacing8),
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(AppDimens.spacing8),
            ) {
                Icon(
                    imageVector = Icons.Filled.Bolt,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(AppDimens.iconSize),
                )
                Text(
                    text = stringResource(R.string.cravings_sos_title),
                    style = AppTypography.headline,
                    color = Color.White,
                )
            }
            Text(
                text = stringResource(R.string.cravings_sos_sub),
                style = AppTypography.subheadline,
                color = Color.White.copy(alpha = 0.9f),
            )
        }
    }
}

@Composable
private fun BeatenCard(state: CravingsUiState, colors: AppColors) {
    GroupCard {
        Row(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(AppDimens.spacing16),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(AppDimens.spacing12),
        ) {
            Icon(
                imageVector = Icons.Filled.CheckCircle,
                contentDescription = null,
                tint = colors.success,
                modifier = Modifier.size(40.dp),
            )
            Column {
                Text(
                    text = state.cravingsBeaten.toString(),
                    style = AppTypography.largeTitle,
                    color = colors.label,
                )
                Text(
                    text = stringResource(R.string.cravings_beaten_title),
                    style = AppTypography.footnote,
                    color = colors.secondaryLabel,
                )
            }
        }
    }
}

@Composable
private fun InsightCard(state: CravingsUiState, colors: AppColors) {
    GroupCard {
        Column(
            modifier = Modifier.padding(AppDimens.spacing16),
            verticalArrangement = Arrangement.spacedBy(AppDimens.spacing4),
        ) {
            Text(
                text = stringResource(R.string.cravings_insight_title),
                style = AppTypography.headline,
                color = colors.label,
            )
            Text(
                text =
                    stringResource(
                        R.string.cravings_insight_text,
                        state.peakHourLabel ?: "—",
                        state.topTrigger ?: "—",
                    ),
                style = AppTypography.subheadline,
                color = colors.secondaryLabel,
            )
        }
    }
}

@Composable
private fun HistoryCard(state: CravingsUiState, colors: AppColors) {
    GroupCard {
        Column {
            state.history.forEachIndexed { index, item ->
                if (index > 0) RowDivider()
                HistoryRow(item = item, colors = colors)
            }
        }
    }
}

@Composable
private fun HistoryRow(item: CravingHistoryItem, colors: AppColors) {
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
        Icon(
            imageVector =
                if (item.resisted) {
                    Icons.Filled.CheckCircle
                } else {
                    Icons.Filled.RadioButtonUnchecked
                },
            contentDescription = null,
            tint = if (item.resisted) colors.success else colors.secondaryLabel,
            modifier = Modifier.size(AppDimens.iconSize),
        )
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = item.trigger,
                style = AppTypography.body,
                color = colors.label,
            )
            Text(
                text =
                    stringResource(
                        R.string.cravings_history_meta,
                        item.intensity,
                        item.timeAgo,
                    ),
                style = AppTypography.caption,
                color = colors.secondaryLabel,
            )
        }
        Text(
            text =
                stringResource(
                    if (item.resisted) {
                        R.string.cravings_resisted
                    } else {
                        R.string.cravings_slipped
                    },
                ),
            style = AppTypography.caption,
            color = if (item.resisted) colors.success else colors.secondaryLabel,
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
@Suppress("LongMethod", "LongParameterList")
private fun SosDialog(
    sos: SosUiState,
    logIntensity: Int,
    logTrigger: String?,
    onDismiss: () -> Unit,
    onStartRiding: () -> Unit,
    onStartBreathing: () -> Unit,
    onEndEarly: () -> Unit,
    onLogIntensityChanged: (Int) -> Unit,
    onLogTriggerChanged: (String?) -> Unit,
    onBeatIt: () -> Unit,
    onSlipped: () -> Unit,
) {
    val colors = LocalAppColors.current
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false),
    ) {
        Box(
            modifier =
                Modifier
                    .fillMaxSize()
                    .background(colors.groupedBackground)
                    .padding(AppDimens.screenMargin),
        ) {
            IconButton(
                onClick = onDismiss,
                modifier = Modifier.align(Alignment.TopEnd),
            ) {
                Icon(
                    imageVector = Icons.Filled.Close,
                    contentDescription = stringResource(R.string.action_close),
                    tint = colors.secondaryLabel,
                )
            }
            when (sos) {
                SosUiState.Hidden -> Unit
                SosUiState.ModeSelect ->
                    SosModeSelect(
                        onStartRiding = onStartRiding,
                        onStartBreathing = onStartBreathing,
                        colors = colors,
                    )
                is SosUiState.Riding ->
                    SosRiding(
                        sos = sos,
                        onEndEarly = onEndEarly,
                        colors = colors,
                    )
                is SosUiState.Breathing ->
                    SosBreathing(
                        sos = sos,
                        onEndEarly = onEndEarly,
                        colors = colors,
                    )
                SosUiState.LogTrigger ->
                    SosLog(
                        logIntensity = logIntensity,
                        logTrigger = logTrigger,
                        onLogIntensityChanged = onLogIntensityChanged,
                        onLogTriggerChanged = onLogTriggerChanged,
                        onBeatIt = onBeatIt,
                        onSlipped = onSlipped,
                        colors = colors,
                    )
                SosUiState.DoneResisted -> SosDoneResisted(onDismiss = onDismiss, colors = colors)
                is SosUiState.DoneSlipped ->
                    SosDoneSlipped(
                        cleanDays = sos.totalCleanDays,
                        onDismiss = onDismiss,
                        colors = colors,
                    )
            }
        }
    }
}

@Composable
private fun SosModeSelect(
    onStartRiding: () -> Unit,
    onStartBreathing: () -> Unit,
    colors: AppColors,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = stringResource(R.string.sos_choose_title),
            style = AppTypography.largeTitle,
            color = colors.label,
            textAlign = TextAlign.Center,
        )
        Spacer(modifier = Modifier.height(AppDimens.spacing24))
        SosModeCard(
            title = stringResource(R.string.sos_ride_title),
            subtitle = stringResource(R.string.sos_ride_sub),
            icon = Icons.Filled.Timer,
            onClick = onStartRiding,
            colors = colors,
        )
        Spacer(modifier = Modifier.height(AppDimens.spacing12))
        SosModeCard(
            title = stringResource(R.string.sos_breathe_title),
            subtitle = stringResource(R.string.sos_breathe_sub),
            icon = Icons.Filled.SelfImprovement,
            onClick = onStartBreathing,
            colors = colors,
        )
    }
}

@Composable
private fun SosModeCard(
    title: String,
    subtitle: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit,
    colors: AppColors,
) {
    val tick = rememberHapticTick(medium = true)
    Card(
        shape = AppShapes.card,
        colors = CardDefaults.cardColors(containerColor = colors.cardSurface),
        onClick = {
            tick()
            onClick()
        },
        modifier = Modifier.fillMaxWidth(),
    ) {
        Row(
            modifier = Modifier.padding(AppDimens.spacing16),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(AppDimens.spacing12),
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = colors.accent,
                modifier = Modifier.size(32.dp),
            )
            Column {
                Text(text = title, style = AppTypography.headline, color = colors.label)
                Text(text = subtitle, style = AppTypography.subheadline, color = colors.secondaryLabel)
            }
        }
    }
}

@Composable
private fun SosRiding(sos: SosUiState.Riding, onEndEarly: () -> Unit, colors: AppColors) {
    val phaseTitles =
        listOf(
            R.string.sos_phase_0_title to R.string.sos_phase_0_sub,
            R.string.sos_phase_1_title to R.string.sos_phase_1_sub,
            R.string.sos_phase_2_title to R.string.sos_phase_2_sub,
            R.string.sos_phase_3_title to R.string.sos_phase_3_sub,
        )
    val (titleRes, subRes) = phaseTitles[sos.phaseIndex.coerceIn(0, phaseTitles.lastIndex)]
    val minutes = sos.secondsLeft / SECONDS_PER_MINUTE
    val seconds = sos.secondsLeft % SECONDS_PER_MINUTE
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = stringResource(R.string.sos_riding_title),
            style = AppTypography.title2,
            color = colors.secondaryLabel,
            textAlign = TextAlign.Center,
        )
        Spacer(modifier = Modifier.height(AppDimens.spacing24))
        CountdownRing(
            progress = sos.secondsLeft / RIDE_TOTAL_SECONDS,
            label = "%d:%02d".format(minutes, seconds),
            colors = colors,
        )
        Spacer(modifier = Modifier.height(AppDimens.spacing24))
        Text(
            text = stringResource(titleRes),
            style = AppTypography.headline,
            color = colors.label,
            textAlign = TextAlign.Center,
        )
        Text(
            text = stringResource(subRes),
            style = AppTypography.body,
            color = colors.secondaryLabel,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = AppDimens.spacing24),
        )
        Spacer(modifier = Modifier.height(AppDimens.spacing24))
        TextButton(onClick = onEndEarly) {
            Text(
                text = stringResource(R.string.sos_end_early),
                style = AppTypography.subheadline,
                color = colors.secondaryLabel,
            )
        }
    }
}

@Composable
private fun CountdownRing(progress: Float, label: String, colors: AppColors) {
    val trackColor = colors.secondaryLabel.copy(alpha = 0.2f)
    val progressColor = colors.accent
    Box(
        modifier = Modifier.size(200.dp),
        contentAlignment = Alignment.Center,
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            drawArc(
                color = trackColor,
                startAngle = 0f,
                sweepAngle = FULL_CIRCLE_DEGREES,
                useCenter = false,
                style = Stroke(width = 12.dp.toPx(), cap = StrokeCap.Round),
            )
            drawArc(
                color = progressColor,
                startAngle = ARC_START_ANGLE,
                sweepAngle = FULL_CIRCLE_DEGREES * progress.coerceIn(0f, 1f),
                useCenter = false,
                style = Stroke(width = 12.dp.toPx(), cap = StrokeCap.Round),
            )
        }
        Text(text = label, style = AppTypography.largeTitle, color = colors.label)
    }
}

@Composable
@Suppress("LongMethod")
private fun SosBreathing(sos: SosUiState.Breathing, onEndEarly: () -> Unit, colors: AppColors) {
    val targetScale =
        when (sos.phase) {
            BreathPhase.INHALE, BreathPhase.HOLD -> 1f
            BreathPhase.EXHALE -> 0.55f
        }
    val scale by animateFloatAsState(
        targetValue = targetScale,
        animationSpec =
            tween(
                durationMillis =
                    when (sos.phase) {
                        BreathPhase.INHALE -> 8_000
                        BreathPhase.HOLD -> 4_000
                        BreathPhase.EXHALE -> 8_000
                    },
            ),
        label = "breath",
    )
    val phaseText =
        when (sos.phase) {
            BreathPhase.INHALE -> stringResource(R.string.sos_breath_inhale)
            BreathPhase.HOLD -> stringResource(R.string.sos_breath_hold)
            BreathPhase.EXHALE -> stringResource(R.string.sos_breath_exhale)
        }
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = stringResource(R.string.sos_breath_round, sos.round, 5),
            style = AppTypography.subheadline,
            color = colors.secondaryLabel,
        )
        Spacer(modifier = Modifier.height(AppDimens.spacing24))
        Box(
            modifier =
                Modifier
                    .size(200.dp)
                    .scale(scale)
                    .clip(CircleShape)
                    .background(colors.accent.copy(alpha = 0.25f)),
            contentAlignment = Alignment.Center,
        ) {
            Box(
                modifier =
                    Modifier
                        .size(120.dp)
                        .clip(CircleShape)
                        .background(colors.accent.copy(alpha = 0.5f)),
            )
        }
        Spacer(modifier = Modifier.height(AppDimens.spacing24))
        Text(
            text = phaseText,
            style = AppTypography.title2,
            color = colors.label,
        )
        Spacer(modifier = Modifier.height(AppDimens.spacing24))
        TextButton(onClick = onEndEarly) {
            Text(
                text = stringResource(R.string.sos_end_early),
                style = AppTypography.subheadline,
                color = colors.secondaryLabel,
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
@Suppress("LongMethod", "LongParameterList")
private fun SosLog(
    logIntensity: Int,
    logTrigger: String?,
    onLogIntensityChanged: (Int) -> Unit,
    onLogTriggerChanged: (String?) -> Unit,
    onBeatIt: () -> Unit,
    onSlipped: () -> Unit,
    colors: AppColors,
) {
    val triggers = triggerOptions()
    val beatTick = rememberHapticTick(medium = true)
    Column(
        modifier =
            Modifier
                .fillMaxSize()
                .padding(top = 64.dp),
        verticalArrangement = Arrangement.spacedBy(AppDimens.spacing16),
    ) {
        Text(
            text = stringResource(R.string.sos_log_title),
            style = AppTypography.largeTitle,
            color = colors.label,
        )
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(AppDimens.spacing8),
            verticalArrangement = Arrangement.spacedBy(AppDimens.spacing8),
        ) {
            triggers.forEach { trigger ->
                FilterChip(
                    selected = logTrigger == trigger,
                    onClick = { onLogTriggerChanged(if (logTrigger == trigger) null else trigger) },
                    label = {
                        Text(text = trigger, style = AppTypography.subheadline)
                    },
                )
            }
        }
        Text(
            text = stringResource(R.string.sos_log_intensity, logIntensity),
            style = AppTypography.headline,
            color = colors.label,
        )
        Slider(
            value = logIntensity.toFloat(),
            onValueChange = { onLogIntensityChanged(it.toInt()) },
            valueRange = 1f..10f,
            steps = 8,
        )
        Spacer(modifier = Modifier.weight(1f))
        Button(
            onClick = {
                beatTick()
                onBeatIt()
            },
            colors = ButtonDefaults.buttonColors(containerColor = colors.success),
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text(
                text = stringResource(R.string.sos_beat_it),
                style = AppTypography.headline,
                color = Color.White,
            )
        }
        TextButton(
            onClick = onSlipped,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text(
                text = stringResource(R.string.sos_slipped),
                style = AppTypography.subheadline,
                color = colors.secondaryLabel,
            )
        }
        Spacer(modifier = Modifier.height(AppDimens.spacing16))
    }
}

@Composable
private fun SosDoneResisted(onDismiss: () -> Unit, colors: AppColors) {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Icon(
            imageVector = Icons.Filled.CheckCircle,
            contentDescription = null,
            tint = colors.success,
            modifier = Modifier.size(96.dp),
        )
        Spacer(modifier = Modifier.height(AppDimens.spacing16))
        Text(
            text = stringResource(R.string.sos_done_resisted_title),
            style = AppTypography.largeTitle,
            color = colors.label,
            textAlign = TextAlign.Center,
        )
        Spacer(modifier = Modifier.height(AppDimens.spacing8))
        Text(
            text = stringResource(R.string.sos_done_resisted_sub),
            style = AppTypography.body,
            color = colors.secondaryLabel,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = AppDimens.spacing24),
        )
        Spacer(modifier = Modifier.height(AppDimens.spacing24))
        Button(
            onClick = onDismiss,
            colors = ButtonDefaults.buttonColors(containerColor = colors.accent),
        ) {
            Text(
                text = stringResource(R.string.sos_done_action),
                style = AppTypography.headline,
                color = Color.White,
            )
        }
    }
}

@Composable
private fun SosDoneSlipped(cleanDays: Long, onDismiss: () -> Unit, colors: AppColors) {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = stringResource(R.string.sos_done_slipped_title, cleanDays),
            style = AppTypography.largeTitle,
            color = colors.label,
            textAlign = TextAlign.Center,
        )
        Spacer(modifier = Modifier.height(AppDimens.spacing8))
        Text(
            text = stringResource(R.string.sos_done_slipped_sub),
            style = AppTypography.body,
            color = colors.secondaryLabel,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = AppDimens.spacing24),
        )
        Spacer(modifier = Modifier.height(AppDimens.spacing24))
        Button(
            onClick = onDismiss,
            colors = ButtonDefaults.buttonColors(containerColor = colors.accent),
        ) {
            Text(
                text = stringResource(R.string.sos_done_action),
                style = AppTypography.headline,
                color = Color.White,
            )
        }
    }
}

@Composable
private fun triggerOptions(): List<String> =
    listOf(
        stringResource(R.string.trigger_stress),
        stringResource(R.string.trigger_boredom),
        stringResource(R.string.trigger_social),
        stringResource(R.string.trigger_meals),
        stringResource(R.string.trigger_alcohol),
        stringResource(R.string.trigger_habit),
        stringResource(R.string.trigger_other),
    )

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CravingsTopBar(scrollBehavior: TopAppBarScrollBehavior, colors: AppColors) {
    LargeTopAppBar(
        title = {
            Text(
                text = stringResource(R.string.tab_cravings),
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

private const val SECONDS_PER_MINUTE = 60

/** Mirrors the SOS ride timer length in [CravingsViewModel]; the ring shows the fraction left. */
private const val RIDE_TOTAL_SECONDS = 300f

private const val FULL_CIRCLE_DEGREES = 360f
private const val ARC_START_ANGLE = -90f
