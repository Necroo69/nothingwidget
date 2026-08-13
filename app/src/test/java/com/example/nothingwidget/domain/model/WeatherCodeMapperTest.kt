package com.example.nothingwidget.domain.model

import org.junit.Assert.assertEquals
import org.junit.Test

class WeatherCodeMapperTest {

    @Test
    fun code0_isSunny() {
        assertEquals(WeatherCondition.SUNNY, WeatherCondition.fromWmoCode(0))
    }

    @Test
    fun cloudDevelopmentCodes_arePartlyCloudy() {
        listOf(1, 2, 3).forEach {
            assertEquals(WeatherCondition.PARTLY_CLOUDY, WeatherCondition.fromWmoCode(it))
        }
    }

    @Test
    fun fogCodes_areFoggy() {
        listOf(45, 48).forEach {
            assertEquals(WeatherCondition.FOGGY, WeatherCondition.fromWmoCode(it))
        }
    }

    @Test
    fun drizzleRainAndShowerCodes_areRainy() {
        listOf(51, 55, 61, 65, 66, 80, 82).forEach {
            assertEquals("code $it", WeatherCondition.RAINY, WeatherCondition.fromWmoCode(it))
        }
    }

    @Test
    fun snowCodes_areSnowy() {
        listOf(71, 75, 77, 85, 86).forEach {
            assertEquals("code $it", WeatherCondition.SNOWY, WeatherCondition.fromWmoCode(it))
        }
    }

    @Test
    fun thunderstormCodes_areThunderstorm() {
        listOf(95, 96, 99).forEach {
            assertEquals("code $it", WeatherCondition.THUNDERSTORM, WeatherCondition.fromWmoCode(it))
        }
    }

    @Test
    fun unknownCode_defaultsToCloudy() {
        assertEquals(WeatherCondition.CLOUDY, WeatherCondition.fromWmoCode(12345))
    }
}
