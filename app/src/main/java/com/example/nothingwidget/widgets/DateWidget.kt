package com.example.nothingwidget.widgets

import android.appwidget.AppWidgetManager
import android.content.Context
import android.widget.RemoteViews
import com.example.nothingwidget.R

// The date is a TextClock in widget_date.xml, which the launcher re-formats at midnight on its
// own. This provider only applies each widget's accent color.
class DateWidget : NothingWidgetProvider() {
    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        runAsync {
            val repo = widgetRepository(context)

            for (appWidgetId in appWidgetIds) {
                val color = accentColorOf(repo.getConfigForWidget(appWidgetId, DEFAULT_PRESET_ID))
                val views = RemoteViews(context.packageName, R.layout.widget_date)

                views.setTextColor(R.id.widget_date_text, color)
                views.setOnClickPendingIntent(
                    R.id.widget_date_root, editWidgetPendingIntent(context, appWidgetId, DEFAULT_PRESET_ID)
                )
                appWidgetManager.updateAppWidget(appWidgetId, views)
            }
        }
    }

    companion object {
        const val DEFAULT_PRESET_ID = "date_default"
    }
}
