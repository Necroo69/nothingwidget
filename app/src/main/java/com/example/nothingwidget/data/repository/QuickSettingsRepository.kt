package com.example.nothingwidget.data.repository

import com.example.nothingwidget.domain.model.QuickToggleState
import com.example.nothingwidget.domain.model.RingerMode
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class QuickSettingsRepository {
    private val _toggleState = MutableStateFlow(QuickToggleState())
    val toggleState: Flow<QuickToggleState> = _toggleState.asStateFlow()

    fun toggleWifi() {
        val curr = _toggleState.value
        _toggleState.value = curr.copy(isWifiOn = !curr.isWifiOn)
    }

    fun toggleBluetooth() {
        val curr = _toggleState.value
        _toggleState.value = curr.copy(isBluetoothOn = !curr.isBluetoothOn)
    }

    fun toggleFlashlight() {
        val curr = _toggleState.value
        _toggleState.value = curr.copy(isFlashlightOn = !curr.isFlashlightOn)
    }

    fun toggleDnd() {
        val curr = _toggleState.value
        _toggleState.value = curr.copy(isDoNotDisturbOn = !curr.isDoNotDisturbOn)
    }

    fun toggleAirplaneMode() {
        val curr = _toggleState.value
        _toggleState.value = curr.copy(isAirplaneModeOn = !curr.isAirplaneModeOn)
    }

    fun cycleRingerMode() {
        val curr = _toggleState.value
        val nextMode = when (curr.ringerMode) {
            RingerMode.SOUND -> RingerMode.VIBRATE
            RingerMode.VIBRATE -> RingerMode.SILENT
            RingerMode.SILENT -> RingerMode.SOUND
        }
        _toggleState.value = curr.copy(ringerMode = nextMode)
    }
}
