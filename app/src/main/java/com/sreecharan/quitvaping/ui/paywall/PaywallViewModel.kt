package com.sreecharan.quitvaping.ui.paywall

import android.app.Activity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sreecharan.quitvaping.data.billing.BillingPlan
import com.sreecharan.quitvaping.data.billing.BillingRepository
import com.sreecharan.quitvaping.data.billing.EntitlementState
import com.sreecharan.quitvaping.data.billing.PurchaseResult
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class PaywallUiState(
    /** Null while offerings load. */
    val plans: List<BillingPlan>? = null,
    val selectedPlanId: String? = null,
    val isPurchasing: Boolean = false,
    val errorMessage: String? = null,
    val isRestoring: Boolean = false,
    val restoreMessage: String? = null,
    val billingUnavailable: Boolean = false,
)

sealed interface PaywallEvent {
    /** The user is premium now (purchase or restore): close the paywall. */
    data object Dismiss : PaywallEvent
}

/**
 * Hard paywall shown after onboarding and from settings. Free tier keeps the
 * counter and pledge; everything else requires the premium entitlement.
 */
@HiltViewModel
class PaywallViewModel
    @Inject
    constructor(
        private val billing: BillingRepository,
    ) : ViewModel() {
        private val _uiState = MutableStateFlow(PaywallUiState())
        val uiState: StateFlow<PaywallUiState> = _uiState.asStateFlow()

        private val _events = Channel<PaywallEvent>(Channel.BUFFERED)
        val events = _events.receiveAsFlow()

        init {
            viewModelScope.launch {
                billing.entitlement.collect { state ->
                    when (state) {
                        is EntitlementState.Ready -> {
                            if (state.isPremium) {
                                _events.send(PaywallEvent.Dismiss)
                            }
                        }
                        EntitlementState.Unavailable -> {
                            _uiState.update { it.copy(billingUnavailable = true) }
                        }
                        EntitlementState.Loading -> Unit
                    }
                }
            }
            viewModelScope.launch {
                billing.plans.collect { plans ->
                    _uiState.update { state ->
                        val selected =
                            state.selectedPlanId
                                ?: plans
                                    ?.firstOrNull { it.isAnnual }
                                    ?.packageId
                                ?: plans?.firstOrNull()?.packageId
                        state.copy(plans = plans, selectedPlanId = selected)
                    }
                }
            }
            viewModelScope.launch { billing.refresh() }
        }

        fun onPlanSelected(planId: String) {
            _uiState.update {
                it.copy(selectedPlanId = planId, errorMessage = null)
            }
        }

        fun onPurchaseClicked(activity: Activity) {
            val plan =
                _uiState.value.plans?.firstOrNull {
                    it.packageId == _uiState.value.selectedPlanId
                } ?: return
            if (_uiState.value.isPurchasing) return
            _uiState.update { it.copy(isPurchasing = true, errorMessage = null) }
            viewModelScope.launch {
                when (val result = billing.purchase(activity, plan)) {
                    is PurchaseResult.Success -> {
                        // Entitlement listener also fires; Dismiss is idempotent.
                        _events.send(PaywallEvent.Dismiss)
                    }
                    PurchaseResult.Cancelled -> {
                        // Normal: user dismissed the Play sheet. Stay on paywall.
                        _uiState.update { it.copy(isPurchasing = false) }
                    }
                    is PurchaseResult.Failed -> {
                        _uiState.update {
                            it.copy(
                                isPurchasing = false,
                                errorMessage = result.message,
                            )
                        }
                    }
                }
            }
        }

        fun onRestoreClicked() {
            if (_uiState.value.isRestoring) return
            _uiState.update { it.copy(isRestoring = true, restoreMessage = null) }
            viewModelScope.launch {
                val found = billing.restore()
                _uiState.update {
                    it.copy(
                        isRestoring = false,
                        restoreMessage =
                            if (found) {
                                null // Dismiss fires via entitlement listener.
                            } else {
                                "No active subscription found on this account."
                            },
                    )
                }
            }
        }

        fun onRetryPlans() {
            viewModelScope.launch { billing.refresh() }
        }

        fun onErrorDismissed() {
            _uiState.update { it.copy(errorMessage = null, restoreMessage = null) }
        }
    }
