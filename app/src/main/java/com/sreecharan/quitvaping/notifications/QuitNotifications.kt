package com.sreecharan.quitvaping.notifications

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.sreecharan.quitvaping.MainActivity
import com.sreecharan.quitvaping.R
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Posts all quit-tracker notifications. Two channels: reminders (daily,
 * low-key) and celebrations (milestones, money — higher importance).
 *
 * Tap opens [MainActivity] with [EXTRA_DESTINATION] so the nav layer can
 * route to the right screen (pledge / progress / cravings). The nav agent
 * owns the route mapping; unknown values just open the home screen.
 */
@Singleton
class QuitNotifications
    @Inject
    constructor(
        @ApplicationContext private val context: Context,
    ) {
        companion object {
            const val EXTRA_DESTINATION = "notification_destination"
            const val DEST_PLEDGE = "pledge"
            const val DEST_PROGRESS = "progress"
            const val DEST_CRAVINGS = "cravings"
            const val DEST_HOME = "home"

            const val CHANNEL_REMINDERS = "quit_reminders"
            const val CHANNEL_CELEBRATIONS = "quit_celebrations"

            const val NOTIF_PLEDGE = 1001
            const val NOTIF_CHECKIN = 1002
            const val NOTIF_EVENING = 1003
            const val NOTIF_MILESTONE_BASE = 2000
            const val NOTIF_MONEY_BASE = 3000
            const val NOTIF_LAPSE = 4001
        }

        fun ensureChannels() {
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
            val manager = context.getSystemService(NotificationManager::class.java) ?: return
            manager.createNotificationChannel(
                NotificationChannel(
                    CHANNEL_REMINDERS,
                    "Quit reminders",
                    NotificationManager.IMPORTANCE_DEFAULT,
                ).apply { description = "Daily pledge and check-in reminders" },
            )
            manager.createNotificationChannel(
                NotificationChannel(
                    CHANNEL_CELEBRATIONS,
                    "Milestones",
                    NotificationManager.IMPORTANCE_HIGH,
                ).apply { description = "Milestone and savings celebrations" },
            )
        }

        fun postPledge(dayCount: Long) {
            post(
                id = NOTIF_PLEDGE,
                channel = CHANNEL_REMINDERS,
                title = "Day $dayCount. Tap to pledge.",
                text = "One tap: I won't vape today.",
                destination = DEST_PLEDGE,
            )
        }

        fun postCheckin() {
            post(
                id = NOTIF_CHECKIN,
                channel = CHANNEL_REMINDERS,
                title = "Craving window",
                text = "This is a high-risk time for you. Ride the wave, it passes in minutes.",
                destination = DEST_CRAVINGS,
            )
        }

        fun postEvening(dayCount: Long) {
            post(
                id = NOTIF_EVENING,
                channel = CHANNEL_REMINDERS,
                title = "Day $dayCount nearly done",
                text = "How many cravings did you beat today?",
                destination = DEST_HOME,
            )
        }

        fun postMilestone(milestone: Milestone, index: Int) {
            post(
                id = NOTIF_MILESTONE_BASE + (index % 500),
                channel = CHANNEL_CELEBRATIONS,
                title = milestone.title,
                text = milestone.message,
                destination = DEST_PROGRESS,
            )
        }

        fun postMoneyThreshold(savedCents: Long, index: Int) {
            post(
                id = NOTIF_MONEY_BASE + (index % 500),
                channel = CHANNEL_CELEBRATIONS,
                title = "You've saved ${QuitDate.formatMoney(savedCents)}!",
                text = "That's real money staying in your pocket.",
                destination = DEST_PROGRESS,
            )
        }

        /** Compassionate, never shaming. Fired when a lapse is logged. */
        fun postLapseCompassion(dayCount: Long) {
            post(
                id = NOTIF_LAPSE,
                channel = CHANNEL_REMINDERS,
                title = "One slip doesn't erase $dayCount clean days",
                text = "Log what triggered it and keep going. Progress, not perfection.",
                destination = DEST_HOME,
            )
        }

        private fun post(id: Int, channel: String, title: String, text: String, destination: String) {
            ensureChannels()
            val tapIntent =
                Intent(context, MainActivity::class.java).apply {
                    putExtra(EXTRA_DESTINATION, destination)
                    flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
                }
            val pendingIntent =
                PendingIntent.getActivity(
                    context,
                    id,
                    tapIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
                )
            val notification =
                NotificationCompat.Builder(context, channel)
                    .setSmallIcon(R.drawable.ic_stat_quit)
                    .setContentTitle(title)
                    .setContentText(text)
                    .setStyle(NotificationCompat.BigTextStyle().bigText(text))
                    .setContentIntent(pendingIntent)
                    .setAutoCancel(true)
                    .setPriority(
                        if (channel == CHANNEL_CELEBRATIONS) {
                            NotificationCompat.PRIORITY_HIGH
                        } else {
                            NotificationCompat.PRIORITY_DEFAULT
                        },
                    ).build()
            val manager = context.getSystemService(NotificationManager::class.java) ?: return
            try {
                manager.notify(id, notification)
            } catch (_: SecurityException) {
                // POST_NOTIFICATIONS revoked between schedule and fire; stay silent.
            }
        }
    }
