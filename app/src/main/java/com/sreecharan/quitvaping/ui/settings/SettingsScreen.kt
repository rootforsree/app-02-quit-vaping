package com.sreecharan.quitvaping.ui.settings

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Devices
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Restore
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.SettingsBrightness
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Vibration
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material.icons.filled.WorkspacePremium
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.sreecharan.quitvaping.BuildConfig
import com.sreecharan.quitvaping.R
import com.sreecharan.quitvaping.data.model.DeviceType
import com.sreecharan.quitvaping.data.model.ThemeMode
import com.sreecharan.quitvaping.ui.components.GroupCard
import com.sreecharan.quitvaping.ui.components.InsetGroupedList
import com.sreecharan.quitvaping.ui.components.RowDivider
import com.sreecharan.quitvaping.ui.components.SectionHeader
import com.sreecharan.quitvaping.ui.components.SettingsRow
import com.sreecharan.quitvaping.ui.components.formatMoneyCents
import com.sreecharan.quitvaping.ui.components.formatTimeLabel
import com.sreecharan.quitvaping.ui.theme.AppColors
import com.sreecharan.quitvaping.ui.theme.AppDimens
import com.sreecharan.quitvaping.ui.theme.AppTypography
import com.sreecharan.quitvaping.ui.theme.LocalAppColors
import com.sreecharan.quitvaping.ui.theme.rememberHapticTick
import kotlinx.collections.immutable.persistentListOf
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import android.provider.Settings as SystemSettings

private const val MAX_WEEKLY_SPEND_CENTS = 100_00

@Composable
fun SettingsRoute(
    onOpenPaywall: () -> Unit,
    onResetComplete: () -> Unit,
    viewModel: SettingsViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }
    SettingsScreenContent(
        state = state,
        snackbarHostState = snackbarHostState,
        onThemeModeSelected = viewModel::onThemeModeSelected,
        onHapticsToggled = viewModel::onHapticsToggled,
        onQuitDateChanged = viewModel::onQuitDateChanged,
        onWeeklySpendChanged = viewModel::onWeeklySpendChanged,
        onPuffsPerDayChanged = viewModel::onPuffsPerDayChanged,
        onDeviceTypeChanged = viewModel::onDeviceTypeChanged,
        onPledgeToggled = viewModel::onPledgeToggled,
        onPledgeTimeChanged = viewModel::onPledgeTimeChanged,
        onMilestoneCelebrationsChanged = viewModel::onMilestoneCelebrationsChanged,
        onEveningToggled = viewModel::onEveningToggled,
        onOpenNotificationSettings = { openNotificationSettings(context) },
        onOpenPaywall = onOpenPaywall,
        onRestorePurchases = viewModel::onRestorePurchases,
        onExportData = viewModel::onExportData,
        onResetClicked = viewModel::onResetClicked,
        onResetDismissed = viewModel::onResetDismissed,
        onResetConfirmed = { viewModel.onResetConfirmed(onResetComplete) },
    )
    val shareUri = state.shareUri
    LaunchedEffect(shareUri) {
        if (shareUri != null) {
            shareExport(context, shareUri)
            viewModel.onShareConsumed()
        }
    }
    val message = state.message
    LaunchedEffect(message) {
        if (message != null) {
            snackbarHostState.showSnackbar(message)
            viewModel.onMessageConsumed()
        }
    }
}

