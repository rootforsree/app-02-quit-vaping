package com.sreecharan.quitvaping.ui.paywall

import android.app.Activity
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.sreecharan.quitvaping.data.billing.BillingPlan
import com.sreecharan.quitvaping.ui.theme.AppDimens
import com.sreecharan.quitvaping.ui.theme.AppShapes
import com.sreecharan.quitvaping.ui.theme.AppTypography
import com.sreecharan.quitvaping.ui.theme.LocalAppColors
import kotlinx.collections.immutable.persistentListOf

/**
 * Thin stateful route: owns the ViewModel, forwards the dismiss event.
 * Shown as a full-screen modal after onboarding and from settings.
 */
@Composable
fun PaywallRoute(
    onDismiss: () -> Unit,
    viewModel: PaywallViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                PaywallEvent.Dismiss -> onDismiss()
            }
        }
    }

    PaywallScreenContent(
        state = state,
        onPlanSelected = viewModel::onPlanSelected,
        onPurchase = viewModel::onPurchaseClicked,
        onRestore = viewModel::onRestoreClicked,
        onDismissFree = onDismiss,
        onRetryPlans = viewModel::onRetryPlans,
        onErrorDismissed = viewModel::onErrorDismissed,
    )
}

@Composable
fun PaywallScreenContent(
    state: PaywallUiState,
    onPlanSelected: (String) -> Unit,
    onPurchase: (Activity) -> Unit,
    onRestore: () -> Unit,
    onDismissFree: () -> Unit,
    onRetryPlans: () -> Unit,
    onErrorDismissed: () -> Unit,
) {
    val colors = LocalAppColors.current
    val context = LocalContext.current
    val activity = context as? Activity

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = colors.groupedBackground,
    ) {
        when {
            state.billingUnavailable -> {
                PaywallMessage(
                    title = "Billing unavailable",
                    body =
                        "Purchases are not configured in this build. " +
                            "The free version keeps working.",
                    actionLabel = "Continue",
                    onAction = onDismissFree,
                )
            }
            state.plans == null -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center,
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        CircularProgressIndicator(color = colors.accent)
                        Spacer(Modifier.height(AppDimens.spacing16))
                        Text(
                            "Loading plans…",
                            style = AppTypography.body,
                            color = colors.secondaryLabel,
                        )
                    }
                }
            }
            state.plans.isEmpty() -> {
                PaywallMessage(
                    title = "Plans unavailable",
                    body =
                        "We couldn't load subscription plans. " +
                            "Check your connection and try again.",
                    actionLabel = "Retry",
                    onAction = onRetryPlans,
                )
            }
            else -> {
                Column(
                    modifier =
                        Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                            .padding(AppDimens.screenMargin),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Spacer(Modifier.height(AppDimens.spacing32))
                    Text(
                        text = "Quit vaping for good.",
                        style = AppTypography.largeTitle,
                        color = colors.label,
                        textAlign = TextAlign.Center,
                    )
                    Spacer(Modifier.height(AppDimens.spacing8))
                    Text(
                        text =
                            "Your lungs start healing in 20 minutes. " +
                                "Go Pro and get every tool that keeps you quit.",
                        style = AppTypography.body,
                        color = colors.secondaryLabel,
                        textAlign = TextAlign.Center,
                    )
                    Spacer(Modifier.height(AppDimens.spacing24))

                    PaywallBenefits()

                    Spacer(Modifier.height(AppDimens.spacing24))
                    state.plans.forEach { plan ->
                        PlanCard(
                            plan = plan,
                            selected = plan.packageId == state.selectedPlanId,
                            onSelect = { onPlanSelected(plan.packageId) },
                        )
                        Spacer(Modifier.height(AppDimens.spacing12))
                    }

                    Text(
                        text = "Start with a 3-day free trial. Cancel anytime.",
                        style = MaterialTheme.typography.bodySmall,
                        color = colors.secondaryLabel,
                        textAlign = TextAlign.Center,
                    )
                    Spacer(Modifier.height(AppDimens.spacing16))

                    Button(
                        onClick = { activity?.let(onPurchase) },
                        enabled = !state.isPurchasing && activity != null,
                        modifier =
                            Modifier
                                .fillMaxWidth()
                                .height(52.dp),
                        colors =
                            ButtonDefaults.buttonColors(
                                containerColor = colors.accent,
                            ),
                    ) {
                        if (state.isPurchasing) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                color = colors.cardSurface,
                                strokeWidth = 2.dp,
                            )
                        } else {
                            Text(
                                "Start free trial",
                                style = AppTypography.headline,
                                fontWeight = FontWeight.Bold,
                            )
                        }
                    }

                    state.errorMessage?.let { message ->
                        Spacer(Modifier.height(AppDimens.spacing12))
                        Text(
                            text = message,
                            style = MaterialTheme.typography.bodySmall,
                            color = colors.destructive,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.clickable { onErrorDismissed() },
                        )
                    }

                    Spacer(Modifier.height(AppDimens.spacing8))
                    TextButton(onClick = onRestore) {
                        if (state.isRestoring) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(16.dp),
                                strokeWidth = 2.dp,
                                color = colors.accent,
                            )
                            Spacer(Modifier.width(AppDimens.spacing8))
                        }
                        Text("Restore purchases", color = colors.accent)
                    }
                    state.restoreMessage?.let { message ->
                        Text(
                            text = message,
                            style = MaterialTheme.typography.bodySmall,
                            color = colors.secondaryLabel,
                            textAlign = TextAlign.Center,
                        )
                    }

                    Spacer(Modifier.height(AppDimens.spacing8))
                    TextButton(onClick = onDismissFree) {
                        Text(
                            "Continue with free version",
                            color = colors.secondaryLabel,
                        )
                    }
                    Spacer(Modifier.height(AppDimens.spacing24))
                }
            }
        }
    }
}

