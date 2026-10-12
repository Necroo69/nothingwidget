package com.example.nothingwidget.worker

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.nothingwidget.data.repository.WeatherRefreshResult
import com.example.nothingwidget.data.repository.WeatherRepository
import com.example.nothingwidget.widgets.WeatherWidget

/** Fetches the forecast for the saved location, caches it, and redraws every weather widget. */
class WeatherWorker(
    private val context: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {
        val result = WeatherRepository(context).refresh()

        // Redraw even on failure, so widgets show the cached forecast's time or a setup prompt.
        // Not WeatherWidget().onUpdate(): its goAsync() is null outside a broadcast.
        WeatherWidget.updateAll(context)

        return if (result == WeatherRefreshResult.FAILED && runAttemptCount < MAX_RETRIES) Result.retry() else Result.success()
    }

    private companion object {
        const val MAX_RETRIES = 3
    }
}
