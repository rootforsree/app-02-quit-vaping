# Onboarding Flow — Integration Contract

Built by the onboarding agent. Other agents (home/counter, payments, notifications,
data layer) read this before integrating.

## What was built

7-screen onboarding flow for the quit-vaping tracker, Apple HIG on Android,
<90 seconds, no account creation.

**Files:**
- `ui/onboarding/OnboardingScreen.kt` — pager container (progress dots, Back/Next/Skip)
- `ui/onboarding/OnboardingPages.kt` — the 7 pages
- `ui/onboarding/OnboardingViewModel.kt` — Hilt ViewModel, all screen state
- `data/datastore/OnboardingRepository.kt` — repository interface + Preferences DataStore impl
- `ui/paywall/PaywallStubScreen.kt` — STUB for payments agent to replace
- `navigation/AppNavHost.kt` — updated: OnboardingRoute + PaywallRoute, onboarding gates app start
- `di/DataStoreModule.kt` — added `@Named("onboarding")` Preferences DataStore
- `di/RepositoryModule.kt` — binds OnboardingRepository
- `MainActivity.kt` — injects OnboardingRepository, passes to AppNavHost

## The 7 screens

1. **Welcome** — hero, value props, "Get Started"
2. **Quit date** — DatePicker dialog (default today) + "I haven't quit yet" plan mode
3. **Device + spend** — device chips (Disposable/Pod/Refillable), units/week stepper,
   cost/unit text field, **LIVE yearly savings** updating on every input
4. **Puffs + nicotine** — puffs/day chips (<50/50-100/100-200/200+/Not sure),
   nicotine mg/mL slider (0-60)
5. **Triggers** — multi-select chips (Stress/Boredom/Social/After meals/Alcohol/Habit/Other)
6. **Notifications** — pledge time picker (default 8:00 AM), milestone toggle,
   POST_NOTIFICATIONS permission request (API 33+)
7. **Ready** — summary (savings, device, pledge time) → "Continue" → paywall

## Repository interface (for other agents)

```kotlin
// Inject this anywhere with Hilt:
@Inject lateinit var onboardingRepository: OnboardingRepository

// Read all onboarding data (quit date, spend, etc.):
onboardingRepository.onboardingData: Flow<OnboardingData>

// Check if onboarding finished:
onboardingRepository.isOnboardingCompleted: Flow<Boolean>

data class OnboardingData(
    val quitDateEpochMs: Long,      // day counter starts here
    val deviceType: DeviceType,     // DISPOSABLE | POD | REFILLABLE
    val unitsPerWeek: Int,
    val costPerUnitCents: Int,      // money-saved = units/week * cost / 7 * days
    val puffsPerDay: Int,           // -1 = "not sure"
    val nicotineMgPerMl: Float,     // 0f = "not sure"
    val triggers: Set<String>,      // e.g. {"Stress", "Boredom"}
    val pledgeHour: Int,            // daily pledge reminder time
    val pledgeMinute: Int,
    val milestoneCelebrations: Boolean,
    val onboardingCompleted: Boolean,
)
```

**Home/counter agent:** inject `OnboardingRepository`, collect `onboardingData` for
quit date + weekly spend. `yearlySavingsDollars` is computed on the data class.

**Notifications agent:** read `pledgeHour`/`pledgeMinute`/`milestoneCelebrations`
from `onboardingData` to schedule alarms.

**Data layer agent:** this uses Preferences DataStore (no Room changes, no
conflicts). If you migrate to Room later, keep the `OnboardingRepository`
interface stable.

## Navigation

- `OnboardingRoute` (object, @Serializable) → `OnboardingRoute()` composable
- `PaywallRoute` (object, @Serializable) → `PaywallStubRoute()` composable
- App starts at `OnboardingRoute` until `isOnboardingCompleted` is true
- Onboarding completion → `PaywallRoute` (back stack cleared)
- Paywall dismiss → `HomeRoute` (back stack cleared)
- Tab bar hidden during onboarding/paywall

## Payments agent

Replace `ui/paywall/PaywallStubScreen.kt` with the real paywall. Keep the
`onPaywallDismissed: () -> Unit` callback contract. Route name `PaywallRoute`
stays the same.

## Design tokens used

All from `ui/theme/Tokens.kt`: `AppTypography.largeTitle` (34sp),
`LocalAppColors.current` (accent #007AFF, groupedBackground, cardSurface),
`AppShapes.card` (12dp), `AppDimens` spacing. `GroupCard` for inset cards.
`rememberHapticTick()` on interactive elements.
