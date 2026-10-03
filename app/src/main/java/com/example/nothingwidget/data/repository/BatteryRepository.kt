package com.example.nothingwidget.data.repository

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import androidx.core.content.ContextCompat
import com.example.nothingwidget.domain.model.BatteryInfo
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.distinctUntilChanged

class BatteryRepository(private val context: Context) {

    // Live battery state while collected. ACTION_BATTERY_CHANGED is sticky, so the receiver
    // gets the current value as soon as it registers, then every change until collection stops.
    val batteryState: Flow<BatteryInfo> = callbackFlow {
        val receiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {
                trySend(batteryInfoFrom(intent))
            }
        }
        val appContext = context.applicationContext
        ContextCompat.registerReceiver(
            appContext,
            receiver,
            IntentFilter(Intent.ACTION_BATTERY_CHANGED),
            ContextCompat.RECEIVER_NOT_EXPORTED
        )
        awaitClose { appContext.unregisterReceiver(receiver) }
    }.distinctUntilChanged() // the broadcast also fires for voltage/temperature-only changes

    /** One-off read, e.g. as the initial value before [batteryState] emits. */
    fun currentBatteryInfo(): BatteryInfo = getBatteryInfoSync(context)

    companion object {
        fun getBatteryInfoSync(context: Context): BatteryInfo {
            val batteryIntent = context.applicationContext.registerReceiver(null, IntentFilter(Intent.ACTION_BATTERY_CHANGED))
            return batteryInfoFrom(batteryIntent)
        }

        private fun batteryInfoFrom(batteryIntent: Intent?): BatteryInfo {
            val level = batteryIntent?.getIntExtra(BatteryManager.EXTRA_LEVEL, -1) ?: -1
            val scale = batteryIntent?.getIntExtra(BatteryManager.EXTRA_SCALE, -1) ?: -1
            val status = batteryIntent?.getIntExtra(BatteryManager.EXTRA_STATUS, -1) ?: -1

            val batteryPct = if (level != -1 && scale != -1) {
                (level * 100 / scale.toFloat()).toInt()
            } else {
                100
            }

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
