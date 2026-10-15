package com.sreecharan.quitvaping.ui.onboarding

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.outlined.CalendarToday
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.sreecharan.quitvaping.data.model.DeviceType
import com.sreecharan.quitvaping.ui.components.GroupCard
import com.sreecharan.quitvaping.ui.theme.AppDimens
import com.sreecharan.quitvaping.ui.theme.AppShapes
import com.sreecharan.quitvaping.ui.theme.AppTypography
import com.sreecharan.quitvaping.ui.theme.LocalAppColors
import com.sreecharan.quitvaping.ui.theme.rememberHapticTick
import java.text.NumberFormat
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

// ---------------------------------------------------------------------------
// Shared page scaffold
// ---------------------------------------------------------------------------

@Composable
private fun PageColumn(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    Column(
        modifier =
            modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = AppDimens.screenMargin),
        verticalArrangement = Arrangement.spacedBy(AppDimens.spacing16),
    ) {
        Spacer(modifier = Modifier.height(AppDimens.spacing8))
        content()
        Spacer(modifier = Modifier.height(AppDimens.spacing8))
    }
}

@Composable
private fun PageTitle(text: String) {
    val colors = LocalAppColors.current
    Text(
        text = text,
        style = AppTypography.largeTitle,
        color = colors.label,
    )
}

@Composable
private fun PageSubtitle(text: String) {
    val colors = LocalAppColors.current
    Text(
        text = text,
        style = AppTypography.body,
        color = colors.secondaryLabel,
    )
}

// ---------------------------------------------------------------------------
// Page 1: Welcome
// ---------------------------------------------------------------------------

@Composable
fun WelcomePage(modifier: Modifier = Modifier) {
    val colors = LocalAppColors.current
    Column(
        modifier =
            modifier
                .fillMaxSize()
                .padding(horizontal = AppDimens.screenMargin),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = "\uD83C\uDF31",
            fontSize = 72.sp,
        )
        Spacer(modifier = Modifier.height(AppDimens.spacing24))
        Text(
            text = "Quit vaping for good.",
            style = AppTypography.largeTitle,
            color = colors.label,
            textAlign = TextAlign.Center,
        )
        Spacer(modifier = Modifier.height(AppDimens.spacing12))
        Text(
            text = "Track your progress, beat cravings, and watch your savings grow \u2014 one day at a time.",
            style = AppTypography.body,
            color = colors.secondaryLabel,
            textAlign = TextAlign.Center,
        )
        Spacer(modifier = Modifier.height(AppDimens.spacing24))
        GroupCard {
            Column(modifier = Modifier.padding(AppDimens.spacing16)) {
                ValueRow(emoji = "\uD83D\uDCCA", title = "Days vape-free", subtitle = "Your streak, live")
                ValueRow(emoji = "\uD83D\uDCB0", title = "Money saved", subtitle = "Watch it stack up")
                ValueRow(emoji = "\uD83E\uDEDA", title = "Health recovery", subtitle = "Your body healing")
            }
        }
    }
}

@Composable
private fun ValueRow(
    emoji: String,
    title: String,
    subtitle: String,
) {
    val colors = LocalAppColors.current
    Row(
        modifier = Modifier.padding(vertical = AppDimens.spacing8),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(AppDimens.spacing12),
    ) {
        Text(text = emoji, fontSize = 28.sp)
        Column {
            Text(text = title, style = AppTypography.headline, color = colors.label)
            Text(text = subtitle, style = AppTypography.subheadline, color = colors.secondaryLabel)
        }
    }
}

