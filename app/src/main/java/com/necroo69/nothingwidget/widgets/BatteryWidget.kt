package com.necroo69.nothingwidget.widgets

import android.appwidget.AppWidgetManager
import android.content.Context
import android.content.res.ColorStateList
import android.os.Build
import android.widget.RemoteViews
import com.necroo69.nothingwidget.R
import com.necroo69.nothingwidget.data.repository.BatteryRepository

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
                val config = repo.getConfigForWidget(appWidgetId, DEFAULT_PRESET_ID)
                val color = accentColorOf(config)
                val views = RemoteViews(context.packageName, R.layout.widget_battery)

                views.applyCard(context, appWidgetManager, appWidgetId, config, R.id.widget_battery_root, DEFAULT_PRESET_ID, DEFAULT_SIZE_DP)
                views.setTextViewText(R.id.widget_battery_text, "${batteryInfo.percentage}%")
                views.setTextColor(R.id.widget_battery_text, color)
                views.setProgressBar(R.id.battery_progress, 100, batteryInfo.percentage, false)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    views.setColorStateList(R.id.battery_progress, "setProgressTintList", ColorStateList.valueOf(color))
                }
                views.setContentDescription(R.id.widget_battery_root, "Battery ${batteryInfo.percentage}%")
                appWidgetManager.updateAppWidget(appWidgetId, views)
            }
        }
    }

    companion object {
        const val DEFAULT_PRESET_ID = "battery_default"

        /** minWidth x minHeight from widget_battery_info.xml. */
        private val DEFAULT_SIZE_DP = 130 to 50
    }
}
