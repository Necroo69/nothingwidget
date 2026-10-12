package com.necroo69.nothingwidget.data.repository

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.doublePreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.necroo69.nothingwidget.data.remote.ForecastResponse
import com.necroo69.nothingwidget.data.remote.OpenMeteoApi
import com.necroo69.nothingwidget.domain.model.HourlyForecast
import com.necroo69.nothingwidget.domain.model.WeatherCondition
import com.necroo69.nothingwidget.domain.model.WeatherInfo
import com.necroo69.nothingwidget.domain.model.WeatherLocation
import com.necroo69.nothingwidget.domain.model.WeatherStatus
import com.squareup.moshi.JsonClass
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.abs
import kotlin.math.roundToInt

private val Context.weatherDataStore: DataStore<Preferences> by preferencesDataStore(name = "weather")

enum class WeatherRefreshResult { SUCCESS, NO_LOCATION, FAILED }

/**
 * Real weather from Open-Meteo. The chosen location and the last successful forecast are kept in
 * the "weather" DataStore, so widgets and previews show the cached forecast (with its fetch time)
 * when offline. Only the worker and the app's foreground refresh call [refresh].
 */
class WeatherRepository(
    context: Context,
    private val api: OpenMeteoApi = OpenMeteoApi.instance
) {
    private val appContext = context.applicationContext
    private val store = appContext.weatherDataStore
    private val prefs = AppPreferencesRepository(appContext)
    private val cacheAdapter = OpenMeteoApi.moshi.adapter(CachedWeather::class.java)

    val locationFlow: Flow<WeatherLocation?> = store.data.map { p ->
        val name = p[LOCATION_NAME]
        val lat = p[LOCATION_LAT]
        val lon = p[LOCATION_LON]
        if (name == null || lat == null || lon == null) null
        else WeatherLocation(name, lat, lon, isDeviceLocation = p[LOCATION_IS_DEVICE] ?: false)
    }

    private val cacheFlow: Flow<CachedWeather?> = store.data.map { p ->
        p[CACHE_JSON]?.let { json -> runCatching { cacheAdapter.fromJson(json) }.getOrNull() }
    }

    val weatherState: Flow<WeatherInfo> = combine(
        locationFlow, cacheFlow, prefs.isCelsiusFlow, prefs.is24HourClockFlow
    ) { location, cache, isCelsius, is24Hour ->
        toWeatherInfo(location, cache, isCelsius, is24Hour, System.currentTimeMillis())
    }

    suspend fun currentWeather(): WeatherInfo = weatherState.first()

    suspend fun setLocation(location: WeatherLocation) {
        store.edit { p ->
            p[LOCATION_NAME] = location.name
            p[LOCATION_LAT] = location.latitude
            p[LOCATION_LON] = location.longitude
            p[LOCATION_IS_DEVICE] = location.isDeviceLocation
        }
    }

    /**
     * Takes a fresh device fix and saves it as the weather location. Returns null without a
     * permission or a fix. Returns true in the pair if the location moved enough to need new data.
     */
    suspend fun locateDevice(): Pair<WeatherLocation, Boolean>? {
        val fix = DeviceLocation.current(appContext) ?: return null
        val previous = locationFlow.first()
        val name = DeviceLocation.placeName(appContext, fix) ?: previous?.takeIf { it.isDeviceLocation }?.name ?: MY_LOCATION
        val location = WeatherLocation(name, fix.latitude, fix.longitude, isDeviceLocation = true)
        setLocation(location)
        val moved = previous == null || !sameSpot(previous.latitude, previous.longitude, fix.latitude, fix.longitude)
        return location to moved
    }

    /** Cities matching [query]. Throws on network errors so the UI can say so. */
    suspend fun searchCities(query: String): List<WeatherLocation> =
        api.searchCities(query.trim()).results.orEmpty().map { result ->
            // "Tokyo, Japan", not "Tokyo, Tokyo, Japan".
            val region = listOfNotNull(result.admin1?.takeIf { it.isNotBlank() && it != result.name }, result.country)
                .joinToString(", ")
            WeatherLocation(
                name = if (region.isEmpty()) result.name else "${result.name}, $region",
                latitude = result.latitude,
                longitude = result.longitude,
                isDeviceLocation = false
            )
        }

    /** True if there is no forecast for the current location, or it is older than [maxAgeMillis]. */
    suspend fun isStale(maxAgeMillis: Long): Boolean {
        val location = locationFlow.first() ?: return false
        val cache = cacheFlow.first() ?: return true
        if (!cache.isFor(location)) return true
        return System.currentTimeMillis() - cache.fetchedAtMillis > maxAgeMillis
    }

    /** Fetches and caches the forecast for the saved location. Keeps the old cache on failure. */
    suspend fun refresh(): WeatherRefreshResult {
        val location = locationFlow.first() ?: return WeatherRefreshResult.NO_LOCATION
        return try {
            val response = api.forecast(location.latitude, location.longitude)
            val cache = response.toCache(location, System.currentTimeMillis()) ?: return WeatherRefreshResult.FAILED
            store.edit { it[CACHE_JSON] = cacheAdapter.toJson(cache) }
            WeatherRefreshResult.SUCCESS
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            // IOException (offline), HttpException, or malformed JSON: keep showing the cache.
            WeatherRefreshResult.FAILED
        }
    }

    companion object {
        private val LOCATION_NAME = stringPreferencesKey("location_name")
        private val LOCATION_LAT = doublePreferencesKey("location_lat")
        private val LOCATION_LON = doublePreferencesKey("location_lon")
        private val LOCATION_IS_DEVICE = booleanPreferencesKey("location_is_device")
        private val CACHE_JSON = stringPreferencesKey("cache_json")

        const val MY_LOCATION = "My location"

        /** About 1 km. Closer than this, a new device fix doesn't need a new forecast. */
        private const val SAME_SPOT_DEGREES = 0.01

        private const val HOURLY_STEP = 3
        private const val HOURLY_COUNT = 4

        private fun sameSpot(lat1: Double, lon1: Double, lat2: Double, lon2: Double) =
            abs(lat1 - lat2) < SAME_SPOT_DEGREES && abs(lon1 - lon2) < SAME_SPOT_DEGREES

        private fun CachedWeather.isFor(location: WeatherLocation) =
            sameSpot(latitude, longitude, location.latitude, location.longitude)

        fun toDisplayTemp(celsius: Double, isCelsius: Boolean): Int =
            (if (isCelsius) celsius else celsius * 9 / 5 + 32).roundToInt()

        internal fun toWeatherInfo(
            location: WeatherLocation?,
            cache: CachedWeather?,
            isCelsius: Boolean,
            is24Hour: Boolean,
            nowMillis: Long
        ): WeatherInfo {
            val unit = if (isCelsius) "C" else "F"
            if (location == null) return WeatherInfo(status = WeatherStatus.NO_LOCATION, unitSymbol = unit)
            if (cache == null || !cache.isFor(location)) {
                return WeatherInfo(status = WeatherStatus.NO_DATA, cityName = location.name, unitSymbol = unit)
            }

            val hourFormat = SimpleDateFormat(if (is24Hour) "HH:00" else "h a", Locale.getDefault())
            val hourly = cache.hourly
                .filter { it.epochSeconds * 1000 >= nowMillis - 3_600_000 } // from the current hour on
                .filterIndexed { index, _ -> index % HOURLY_STEP == 0 }
                .take(HOURLY_COUNT)
                .map { hour ->
                    HourlyForecast(
                        timeLabel = hourFormat.format(Date(hour.epochSeconds * 1000)).uppercase(),
                        temp = toDisplayTemp(hour.temperatureC, isCelsius),
                        condition = WeatherCondition.fromWmoCode(hour.weatherCode, hour.isDay)
                    )
                }

            return WeatherInfo(
                status = WeatherStatus.READY,
                cityName = location.name,
                temperature = toDisplayTemp(cache.temperatureC, isCelsius),
                unitSymbol = unit,
                condition = WeatherCondition.fromWmoCode(cache.weatherCode, cache.isDay),
                high = cache.highC?.let { toDisplayTemp(it, isCelsius) },
                low = cache.lowC?.let { toDisplayTemp(it, isCelsius) },
                humidityPercent = cache.humidityPercent,
                windSpeedKmh = cache.windSpeedKmh?.roundToInt(),
                uvIndex = cache.uvIndex?.roundToInt(),
                precipitationChancePercent = cache.precipitationChancePercent,
                forecastHourly = hourly,
                updatedAtMillis = cache.fetchedAtMillis
            )
        }

        internal fun ForecastResponse.toCache(location: WeatherLocation, nowMillis: Long): CachedWeather? {
            val now = current ?: return null
            val temperature = now.temperature ?: return null
            val hours = hourly?.let { h ->
                h.time.indices.mapNotNull { i ->
                    val temp = h.temperature.getOrNull(i) ?: return@mapNotNull null
                    val code = h.weatherCode.getOrNull(i) ?: return@mapNotNull null
                    CachedHour(h.time[i], temp, code.toInt(), (h.isDay.getOrNull(i) ?: 1.0) > 0)
                }
            }.orEmpty()
            return CachedWeather(
                fetchedAtMillis = nowMillis,
                latitude = location.latitude,
                longitude = location.longitude,
                temperatureC = temperature,
                weatherCode = now.weatherCode?.toInt() ?: 3,
                isDay = (now.isDay ?: 1.0) > 0,
                highC = daily?.temperatureMax?.firstOrNull(),
                lowC = daily?.temperatureMin?.firstOrNull(),
                humidityPercent = now.humidity?.roundToInt(),
                windSpeedKmh = now.windSpeed,
                uvIndex = daily?.uvIndexMax?.firstOrNull(),
                precipitationChancePercent = daily?.precipitationChanceMax?.firstOrNull()?.roundToInt(),
                hourly = hours
            )
        }
    }
}

/** The last successful forecast, stored as JSON. Temperatures in °C. */
@JsonClass(generateAdapter = true)
data class CachedWeather(
    val fetchedAtMillis: Long,
    val latitude: Double,
    val longitude: Double,
    val temperatureC: Double,
    val weatherCode: Int,
    val isDay: Boolean,
    val highC: Double?,
    val lowC: Double?,
    val humidityPercent: Int?,
    val windSpeedKmh: Double?,
    val uvIndex: Double?,
    val precipitationChancePercent: Int?,
    val hourly: List<CachedHour>
)

@JsonClass(generateAdapter = true)
data class CachedHour(
    val epochSeconds: Long,
    val temperatureC: Double,
    val weatherCode: Int,
    val isDay: Boolean
)
