package com.necroo69.nothingwidget.domain.model

enum class WeatherStatus {
    /** Real data for the chosen location. */
    READY,
    /** No location chosen yet (or location permission was denied before one was found). */
    NO_LOCATION,
    /** A location is set, but nothing has been fetched for it yet (first fetch pending or offline). */
    NO_DATA
}

/** Temperatures are already converted to the user's unit; [unitSymbol] says which. */
data class WeatherInfo(
    val status: WeatherStatus = WeatherStatus.NO_LOCATION,
    val cityName: String = "",
    val temperature: Int? = null,
    val unitSymbol: String = "C",
    val condition: WeatherCondition = WeatherCondition.PARTLY_CLOUDY,
    val high: Int? = null,
    val low: Int? = null,
    val humidityPercent: Int? = null,
    val windSpeedKmh: Int? = null,
    val uvIndex: Int? = null,
    val precipitationChancePercent: Int? = null,
    val forecastHourly: List<HourlyForecast> = emptyList(),
    /** When the shown data was fetched, or null if there is none. */
    val updatedAtMillis: Long? = null
)

/** Where weather is fetched for. Device locations are captured while the app is open. */
data class WeatherLocation(
    val name: String,
    val latitude: Double,
    val longitude: Double,
    val isDeviceLocation: Boolean
)

enum class WeatherCondition(val label: String, val dotSymbol: String) {
    SUNNY("Sunny", "☀"),
    CLEAR_NIGHT("Clear", "☽"),
    PARTLY_CLOUDY("Partly Cloudy", "⛅"),
    CLOUDY("Cloudy", "☁"),
    RAINY("Rain", "🌧"),
    THUNDERSTORM("Thunderstorm", "⚡"),
    SNOWY("Snow", "❄"),
    FOGGY("Fog", "≡");

    companion object {
        /** Maps a WMO weather interpretation code (as sent by Open-Meteo) to a condition. */
        fun fromWmoCode(code: Int, isDay: Boolean): WeatherCondition = when (code) {
            0 -> if (isDay) SUNNY else CLEAR_NIGHT
            1, 2 -> PARTLY_CLOUDY
            3 -> CLOUDY
            45, 48 -> FOGGY
            in 51..67, in 80..82 -> RAINY
            in 71..77, 85, 86 -> SNOWY
            in 95..99 -> THUNDERSTORM
            else -> CLOUDY
        }
    }
}

data class HourlyForecast(
    val timeLabel: String,
    val temp: Int?,
    val condition: WeatherCondition
)