private fun shareExport(context: Context, uri: Uri) {
    val intent =
        Intent(Intent.ACTION_SEND).apply {
            type = "application/json"
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
    context.startActivity(Intent.createChooser(intent, null))
}

private data class ThemeOption(
    val mode: ThemeMode,
    @StringRes val titleRes: Int,
    @StringRes val subtitleRes: Int,
    val icon: ImageVector,
)

private val ThemeOptions =
    persistentListOf(
        ThemeOption(
            mode = ThemeMode.SYSTEM,
            titleRes = R.string.theme_system,
            subtitleRes = R.string.theme_system_sub,
            icon = Icons.Filled.SettingsBrightness,
        ),
        ThemeOption(
            mode = ThemeMode.LIGHT,
            titleRes = R.string.theme_light,
            subtitleRes = R.string.theme_light_sub,
            icon = Icons.Filled.LightMode,
        ),
        ThemeOption(
            mode = ThemeMode.DARK,
            titleRes = R.string.theme_dark,
            subtitleRes = R.string.theme_dark_sub,
            icon = Icons.Filled.DarkMode,
        ),
    )

@Suppress("LongMethod", "LongParameterList")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreenContent(
    state: SettingsUiState,
    snackbarHostState: SnackbarHostState,
    onThemeModeSelected: (ThemeMode) -> Unit,
    onHapticsToggled: (Boolean) -> Unit,
    onQuitDateChanged: (Long) -> Unit,
    onWeeklySpendChanged: (Int) -> Unit,
    onPuffsPerDayChanged: (Int) -> Unit,
    onDeviceTypeChanged: (DeviceType) -> Unit,
    onPledgeToggled: (Boolean) -> Unit,
    onPledgeTimeChanged: (Int, Int) -> Unit,
    onMilestoneCelebrationsChanged: (Boolean) -> Unit,
    onEveningToggled: (Boolean) -> Unit,
    onOpenNotificationSettings: () -> Unit,
    onOpenPaywall: () -> Unit,
    onRestorePurchases: () -> Unit,
    onExportData: () -> Unit,
    onResetClicked: () -> Unit,
    onResetDismissed: () -> Unit,
    onResetConfirmed: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = LocalAppColors.current
    val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior()
    val tick = rememberHapticTick(medium = true)
    var quitDateDialog by remember { mutableStateOf(false) }
    var spendDialog by remember { mutableStateOf(false) }
    var puffsDialog by remember { mutableStateOf(false) }
    var deviceDialog by remember { mutableStateOf(false) }
    var pledgeTimeDialog by remember { mutableStateOf(false) }

    Scaffold(
        modifier = modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = { SettingsTopBar(scrollBehavior = scrollBehavior, colors = colors) },
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        containerColor = colors.groupedBackground,
    ) { padding ->
        InsetGroupedList(
            contentPadding =
                PaddingValues(
                    top = padding.calculateTopPadding(),
                    bottom = padding.calculateBottomPadding() + AppDimens.tabBarClearance,
                ),
        ) {
            item(key = "plan-header", contentType = "header") {
                SectionHeader(title = stringResource(R.string.section_quit_plan))
            }
            item(key = "plan-card", contentType = "card") {
                QuitPlanCard(
                    state = state,
                    colors = colors,
                    onQuitDateClicked = { quitDateDialog = true },
                    onSpendClicked = { spendDialog = true },
                    onPuffsClicked = { puffsDialog = true },
                    onDeviceClicked = { deviceDialog = true },
                )
            }
            item(key = "notifications-header", contentType = "header") {
                SectionHeader(title = stringResource(R.string.section_notifications))
            }
            item(key = "notifications-card", contentType = "card") {
                NotificationsCard(
                    state = state,
                    colors = colors,
                    onPledgeToggled = { tick(); onPledgeToggled(!state.pledgeEnabled) },
                    onPledgeTimeClicked = { pledgeTimeDialog = true },
                    onMilestoneCelebrationsChanged = {
                        tick()
                        onMilestoneCelebrationsChanged(!state.milestoneCelebrations)
                    },
                    onEveningToggled = { tick(); onEveningToggled(!state.eveningEnabled) },
                    onOpenNotificationSettings = onOpenNotificationSettings,
                )
            }
            item(key = "appearance-header", contentType = "header") {
                SectionHeader(title = stringResource(R.string.section_appearance))
            }
            item(key = "appearance-card", contentType = "card") {
                GroupCard {
                    ThemeOptions.forEachIndexed { index, option ->
                        if (index > 0) RowDivider()
                        val selected = state.themeMode == option.mode
                        SettingsRow(
                            title = stringResource(option.titleRes),
                            subtitle = stringResource(option.subtitleRes),
                            leadingIcon = {
                                Icon(
                                    imageVector = option.icon,
                                    contentDescription = null,
                                    tint = colors.secondaryLabel,
                                    modifier = Modifier.size(AppDimens.iconSize),
                                )
                            },
                            trailing = {
                                RadioButton(
                                    selected = selected,
                                    onClick = null,
                                    colors = RadioButtonDefaults.colors(selectedColor = colors.accent),
                                )
                            },
                            selectableSelected = selected,
                            onClick = { tick(); onThemeModeSelected(option.mode) },
                        )
                    }
                    RowDivider()
                    SettingsRow(
                        title = stringResource(R.string.settings_haptics_title),
                        subtitle = stringResource(R.string.settings_haptics_sub),
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Filled.Vibration,
                                contentDescription = null,
                                tint = colors.secondaryLabel,
                                modifier = Modifier.size(AppDimens.iconSize),
                            )
                        },
                        trailing = { Switch(checked = state.hapticsEnabled, onCheckedChange = null) },
                        toggleableValue = state.hapticsEnabled,
                        onClick = { tick(); onHapticsToggled(!state.hapticsEnabled) },
                    )
                }
            }
            item(key = "subscription-header", contentType = "header") {
                SectionHeader(title = stringResource(R.string.section_subscription))
            }
            item(key = "subscription-card", contentType = "card") {
                SubscriptionCard(
                    state = state,
                    colors = colors,
                    onOpenPaywall = onOpenPaywall,
                    onRestorePurchases = onRestorePurchases,
                )
            }
            item(key = "data-header", contentType = "header") {
                SectionHeader(title = stringResource(R.string.section_data))
            }
            item(key = "data-card", contentType = "card") {
                DataCard(
                    state = state,
                    colors = colors,
                    onExportData = onExportData,
                    onResetClicked = onResetClicked,
                )
            }
            item(key = "about-header", contentType = "header") {
                SectionHeader(title = stringResource(R.string.section_about))
            }
            item(key = "about-card", contentType = "card") {
                AboutCard(colors = colors)
            }
        }
    }

    if (quitDateDialog) {
        QuitDateDialog(
            currentEpochMs = state.quitDateEpochMs,
            onDismiss = { quitDateDialog = false },
            onConfirm = {
                quitDateDialog = false
                onQuitDateChanged(it)
            },
        )
    }
    if (spendDialog) {
        WeeklySpendDialog(
            currentCents = state.weeklySpendCents,
            onDismiss = { spendDialog = false },
            onConfirm = {
                spendDialog = false
                onWeeklySpendChanged(it)
            },
        )
    }
    if (puffsDialog) {
        PuffsDialog(
            current = state.puffsPerDay,
            onDismiss = { puffsDialog = false },
            onConfirm = {
                puffsDialog = false
                onPuffsPerDayChanged(it)
            },
        )
    }
    if (deviceDialog) {
        DeviceDialog(
            current = state.deviceType,
            onDismiss = { deviceDialog = false },
            onConfirm = {
                deviceDialog = false
                onDeviceTypeChanged(it)
            },
        )
    }
    if (pledgeTimeDialog) {
        PledgeTimeDialog(
            hour = state.pledgeHour,
            minute = state.pledgeMinute,
            onDismiss = { pledgeTimeDialog = false },
            onConfirm = {
                pledgeTimeDialog = false
                onPledgeTimeChanged(it.first, it.second)
            },
        )
    }
    if (state.showResetConfirm) {
        ResetConfirmDialog(
            onDismiss = onResetDismissed,
            onConfirm = onResetConfirmed,
        )
    }
}

