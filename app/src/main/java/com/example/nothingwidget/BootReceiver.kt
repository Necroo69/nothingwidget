package com.example.nothingwidget

import android.appwidget.AppWidgetManager
import android.content.BroadcastReceiver
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import com.example.nothingwidget.widgets.BatteryWidget
import com.example.nothingwidget.widgets.ClockWidget
import com.example.nothingwidget.widgets.DateWidget
import com.example.nothingwidget.widgets.WeatherWidget
import com.example.nothingwidget.widgets.runAsync
import com.example.nothingwidget.worker.WeatherScheduler

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            // Manually ping all widgets so they aren't blank for 10-15s
            val widgetManager = AppWidgetManager.getInstance(context)
            val widgetClasses = listOf(
                ClockWidget::class.java,
                DateWidget::class.java,
                BatteryWidget::class.java,
                WeatherWidget::class.java
            )

            widgetClasses.forEach { providerClass ->
                val ids = widgetManager.getAppWidgetIds(ComponentName(context, providerClass))
                if (ids.isNotEmpty()) {
                    val updateIntent = Intent(context, providerClass).apply {
                        action = AppWidgetManager.ACTION_APPWIDGET_UPDATE
                        putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, ids)
                    }
                    context.sendBroadcast(updateIntent)
                }
            }

            // Periodic work survives reboot on its own; this also covers a schedule that was
            // cancelled by a force-stop, and fetches fresh data for the widgets just pinged.
            runAsync {
                WeatherScheduler.sync(context)
                if (WeatherScheduler.hasWeatherWidgets(context)) WeatherScheduler.refreshNow(context)
            }
        }
    }
}
