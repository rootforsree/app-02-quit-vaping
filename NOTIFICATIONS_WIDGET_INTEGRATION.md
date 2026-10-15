# Notifications + Widget + Backup — Integration Notes

Built by the notifications/widget subagent. All files compile-checked by review
(no Gradle run in this environment; CI will verify).

## What was built

**Data layer** (`data/reminders/`):
- `ReminderSettings.kt` — single data class: quit date, pledge time, high-risk
  times, evening reflection, weekly spend, fired money thresholds/milestones.
- `ReminderSettingsRepository.kt` — interface + Preferences DataStore impl.
  File name: `quit_prefs` (matches backup allow-list).

**Notifications** (`notifications/`):
- `QuitDate.kt` — day-count math (quit day = Day 1), money-saved math, formatting.
- `Milestones.kt` — 12 CDC/VA/ACS health milestones (20min → 10yr).
- `QuitNotifications.kt` — 2 channels (reminders / celebrations), 6 notification
  types. Tap opens MainActivity with `EXTRA_DESTINATION` (`pledge`, `progress`,
  `cravings`, `home`) — nav agent maps these to routes.
- `ReminderScheduler.kt` — AlarmManager one-shot exact alarms. `scheduleAll()`
  after onboarding/settings/boot/time changes. `canScheduleExact()` for the
  permission UI.
- `ReminderReceiver.kt` — fires notifications, re-arms next alarm, sweeps missed
  milestones + money thresholds ($50/$100/$500), refreshes widget.
- `BootReceiver.kt` — re-schedules on BOOT_COMPLETED / TIME_SET /
  TIMEZONE_CHANGED / DATE_CHANGED / LOCALE_CHANGED.
- `NotificationPermission.kt` — POST_NOTIFICATIONS check, exact-alarm settings
  intent, app notification settings intent.

**Widget** (`widget/`):
- `QuitWidget.kt` — Glance day-counter (days + money saved), tap opens app.
- `QuitWidgetReceiver.kt` — manifest-declared host entry.
- `WidgetUpdater.kt` — caches day count in `quit_widget` DataStore, triggers
  Glance update. Called from receivers; app foreground should also call
  `widgetUpdater.refresh()`.

**Backup** (`backup/` + `res/xml/`):
- `BackupManager.kt` — JSON export to Downloads via MediaStore, import from Uri.
  Never paywalled.
- `data_extraction_rules.xml` (API 31+) + `backup_rules.xml` (API 23–30):
  database + `quit_prefs.xml` allow-listed.

**DI** (`di/ReminderModule.kt`): provides `DataStore<Preferences>` (quit_prefs)
and `ReminderSettingsRepository`. No clash with the existing
`DataStore<SettingsProto>` (different type).

**Manifest**: POST_NOTIFICATIONS, SCHEDULE_EXACT_ALARM, RECEIVE_BOOT_COMPLETED
permissions; 3 receivers; dataExtractionRules + fullBackupContent.

**Catalog**: added `glance = "1.2.0"`, `glance-appwidget`, `glance-material3`.

**Tests**: `QuitDateTest.kt` — pure-JVM JUnit5 tests for day math, money,
milestones, trigger computation.

## Integration points for other agents

1. **Onboarding agent**: write quit date + spend via
   `ReminderSettingsRepository.setQuitDate()` / `setWeeklySpendCents()` /
   `setPledge()` / `setHighRiskTimes()`, then call
   `ReminderScheduler.scheduleAll()`.
2. **Settings agent**: use `NotificationPermission` for the permission rows;
   call `scheduler.scheduleAll()` after any reminder pref change.
3. **Lapse logging** (cravings agent): call
   `QuitNotifications.postLapseCompassion(days)` after a slip is logged.
4. **MainActivity / nav**: read `EXTRA_DESTINATION` from the launch intent to
   deep-link from notification taps.
5. **App foreground**: call `widgetUpdater.refresh()` in `onResume` (or a
   lifecycle observer) so the widget never goes stale.
6. **Paywall agent**: `BackupManager` is injectable anywhere; expose Export/
   Import in Settings, free tier.
7. **Data-layer agent**: if you create a Room `QuitProfile`, keep
   `quit_prefs` as the source of truth for the scheduler OR sync quit date
   into `ReminderSettingsRepository.setQuitDate()` whenever it changes.
