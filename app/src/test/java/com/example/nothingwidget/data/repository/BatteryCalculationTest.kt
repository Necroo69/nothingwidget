package com.example.nothingwidget.data.repository

import org.junit.Assert.assertEquals
import org.junit.Test

class BatteryCalculationTest {

    @Test
    fun batteryPercentage_halfCharge_returns50() {
        assertEquals(50, BatteryRepository.batteryPercentage(level = 50, scale = 100))
    }

    @Test
    fun batteryPercentage_empty_returns0() {
        assertEquals(0, BatteryRepository.batteryPercentage(level = 0, scale = 100))
    }

    @Test
    fun batteryPercentage_full_returns100() {
        assertEquals(100, BatteryRepository.batteryPercentage(level = 100, scale = 100))
    }

    @Test
    fun batteryPercentage_nonHundredScale_isNormalized() {
        // Some devices report on a 0..255 scale.
        assertEquals(50, BatteryRepository.batteryPercentage(level = 128, scale = 255))
    }

    @Test
    fun batteryPercentage_unavailableLevel_fallsBackTo100() {
        assertEquals(100, BatteryRepository.batteryPercentage(level = -1, scale = -1))
    }

    @Test
    fun batteryPercentage_zeroScale_doesNotDivideByZero() {
        assertEquals(100, BatteryRepository.batteryPercentage(level = 50, scale = 0))
    }
}