@Composable
@Suppress("LongMethod")
private fun QuitPlanCard(
    state: SettingsUiState,
    colors: AppColors,
    onQuitDateClicked: () -> Unit,
    onSpendClicked: () -> Unit,
    onPuffsClicked: () -> Unit,
    onDeviceClicked: () -> Unit,
) {
    GroupCard {
        SettingsRow(
            title = stringResource(R.string.settings_quit_date_title),
            subtitle = quitDateLabel(state.quitDateEpochMs),
            leadingIcon = {
                Icon(
                    imageVector = Icons.Filled.DateRange,
                    contentDescription = null,
                    tint = colors.secondaryLabel,
                    modifier = Modifier.size(AppDimens.iconSize),
                )
            },
            trailing = chevron(colors),
            onClick = onQuitDateClicked,
        )
        RowDivider()
        SettingsRow(
            title = stringResource(R.string.settings_device_title),
            subtitle = deviceLabel(state.deviceType),
            leadingIcon = {
                Icon(
                    imageVector = Icons.Filled.Devices,
                    contentDescription = null,
                    tint = colors.secondaryLabel,
                    modifier = Modifier.size(AppDimens.iconSize),
                )
            },
            trailing = chevron(colors),
            onClick = onDeviceClicked,
        )
        RowDivider()
        SettingsRow(
            title = stringResource(R.string.settings_spend_title),
            subtitle = formatMoneyCents(state.weeklySpendCents),
            leadingIcon = {
                Icon(
                    imageVector = Icons.Filled.Schedule,
                    contentDescription = null,
                    tint = colors.secondaryLabel,
                    modifier = Modifier.size(AppDimens.iconSize),
                )
            },
            trailing = chevron(colors),
            onClick = onSpendClicked,
        )
        RowDivider()
        SettingsRow(
            title = stringResource(R.string.settings_puffs_title),
            subtitle = puffsLabel(state.puffsPerDay),
            leadingIcon = {
                Icon(
                    imageVector = Icons.Filled.WaterDrop,
                    contentDescription = null,
                    tint = colors.secondaryLabel,
                    modifier = Modifier.size(AppDimens.iconSize),
                )
            },
            trailing = chevron(colors),
            onClick = onPuffsClicked,
        )
    }
}

