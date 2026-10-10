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
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

// The time and date are TextClocks in widget_clock_layout.xml. The launcher ticks them every
// minute and on time, timezone and date changes, so this provider only applies the accent color
// and the app's 12/24-hour setting. No AlarmManager is needed (verified across Doze, reboot
// and midnight; see docs/Memory.md §8).
class ClockWidget : AppWidgetProvider() {
    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        val pendingResult = goAsync()
        
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val db = AppDatabase.getInstance(context)
                val repo = WidgetRepository(db.widgetConfigDao())
                val clockConfig = repo.getConfigById("clock_digital_default")
                
                // Parse color from config, fallback to white if not found
                val colorHex = clockConfig?.accentColorHex ?: "#FFFFFF"
                val parsedColor = try { Color.parseColor(colorHex) } catch (e: Exception) { Color.WHITE }
                val timePattern = timePattern(AppPreferencesRepository(context).is24HourClockFlow.first())

                for (appWidgetId in appWidgetIds) {
                    val views = RemoteViews(context.packageName, R.layout.widget_clock_layout)
                    
                    // Apply color if supported by the layout (widget_clock_time might not exist in old layout, but it does in widget_clock_layout)
                    views.setTextColor(R.id.widget_clock_time, parsedColor)
                    views.setTextColor(R.id.widget_clock_date, parsedColor)

                    // TextClock picks format12Hour or format24Hour from the *system* setting.
                    // Setting both makes the widget follow the app setting instead.
                    views.setCharSequence(R.id.widget_clock_time, "setFormat12Hour", timePattern)
                    views.setCharSequence(R.id.widget_clock_time, "setFormat24Hour", timePattern)

                    appWidgetManager.updateAppWidget(appWidgetId, views)
                }
            } finally {
                pendingResult.finish()
            }
        }
    }

    companion object {
        /** Time pattern for the app's 24-hour setting. 12-hour has no AM/PM, to match the minimal look. */
        fun timePattern(is24Hour: Boolean): String = if (is24Hour) "HH:mm" else "h:mm"
    }
}
