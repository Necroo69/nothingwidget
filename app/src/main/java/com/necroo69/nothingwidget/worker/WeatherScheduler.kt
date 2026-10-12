package com.necroo69.nothingwidget.worker

import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Context
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.necroo69.nothingwidget.data.repository.AppPreferencesRepository
import com.necroo69.nothingwidget.data.repository.DeviceLocation
import com.necroo69.nothingwidget.data.repository.WeatherRepository
import com.necroo69.nothingwidget.widgets.WeatherWidget
import kotlinx.coroutines.flow.first
import java.util.concurrent.TimeUnit

/**
 * Owns all WeatherWorker scheduling. Periodic work runs only while a weather widget is placed;
 * the interval comes from the Settings refresh-rate choice.
 */
object WeatherScheduler {
    private const val PERIODIC_WORK = "WeatherUpdateWork"
    private const val ONE_TIME_WORK = "WeatherRefreshNow"

    private val networkConstraint = Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build()

    fun hasWeatherWidgets(context: Context): Boolean =
        AppWidgetManager.getInstance(context)
            .getAppWidgetIds(ComponentName(context, WeatherWidget::class.java))
            .isNotEmpty()

    /**
     * Schedules the periodic refresh if a weather widget is placed, or cancels it if none is.
     * [intervalChanged] replaces an existing schedule; otherwise an existing one is kept.
     */
    suspend fun sync(context: Context, intervalChanged: Boolean = false) {
        val workManager = WorkManager.getInstance(context)
        if (!hasWeatherWidgets(context)) {
            workManager.cancelUniqueWork(PERIODIC_WORK)
            return
        }
        val minutes = AppPreferencesRepository(context).weatherRefreshMinutesFlow.first().toLong()
        val request = PeriodicWorkRequestBuilder<WeatherWorker>(minutes, TimeUnit.MINUTES)
            .setConstraints(networkConstraint)
            .build()
        workManager.enqueueUniquePeriodicWork(
            PERIODIC_WORK,
            if (intervalChanged) ExistingPeriodicWorkPolicy.UPDATE else ExistingPeriodicWorkPolicy.KEEP,
            request
        )
    }

    fun cancel(context: Context) {
        WorkManager.getInstance(context).cancelUniqueWork(PERIODIC_WORK)
    }

    /**
     * Called whenever the app comes to the foreground, the only time a device location can be
     * read (no background location permission). Updates a device location if it has moved, and
     * fetches weather if that happened or the forecast is older than the refresh interval.
     */
    suspend fun onAppForeground(context: Context) {
        val repo = WeatherRepository(context)
        var moved = false
        if (repo.locationFlow.first()?.isDeviceLocation == true && DeviceLocation.hasPermission(context)) {
            moved = repo.locateDevice()?.second == true
        }
        val minutes = AppPreferencesRepository(context).weatherRefreshMinutesFlow.first()
        if (moved || repo.isStale(minutes * 60_000L)) refreshNow(context)
        sync(context)
    }

    /** Fetches weather as soon as there is a network, then redraws weather widgets. */
    fun refreshNow(context: Context) {
        val request = OneTimeWorkRequestBuilder<WeatherWorker>().setConstraints(networkConstraint).build()
        WorkManager.getInstance(context).enqueueUniqueWork(ONE_TIME_WORK, ExistingWorkPolicy.REPLACE, request)
    }
}
