package com.example.nothingwidget.widgets

import android.appwidget.AppWidgetManager
import android.content.Context
import android.widget.RemoteViews
import com.example.nothingwidget.R

// The date is a TextClock in widget_date.xml, which the launcher re-formats at midnight on its
// own. This provider only applies each widget's style.
class DateWidget : NothingWidgetProvider() {
    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        runAsync {
            val repo = widgetRepository(context)

            for (appWidgetId in appWidgetIds) {
                val config = repo.getConfigForWidget(appWidgetId, DEFAULT_PRESET_ID)
                val views = RemoteViews(context.packageName, R.layout.widget_date)

                views.applyCard(context, appWidgetManager, appWidgetId, config, R.id.widget_date_root, DEFAULT_PRESET_ID, DEFAULT_SIZE_DP)
                views.setTextColor(R.id.widget_date_text, accentColorOf(config))
                views.setOptionalText(R.id.widget_date_subtitle, config?.customSubtitle)
                appWidgetManager.updateAppWidget(appWidgetId, views)
            }
        }
    }

    companion object {
        const val DEFAULT_PRESET_ID = "date_default"

        /** minWidth x minHeight from widget_date_info.xml. */
        private val DEFAULT_SIZE_DP = 250 to 50
    }
}