// ---------------------------------------------------------------------------
// Page 2: Quit date
// ---------------------------------------------------------------------------

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuitDatePage(
    state: OnboardingUiState,
    onQuitDateChange: (Long) -> Unit,
    onPlanModeChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = LocalAppColors.current
    val tick = rememberHapticTick()
    var showPicker by remember { mutableStateOf(false) }
    val dateText = formatDate(state.quitDateEpochMs)

    PageColumn(modifier = modifier) {
        PageTitle("When did you quit?")
        PageSubtitle("This sets your day counter. Every stat in the app flows from this date.")

        GroupCard {
            Row(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .clickable {
                            tick()
                            showPicker = true
                        }.padding(AppDimens.spacing16),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(AppDimens.spacing12),
            ) {
                Icon(
                    imageVector = Icons.Outlined.CalendarToday,
                    contentDescription = null,
                    tint = colors.accent,
                    modifier = Modifier.size(AppDimens.iconSize),
                )
                Column(modifier = Modifier.weight(1f)) {
                    Text(text = "Quit date", style = AppTypography.headline, color = colors.label)
                    Text(text = dateText, style = AppTypography.subheadline, color = colors.secondaryLabel)
                }
            }
        }

        // Plan mode: haven't quit yet.
        Row(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .clickable {
                        tick()
                        onPlanModeChange(!state.hasQuitDate)
                    }.padding(vertical = AppDimens.spacing8),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(AppDimens.spacing12),
        ) {
            val checked = !state.hasQuitDate
            Surface(
                shape = RoundedCornerShape(6.dp),
                color = if (checked) colors.accent else colors.secondaryLabel.copy(alpha = 0.2f),
                modifier = Modifier.size(24.dp),
            ) {
                if (checked) {
                    Icon(
                        imageVector = Icons.Filled.Check,
                        contentDescription = null,
                        tint = colors.cardSurface,
                        modifier = Modifier.padding(4.dp),
                    )
                }
            }
            Text(
                text = "I haven't quit yet \u2014 help me plan",
                style = AppTypography.body,
                color = colors.label,
            )
        }
        if (!state.hasQuitDate) {
            Text(
                text = "Pick the date you plan to quit. We'll count down with you.",
                style = AppTypography.footnote,
                color = colors.secondaryLabel,
            )
        }
    }

    if (showPicker) {
        val pickerState =
            rememberDatePickerState(
                initialSelectedDateMillis = state.quitDateEpochMs,
            )
        DatePickerDialog(
            onDismissRequest = { showPicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        pickerState.selectedDateMillis?.let { onQuitDateChange(it) }
                        showPicker = false
                    },
                ) { Text("Done") }
            },
            dismissButton = {
                TextButton(onClick = { showPicker = false }) { Text("Cancel") }
            },
        ) {
            DatePicker(state = pickerState)
        }
    }
}

private fun formatDate(epochMs: Long): String =
    Instant
        .ofEpochMilli(epochMs)
        .atZone(ZoneId.systemDefault())
        .toLocalDate()
        .format(DateTimeFormatter.ofPattern("EEEE, MMM d, yyyy", Locale.getDefault()))

// ---------------------------------------------------------------------------
// Page 3: Device + spend (LIVE savings calculator)
// ---------------------------------------------------------------------------

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SpendPage(
    state: OnboardingUiState,
    onDeviceTypeChange: (DeviceType) -> Unit,
    onUnitsPerWeekChange: (Int) -> Unit,
    onCostPerUnitChange: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = LocalAppColors.current
    val tick = rememberHapticTick()
    val savingsText =
        NumberFormat.getCurrencyInstance(Locale.US).format(state.yearlySavingsDollars)

    PageColumn(modifier = modifier) {
        PageTitle("What did you spend?")
        PageSubtitle("Be honest \u2014 this powers your money-saved counter.")

        Text(text = "Device type", style = AppTypography.headline, color = colors.label)
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(AppDimens.spacing8),
            verticalArrangement = Arrangement.spacedBy(AppDimens.spacing8),
        ) {
            DeviceType.entries.forEach { type ->
                val label =
                    when (type) {
                        DeviceType.DISPOSABLE -> "Disposable"
                        DeviceType.POD -> "Pod system"
                        DeviceType.REFILLABLE -> "Refillable"
                    }
                FilterChip(
                    selected = state.deviceType == type,
                    onClick = {
                        tick()
                        onDeviceTypeChange(type)
                    },
                    label = { Text(label) },
                    leadingIcon =
                        if (state.deviceType == type) {
                            {
                                Icon(
                                    imageVector = Icons.Filled.Check,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp),
                                )
                            }
                        } else {
                            null
                        },
                    colors =
                        FilterChipDefaults.filterChipColors(
                            selectedContainerColor = colors.accent,
                            selectedLabelColor = colors.cardSurface,
                            selectedLeadingIconColor = colors.cardSurface,
                        ),
                )
            }
        }

        // Units per week stepper.
        Text(text = "Units per week", style = AppTypography.headline, color = colors.label)
        GroupCard {
            Row(
                modifier = Modifier.padding(AppDimens.spacing16),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(AppDimens.spacing16),
            ) {
                StepperButton(label = "\u2212", onClick = { onUnitsPerWeekChange(state.unitsPerWeek - 1) })
                Text(
                    text = "${state.unitsPerWeek}",
                    style = AppTypography.title2,
                    color = colors.label,
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.Center,
                )
                StepperButton(label = "+", onClick = { onUnitsPerWeekChange(state.unitsPerWeek + 1) })
            }
        }

        // Cost per unit.
        Text(text = "Cost per unit", style = AppTypography.headline, color = colors.label)
        var costText by remember(state.costPerUnitCents) {
            mutableStateOf("%.2f".format(state.costPerUnitCents / 100.0))
        }
        OutlinedTextField(
            value = costText,
            onValueChange = { input ->
                costText = input
                input.toDoubleOrNull()?.let { dollars ->
                    onCostPerUnitChange((dollars * 100).toInt())
                }
            },
            label = { Text("Dollars") },
            prefix = { Text("$") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            shape = AppShapes.card,
        )

        // LIVE savings hero. Updates on every keystroke/step.
        GroupCard {
            Column(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .background(
                            colors.accent.copy(alpha = 0.08f),
                            shape = AppShapes.card,
                        ).padding(AppDimens.spacing16),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(
                    text = "You'll save",
                    style = AppTypography.subheadline,
                    color = colors.secondaryLabel,
                )
                Text(
                    text = "$savingsText / year",
                    style = AppTypography.title2.copy(fontWeight = FontWeight.Bold),
                    color = colors.accent,
                )
                Text(
                    text = "That's real money back in your pocket.",
                    style = AppTypography.footnote,
                    color = colors.secondaryLabel,
                    textAlign = TextAlign.Center,
                )
            }
        }
    }
}

