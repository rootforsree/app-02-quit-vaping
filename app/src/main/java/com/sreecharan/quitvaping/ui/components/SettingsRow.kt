package com.sreecharan.quitvaping.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.toggleable
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import com.sreecharan.quitvaping.ui.theme.AppColors
import com.sreecharan.quitvaping.ui.theme.AppDimens
import com.sreecharan.quitvaping.ui.theme.AppTypography
import com.sreecharan.quitvaping.ui.theme.LocalAppColors
import com.sreecharan.quitvaping.ui.theme.rememberHapticTick

/**
 * One settings row: at least 48dp tall, optional leading icon, title,
 * subtitle and trailing slot. Exactly one interaction mode is active:
 * toggleable (switch rows), selectable (radio rows) or clickable.
 * Children merge into one TalkBack node so the row announces as a single
 * control, and every tap plays a light haptic tick unless haptics are off.
 */
@Composable
@Suppress("LongParameterList") // Flexible row component: 9 optional slots for different settings types
fun SettingsRow(
    title: String,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    leadingIcon: (@Composable () -> Unit)? = null,
    trailing: (@Composable () -> Unit)? = null,
    onClick: (() -> Unit)? = null,
    toggleableValue: Boolean? = null,
    selectableSelected: Boolean? = null,
    contentDescription: String? = null,
) {
    val colors = LocalAppColors.current
    val tick = rememberHapticTick()
    val description =
        contentDescription ?: buildString {
            append(title)
            if (subtitle != null) {
                append(", ")
                append(subtitle)
            }
        }
    val interaction =
        Modifier.interactionModifier(
            toggleableValue = toggleableValue,
            selectableSelected = selectableSelected,
            onClick = onClick,
            tick = tick,
        )
    Row(
        modifier =
            modifier
                .fillMaxWidth()
                .defaultMinSize(minHeight = AppDimens.rowMinHeight)
                .then(interaction)
                .semantics(mergeDescendants = true) {
                    this.contentDescription = description
                }.padding(
                    horizontal = AppDimens.rowHorizontalPadding,
                    vertical = AppDimens.rowVerticalPadding,
                ),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(AppDimens.spacing12),
    ) {
        SettingsRowContent(
            title = title,
            subtitle = subtitle,
            leadingIcon = leadingIcon,
            trailing = trailing,
            colors = colors,
        )
    }
}

@Composable
private fun RowScope.SettingsRowContent(
    title: String,
    subtitle: String?,
    leadingIcon: (@Composable () -> Unit)?,
    trailing: (@Composable () -> Unit)?,
    colors: AppColors,
) {
    if (leadingIcon != null) {
        Box(modifier = Modifier.size(AppDimens.iconSize)) {
            leadingIcon()
        }
    }
    Column(modifier = Modifier.weight(1f)) {
        Text(
            text = title,
            style = AppTypography.body,
            color = colors.label,
        )
        if (subtitle != null) {
            Text(
                text = subtitle,
                style = AppTypography.footnote,
                color = colors.secondaryLabel,
            )
        }
    }
    if (trailing != null) {
        trailing()
    }
}

private fun Modifier.interactionModifier(
    toggleableValue: Boolean?,
    selectableSelected: Boolean?,
    onClick: (() -> Unit)?,
    tick: () -> Unit,
): Modifier =
    when {
        toggleableValue != null -> {
            this.toggleable(
                value = toggleableValue,
                role = Role.Switch,
                onValueChange = {
                    tick()
                    onClick?.invoke()
                },
            )
        }

        selectableSelected != null -> {
            this.selectable(
                selected = selectableSelected,
                role = Role.RadioButton,
                onClick = {
                    tick()
                    onClick?.invoke()
                },
            )
        }

        onClick != null -> {
            this.clickable(
                role = Role.Button,
                onClick = {
                    tick()
                    onClick()
                },
            )
        }

        else -> {
            this
        }
    }
