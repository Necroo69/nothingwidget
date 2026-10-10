package com.example.nothingwidget.widgets

import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import com.example.nothingwidget.domain.model.WidgetType

/**
 * The home-screen provider registered in AndroidManifest.xml for [type], or null if that type
 * has no registered provider yet. Pinning an unregistered provider shows no pin dialog and
 * fails silently, so callers must tell the user instead of pinning a stand-in widget.
 */
fun providerClassFor(type: WidgetType): Class<out AppWidgetProvider>? = when (type) {
    WidgetType.DIGITAL_CLOCK, WidgetType.ANALOG_CLOCK, WidgetType.WORLD_CLOCK -> ClockWidget::class.java
    WidgetType.DATE -> DateWidget::class.java
    WidgetType.BATTERY_CIRCLE -> BatteryWidget::class.java
    WidgetType.WEATHER -> WeatherWidget::class.java
    WidgetType.QUICK_TOGGLES, WidgetType.STEP_TRACKER, WidgetType.AUDIO_PLAYER, WidgetType.QUICK_NOTE -> null
}

/**
 * Asks every placed home-screen widget of [type] to redraw. Does nothing if [type] has no
 * provider or none is placed. The update broadcast must carry the ids, or AppWidgetProvider
 * ignores it.
 */
fun requestWidgetUpdate(context: Context, type: WidgetType) {
    val providerClass = providerClassFor(type) ?: return
    val ids = AppWidgetManager.getInstance(context).getAppWidgetIds(ComponentName(context, providerClass))
    if (ids.isEmpty()) return

    context.sendBroadcast(Intent(context, providerClass).apply {
        action = AppWidgetManager.ACTION_APPWIDGET_UPDATE
        putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, ids)
    })
}
