package com.sreecharan.quitvaping.notifications

import android.Manifest
import android.app.AlarmManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.provider.Settings
import androidx.core.content.ContextCompat
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Permission plumbing for the two notification-adjacent permissions.
 *
 * POST_NOTIFICATIONS: runtime permission (API 33+). Ask contextually when
 * the user enables reminders, not at first launch.
 *
 * SCHEDULE_EXACT_ALARM: special access, denied by default on API 33+.
 * There is no runtime dialog — the only path is a full context switch to
 * system settings via [exactAlarmSettingsIntent]. Re-check in onResume()
 * after the user returns.
 */
@Singleton
class NotificationPermission
    @Inject
    constructor(
        @ApplicationContext private val context: Context,
    ) {
        fun hasPostNotifications(): Boolean {
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) return true
            return ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS,
            ) == PackageManager.PERMISSION_GRANTED
        }

        fun postNotificationsPermission(): String = Manifest.permission.POST_NOTIFICATIONS

        fun canScheduleExactAlarms(): Boolean {
            val am = context.getSystemService(AlarmManager::class.java) ?: return false
            return Build.VERSION.SDK_INT < Build.VERSION_CODES.S || am.canScheduleExactAlarms()
        }

        /** Intent for Settings > Alarms & reminders. Null on API < 31. */
        fun exactAlarmSettingsIntent(): Intent? {
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S) return null
            return Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM).apply {
                // Deep-link straight to this app's entry when supported.
                data = android.net.Uri.parse("package:${context.packageName}")
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
        }

        /** Intent for the app's notification settings page. */
        fun appNotificationSettingsIntent(): Intent =
            Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS).apply {
                putExtra(Settings.EXTRA_APP_PACKAGE, context.packageName)
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
    }
