package com.sreecharan.quitvaping.widget

import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver

/** Host entry point for the quit day-counter widget. Declared in the manifest. */
class QuitWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = QuitWidget()
}
