package com.sreecharan.quitvaping.notifications

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.sreecharan.quitvaping.widget.WidgetUpdater
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Re-schedules every alarm after boot and after any wall-clock change
 * (time set, timezone, date, locale). Alarms do not survive reboot, and
 * daily triggers computed in wall-clock time go stale on timezone change.
 */
@AndroidEntryPoint
class BootReceiver : BroadcastReceiver() {
    @Inject lateinit var scheduler: ReminderScheduler

    @Inject lateinit var widgetUpdater: WidgetUpdater

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    override fun onReceive(context: Context, intent: Intent) {
        when (intent.action) {
            Intent.ACTION_BOOT_COMPLETED,
            Intent.ACTION_TIME_CHANGED,
            Intent.ACTION_TIMEZONE_CHANGED,
            Intent.ACTION_DATE_CHANGED,
            Intent.ACTION_LOCALE_CHANGED,
            -> {
                val pendingResult = goAsync()
                scope.launch {
                    try {
                        scheduler.scheduleAll()
                        widgetUpdater.refresh()
                    } finally {
                        pendingResult.finish()
                    }
                }
            }
        }
    }
}
