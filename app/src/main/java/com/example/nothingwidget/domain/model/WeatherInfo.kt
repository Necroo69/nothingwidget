package com.example.nothingwidget.domain.model

data class WeatherInfo(
    val cityName: String = "London",
    val temperatureC: Int? = null,
    val condition: WeatherCondition = WeatherCondition.PARTLY_CLOUDY,
    val highC: Int? = null,
    val lowC: Int? = null,
    val humidityPercent: Int? = null,
    val windSpeedKmh: Int? = null,
    val uvIndex: Int? = null,
    val precipitationChancePercent: Int = 10,
    val forecastHourly: List<HourlyForecast> = listOf(
        HourlyForecast("12 PM", null, WeatherCondition.SUNNY),
        HourlyForecast("3 PM", null, WeatherCondition.PARTLY_CLOUDY),
        HourlyForecast("6 PM", null, WeatherCondition.SUNNY),
        HourlyForecast("9 PM", null, WeatherCondition.CLEAR_NIGHT),
        HourlyForecast("12 AM", null, WeatherCondition.CLEAR_NIGHT)
    )
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
        /**
         * Maps an OpenMeteo WMO weather interpretation code to a [WeatherCondition].
         * Single source of truth shared by the repository and the widget provider.
         */
        fun fromWmoCode(code: Int): WeatherCondition = when (code) {
            0 -> SUNNY
            1, 2, 3 -> PARTLY_CLOUDY
            45, 48 -> FOGGY
            51, 53, 55, 56, 57 -> RAINY
            61, 63, 65, 66, 67 -> RAINY
            71, 73, 75, 77 -> SNOWY
            80, 81, 82 -> RAINY
            85, 86 -> SNOWY
            95, 96, 99 -> THUNDERSTORM
            else -> CLOUDY
        }
    }
}

data class HourlyForecast(
    val timeLabel: String,
    val tempC: Int?,
    val condition: WeatherCondition
)
