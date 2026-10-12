package com.necroo69.nothingwidget.data.repository

import com.necroo69.nothingwidget.data.remote.CurrentDto
import com.necroo69.nothingwidget.data.remote.DailyDto
import com.necroo69.nothingwidget.data.remote.ForecastResponse
import com.necroo69.nothingwidget.data.remote.HourlyDto
import com.necroo69.nothingwidget.data.repository.WeatherRepository.Companion.toCache
import com.necroo69.nothingwidget.domain.model.WeatherCondition
import com.necroo69.nothingwidget.domain.model.WeatherLocation
import com.necroo69.nothingwidget.domain.model.WeatherStatus
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class WeatherMappingTest {

    private val berlin = WeatherLocation("Berlin", 52.52, 13.405, isDeviceLocation = true)
    private val now = 1_760_000_000_000L // fixed clock

    private fun response(currentTemp: Double? = 14.4) = ForecastResponse(
        current = CurrentDto(time = now / 1000, temperature = currentTemp, humidity = 62.0, isDay = 1.0, weatherCode = 2.0, windSpeed = 11.6),
        hourly = HourlyDto(
            time = (0 until 12).map { now / 1000 + it * 3600L },
            temperature = (0 until 12).map { 10.0 + it },
            weatherCode = (0 until 12).map { 0.0 },
            isDay = (0 until 12).map { 0.0 }
        ),
        daily = DailyDto(listOf(20.2), listOf(7.6), listOf(4.4), listOf(15.0))
    )

    @Test
    fun `no location means the widget asks for one`() {
        val info = WeatherRepository.toWeatherInfo(null, null, isCelsius = true, is24Hour = true, nowMillis = now)
        assertEquals(WeatherStatus.NO_LOCATION, info.status)
    }

    @Test
    fun `location without a forecast is waiting for data`() {
        val info = WeatherRepository.toWeatherInfo(berlin, null, isCelsius = true, is24Hour = true, nowMillis = now)
        assertEquals(WeatherStatus.NO_DATA, info.status)
        assertEquals("Berlin", info.cityName)
    }

    @Test
    fun `a forecast for another place is not shown`() {
        val tokyoCache = response().toCache(WeatherLocation("Tokyo", 35.69, 139.69, false), now)
        val info = WeatherRepository.toWeatherInfo(berlin, tokyoCache, isCelsius = true, is24Hour = true, nowMillis = now)
        assertEquals(WeatherStatus.NO_DATA, info.status)
    }

    @Test
    fun `celsius forecast maps to rounded display values`() {
        val cache = response().toCache(berlin, now)
        val info = WeatherRepository.toWeatherInfo(berlin, cache, isCelsius = true, is24Hour = true, nowMillis = now)
        assertEquals(WeatherStatus.READY, info.status)
        assertEquals(14, info.temperature)
        assertEquals(20, info.high)
        assertEquals(8, info.low)
        assertEquals("C", info.unitSymbol)
        assertEquals(WeatherCondition.PARTLY_CLOUDY, info.condition)
        assertEquals(now, info.updatedAtMillis)
    }

    @Test
    fun `fahrenheit converts from the cached celsius values`() {
        val cache = response().toCache(berlin, now)
        val info = WeatherRepository.toWeatherInfo(berlin, cache, isCelsius = false, is24Hour = true, nowMillis = now)
        assertEquals(58, info.temperature) // 14.4 °C
        assertEquals(68, info.high) // 20.2 °C
        assertEquals("F", info.unitSymbol)
    }

    @Test
    fun `hourly forecast takes every third hour from now`() {
        val cache = response().toCache(berlin, now)
        val info = WeatherRepository.toWeatherInfo(berlin, cache, isCelsius = true, is24Hour = true, nowMillis = now)
        assertEquals(listOf(10, 13, 16, 19), info.forecastHourly.map { it.temp })
        assertEquals(WeatherCondition.CLEAR_NIGHT, info.forecastHourly.first().condition)
    }

    @Test
    fun `a response without a current temperature is rejected`() {
        assertNull(response(currentTemp = null).toCache(berlin, now))
    }

    @Test
    fun `wmo codes map to conditions`() {
        assertEquals(WeatherCondition.SUNNY, WeatherCondition.fromWmoCode(0, isDay = true))
        assertEquals(WeatherCondition.CLEAR_NIGHT, WeatherCondition.fromWmoCode(0, isDay = false))
        assertEquals(WeatherCondition.CLOUDY, WeatherCondition.fromWmoCode(3, isDay = true))
        assertEquals(WeatherCondition.FOGGY, WeatherCondition.fromWmoCode(45, isDay = true))
        assertEquals(WeatherCondition.RAINY, WeatherCondition.fromWmoCode(61, isDay = true))
        assertEquals(WeatherCondition.RAINY, WeatherCondition.fromWmoCode(81, isDay = true))
        assertEquals(WeatherCondition.SNOWY, WeatherCondition.fromWmoCode(73, isDay = true))
        assertEquals(WeatherCondition.THUNDERSTORM, WeatherCondition.fromWmoCode(95, isDay = true))
    }
}
