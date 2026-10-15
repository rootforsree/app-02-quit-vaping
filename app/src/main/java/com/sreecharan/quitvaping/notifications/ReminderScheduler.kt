package com.sreecharan.quitvaping.notifications

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import com.sreecharan.quitvaping.data.reminders.ReminderSettings
import com.sreecharan.quitvaping.data.reminders.ReminderSettingsRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.first
import java.time.LocalDateTime
import java.time.ZoneId
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Owns all AlarmManager scheduling. One-shot exact alarms, re-scheduled
 * inside [ReminderReceiver] after each fire. Never setRepeating (inexact
 * since API 19) and never WorkManager (15-min minimum, Doze-batched).
 *
 * Call [scheduleAll] after: onboarding completes, settings change, boot,
 * time/timezone/locale change.
 */
@Singleton
class ReminderScheduler
    @Inject
    constructor(
        @ApplicationContext private val context: Context,
        private val settingsRepository: ReminderSettingsRepository,
    ) {
        companion object {
            const val ACTION_PLEDGE = "com.sreecharan.quitvaping.action.PLEDGE"
            const val ACTION_CHECKIN = "com.sreecharan.quitvaping.action.CHECKIN"
            const val ACTION_EVENING = "com.sreecharan.quitvaping.action.EVENING"
            const val ACTION_MILESTONE = "com.sreecharan.quitvaping.action.MILESTONE"
            const val EXTRA_MILESTONE_ID = "milestone_id"
            const val EXTRA_CHECKIN_INDEX = "checkin_index"

            private const val REQ_PLEDGE = 5001
            private const val REQ_EVENING = 5002
            private const val REQ_CHECKIN_BASE = 5100
            private const val REQ_MILESTONE_BASE = 5200

            /** Next wall-clock occurrence of (hour, minute), strictly in the future. */
            fun nextDailyTrigger(hour: Int, minute: Int, nowMillis: Long = System.currentTimeMillis()): Long {
                val zone = ZoneId.systemDefault()
                var next =
                    LocalDateTime
                        .ofInstant(java.time.Instant.ofEpochMilli(nowMillis), zone)
                        .withHour(hour)
                        .withMinute(minute)
                        .withSecond(0)
                        .withNano(0)
                if (next.atZone(zone).toInstant().toEpochMilli() <= nowMillis) {
                    next = next.plusDays(1)
                }
                return next.atZone(zone).toInstant().toEpochMilli()
            }
        }

        private val alarmManager: AlarmManager? =
            context.getSystemService(AlarmManager::class.java)

        /** True when exact alarms will actually fire at the requested time. */
        fun canScheduleExact(): Boolean {
            val am = alarmManager ?: return false
            return Build.VERSION.SDK_INT < Build.VERSION_CODES.S || am.canScheduleExactAlarms()
        }

        suspend fun scheduleAll() {
            cancelAll()
            val settings = settingsRepository.settings.first()
            val quitDate = settings.quitDateEpochMillis ?: return
            schedulePledge(settings)
            scheduleCheckins(settings)
            scheduleEvening(settings)
            scheduleMilestones(settings, quitDate)
        }

        fun cancelAll() {
            val am = alarmManager ?: return
            am.cancel(pendingIntent(ACTION_PLEDGE, REQ_PLEDGE))
            am.cancel(pendingIntent(ACTION_EVENING, REQ_EVENING))
            repeat(3) { i -> am.cancel(pendingIntent(ACTION_CHECKIN, REQ_CHECKIN_BASE + i)) }
            Milestones.ALL.forEachIndexed { i, _ ->
                am.cancel(pendingIntent(ACTION_MILESTONE, REQ_MILESTONE_BASE + i))
            }
        }

        /** Next milestone alarm only (used after one fires). */
        suspend fun scheduleNextMilestone() {
            val settings = settingsRepository.settings.first()
            val quitDate = settings.quitDateEpochMillis ?: return
            val am = alarmManager ?: return
            // Clear existing milestone alarms, then arm the nearest unfired one.
            Milestones.ALL.forEachIndexed { i, _ ->
                am.cancel(pendingIntent(ACTION_MILESTONE, REQ_MILESTONE_BASE + i))
            }
            scheduleMilestones(settings, quitDate)
        }

        private fun schedulePledge(settings: ReminderSettings) {
            if (!settings.pledgeEnabled) return
            val trigger = nextDailyTrigger(settings.pledgeHour, settings.pledgeMinute)
            scheduleExact(trigger, pendingIntent(ACTION_PLEDGE, REQ_PLEDGE))
        }

        private fun scheduleCheckins(settings: ReminderSettings) {
            settings.highRiskTimes.take(3).forEachIndexed { i, (hour, minute) ->
                val trigger = nextDailyTrigger(hour, minute)
                val intent =
                    Intent(context, ReminderReceiver::class.java).apply {
                        action = ACTION_CHECKIN
                        putExtra(EXTRA_CHECKIN_INDEX, i)
                    }
                val pi =
                    PendingIntent.getBroadcast(
                        context,
                        REQ_CHECKIN_BASE + i,
                        intent,
                        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
                    )
                scheduleExact(trigger, pi)
            }
        }

        private fun scheduleEvening(settings: ReminderSettings) {
            if (!settings.eveningReflectionEnabled) return
            val trigger = nextDailyTrigger(settings.eveningHour, settings.eveningMinute)
            scheduleExact(trigger, pendingIntent(ACTION_EVENING, REQ_EVENING))
        }

        private fun scheduleMilestones(settings: ReminderSettings, quitDate: Long) {
            val now = System.currentTimeMillis()
            Milestones.ALL.forEachIndexed { i, milestone ->
                if (milestone.id in settings.firedMilestones) return@forEachIndexed
                val triggerAt = quitDate + milestone.after.toMillis()
                if (triggerAt <= now) return@forEachIndexed // handled by missed-check
                val intent =
                    Intent(context, ReminderReceiver::class.java).apply {
                        action = ACTION_MILESTONE
                        putExtra(EXTRA_MILESTONE_ID, milestone.id)
                    }
                val pi =
                    PendingIntent.getBroadcast(
                        context,
                        REQ_MILESTONE_BASE + i,
                        intent,
                        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
                    )
                scheduleExact(triggerAt, pi)
            }
        }

        private fun pendingIntent(action: String, requestCode: Int): PendingIntent {
            val intent = Intent(context, ReminderReceiver::class.java).setAction(action)
            return PendingIntent.getBroadcast(
                context,
                requestCode,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
            )
        }

        /**
         * Exact when allowed; 10-minute window fallback when the user denied
         * SCHEDULE_EXACT_ALARM. Callers should surface the "may arrive late"
         * note when [canScheduleExact] is false.
         */
        private fun scheduleExact(triggerAtMillis: Long, pendingIntent: PendingIntent) {
            val am = alarmManager ?: return
            try {
                if (canScheduleExact()) {
                    am.setExactAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        triggerAtMillis,
                        pendingIntent,
                    )
                } else {
                    am.setWindow(
                        AlarmManager.RTC_WAKEUP,
                        triggerAtMillis,
                        10 * 60 * 1000L,
                        pendingIntent,
                    )
                }
            } catch (_: SecurityException) {
                // Exact alarm revoked; the inexact fallback needs the same permission
                // on API 31+, so there is nothing more to do here.
            }
        }

    }
