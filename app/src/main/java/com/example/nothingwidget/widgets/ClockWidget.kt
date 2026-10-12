package com.example.nothingwidget.widgets

import android.appwidget.AppWidgetManager
import android.content.Context
import android.widget.RemoteViews
import com.example.nothingwidget.R
import com.example.nothingwidget.data.repository.AppPreferencesRepository
import kotlinx.coroutines.flow.first

// The time and date are TextClocks in widget_clock_layout.xml. The launcher ticks them every
// minute and on time, timezone and date changes, so this provider only applies each widget's
// style and the app's 12/24-hour setting. No AlarmManager is needed (verified across
// Doze, reboot and midnight; see docs/Memory.md §8).
class ClockWidget : NothingWidgetProvider() {
    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        runAsync {
            val repo = widgetRepository(context)
            val timePattern = timePattern(AppPreferencesRepository(context).is24HourClockFlow.first())

            for (appWidgetId in appWidgetIds) {
                val config = repo.getConfigForWidget(appWidgetId, DEFAULT_PRESET_ID)
                val color = accentColorOf(config)
                val views = RemoteViews(context.packageName, R.layout.widget_clock_layout)

                views.applyCard(context, appWidgetManager, appWidgetId, config, R.id.widget_clock_root, DEFAULT_PRESET_ID, DEFAULT_SIZE_DP)
                views.setTextColor(R.id.widget_clock_time, color)
                views.setTextColor(R.id.widget_clock_date, color)
                views.setOptionalText(R.id.widget_clock_subtitle, config?.customSubtitle)

                // TextClock picks format12Hour or format24Hour from the *system* setting.
                // Setting both makes the widget follow the app setting instead.
                views.setCharSequence(R.id.widget_clock_time, "setFormat12Hour", timePattern)
                views.setCharSequence(R.id.widget_clock_time, "setFormat24Hour", timePattern)

                appWidgetManager.updateAppWidget(appWidgetId, views)
            }
        }
    }

    companion object {
        /** Template for clocks added from the launcher's widget picker rather than the gallery. */
        const val DEFAULT_PRESET_ID = "clock_digital_default"

        /** minWidth x minHeight from widget_clock_info.xml. */
        private val DEFAULT_SIZE_DP = 250 to 110

        /** Time pattern for the app's 24-hour setting. 12-hour has no AM/PM, to match the minimal look. */
        fun timePattern(is24Hour: Boolean): String = if (is24Hour) "HH:mm" else "h:mm"
    }
}
