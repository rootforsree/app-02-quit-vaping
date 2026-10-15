package com.sreecharan.quitvaping

import android.app.Activity
import app.cash.turbine.test
import com.sreecharan.quitvaping.data.billing.BillingPlan
import com.sreecharan.quitvaping.data.billing.BillingRepository
import com.sreecharan.quitvaping.data.billing.EntitlementState
import com.sreecharan.quitvaping.data.billing.PurchaseResult
import com.sreecharan.quitvaping.ui.paywall.PaywallEvent
import com.sreecharan.quitvaping.ui.paywall.PaywallViewModel
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

private class FakeBillingRepository : BillingRepository {
    val entitlementState = MutableStateFlow<EntitlementState>(EntitlementState.Loading)
    override val entitlement: StateFlow<EntitlementState> = entitlementState

    val plansState = MutableStateFlow<List<BillingPlan>?>(null)
    override val plans: StateFlow<List<BillingPlan>?> = plansState

    var purchaseResult: PurchaseResult = PurchaseResult.Cancelled
    var restoreResult: Boolean = false
    var refreshCalls = 0

    override suspend fun refresh() {
        refreshCalls++
    }

    override suspend fun purchase(activity: Activity, plan: BillingPlan): PurchaseResult =
        purchaseResult

    override suspend fun restore(): Boolean = restoreResult
}

private fun monthlyPlan() =
    BillingPlan(
        packageId = "\$rc_monthly",
        title = "Monthly",
        price = "$4.99",
        pricePerPeriod = "$4.99/mo",
        isAnnual = false,
        isBestValue = false,
    )

private fun annualPlan() =
    BillingPlan(
        packageId = "\$rc_annual",
        title = "Yearly",
        price = "$29.99",
        pricePerPeriod = "$29.99/yr",
        isAnnual = true,
        isBestValue = true,
    )

@OptIn(ExperimentalCoroutinesApi::class)
class PaywallViewModelTest {
    private val testDispatcher = StandardTestDispatcher()
    private lateinit var billing: FakeBillingRepository
    private lateinit var viewModel: PaywallViewModel

    @BeforeEach
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        billing = FakeBillingRepository()
    }

    @AfterEach
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `annual plan is preselected when plans arrive`() =
        runTest(testDispatcher) {
            viewModel = PaywallViewModel(billing)
            billing.entitlementState.value =
                EntitlementState.Ready(isPremium = false, status = null)
            billing.plansState.value = listOf(monthlyPlan(), annualPlan())
            testDispatcher.scheduler.advanceUntilIdle()

            val state = viewModel.uiState.value
            assertEquals("\$rc_annual", state.selectedPlanId)
            assertEquals(2, state.plans!!.size)
        }

    @Test
    fun `dismiss event fires when entitlement becomes premium`() =
        runTest(testDispatcher) {
            viewModel = PaywallViewModel(billing)
            billing.entitlementState.value =
                EntitlementState.Ready(isPremium = false, status = null)
            testDispatcher.scheduler.advanceUntilIdle()

            viewModel.events.test {
                billing.entitlementState.value =
                    EntitlementState.Ready(isPremium = true, status = null)
                testDispatcher.scheduler.advanceUntilIdle()
                assertEquals(PaywallEvent.Dismiss, awaitItem())
                cancelAndIgnoreRemainingEvents()
            }
        }

    @Test
    fun `user-cancelled purchase keeps paywall open without error`() =
        runTest(testDispatcher) {
            billing.purchaseResult = PurchaseResult.Cancelled
            viewModel = PaywallViewModel(billing)
            billing.plansState.value = listOf(monthlyPlan())
            billing.entitlementState.value =
                EntitlementState.Ready(isPremium = false, status = null)
            testDispatcher.scheduler.advanceUntilIdle()
            viewModel.onPlanSelected("\$rc_monthly")

            viewModel.onPurchaseClicked(mockk(relaxed = true))
            testDispatcher.scheduler.advanceUntilIdle()

            val state = viewModel.uiState.value
            assertFalse(state.isPurchasing)
            assertNull(state.errorMessage)
        }

    @Test
    fun `failed purchase surfaces error message`() =
        runTest(testDispatcher) {
            billing.purchaseResult = PurchaseResult.Failed("Network error.")
            viewModel = PaywallViewModel(billing)
            billing.plansState.value = listOf(monthlyPlan())
            billing.entitlementState.value =
                EntitlementState.Ready(isPremium = false, status = null)
            testDispatcher.scheduler.advanceUntilIdle()
            viewModel.onPlanSelected("\$rc_monthly")

            viewModel.onPurchaseClicked(mockk(relaxed = true))
            testDispatcher.scheduler.advanceUntilIdle()

            val state = viewModel.uiState.value
            assertFalse(state.isPurchasing)
            assertEquals("Network error.", state.errorMessage)
        }

    @Test
    fun `restore with no subscription shows message`() =
        runTest(testDispatcher) {
            billing.restoreResult = false
            viewModel = PaywallViewModel(billing)
            testDispatcher.scheduler.advanceUntilIdle()

            viewModel.onRestoreClicked()
            testDispatcher.scheduler.advanceUntilIdle()

            val state = viewModel.uiState.value
            assertFalse(state.isRestoring)
            assertEquals(
                "No active subscription found on this account.",
                state.restoreMessage,
            )
        }

    @Test
    fun `unavailable billing flags state`() =
        runTest(testDispatcher) {
            viewModel = PaywallViewModel(billing)
            billing.entitlementState.value = EntitlementState.Unavailable
            testDispatcher.scheduler.advanceUntilIdle()

            assertTrue(viewModel.uiState.value.billingUnavailable)
        }

    @Test
    fun `plan selection clears previous error`() =
        runTest(testDispatcher) {
            viewModel = PaywallViewModel(billing)
            billing.plansState.value = listOf(monthlyPlan(), annualPlan())
            testDispatcher.scheduler.advanceUntilIdle()

            viewModel.onPlanSelected("\$rc_monthly")
            val state = viewModel.uiState.value
            assertEquals("\$rc_monthly", state.selectedPlanId)
            assertNull(state.errorMessage)
        }
}
