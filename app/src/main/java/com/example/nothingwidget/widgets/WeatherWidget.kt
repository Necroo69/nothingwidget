package com.example.nothingwidget.widgets

import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.graphics.Color
import android.widget.RemoteViews
import com.example.nothingwidget.R
import com.example.nothingwidget.data.local.AppDatabase
import com.example.nothingwidget.data.repository.AppPreferencesRepository
import com.example.nothingwidget.data.repository.WidgetRepository
import com.example.nothingwidget.domain.model.WeatherCondition
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
                val prefsRepo = AppPreferencesRepository(context)
                val weatherConfig = repo.getConfigById("weather_default")
                
                // Parse color from config, fallback to white if not found
                val colorHex = weatherConfig?.accentColorHex ?: "#FFFFFF"
                val parsedColor = try { Color.parseColor(colorHex) } catch (e: Exception) { Color.WHITE }

                val useGps = prefsRepo.useGpsLocationFlow.first()
                val detectedCity = prefsRepo.detectedCityNameFlow.first()
                val selectedCity = prefsRepo.selectedCityFlow.first()
                val city = if (useGps && !detectedCity.isNullOrBlank()) detectedCity else selectedCity
                
                val temp = prefsRepo.lastWeatherTempFlow.first().toInt()
                val code = prefsRepo.lastWeatherCodeFlow.first()
                val condition = WeatherCondition.fromWmoCode(code)

                for (appWidgetId in appWidgetIds) {
                    val views = RemoteViews(context.packageName, R.layout.widget_weather)
                    
                    views.setTextColor(R.id.widget_weather_temp, parsedColor)
                    views.setTextViewText(R.id.widget_weather_temp, "${temp}°")
                    views.setTextViewText(R.id.widget_weather_desc, condition.label.uppercase())
                    views.setTextViewText(R.id.widget_weather_city, city.uppercase())
                    
                    appWidgetManager.updateAppWidget(appWidgetId, views)
                }
            } finally {
                pendingResult.finish()
            }
        }
    }
}
