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

// The date is a TextClock in widget_date.xml, which the launcher re-formats at midnight on its
// own. This provider only applies the accent color.
class DateWidget : AppWidgetProvider() {
    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        val pendingResult = goAsync()
        
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val db = AppDatabase.getInstance(context)
                val repo = WidgetRepository(db.widgetConfigDao())
                val dateConfig = repo.getConfigById("date_default")
                
                // Parse color from config, fallback to white if not found
                val colorHex = dateConfig?.accentColorHex ?: "#FFFFFF"
                val parsedColor = try { Color.parseColor(colorHex) } catch (e: Exception) { Color.WHITE }

                for (appWidgetId in appWidgetIds) {
                    val views = RemoteViews(context.packageName, R.layout.widget_date)
                    
                    views.setTextColor(R.id.widget_date_text, parsedColor)
                    
                    appWidgetManager.updateAppWidget(appWidgetId, views)
                }
            } finally {
                pendingResult.finish()
            }
        }
    }
}