@Composable
private fun StepperButton(
    label: String,
    onClick: () -> Unit,
) {
    val colors = LocalAppColors.current
    val tick = rememberHapticTick()
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = colors.accent.copy(alpha = 0.12f),
        modifier =
            Modifier
                .size(44.dp)
                .clickable {
                    tick()
                    onClick()
                },
    ) {
        androidx.compose.foundation.layout.Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.fillMaxSize(),
        ) {
            Text(text = label, style = AppTypography.title2, color = colors.accent)
        }
    }
}

// ---------------------------------------------------------------------------
// Page 4: Puffs + nicotine
// ---------------------------------------------------------------------------

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun UsagePage(
    state: OnboardingUiState,
    onPuffsPerDayChange: (Int) -> Unit,
    onNicotineChange: (Float) -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = LocalAppColors.current
    val tick = rememberHapticTick()

    PageColumn(modifier = modifier) {
        PageTitle("How much did you vape?")
        PageSubtitle("Rough estimates are fine. This powers your puffs-avoided stat.")

        Text(text = "Puffs per day", style = AppTypography.headline, color = colors.label)
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(AppDimens.spacing8),
            verticalArrangement = Arrangement.spacedBy(AppDimens.spacing8),
        ) {
            PUFF_OPTIONS.forEach { (label, value) ->
                FilterChip(
                    selected = state.puffsPerDay == value,
                    onClick = {
                        tick()
                        onPuffsPerDayChange(value)
                    },
                    label = { Text(label) },
                    colors =
                        FilterChipDefaults.filterChipColors(
                            selectedContainerColor = colors.accent,
                            selectedLabelColor = colors.cardSurface,
                        ),
                )
            }
        }

        Text(text = "Nicotine strength", style = AppTypography.headline, color = colors.label)
        PageSubtitle("Most disposables are 50 mg/mL (5%). Leave at 0 if unsure.")
        GroupCard {
            Column(modifier = Modifier.padding(AppDimens.spacing16)) {
                Text(
                    text =
                        if (state.nicotineMgPerMl <= 0f) {
                            "Not sure"
                        } else {
                            "${state.nicotineMgPerMl.toInt()} mg/mL"
                        },
                    style = AppTypography.title2,
                    color = colors.label,
                    modifier = Modifier.align(Alignment.CenterHorizontally),
                )
                Slider(
                    value = state.nicotineMgPerMl,
                    onValueChange = onNicotineChange,
                    valueRange = 0f..60f,
                    steps = 11,
                    colors =
                        SliderDefaults.colors(
                            thumbColor = colors.accent,
                            activeTrackColor = colors.accent,
                        ),
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Text(text = "0", style = AppTypography.caption, color = colors.secondaryLabel)
                    Text(text = "60 mg", style = AppTypography.caption, color = colors.secondaryLabel)
                }
            }
        }
    }
}

