package com.sreecharan.quitvaping.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.automirrored.outlined.TrendingUp
import androidx.compose.material.icons.filled.Air
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Today
import androidx.compose.material.icons.outlined.Air
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.Today
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import com.sreecharan.quitvaping.R
import com.sreecharan.quitvaping.data.datastore.OnboardingRepository
import com.sreecharan.quitvaping.ui.components.GlassTabBar
import com.sreecharan.quitvaping.ui.components.TabItem
import com.sreecharan.quitvaping.ui.theme.AppDimens
import kotlinx.collections.immutable.persistentListOf
import kotlinx.serialization.Serializable

// Route objects deliberately share names with their screen composables
// (TodayRoute, ProgressRoute, CravingsRoute, SettingsRoute, OnboardingRoute,
// PaywallRoute). The composables are called with fully qualified names below
// so the declarations never clash with the imports.

@Serializable
object TodayRoute

@Serializable
object ProgressRoute

@Serializable
object CravingsRoute

@Serializable
object SettingsRoute

@Serializable
object OnboardingRoute

@Serializable
data class PaywallRoute(val fromSettings: Boolean = false)

/**
 * One activity, one root NavHost, type-safe routes. The glass tab bar floats
 * over the NavHost content; per-tab state survives tab switches through
 * saveState/restoreState, and screens reserve bottom clearance for the bar.
 *
 * Onboarding gates the whole app: until [OnboardingRepository] reports
 * completion, the graph starts at [OnboardingRoute]. After the last onboarding
 * page the user lands on [PaywallRoute] (hard paywall, RevenueCat-backed),
 * then the main tab graph. Settings can also open the paywall with
 * fromSettings = true, and its dismiss pops straight back to Settings.
 */
@Composable
@Suppress("LongMethod")
fun AppNavHost(
    onboardingRepository: OnboardingRepository,
    modifier: Modifier = Modifier,
) {
    val navController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val destination = backStackEntry?.destination
    val tabs = rememberTabs()
    val selectedIndex =
        when {
            destination?.hasRoute(TodayRoute::class) == true -> 0
            destination?.hasRoute(ProgressRoute::class) == true -> 1
            destination?.hasRoute(CravingsRoute::class) == true -> 2
            destination?.hasRoute(SettingsRoute::class) == true -> 3
            else -> 0
        }
    val onboardingCompleted by onboardingRepository.isOnboardingCompleted
        .collectAsStateWithLifecycle(initialValue = null)
    val startDestination: Any = if (onboardingCompleted == false) OnboardingRoute else TodayRoute
    val showTabBar =
        onboardingCompleted == true &&
        destination?.let {
            it.hasRoute(TodayRoute::class) ||
                it.hasRoute(ProgressRoute::class) ||
                it.hasRoute(CravingsRoute::class) ||
                it.hasRoute(SettingsRoute::class)
        } == true

    // Wait for the DataStore read before choosing the graph.
    if (onboardingCompleted == null) {
        Box(modifier = modifier.fillMaxSize())
        return
    }

    Box(modifier = modifier.fillMaxSize()) {
        val switchTab: (Int) -> Unit = { index ->
            val route: Any =
                when (index) {
                    0 -> TodayRoute
                    1 -> ProgressRoute
                    2 -> CravingsRoute
                    else -> SettingsRoute
                }
            navController.navigate(route) {
                popUpTo(navController.graph.findStartDestination().id) {
                    saveState = true
                }
                launchSingleTop = true
                restoreState = true
            }
        }
        NavHost(
            navController = navController,
            startDestination = startDestination,
            modifier = Modifier.fillMaxSize(),
        ) {
            composable<OnboardingRoute> {
                com.sreecharan.quitvaping.ui.onboarding.OnboardingRoute(
                    onFinishOnboarding = {
                        navController.navigate(PaywallRoute(fromSettings = false)) {
                            popUpTo(OnboardingRoute) { inclusive = true }
                        }
                    },
                )
            }
            composable<PaywallRoute> { entry ->
                val args = entry.toRoute<PaywallRoute>()
                com.sreecharan.quitvaping.ui.paywall.PaywallRoute(
                    onDismiss = {
                        if (args.fromSettings) {
                            navController.popBackStack()
                        } else {
                            navController.navigate(TodayRoute) {
                                popUpTo(PaywallRoute(fromSettings = false)) { inclusive = true }
                            }
                        }
                    },
                )
            }
            composable<TodayRoute> {
                com.sreecharan.quitvaping.ui.today.TodayRoute(
                    onOpenCravings = { switchTab(CRAVINGS_TAB_INDEX) },
                    onOpenOnboarding = { navController.navigate(OnboardingRoute) },
                )
            }
            composable<ProgressRoute> {
                com.sreecharan.quitvaping.ui.progress.ProgressRoute()
            }
            composable<CravingsRoute> {
                com.sreecharan.quitvaping.ui.cravings.CravingsRoute()
            }
            composable<SettingsRoute> {
                com.sreecharan.quitvaping.ui.settings.SettingsRoute(
                    onOpenPaywall = {
                        navController.navigate(PaywallRoute(fromSettings = true))
                    },
                    onResetComplete = {
                        navController.navigate(OnboardingRoute) {
                            popUpTo(TodayRoute) { inclusive = true }
                        }
                    },
                )
            }
        }
        if (showTabBar) {
            GlassTabBar(
                tabs = tabs,
                selectedIndex = selectedIndex,
                onTabSelected = switchTab,
                modifier =
                    Modifier
                        .align(Alignment.BottomCenter)
                        .windowInsetsPadding(WindowInsets.navigationBars)
                        .padding(
                            start = AppDimens.screenMargin,
                            end = AppDimens.screenMargin,
                            bottom = AppDimens.screenMargin,
                        ),
            )
        }
    }
}

@Composable
private fun rememberTabs() =
    persistentListOf(
        TabItem(
            label = stringResource(R.string.tab_today),
            selectedIcon = Icons.Filled.Today,
            unselectedIcon = Icons.Outlined.Today,
        ),
        TabItem(
            label = stringResource(R.string.tab_progress),
            selectedIcon = Icons.AutoMirrored.Filled.TrendingUp,
            unselectedIcon = Icons.AutoMirrored.Outlined.TrendingUp,
        ),
        TabItem(
            label = stringResource(R.string.tab_cravings),
            selectedIcon = Icons.Filled.Air,
            unselectedIcon = Icons.Outlined.Air,
        ),
        TabItem(
            label = stringResource(R.string.tab_settings),
            selectedIcon = Icons.Filled.Settings,
            unselectedIcon = Icons.Outlined.Settings,
        ),
    )

private const val CRAVINGS_TAB_INDEX = 2
