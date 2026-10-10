package com.example.nothingwidget.widgets

import android.appwidget.AppWidgetManager
import android.content.Context
import android.widget.RemoteViews
import com.example.nothingwidget.R

class WeatherWidget : NothingWidgetProvider() {
    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        runAsync { updateWidgets(context, appWidgetManager, appWidgetIds) }
    }

    companion object {
        const val DEFAULT_PRESET_ID = "weather_default"

        // Callable outside a broadcast (e.g. from WeatherWorker). Never route those callers
        // through onUpdate: goAsync() returns null when no broadcast is being delivered,
        // and the resulting NPE in finish() crashes the app process.
        suspend fun updateWidgets(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
            val repo = widgetRepository(context)

            for (appWidgetId in appWidgetIds) {
                val color = accentColorOf(repo.getConfigForWidget(appWidgetId, DEFAULT_PRESET_ID))
                val views = RemoteViews(context.packageName, R.layout.widget_weather)

                views.setTextColor(R.id.widget_weather_temp, color)
                views.setTextViewText(R.id.widget_weather_temp, "--°")
                views.setTextViewText(R.id.widget_weather_desc, context.getString(R.string.weather_unavailable))
                views.setOnClickPendingIntent(
                    R.id.widget_weather_root, editWidgetPendingIntent(context, appWidgetId, DEFAULT_PRESET_ID)
                )
                appWidgetManager.updateAppWidget(appWidgetId, views)
            }
        }
    }
}