@Composable
@Suppress("LongMethod")
private fun NotificationsCard(
    state: SettingsUiState,
    colors: AppColors,
    onPledgeToggled: () -> Unit,
    onPledgeTimeClicked: () -> Unit,
    onMilestoneCelebrationsChanged: () -> Unit,
    onEveningToggled: () -> Unit,
    onOpenNotificationSettings: () -> Unit,
) {
    GroupCard {
        SettingsRow(
            title = stringResource(R.string.settings_pledge_title),
            subtitle = stringResource(R.string.settings_pledge_sub),
            leadingIcon = {
                Icon(
                    imageVector = Icons.Filled.Notifications,
                    contentDescription = null,
                    tint = colors.secondaryLabel,
                    modifier = Modifier.size(AppDimens.iconSize),
                )
            },
            trailing = { Switch(checked = state.pledgeEnabled, onCheckedChange = null) },
            toggleableValue = state.pledgeEnabled,
            onClick = onPledgeToggled,
        )
        if (state.pledgeEnabled) {
            RowDivider()
            SettingsRow(
                title = stringResource(R.string.settings_pledge_time_title),
                subtitle = formatTimeLabel(state.pledgeHour, state.pledgeMinute),
                trailing = chevron(colors),
                onClick = onPledgeTimeClicked,
            )
        }
        RowDivider()
        SettingsRow(
            title = stringResource(R.string.settings_milestones_title),
            subtitle = stringResource(R.string.settings_milestones_sub),
            trailing = { Switch(checked = state.milestoneCelebrations, onCheckedChange = null) },
            toggleableValue = state.milestoneCelebrations,
            onClick = onMilestoneCelebrationsChanged,
        )
        RowDivider()
        SettingsRow(
            title = stringResource(R.string.settings_evening_title),
            subtitle = stringResource(R.string.settings_evening_sub),
            trailing = { Switch(checked = state.eveningEnabled, onCheckedChange = null) },
            toggleableValue = state.eveningEnabled,
            onClick = onEveningToggled,
        )
        RowDivider()
        SettingsRow(
            title = stringResource(R.string.settings_system_notifications_title),
            subtitle = stringResource(R.string.settings_system_notifications_sub),
            leadingIcon = {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ExitToApp,
                    contentDescription = null,
                    tint = colors.secondaryLabel,
                    modifier = Modifier.size(AppDimens.iconSize),
                )
            },
            trailing = chevron(colors),
            onClick = onOpenNotificationSettings,
        )
    }
}

