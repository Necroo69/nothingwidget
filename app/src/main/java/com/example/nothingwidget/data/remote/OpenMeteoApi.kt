package com.example.nothingwidget.data.remote

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import com.squareup.moshi.Moshi
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query

/**
 * Open-Meteo forecast and geocoding APIs (no API key). The free tier is for non-commercial use
 * only; a paid release needs an Open-Meteo subscription (see docs/roadmap.md P11).
 */
interface OpenMeteoApi {

    @GET("v1/forecast")
    suspend fun forecast(
        @Query("latitude") latitude: Double,
        @Query("longitude") longitude: Double,
        @Query("current") current: String = "temperature_2m,relative_humidity_2m,is_day,weather_code,wind_speed_10m",
        @Query("hourly") hourly: String = "temperature_2m,weather_code,is_day",
        @Query("daily") daily: String = "temperature_2m_max,temperature_2m_min,uv_index_max,precipitation_probability_max",
        // Temperatures are always fetched in °C and converted for display, so the unit setting
        // applies instantly without a new request.
        @Query("timezone") timezone: String = "auto",
        @Query("timeformat") timeFormat: String = "unixtime",
        @Query("forecast_days") forecastDays: Int = 2
    ): ForecastResponse

    @GET("https://geocoding-api.open-meteo.com/v1/search")
    suspend fun searchCities(
        @Query("name") name: String,
        @Query("count") count: Int = 8,
        @Query("language") language: String = "en",
        @Query("format") format: String = "json"
    ): GeocodingResponse

    companion object {
        val moshi: Moshi = Moshi.Builder().build()

        val instance: OpenMeteoApi by lazy {
            Retrofit.Builder()
                .baseUrl("https://api.open-meteo.com/")
                .addConverterFactory(MoshiConverterFactory.create(moshi))
                .build()
                .create(OpenMeteoApi::class.java)
        }
    }
}

// Numbers are read as Double throughout: Open-Meteo may send "62" or "62.0" for the same field.

@JsonClass(generateAdapter = true)
data class ForecastResponse(
    val current: CurrentDto?,
    val hourly: HourlyDto?,
    val daily: DailyDto?
)

@JsonClass(generateAdapter = true)
data class CurrentDto(
    val time: Long,
    @Json(name = "temperature_2m") val temperature: Double?,
    @Json(name = "relative_humidity_2m") val humidity: Double?,
    @Json(name = "is_day") val isDay: Double?,
    @Json(name = "weather_code") val weatherCode: Double?,
    @Json(name = "wind_speed_10m") val windSpeed: Double?
)

@JsonClass(generateAdapter = true)
data class HourlyDto(
    val time: List<Long>,
    @Json(name = "temperature_2m") val temperature: List<Double?>,
    @Json(name = "weather_code") val weatherCode: List<Double?>,
    @Json(name = "is_day") val isDay: List<Double?>
)

@JsonClass(generateAdapter = true)
data class DailyDto(
    @Json(name = "temperature_2m_max") val temperatureMax: List<Double?>,
    @Json(name = "temperature_2m_min") val temperatureMin: List<Double?>,
    @Json(name = "uv_index_max") val uvIndexMax: List<Double?>?,
    @Json(name = "precipitation_probability_max") val precipitationChanceMax: List<Double?>?
)

@JsonClass(generateAdapter = true)
data class GeocodingResponse(val results: List<GeocodingResult>?)

@JsonClass(generateAdapter = true)
data class GeocodingResult(
    val name: String,
    val latitude: Double,
    val longitude: Double,
    val country: String?,
    val admin1: String?
)
