package com.example.nothingwidget.worker

import android.content.Context
import android.appwidget.AppWidgetManager
import android.content.ComponentName
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.nothingwidget.widgets.WeatherWidget
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class WeatherWorker(
    private val context: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        try {
            // Phase 7 will fetch actual data here and save to DataStore/Room.
            // For now, force-refresh every placed WeatherWidget instance directly.
            // Sending ACTION_APPWIDGET_UPDATE without EXTRA_APPWIDGET_IDS can be
            // ignored by AppWidgetProvider, so resolve the active IDs here.
            val appWidgetManager = AppWidgetManager.getInstance(context)
            val appWidgetIds = appWidgetManager.getAppWidgetIds(
                ComponentName(context, WeatherWidget::class.java)
            )

            if (appWidgetIds.isNotEmpty()) {
                WeatherWidget().onUpdate(context, appWidgetManager, appWidgetIds)
            }

            Result.success()
        } catch (e: Exception) {
            Result.retry()
        }
    }
}