@Composable
@Suppress("LongMethod")
private fun SubscriptionCard(
    state: SettingsUiState,
    colors: AppColors,
    onOpenPaywall: () -> Unit,
    onRestorePurchases: () -> Unit,
) {
    GroupCard {
        if (state.entitlementLoading) {
            SettingsRow(
                title = stringResource(R.string.settings_subscription_title),
                subtitle = null,
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Filled.WorkspacePremium,
                        contentDescription = null,
                        tint = colors.secondaryLabel,
                        modifier = Modifier.size(AppDimens.iconSize),
                    )
                },
                trailing = {
                    CircularProgressIndicator(
                        modifier = Modifier.size(AppDimens.iconSize),
                        color = colors.accent,
                        strokeWidth = AppDimens.progressStroke,
                    )
                },
                onClick = null,
            )
        } else {
            SettingsRow(
                title =
                    stringResource(
                        if (state.isPremium) {
                            R.string.settings_subscription_premium
                        } else {
                            R.string.settings_subscription_title
                        },
                    ),
                subtitle =
                    state.subscriptionSubtitle
                        ?: stringResource(R.string.settings_subscription_sub),
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Filled.WorkspacePremium,
                        contentDescription = null,
                        tint =
                            if (state.isPremium) {
                                colors.success
                            } else {
                                colors.secondaryLabel
                            },
                        modifier = Modifier.size(AppDimens.iconSize),
                    )
                },
                trailing = chevron(colors),
                onClick = onOpenPaywall,
            )
        }
        RowDivider()
        SettingsRow(
            title = stringResource(R.string.settings_restore_title),
            subtitle = stringResource(R.string.settings_restore_sub),
            leadingIcon = {
                Icon(
                    imageVector = Icons.Filled.Restore,
                    contentDescription = null,
                    tint = colors.secondaryLabel,
                    modifier = Modifier.size(AppDimens.iconSize),
                )
            },
            onClick = onRestorePurchases,
        )
    }
}

@Composable
private fun DataCard(
    state: SettingsUiState,
    colors: AppColors,
    onExportData: () -> Unit,
    onResetClicked: () -> Unit,
) {
    GroupCard {
        SettingsRow(
            title = stringResource(R.string.settings_export_title),
            subtitle = stringResource(R.string.settings_export_sub),
            leadingIcon = {
                Icon(
                    imageVector = Icons.Filled.Description,
                    contentDescription = null,
                    tint = colors.secondaryLabel,
                    modifier = Modifier.size(AppDimens.iconSize),
                )
            },
            trailing =
                if (state.isExporting) {
                    {
                        CircularProgressIndicator(
                            modifier = Modifier.size(AppDimens.iconSize),
                            color = colors.accent,
                            strokeWidth = AppDimens.progressStroke,
                        )
                    }
                } else {
                    chevron(colors)
                },
            onClick = onExportData,
        )
        RowDivider()
        SettingsRow(
            title = stringResource(R.string.settings_reset_title),
            subtitle = stringResource(R.string.settings_reset_sub),
            leadingIcon = {
                Icon(
                    imageVector = Icons.Filled.Refresh,
                    contentDescription = null,
                    tint = colors.destructive,
                    modifier = Modifier.size(AppDimens.iconSize),
                )
            },
            onClick = onResetClicked,
        )
    }
}

