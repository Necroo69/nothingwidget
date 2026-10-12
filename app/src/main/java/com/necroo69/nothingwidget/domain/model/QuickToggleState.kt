package com.necroo69.nothingwidget.domain.model

data class QuickToggleState(
    val isWifiOn: Boolean = true,
    val isBluetoothOn: Boolean = true,
    val isFlashlightOn: Boolean = false,
    val isDoNotDisturbOn: Boolean = false,
    val isAirplaneModeOn: Boolean = false,
    val ringerMode: RingerMode = RingerMode.VIBRATE,
    val screenTimeoutSeconds: Int = 30
)

enum class RingerMode(val label: String) {
    SOUND("Sound"),
    VIBRATE("Vibrate"),
    SILENT("Silent")
}
