package com.necroo69.nothingwidget.domain.model

data class BatteryInfo(
    val percentage: Int = 84,
    val isCharging: Boolean = false,
    val isPowerSaveMode: Boolean = false,
    val estimatedHoursRemaining: Int = 18,
    val temperatureCelsius: Float = 31.5f,
    val healthStatus: String = "Good",
    val connectedDevices: List<ConnectedDevice> = listOf(
        ConnectedDevice("Ear (2)", 92, true),
        ConnectedDevice("Watch Pro", 68, false)
    )
)

data class ConnectedDevice(
    val name: String,
    val batteryPercentage: Int,
    val isConnected: Boolean
)
