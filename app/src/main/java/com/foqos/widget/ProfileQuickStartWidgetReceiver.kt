package com.foqos.widget

import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context

/**
 * Widget for quick start of a specific profile.
 * User can configure which profile to trigger.
 */
class ProfileQuickStartWidgetReceiver : AppWidgetProvider() {
    
    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        // TODO: Show profile quick start button
        // Use Glance for Jetpack Compose widgets
    }
}
