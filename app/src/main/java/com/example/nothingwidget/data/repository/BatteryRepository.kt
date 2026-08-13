package com.example.nothingwidget.data.repository

import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import com.example.nothingwidget.domain.model.BatteryInfo
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class BatteryRepository(private val context: Context) {
    private val _batteryState = MutableStateFlow(getBatteryInfoSync(context))
    val batteryState: Flow<BatteryInfo> = _batteryState.asStateFlow()

    fun updateBatteryState() {
        _batteryState.value = getBatteryInfoSync(context)
    }

    companion object {
        /**
         * Converts a raw battery [level]/[scale] pair (as reported by
         * [BatteryManager]) into a 0-100 percentage. Returns 100 as a safe
         * fallback when the values are unavailable or invalid (negative level
         * or non-positive scale, which would otherwise divide by zero).
         */
        fun batteryPercentage(level: Int, scale: Int): Int {
            return if (level >= 0 && scale > 0) {
                (level * 100 / scale.toFloat()).toInt()
            } else {
                100
            }
        }

        fun getBatteryInfoSync(context: Context): BatteryInfo {
            val batteryIntent = context.applicationContext.registerReceiver(null, IntentFilter(Intent.ACTION_BATTERY_CHANGED))
            val level = batteryIntent?.getIntExtra(BatteryManager.EXTRA_LEVEL, -1) ?: -1
            val scale = batteryIntent?.getIntExtra(BatteryManager.EXTRA_SCALE, -1) ?: -1
            val status = batteryIntent?.getIntExtra(BatteryManager.EXTRA_STATUS, -1) ?: -1

            val batteryPct = batteryPercentage(level, scale)

            val isCharging = status == BatteryManager.BATTERY_STATUS_CHARGING || status == BatteryManager.BATTERY_STATUS_FULL

            return BatteryInfo(
                percentage = batteryPct,
                isCharging = isCharging,
                isPowerSaveMode = false, // Could be queried via PowerManager if needed
                estimatedHoursRemaining = 24, // Mock
                temperatureCelsius = 30.0f, // Mock
                healthStatus = "Good",
                connectedDevices = emptyList()
            )
        }
    }
}