// ---------------------------------------------------------------------------
// Page 5: Triggers multi-select
// ---------------------------------------------------------------------------

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun TriggersPage(
    state: OnboardingUiState,
    onTriggerToggle: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = LocalAppColors.current
    val tick = rememberHapticTick()

    PageColumn(modifier = modifier) {
        PageTitle("What triggers cravings?")
        PageSubtitle("Pick all that apply. We'll watch for patterns and warn you at risky times.")

        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(AppDimens.spacing8),
            verticalArrangement = Arrangement.spacedBy(AppDimens.spacing8),
        ) {
            ONBOARDING_TRIGGERS.forEach { trigger ->
                val selected = trigger in state.triggers
                FilterChip(
                    selected = selected,
                    onClick = {
                        tick()
                        onTriggerToggle(trigger)
                    },
                    label = { Text(trigger) },
                    leadingIcon =
                        if (selected) {
                            {
                                Icon(
                                    imageVector = Icons.Filled.Check,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp),
                                )
                            }
                        } else {
                            null
                        },
                    colors =
                        FilterChipDefaults.filterChipColors(
                            selectedContainerColor = colors.accent,
                            selectedLabelColor = colors.cardSurface,
                            selectedLeadingIconColor = colors.cardSurface,
                        ),
                )
            }
        }

        if (state.triggers.isNotEmpty()) {
            GroupCard {
                Text(
                    text = "We'll keep an eye on ${state.triggers.joinToString(", ").lowercase()} cravings for you.",
                    style = AppTypography.subheadline,
                    color = colors.secondaryLabel,
                    modifier = Modifier.padding(AppDimens.spacing16),
                )
            }
        }
    }
}

// ---------------------------------------------------------------------------
// Page 6: Notifications
// ---------------------------------------------------------------------------

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationsPage(
    state: OnboardingUiState,
    onPledgeTimeChange: (Int, Int) -> Unit,
    onMilestoneCelebrationsChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = LocalAppColors.current
    val tick = rememberHapticTick()
    val context = LocalContext.current
    var showTimePicker by remember { mutableStateOf(false) }

    val permissionGranted =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS,
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            true
        }
    var hasPermission by remember { mutableStateOf(permissionGranted) }
    val permissionLauncher =
        rememberLauncherForActivityResult(
            ActivityResultContracts.RequestPermission(),
        ) { granted ->
            hasPermission = granted
        }

    PageColumn(modifier = modifier) {
        PageTitle("Stay on track")
        PageSubtitle("A morning pledge doubles your odds. We'll remind you \u2014 gently.")

        // Pledge time row.
        GroupCard {
            Row(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .clickable {
                            tick()
                            showTimePicker = true
                        }.padding(AppDimens.spacing16),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(AppDimens.spacing12),
            ) {
                Icon(
                    imageVector = Icons.Outlined.Schedule,
                    contentDescription = null,
                    tint = colors.accent,
                    modifier = Modifier.size(AppDimens.iconSize),
                )
                Column(modifier = Modifier.weight(1f)) {
                    Text(text = "Daily pledge reminder", style = AppTypography.headline, color = colors.label)
                    Text(
                        text = formatTime(state.pledgeHour, state.pledgeMinute),
                        style = AppTypography.subheadline,
                        color = colors.secondaryLabel,
                    )
                }
            }
        }

        // Milestone celebrations toggle.
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
                    imageVector = Icons.Outlined.Notifications,
                    contentDescription = null,
                    tint = colors.accent,
                    modifier = Modifier.size(AppDimens.iconSize),
                )
                Column(modifier = Modifier.weight(1f)) {
                    Text(text = "Milestone celebrations", style = AppTypography.headline, color = colors.label)
                    Text(
                        text = "Cheer when your body hits recovery milestones",
                        style = AppTypography.subheadline,
                        color = colors.secondaryLabel,
                    )
                }
                Switch(
                    checked = state.milestoneCelebrations,
                    onCheckedChange = {
                        tick()
                        onMilestoneCelebrationsChange(it)
                    },
                )
            }
        }

        // Permission CTA (only when not granted on API 33+).
        if (!hasPermission && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            GroupCard {
                Column(modifier = Modifier.padding(AppDimens.spacing16)) {
                    Text(
                        text = "Enable notifications to get your pledge reminders.",
                        style = AppTypography.subheadline,
                        color = colors.secondaryLabel,
                    )
                    Spacer(modifier = Modifier.height(AppDimens.spacing8))
                    Text(
                        text = "Allow notifications",
                        style = AppTypography.headline,
                        color = colors.accent,
                        modifier =
                            Modifier.clickable {
                                permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                            },
                    )
                }
            }
        }
    }

    if (showTimePicker) {
        val timeState =
            rememberTimePickerState(
                initialHour = state.pledgeHour,
                initialMinute = state.pledgeMinute,
            )
        androidx.compose.material3.AlertDialog(
            onDismissRequest = { showTimePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        onPledgeTimeChange(timeState.hour, timeState.minute)
                        showTimePicker = false
                    },
                ) { Text("Done") }
            },
            dismissButton = {
                TextButton(onClick = { showTimePicker = false }) { Text("Cancel") }
            },
            text = { TimePicker(state = timeState) },
        )
    }
}

