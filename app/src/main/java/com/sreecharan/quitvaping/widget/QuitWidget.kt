package com.sreecharan.quitvaping.widget

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.action.actionStartActivity
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.unit.ColorProvider
import androidx.glance.currentState
import androidx.glance.layout.Alignment
import androidx.glance.layout.Column
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.padding
import androidx.glance.state.GlanceStateDefinition
import androidx.glance.state.PreferencesGlanceStateDefinition
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import com.sreecharan.quitvaping.MainActivity
import com.sreecharan.quitvaping.notifications.QuitDate

private val KEY_WIDGET_DAYS = longPreferencesKey("widget_days_clean")
private val KEY_WIDGET_SAVED = longPreferencesKey("widget_saved_cents")

/**
 * Home-screen day counter. Reads the cached values written by
 * [WidgetUpdater] (Glance composition cannot easily read Room).
 * Tapping opens the app.
 */
class QuitWidget : GlanceAppWidget() {
    companion object {
        val Context.widgetDataStore by preferencesDataStore(name = "quit_widget")
    }

    override val stateDefinition: GlanceStateDefinition<*> = PreferencesGlanceStateDefinition

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        provideContent {
            val prefs = currentState<Preferences>()
            val days = prefs[KEY_WIDGET_DAYS] ?: 0L
            val saved = prefs[KEY_WIDGET_SAVED] ?: 0L
            WidgetContent(days, saved)
        }
    }

    @Composable
    private fun WidgetContent(days: Long, savedCents: Long) {
        Column(
            modifier =
                GlanceModifier
                    .fillMaxSize()
                    .background(Color(0xFFF2F2F7))
                    .padding(16.dp)
                    .clickable(actionStartActivity<MainActivity>()),
            verticalAlignment = Alignment.Vertical.CenterVertically,
            horizontalAlignment = Alignment.Horizontal.Start,
        ) {
            Text(
                text = "$days",
                style =
                    TextStyle(
                        fontSize = 40.sp,
                        fontWeight = FontWeight.Bold,
                        color = ColorProvider(Color(0xFF007AFF)),
                    ),
            )
            Text(
                text = if (days == 1L) "day vape-free" else "days vape-free",
                style = TextStyle(fontSize = 14.sp, color = ColorProvider(Color(0xFF3A3A3C))),
            )
            if (savedCents > 0) {
                Text(
                    text = "${QuitDate.formatMoney(savedCents)} saved",
                    style =
                        TextStyle(
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color(0xFF34C759),
                        ),
                )
            }
        }
    }
}
