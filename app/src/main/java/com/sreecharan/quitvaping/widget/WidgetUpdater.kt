package com.sreecharan.quitvaping.widget

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.glance.appwidget.GlanceAppWidgetManager
import com.sreecharan.quitvaping.data.reminders.ReminderSettingsRepository
import com.sreecharan.quitvaping.notifications.QuitDate
import com.sreecharan.quitvaping.widget.QuitWidget.Companion.widgetDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.first
import javax.inject.Inject
import javax.inject.Singleton

private val KEY_WIDGET_DAYS = longPreferencesKey("widget_days_clean")
private val KEY_WIDGET_SAVED = longPreferencesKey("widget_saved_cents")

/**
 * Pushes fresh values into the widget's cached state and triggers a
 * Glance update. Glance composition cannot easily read Room, so the day
 * count and savings are cached here whenever anything meaningful happens:
 * alarm fires, date changes, app comes to foreground.
 */
@Singleton
class WidgetUpdater
    @Inject
    constructor(
        @ApplicationContext private val context: Context,
        private val settingsRepository: ReminderSettingsRepository,
    ) {
        suspend fun refresh() {
            val settings = settingsRepository.settings.first()
            val days = QuitDate.daysClean(settings.quitDateEpochMillis)
            val saved = QuitDate.moneySavedCents(settings.weeklySpendCents, days)
            context.widgetDataStore.edit {
                it[KEY_WIDGET_DAYS] = days
                it[KEY_WIDGET_SAVED] = saved
            }
            updateWidget()
        }

        suspend fun updateWidget() {
            try {
                val manager = GlanceAppWidgetManager(context)
                val widget = QuitWidget()
                manager.getGlanceIds(QuitWidget::class.java).forEach { glanceId ->
                    widget.update(context, glanceId)
                }
            } catch (_: Exception) {
                // Widget host unavailable (e.g. work profile); not fatal.
            }
        }
    }
