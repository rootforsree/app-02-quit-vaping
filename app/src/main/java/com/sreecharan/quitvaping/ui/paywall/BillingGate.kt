package com.sreecharan.quitvaping.ui.paywall

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.sreecharan.quitvaping.data.billing.BillingRepository
import com.sreecharan.quitvaping.data.billing.EntitlementState
import com.sreecharan.quitvaping.ui.theme.AppDimens
import com.sreecharan.quitvaping.ui.theme.AppTypography
import com.sreecharan.quitvaping.ui.theme.LocalAppColors
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class BillingGateViewModel
    @Inject
    constructor(
        billing: BillingRepository,
    ) : ViewModel() {
        val entitlement = billing.entitlement
    }

/**
 * Cold-start gate. Shows a neutral loading surface (no branding flash, no
 * free-tier flash) until RevenueCat reports the real entitlement, then calls
 * [content] with the premium flag. Paying users never see the free tier.
 */
@Composable
fun BillingGate(
    viewModel: BillingGateViewModel = hiltViewModel(),
    loadingContent: @Composable () -> Unit = { BillingLoadingSplash() },
    content: @Composable (isPremium: Boolean) -> Unit,
) {
    val state by viewModel.entitlement.collectAsStateWithLifecycle()
    when (val s = state) {
        EntitlementState.Loading -> loadingContent()
        is EntitlementState.Ready -> content(s.isPremium)
        EntitlementState.Unavailable -> content(false)
    }
}

/**
 * Feature gate for Pro-only surfaces (craving SOS history, health timeline,
 * money projections, milestones). Free tier keeps the counter and pledge.
 * When locked, shows an upsell card with a Go Pro button.
 */
@Composable
fun ProGate(
    onShowPaywall: () -> Unit,
    viewModel: BillingGateViewModel = hiltViewModel(),
    lockedTitle: String = "Go Pro to unlock",
    lockedBody: String = "This feature is part of Pro. Start your 3-day free trial.",
    content: @Composable () -> Unit,
) {
    val state by viewModel.entitlement.collectAsStateWithLifecycle()
    val isPremium =
        when (val s = state) {
            is EntitlementState.Ready -> s.isPremium
            // While loading, hold the previous frame: never flash locked UI
            // at a paying user, and never flash paid UI at a free user.
            else -> null
        }
    when (isPremium) {
        true -> content()
        false -> ProLockedCard(
            title = lockedTitle,
            body = lockedBody,
            onShowPaywall = onShowPaywall,
        )
        null -> BillingLoadingSplash()
    }
}

@Composable
private fun BillingLoadingSplash() {
    val colors = LocalAppColors.current
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = colors.groupedBackground,
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center,
        ) {
            CircularProgressIndicator(color = colors.accent)
        }
    }
}

@Composable
private fun ProLockedCard(
    title: String,
    body: String,
    onShowPaywall: () -> Unit,
) {
    val colors = LocalAppColors.current
    Box(
        modifier =
            Modifier
                .fillMaxSize()
                .padding(AppDimens.screenMargin),
        contentAlignment = Alignment.Center,
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = title,
                style = AppTypography.title2,
                color = colors.label,
                textAlign = TextAlign.Center,
            )
            Spacer(Modifier.height(AppDimens.spacing8))
            Text(
                text = body,
                style = AppTypography.body,
                color = colors.secondaryLabel,
                textAlign = TextAlign.Center,
            )
            Spacer(Modifier.height(AppDimens.spacing16))
            Button(onClick = onShowPaywall) {
                Text("Go Pro")
            }
        }
    }
}
