package com.necroo69.nothingwidget.widgets

import android.content.Context
import com.necroo69.nothingwidget.domain.model.WidgetType
import com.necroo69.nothingwidget.worker.WeatherScheduler

/** What screens may ask of the home-screen widgets and their background work. */
interface WidgetEngine {
    /** Redraws placed widgets of [type] from saved configs and cached data. */
    fun redraw(type: WidgetType)

    /** Fetches weather now (when online), then redraws weather widgets. */
    fun refreshWeather()

    /** Re-applies the weather refresh interval from Settings. */
    suspend fun rescheduleWeather()

    /** Redraws every placed widget and fetches fresh weather. */
    fun refreshAll()
}

class AndroidWidgetEngine(context: Context) : WidgetEngine {
    private val appContext = context.applicationContext

    override fun redraw(type: WidgetType) = requestWidgetUpdate(appContext, type)

    override fun refreshWeather() = WeatherScheduler.refreshNow(appContext)

    override suspend fun rescheduleWeather() = WeatherScheduler.sync(appContext, intervalChanged = true)

    override fun refreshAll() {
        requestAllWidgetUpdates(appContext)
        WeatherScheduler.refreshNow(appContext)
    }
}