private val paywallBenefits =
    persistentListOf(
        "Unlimited craving SOS with breathing coach",
        "Full health recovery timeline",
        "Money saved tracker and projections",
        "Milestone badges and celebrations",
        "Daily pledge streaks",
    )

@Composable
private fun PaywallBenefits() {
    val colors = LocalAppColors.current
    Surface(
        shape = AppShapes.card,
        color = colors.cardSurface,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(modifier = Modifier.padding(AppDimens.spacing16)) {
            paywallBenefits.forEachIndexed { index, benefit ->
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier =
                            Modifier
                                .size(24.dp)
                                .clip(CircleShape)
                                .background(colors.success.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(
                            Icons.Filled.Check,
                            contentDescription = null,
                            tint = colors.success,
                            modifier = Modifier.size(16.dp),
                        )
                    }
                    Spacer(Modifier.width(AppDimens.spacing12))
                    Text(
                        text = benefit,
                        style = AppTypography.body,
                        color = colors.label,
                    )
                }
                if (index < paywallBenefits.lastIndex) {
                    Spacer(Modifier.height(AppDimens.spacing12))
                }
            }
        }
    }
}

@Composable
private fun PlanCard(
    plan: BillingPlan,
    selected: Boolean,
    onSelect: () -> Unit,
) {
    val colors = LocalAppColors.current
    Surface(
        shape = AppShapes.card,
        color = colors.cardSurface,
        border =
            if (selected) {
                BorderStroke(2.dp, colors.accent)
            } else {
                BorderStroke(1.dp, colors.secondaryLabel.copy(alpha = 0.3f))
            },
        modifier =
            Modifier
                .fillMaxWidth()
                .selectable(
                    selected = selected,
                    role = Role.RadioButton,
                    onClick = onSelect,
                ),
    ) {
        Row(
            modifier = Modifier.padding(AppDimens.spacing16),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            RadioButton(
                selected = selected,
                onClick = null,
            )
            Spacer(Modifier.width(AppDimens.spacing8))
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = if (plan.isAnnual) "Yearly" else "Monthly",
                        style = AppTypography.headline,
                        color = colors.label,
                    )
                    if (plan.isBestValue) {
                        Spacer(Modifier.width(AppDimens.spacing8))
                        Surface(
                            shape = AppShapes.capsule,
                            color = colors.accent,
                        ) {
                            Text(
                                text = "BEST VALUE",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = colors.cardSurface,
                                modifier =
                                    Modifier.padding(
                                        horizontal = 8.dp,
                                        vertical = 4.dp,
                                    ),
                            )
                        }
                    }
                }
                Text(
                    text = plan.pricePerPeriod,
                    style = AppTypography.body,
                    color = colors.secondaryLabel,
                )
            }
            Text(
                text = plan.price,
                style = AppTypography.headline,
                fontWeight = FontWeight.Bold,
                color = colors.label,
            )
        }
    }
}

@Composable
private fun PaywallMessage(
    title: String,
    body: String,
    actionLabel: String,
    onAction: () -> Unit,
) {
    val colors = LocalAppColors.current
    Column(
        modifier =
            Modifier
                .fillMaxSize()
                .padding(AppDimens.screenMargin),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
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
        Spacer(Modifier.height(AppDimens.spacing24))
        Button(onClick = onAction) {
            Text(actionLabel)
        }
    }
}