@Composable
private fun AboutCard(colors: AppColors) {
    val context = LocalContext.current
    GroupCard {
        SettingsRow(
            title = stringResource(R.string.settings_version_title),
            subtitle = BuildConfig.VERSION_NAME,
            onClick = null,
        )
        RowDivider()
        SettingsRow(
            title = stringResource(R.string.settings_rate_title),
            subtitle = null,
            leadingIcon = {
                Icon(
                    imageVector = Icons.Filled.Star,
                    contentDescription = null,
                    tint = colors.secondaryLabel,
                    modifier = Modifier.size(AppDimens.iconSize),
                )
            },
            trailing = chevron(colors),
            onClick = { openPlayStore(context) },
        )
    }
}

private fun openPlayStore(context: Context) {
    val intent =
        Intent(Intent.ACTION_VIEW, Uri.parse(context.getString(R.string.settings_rate_url)))
    context.startActivity(intent)
}

@Composable
private fun chevron(colors: AppColors): @Composable () -> Unit =
    {
        Icon(
            imageVector = Icons.Filled.ChevronRight,
            contentDescription = null,
            tint = colors.secondaryLabel,
            modifier = Modifier.size(AppDimens.iconSize),
        )
    }

@Composable
private fun quitDateLabel(quitDateEpochMs: Long?): String {
    val zone = ZoneId.systemDefault()
    val label =
        quitDateEpochMs?.let {
            LocalDate.ofInstant(Instant.ofEpochMilli(it), zone).toString()
        } ?: "—"
    return stringResource(R.string.settings_quit_date_sub, label)
}

@Composable
private fun deviceLabel(deviceType: DeviceType): String =
    stringResource(
        when (deviceType) {
            DeviceType.DISPOSABLE -> R.string.device_disposable
            DeviceType.POD -> R.string.device_pod
            DeviceType.REFILLABLE -> R.string.device_refillable
        },
    )

@Composable
private fun puffsLabel(puffsPerDay: Int): String =
    if (puffsPerDay < 0) {
        stringResource(R.string.puffs_unsure)
    } else {
        stringResource(R.string.settings_puffs_sub, puffsPerDay)
    }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun QuitDateDialog(
    currentEpochMs: Long?,
    onDismiss: () -> Unit,
    onConfirm: (Long) -> Unit,
) {
    val zone = ZoneId.systemDefault()
    val initialMillis =
        currentEpochMs ?: LocalDate.now(zone).atStartOfDay(zone).toInstant().toEpochMilli()
    val datePickerState = rememberDatePickerState(initialSelectedDateMillis = initialMillis)
    DatePickerDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(
                onClick = {
                    datePickerState.selectedDateMillis?.let { onConfirm(it) }
                        ?: onDismiss()
                },
            ) {
                Text(text = stringResource(R.string.action_save))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(text = stringResource(R.string.action_cancel))
            }
        },
    ) {
        DatePicker(state = datePickerState)
    }
}

@Composable
private fun WeeklySpendDialog(
    currentCents: Int,
    onDismiss: () -> Unit,
    onConfirm: (Int) -> Unit,
) {
    var sliderCents by remember { mutableStateOf(currentCents.coerceIn(0, MAX_WEEKLY_SPEND_CENTS)) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(text = stringResource(R.string.settings_spend_title)) },
        text = {
            Column {
                Text(
                    text = formatMoneyCents(sliderCents),
                    style = AppTypography.largeTitle,
                    color = LocalAppColors.current.label,
                )
                Slider(
                    value = sliderCents.toFloat(),
                    onValueChange = { sliderCents = it.toInt() },
                    valueRange = 0f..MAX_WEEKLY_SPEND_CENTS.toFloat(),
                    steps = 99,
                )
            }
        },
        confirmButton = {
            TextButton(onClick = { onConfirm(sliderCents) }) {
                Text(text = stringResource(R.string.action_save))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(text = stringResource(R.string.action_cancel))
            }
        },
    )
}

