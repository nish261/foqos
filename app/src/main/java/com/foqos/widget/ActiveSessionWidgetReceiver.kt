package com.foqos.widget

import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context

/**
 * Widget showing active focus session with elapsed time.
 * Updates every minute automatically.
 */
class ActiveSessionWidgetReceiver : AppWidgetProvider() {
    
    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        // TODO: Update widget with active session info
        // Use Glance for Jetpack Compose widgets
    }
}
