package com.example.nothingwidget.widgets

import android.appwidget.AppWidgetManager
import android.content.Context
import android.widget.RemoteViews
import com.example.nothingwidget.R
import com.example.nothingwidget.data.repository.BatteryRepository

class BatteryWidget : NothingWidgetProvider() {

    // Refreshed by updatePeriodMillis (30 min, the platform minimum) in widget_battery_info.xml,
    // and whenever the app is opened. ACTION_POWER_(DIS)CONNECTED is not delivered to manifest
    // receivers on API 26+, so it cannot be used to refresh this widget without a running process.
    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        if (appWidgetIds.isEmpty()) return

        runAsync {
            val repo = widgetRepository(context)
            val batteryInfo = BatteryRepository.getBatteryInfoSync(context)

            for (appWidgetId in appWidgetIds) {
                val color = accentColorOf(repo.getConfigForWidget(appWidgetId, DEFAULT_PRESET_ID))
                val views = RemoteViews(context.packageName, R.layout.widget_battery)

                views.setTextViewText(R.id.widget_battery_text, "${batteryInfo.percentage}%")
                views.setProgressBar(R.id.battery_progress, 100, batteryInfo.percentage, false)
                views.setTextColor(R.id.widget_battery_text, color)
                views.setOnClickPendingIntent(
                    R.id.widget_battery_root, editWidgetPendingIntent(context, appWidgetId, DEFAULT_PRESET_ID)
                )
                appWidgetManager.updateAppWidget(appWidgetId, views)
            }
        }
    }

    companion object {
        const val DEFAULT_PRESET_ID = "battery_default"
    }
}
