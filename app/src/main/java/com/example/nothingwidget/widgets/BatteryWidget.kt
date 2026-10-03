package com.example.nothingwidget.widgets

import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.graphics.Color
import android.widget.RemoteViews
import com.example.nothingwidget.R
import com.example.nothingwidget.data.local.AppDatabase
import com.example.nothingwidget.data.repository.BatteryRepository
import com.example.nothingwidget.data.repository.WidgetRepository
import com.example.nothingwidget.domain.model.WidgetType
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class BatteryWidget : AppWidgetProvider() {

    // Refreshed by updatePeriodMillis (30 min, the platform minimum) in widget_battery_info.xml.
    // ACTION_POWER_(DIS)CONNECTED is not delivered to manifest receivers on API 26+, so it
    // cannot be used to refresh this widget without a running process.
    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        updateWidgets(context, appWidgetManager, appWidgetIds)
    }

    private fun updateWidgets(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        if (appWidgetIds.isEmpty()) return

        val pendingResult = goAsync()
        
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val db = AppDatabase.getInstance(context)
                val repo = WidgetRepository(db.widgetConfigDao())
                val batteryConfig = repo.getConfigById("battery_default")
                
                // Parse color from config, fallback to white if not found
                val colorHex = batteryConfig?.accentColorHex ?: "#FFFFFF"
                val parsedColor = try { Color.parseColor(colorHex) } catch (e: Exception) { Color.WHITE }

                val batteryInfo = BatteryRepository.getBatteryInfoSync(context)

                for (appWidgetId in appWidgetIds) {
                    val views = RemoteViews(context.packageName, R.layout.widget_battery)
                    
                    views.setTextViewText(R.id.widget_battery_text, "${batteryInfo.percentage}%")
                    views.setProgressBar(R.id.battery_progress, 100, batteryInfo.percentage, false)
                    
                    // The original widget_battery had text view we can tint
                    views.setTextColor(R.id.widget_battery_text, parsedColor)
                    
                    appWidgetManager.updateAppWidget(appWidgetId, views)
                }
            } finally {
                pendingResult.finish()
            }
        }
    }
}