@Composable
private fun PuffsDialog(
    current: Int,
    onDismiss: () -> Unit,
    onConfirm: (Int) -> Unit,
) {
    val unsureLabel = stringResource(R.string.puffs_unsure)
    val options = remember { listOf(-1) + listOf(5, 10, 20, 40, 60) }
    var selected by remember { mutableStateOf(current) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(text = stringResource(R.string.settings_puffs_title)) },
        text = {
            Column(modifier = Modifier.selectableGroup()) {
                options.forEach { value ->
                    Row(
                        modifier =
                            Modifier
                                .fillMaxWidth()
                                .selectable(
                                    selected = selected == value,
                                    onClick = { selected = value },
                                    role = Role.RadioButton,
                                )
                                .padding(vertical = AppDimens.spacing8),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        RadioButton(
                            selected = selected == value,
                            onClick = null,
                            colors = RadioButtonDefaults.colors(selectedColor = LocalAppColors.current.accent),
                        )
                        Spacer(modifier = Modifier.width(AppDimens.spacing8))
                        Text(
                            text = if (value < 0) unsureLabel else value.toString(),
                            style = AppTypography.body,
                            color = LocalAppColors.current.label,
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = { onConfirm(selected) }) {
                Text(text = stringResource(R.string.action_save))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(text = stringResource(R.string.action_cancel))
            }
        },
    )
}

@Composable
private fun DeviceDialog(
    current: DeviceType,
    onDismiss: () -> Unit,
    onConfirm: (DeviceType) -> Unit,
) {
    val colors = LocalAppColors.current
    var selected by remember { mutableStateOf(current) }
    val options = remember { DeviceType.entries.toList() }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(text = stringResource(R.string.settings_device_title)) },
        text = {
            Column(modifier = Modifier.selectableGroup()) {
                options.forEach { device ->
                    Row(
                        modifier =
                            Modifier
                                .fillMaxWidth()
                                .selectable(
                                    selected = selected == device,
                                    onClick = { selected = device },
                                    role = Role.RadioButton,
                                )
                                .padding(vertical = AppDimens.spacing8),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        RadioButton(
                            selected = selected == device,
                            onClick = null,
                            colors = RadioButtonDefaults.colors(selectedColor = colors.accent),
                        )
                        Spacer(modifier = Modifier.width(AppDimens.spacing8))
                        Text(
                            text = deviceLabel(device),
                            style = AppTypography.body,
                            color = colors.label,
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = { onConfirm(selected) }) {
                Text(text = stringResource(R.string.action_save))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(text = stringResource(R.string.action_cancel))
            }
        },
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PledgeTimeDialog(
    hour: Int,
    minute: Int,
    onDismiss: () -> Unit,
    onConfirm: (Pair<Int, Int>) -> Unit,
) {
    val timePickerState = rememberTimePickerState(initialHour = hour, initialMinute = minute)
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(text = stringResource(R.string.settings_pledge_time_title)) },
        text = {
            TimePicker(state = timePickerState)
        },
        confirmButton = {
            TextButton(
                onClick = { onConfirm(timePickerState.hour to timePickerState.minute) },
            ) {
                Text(text = stringResource(R.string.action_save))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(text = stringResource(R.string.action_cancel))
            }
        },
    )
}

@Composable
private fun ResetConfirmDialog(
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(text = stringResource(R.string.settings_reset_confirm_title)) },
        text = { Text(text = stringResource(R.string.settings_reset_confirm_text)) },
        confirmButton = {
            Button(
                onClick = onConfirm,
                colors = ButtonDefaults.buttonColors(containerColor = LocalAppColors.current.destructive),
            ) {
                Text(
                    text = stringResource(R.string.settings_reset_confirm_action),
                    color = Color.White,
                )
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(text = stringResource(R.string.action_cancel))
            }
        },
    )
}

private fun openNotificationSettings(context: Context) {
    val intent =
        Intent(SystemSettings.ACTION_APP_NOTIFICATION_SETTINGS).apply {
            putExtra(SystemSettings.EXTRA_APP_PACKAGE, context.packageName)
        }
    context.startActivity(intent)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SettingsTopBar(
    scrollBehavior: TopAppBarScrollBehavior,
    colors: AppColors,
) {
    LargeTopAppBar(
        title = {
            Text(
                text = stringResource(R.string.settings_title),
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
