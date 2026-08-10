package com.example.nothingwidget.widgets

import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.graphics.Color
import android.widget.RemoteViews
import com.example.nothingwidget.R
import com.example.nothingwidget.data.local.AppDatabase
import com.example.nothingwidget.data.repository.WidgetRepository
import com.example.nothingwidget.domain.model.WidgetType
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class WeatherWidget : AppWidgetProvider() {
    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        val pendingResult = goAsync()
        
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val db = AppDatabase.getInstance(context)
                val repo = WidgetRepository(db.widgetConfigDao())
                val weatherConfig = repo.getConfigById("weather_default")
                
                // Parse color from config, fallback to white if not found
                val colorHex = weatherConfig?.accentColorHex ?: "#FFFFFF"
                val parsedColor = try { Color.parseColor(colorHex) } catch (e: Exception) { Color.WHITE }

                for (appWidgetId in appWidgetIds) {
                    val views = RemoteViews(context.packageName, R.layout.widget_weather)
                    
                    views.setTextColor(R.id.widget_weather_temp, parsedColor)
                    views.setTextViewText(R.id.widget_weather_temp, "--°")
                    views.setTextViewText(R.id.widget_weather_desc, context.getString(R.string.weather_unavailable))

                    appWidgetManager.updateAppWidget(appWidgetId, views)
                }
            } finally {
                pendingResult.finish()
            }
        }
    }
}
