package com.sreecharan.quitvaping.notifications

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.sreecharan.quitvaping.data.reminders.ReminderSettings
import com.sreecharan.quitvaping.data.reminders.ReminderSettingsRepository
import com.sreecharan.quitvaping.widget.WidgetUpdater
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Fires on every scheduled alarm. Posts the notification, re-arms the next
 * occurrence (alarms are one-shot), refreshes the widget, and sweeps for
 * missed milestones / money thresholds.
 *
 * Runs on a coroutine scope because receivers get ~10s on the main thread;
 * the work here is small (DataStore reads + one notification).
 */
@AndroidEntryPoint
class ReminderReceiver : BroadcastReceiver() {
    @Inject lateinit var settingsRepository: ReminderSettingsRepository

    @Inject lateinit var notifications: QuitNotifications

    @Inject lateinit var scheduler: ReminderScheduler

    @Inject lateinit var widgetUpdater: WidgetUpdater

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    override fun onReceive(context: Context, intent: Intent) {
        val pendingResult = goAsync()
        scope.launch {
            try {
                handle(intent.action, intent)
            } finally {
                pendingResult.finish()
            }
        }
    }

    private suspend fun handle(action: String?, intent: Intent) {
        val settings = settingsRepository.settings.first()
        val quitDate = settings.quitDateEpochMillis ?: return

        when (action) {
            ReminderScheduler.ACTION_PLEDGE -> {
                val days = QuitDate.daysClean(quitDate)
                notifications.postPledge(days)
                scheduler.scheduleAll() // re-arm tomorrow's pledge (+ everything else)
                checkMoneyThresholds(settings, quitDate, days)
                checkMissedMilestones(settings, quitDate)
            }
            ReminderScheduler.ACTION_CHECKIN -> {
                notifications.postCheckin()
                scheduler.scheduleAll()
            }
            ReminderScheduler.ACTION_EVENING -> {
                val days = QuitDate.daysClean(quitDate)
                notifications.postEvening(days)
                scheduler.scheduleAll()
            }
            ReminderScheduler.ACTION_MILESTONE -> {
                val id = intent.getStringExtra(ReminderScheduler.EXTRA_MILESTONE_ID)
                val milestone = Milestones.ALL.find { it.id == id }
                if (milestone != null) {
                    val index = Milestones.ALL.indexOf(milestone)
                    notifications.postMilestone(milestone, index)
                    settingsRepository.markMilestoneFired(milestone.id)
                }
                scheduler.scheduleNextMilestone()
            }
        }
        widgetUpdater.refresh()
    }

    /** Money thresholds ($50, $100, $500) fire once each, checked daily. */
    private suspend fun checkMoneyThresholds(
        settings: ReminderSettings,
        quitDate: Long,
        days: Long,
    ) {
        val saved = QuitDate.moneySavedCents(settings.weeklySpendCents, days)
        // Keep the thresholds sorted so the index is stable for notification ids.
        listOf(5_000L, 10_000L, 50_000L).forEachIndexed { index, threshold ->
            if (saved >= threshold && threshold !in settings.firedMoneyThresholds) {
                notifications.postMoneyThreshold(threshold, index)
                settingsRepository.markMoneyThresholdFired(threshold)
            }
        }
    }

    /**
     * Safety net: alarms can be missed (force-stop, OEM quirks). Any due,
     * unfired milestone gets posted now and marked fired.
     */
    private suspend fun checkMissedMilestones(
        settings: ReminderSettings,
        quitDate: Long,
    ) {
        val due = Milestones.due(quitDate, settings.firedMilestones)
        due.forEach { milestone ->
            val index = Milestones.ALL.indexOf(milestone)
            notifications.postMilestone(milestone, index)
            settingsRepository.markMilestoneFired(milestone.id)
        }
    }
}
