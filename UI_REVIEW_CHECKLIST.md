# UI/UX Review Checklist — Quit Vaping Tracker

Reviewer: UI/UX Reviewer (subagent)
Standard: Apple HIG on Android. "Best ever app." Be brutal.

## Foundation (already verified in template)

- [x] Accent color is iOS blue #007AFF (not Material purple, not brown)
- [x] Grouped bg #F2F2F7 light / #1C1C1E dark
- [x] Large title 34sp bold
- [x] Dynamic color OFF
- [x] Dark mode is designed, not inverted
- [x] GlassTabBar: frosted glass, capsule, 56dp, haptic tick on switch
- [x] InsetGroupedList: 16dp inset, 24dp section spacing
- [x] Card corners 12dp, sheet 28dp top radius

## Per-Builder Review Criteria

### Builder 2: Onboarding (7 screens)

**Flow:**
- [ ] 7 screens total, swipeable, skippable at any point
- [ ] Completes in <90 seconds
- [ ] No account creation, no email, no password
- [ ] One question per screen, big tap targets
- [ ] Progress dots at bottom
- [ ] Screen 3 shows live yearly savings calculation
- [ ] Every screen gives value back (never ask without giving)

**Screens:**
1. Welcome: "Quit vaping for good." CTA "Get Started"
2. Quit date: date picker, defaults today, future-date → countdown mode
3. Weekly spend: slider/quick-pick $10-$100+, live "$X/year" display
4. Puffs per day: quick-pick <50 to 200+, "Not sure" option
5. Triggers: multi-select chips (Stress, Boredom, Social, After meals, Alcohol, Habit, Other)
6. Reminder time: time picker (default 8AM), milestone toggle, notification permission
7. Paywall (see Builder 4 criteria)

**Red flags:**
- Generic Material onboarding (stepper, linear progress bar)
- Asking for email/account
- More than 90 seconds
- No live savings calc on spend screen
- Tiny tap targets

### Builder 3: Main Screens (Today, Progress, Cravings, Settings)

**Tab 1: Today**
- [ ] Large title "Today" (34sp, part of scroll content, NOT fixed toolbar)
- [ ] Hero day counter: number at 60+sp, "days vape-free" below
- [ ] Hero feels rewarding (subtle gradient or solid accent, NOT flat gray)
- [ ] Stats row: 3 cards (Money saved, Puffs avoided, Cravings beaten)
- [ ] Money saved is prominent (second most important stat)
- [ ] Daily pledge card with clear CTA
- [ ] Craving SOS button: big, always visible, "Having a craving?"
- [ ] Health milestone progress with progress bar to next milestone
- [ ] Visual hierarchy: counter > money > pledge > SOS > milestone

**Tab 2: Progress**
- [ ] Large title "Progress"
- [ ] 30-day chart (green clean days, red slip markers)
- [ ] Health recovery timeline: inset grouped list, WHO/CDC data
- [ ] Each milestone row: title, description, checkmark (done) / progress (current) / locked (upcoming)
- [ ] Money projection card: "At this rate: $X by [date]" with chart
- [ ] Milestone badge grid: earned in color, unearned grayscale, tap for detail

**Tab 3: Cravings**
- [ ] Large title "Cravings"
- [ ] "Log a craving" button with intensity slider (1-10) and trigger picker
- [ ] Pattern insight at top: "Most cravings hit at 3pm. Stress is #1 trigger."
- [ ] Craving history list
- [ ] "Cravings beaten" counter (micro-wins)
- [ ] Breathing exercise access

**Tab 4: Settings**
- [ ] Large title "Settings"
- [ ] Inset grouped list with groups: Quit Plan, Notifications, Appearance, Subscription, About
- [ ] Quit Plan: quit date, daily spend, puffs/day, device type (editable)
- [ ] Subscription row → opens paywall
- [ ] Restore Purchases row

**Craving SOS Modal (full-screen)**
- [ ] Big countdown ring (5 min), "This craving will pass."
- [ ] Calming colors (soft, NOT red alarm)
- [ ] Breathing animation synchronized to timer
- [ ] "I beat it" → celebratory animation, +1 to cravings beaten
- [ ] "I slipped" → non-judgmental, logs slip, kind message
- [ ] NO shame language anywhere

**Red flags:**
- Fixed top app bar (should be large title in scroll content)
- Generic Material cards with elevation shadows (should be iOS-style flat)
- Day counter not the hero (buried below other content)
- Red/error colors on craving screen (should be calming)
- Shame language on slip: "You failed", "Back to zero", streak reset punishment
- Missing "cravings beaten" counter

### Builder 4: Payments & Paywall

**Paywall screen:**
- [ ] Full-screen modal
- [ ] Hero illustration (NOT stock photo)
- [ ] Headline leads with outcome: "Quit for good" not "Unlock features"
- [ ] Feature list with checkmarks (5 items max)
- [ ] Yearly plan PRE-SELECTED with "Best Value · Save 50%" badge
- [ ] Monthly as secondary option
- [ ] CTA: "Start 3-Day Free Trial" (NOT "Buy Now" or "Subscribe")
- [ ] Fine print: "3 days free, then $29.99/year. Cancel anytime."
- [ ] Restore Purchases link
- [ ] NO fake countdown timers
- [ ] NO dark patterns

**Entitlement:**
- [ ] Cold start shows neutral loading (never flash free tier to payers)
- [ ] Free tier: day counter + pledge only
- [ ] Pro: craving tools, health timeline, projections, badges

**Red flags:**
- Freemium model (should be hard paywall)
- Monthly pre-selected (annual should be default)
- "Buy Now" CTA (should be trial-focused)
- Fake urgency timers
- Flashing free UI before entitlement check

### Builder 1: Database & Data Layer

**UI impact:**
- [ ] Day counter math is correct (the #1 trust feature)
- [ ] Counter survives reinstall (backup/export works)
- [ ] Money saved calculation matches onboarding inputs
- [ ] No loading jank on tab switches

### Builder 5: Notifications & Widget

**Notifications:**
- [ ] Morning pledge at user-chosen time
- [ ] Milestone celebrations (event-driven)
- [ ] Evening review nudge (9 PM)
- [ ] Money thresholds ($50, $100, $500)
- [ ] Lapse compassion (kind, never shaming)
- [ ] Max ~3/day, user-facing frequency control
- [ ] Neutral lock-screen wording (not stigmatizing)

**Widget:**
- [ ] Day counter widget (Glance)
- [ ] Updates reliably
- [ ] Matches app visual style

## Dark Mode

- [ ] All screens reviewed in dark mode
- [ ] Uses DarkAppColors (designed, not inverted)
- [ ] Hero counter readable
- [ ] Charts visible
- [ ] No white flashes

## Haptics

- [ ] Light tick on tab switch
- [ ] Medium on milestone achievement
- [ ] Heavy on "craving beaten"
- [ ] NOT overused (not on every tap)

## No-Shame Audit

Search all strings for:
- [ ] NO "you failed"
- [ ] NO "back to square one"
- [ ] NO streak reset as punishment
- [ ] Slip message: "One slip doesn't erase X clean days"
- [ ] Cumulative clean days always visible alongside streak

## Final Verdict Categories

- **SHIP IT:** Meets all criteria, Apple-HIG quality
- **NEEDS WORK:** Specific issues listed, must fix before merge
- **REJECT:** Looks like generic Android app, start over
