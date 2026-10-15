package com.sreecharan.quitvaping.ui.components

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.sp
import com.sreecharan.quitvaping.ui.theme.AppDimens
import com.sreecharan.quitvaping.ui.theme.AppTypography
import com.sreecharan.quitvaping.ui.theme.LocalAppColors

/**
 * Grouped-list section header: 13sp uppercase secondary label, aligned with
 * the card content below it (16dp list inset + 16dp row padding).
 */
@Composable
fun SectionHeader(
    title: String,
    modifier: Modifier = Modifier,
) {
    // Read through LocalConfiguration so the header recomposes on locale change.
    val locale = LocalConfiguration.current.locales.get(0)
    Text(
        text = title.uppercase(locale),
        style = AppTypography.footnote.copy(letterSpacing = 0.5.sp),
        color = LocalAppColors.current.secondaryLabel,
        modifier =
            modifier.padding(
                start = AppDimens.spacing16,
                top = AppDimens.spacing8,
                bottom = AppDimens.spacing8,
            ),
    )
}
