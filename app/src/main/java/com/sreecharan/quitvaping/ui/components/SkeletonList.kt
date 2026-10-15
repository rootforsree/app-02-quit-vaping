package com.sreecharan.quitvaping.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.sreecharan.quitvaping.ui.theme.AppDimens
import com.sreecharan.quitvaping.ui.theme.LocalAppColors
import com.sreecharan.quitvaping.ui.theme.animationsEnabled

/**
 * Loading placeholder shaped like the real content: grouped cards with
 * title, body and caption bars. Alpha pulse only, no gradient sweep, and
 * a static alpha when Reduce Motion is on. Never a full-screen spinner.
 */
private const val SHIMMER_STATIC_ALPHA = 0.6f
private const val SHIMMER_MIN_ALPHA = 0.35f
private const val SHIMMER_MAX_ALPHA = 1f
private const val SHIMMER_DURATION_MS = 1200

@Composable
fun SkeletonList(
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(0.dp),
    rows: Int = 6,
) {
    val shimmerAlpha =
        if (!animationsEnabled(LocalContext.current)) {
            SHIMMER_STATIC_ALPHA
        } else {
            val transition = rememberInfiniteTransition(label = "skeleton")
            val pulse by transition.animateFloat(
                initialValue = SHIMMER_MIN_ALPHA,
                targetValue = SHIMMER_MAX_ALPHA,
                animationSpec =
                    infiniteRepeatable(
                        animation = tween(durationMillis = SHIMMER_DURATION_MS, easing = LinearEasing),
                        repeatMode = RepeatMode.Reverse,
                    ),
                label = "skeletonAlpha",
            )
            pulse
        }
    InsetGroupedList(
        modifier = modifier,
        contentPadding = contentPadding,
    ) {
        items(
            count = rows,
            key = { index -> "skeleton-$index" },
            contentType = { "skeleton" },
        ) {
            GroupCard {
                Column(
                    modifier = Modifier.padding(AppDimens.spacing16),
                    verticalArrangement = Arrangement.spacedBy(AppDimens.spacing8),
                ) {
                    SkeletonBar(
                        widthFraction = 0.55f,
                        height = AppDimens.spacing16,
                        alpha = shimmerAlpha,
                    )
                    SkeletonBar(
                        widthFraction = 0.9f,
                        height = AppDimens.spacing12,
                        alpha = shimmerAlpha,
                    )
                    SkeletonBar(
                        widthFraction = 0.35f,
                        height = AppDimens.spacing12,
                        alpha = shimmerAlpha,
                    )
                }
            }
        }
    }
}

@Composable
private fun SkeletonBar(
    widthFraction: Float,
    height: Dp,
    alpha: Float,
) {
    Box(
        modifier =
            Modifier
                .fillMaxWidth(widthFraction)
                .height(height)
                .alpha(alpha)
                .background(
                    color = LocalAppColors.current.secondaryLabel.copy(alpha = 0.25f),
                    shape = RoundedCornerShape(AppDimens.spacing4),
                ),
    )
}
