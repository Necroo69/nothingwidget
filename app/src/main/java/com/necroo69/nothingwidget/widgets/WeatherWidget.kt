package com.necroo69.nothingwidget.widgets

import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Context
import android.text.format.DateFormat
import android.widget.RemoteViews
import com.necroo69.nothingwidget.R
import com.necroo69.nothingwidget.data.repository.WeatherRepository
import com.necroo69.nothingwidget.domain.model.WeatherStatus
import com.necroo69.nothingwidget.worker.WeatherScheduler
import java.util.Date

// Renders the cached forecast from WeatherRepository; WeatherWorker fetches it and redraws.
class WeatherWidget : NothingWidgetProvider() {
    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        runAsync { updateWidgets(context, appWidgetManager, appWidgetIds) }
    }

    // First weather widget placed: start the periodic refresh and fetch now.
    override fun onEnabled(context: Context) {
        runAsync {
            WeatherScheduler.sync(context)
            WeatherScheduler.refreshNow(context)
        }
    }

    // Last weather widget removed: stop refreshing.
    override fun onDisabled(context: Context) {
        WeatherScheduler.cancel(context)
    }

    companion object {
        const val DEFAULT_PRESET_ID = "weather_default"

        /** minWidth x minHeight from widget_weather_info.xml. */
        private val DEFAULT_SIZE_DP = 130 to 130

        /** Redraws every placed weather widget. Safe to call outside a broadcast. */
        suspend fun updateAll(context: Context) {
            val manager = AppWidgetManager.getInstance(context)
            val ids = manager.getAppWidgetIds(ComponentName(context, WeatherWidget::class.java))
            if (ids.isNotEmpty()) updateWidgets(context, manager, ids)
        }

        // Callable outside a broadcast (e.g. from WeatherWorker). Never route those callers
        // through onUpdate: goAsync() returns null when no broadcast is being delivered,
        // and the resulting NPE in finish() crashes the app process.
        suspend fun updateWidgets(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
            val repo = widgetRepository(context)
            val weather = WeatherRepository(context).currentWeather()

            for (appWidgetId in appWidgetIds) {
                val config = repo.getConfigForWidget(appWidgetId, DEFAULT_PRESET_ID)
                val views = RemoteViews(context.packageName, R.layout.widget_weather)
                views.applyCard(context, appWidgetManager, appWidgetId, config, R.id.widget_weather_root, DEFAULT_PRESET_ID, DEFAULT_SIZE_DP)
                views.setTextColor(R.id.widget_weather_temp, accentColorOf(config))

                when (weather.status) {
                    WeatherStatus.NO_LOCATION -> {
                        views.setTextViewText(R.id.widget_weather_city, "WEATHER")
                        views.setTextViewText(R.id.widget_weather_temp, "--°")
                        views.setTextViewText(R.id.widget_weather_desc, context.getString(R.string.weather_set_location))
                        views.setOptionalText(R.id.widget_weather_updated, null)
                        views.setOnClickPendingIntent(R.id.widget_weather_root, weatherLocationPendingIntent(context, appWidgetId))
                        views.setContentDescription(R.id.widget_weather_root, context.getString(R.string.weather_set_location))
                    }
                    WeatherStatus.NO_DATA -> {
                        views.setTextViewText(R.id.widget_weather_city, weather.cityName)
                        views.setTextViewText(R.id.widget_weather_temp, "--°")
                        views.setTextViewText(R.id.widget_weather_desc, context.getString(R.string.weather_waiting))
                        views.setOptionalText(R.id.widget_weather_updated, null)
                        views.setContentDescription(R.id.widget_weather_root, "${weather.cityName}, ${context.getString(R.string.weather_waiting)}")
                    }
                    WeatherStatus.READY -> {
                        val temperature = weather.temperature ?: 0
                        // On its own line: "H 14° · L 7°" wraps mid-pair on a 2x2 widget otherwise.
                        val highLow = if (weather.high != null && weather.low != null) {
                            "\n" + context.getString(R.string.weather_high_low, weather.high, weather.low)
                        } else ""
                        val updated = weather.updatedAtMillis?.let {
                            context.getString(R.string.weather_updated, DateFormat.getTimeFormat(context).format(Date(it)))
                        }
                        views.setTextViewText(R.id.widget_weather_city, weather.cityName.substringBefore(","))
                        views.setTextViewText(R.id.widget_weather_temp, "$temperature°")
                        views.setTextViewText(R.id.widget_weather_desc, "${weather.condition.dotSymbol} ${weather.condition.label}$highLow")
                        views.setOptionalText(R.id.widget_weather_updated, updated)
                        views.setContentDescription(
                            R.id.widget_weather_root,
                            "${weather.cityName}, ${context.getString(R.string.weather_temperature, temperature, weather.unitSymbol)}, ${weather.condition.label}"
                        )
                    }
                }
                appWidgetManager.updateAppWidget(appWidgetId, views)
            }
        }
    }
}