private fun formatTime(
    hour: Int,
    minute: Int,
): String {
    val suffix = if (hour < 12) "AM" else "PM"
    val h = if (hour % 12 == 0) 12 else hour % 12
    return "%d:%02d %s".format(h, minute, suffix)
}

// ---------------------------------------------------------------------------
// Page 7: Ready -> paywall transition
// ---------------------------------------------------------------------------

@Composable
fun ReadyPage(
    state: OnboardingUiState,
    modifier: Modifier = Modifier,
) {
    val colors = LocalAppColors.current
    val savingsText =
        NumberFormat.getCurrencyInstance(Locale.US).format(state.yearlySavingsDollars)
    val quitLabel =
        if (state.hasQuitDate) {
            val today = LocalDate.now()
            val quitDate =
                Instant.ofEpochMilli(state.quitDateEpochMs)
                    .atZone(ZoneId.systemDefault())
                    .toLocalDate()
            when {
                quitDate.isAfter(today) -> "Countdown to ${formatDate(state.quitDateEpochMs)}"
                else -> "Day 1 starts now"
            }
        } else {
            "Day 1 starts now"
        }

    Column(
        modifier =
            modifier
                .fillMaxSize()
                .padding(horizontal = AppDimens.screenMargin),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(text = "\uD83D\uDE80", fontSize = 72.sp)
        Spacer(modifier = Modifier.height(AppDimens.spacing24))
        Text(
            text = "You're ready.",
            style = AppTypography.largeTitle,
            color = colors.label,
            textAlign = TextAlign.Center,
        )
        Spacer(modifier = Modifier.height(AppDimens.spacing12))
        Text(
            text = quitLabel,
            style = AppTypography.title2,
            color = colors.accent,
            textAlign = TextAlign.Center,
        )
        Spacer(modifier = Modifier.height(AppDimens.spacing16))
        GroupCard {
            Column(modifier = Modifier.padding(AppDimens.spacing16)) {
                SummaryRow(label = "Yearly savings", value = savingsText)
                SummaryRow(
                    label = "Device",
                    value =
                        when (state.deviceType) {
                            DeviceType.DISPOSABLE -> "Disposable"
                            DeviceType.POD -> "Pod system"
                            DeviceType.REFILLABLE -> "Refillable"
                        },
                )
                SummaryRow(
                    label = "Pledge reminder",
                    value = formatTime(state.pledgeHour, state.pledgeMinute),
                )
            }
        }
        Spacer(modifier = Modifier.height(AppDimens.spacing12))
        Text(
            text = "Tap Continue to unlock your full quit plan.",
            style = AppTypography.footnote,
            color = colors.secondaryLabel,
            textAlign = TextAlign.Center,
        )
    }
}

@Composable
private fun SummaryRow(
    label: String,
    value: String,
) {
    val colors = LocalAppColors.current
    Row(
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(vertical = AppDimens.spacing8),
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(text = label, style = AppTypography.subheadline, color = colors.secondaryLabel)
        Text(
            text = value,
            style = AppTypography.headline,
            color = colors.label,
        )
    }
}
