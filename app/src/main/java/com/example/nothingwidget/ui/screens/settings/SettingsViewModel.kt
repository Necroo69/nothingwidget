package com.example.nothingwidget.ui.screens.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.nothingwidget.data.repository.AppPreferencesRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class SettingsUiState(
    val themeMode: String = "SYSTEM",
    val isHapticFeedbackEnabled: Boolean = true,
    val is24HourClock: Boolean = true,
    val isCelsius: Boolean = true,
    val widgetUpdateIntervalMinutes: Int = 15,
    val selectedCity: String = "London",
    val useGpsLocation: Boolean = false
)

class SettingsViewModel(
    private val appPreferencesRepository: AppPreferencesRepository
) : ViewModel() {

    private val _state = MutableStateFlow(SettingsUiState())
    val state: StateFlow<SettingsUiState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            appPreferencesRepository.themeModeFlow.collect { mode ->
                _state.value = _state.value.copy(themeMode = mode)
            }
        }
        viewModelScope.launch {
            appPreferencesRepository.isHapticFeedbackFlow.collect { isHaptic ->
                _state.value = _state.value.copy(isHapticFeedbackEnabled = isHaptic)
            }
        }
        viewModelScope.launch {
            appPreferencesRepository.is24HourClockFlow.collect { is24h ->
                _state.value = _state.value.copy(is24HourClock = is24h)
            }
        }
        viewModelScope.launch {
            appPreferencesRepository.isCelsiusFlow.collect { isCelsius ->
                _state.value = _state.value.copy(isCelsius = isCelsius)
            }
        }
        viewModelScope.launch {
            appPreferencesRepository.selectedCityFlow.collect { city ->
                _state.value = _state.value.copy(selectedCity = city)
            }
        }
        viewModelScope.launch {
            appPreferencesRepository.useGpsLocationFlow.collect { useGps ->
                _state.value = _state.value.copy(useGpsLocation = useGps)
            }
        }
    }

    fun setThemeMode(mode: String) {
        viewModelScope.launch {
            appPreferencesRepository.setThemeMode(mode)
        }
    }

    fun toggleHaptics() {
        viewModelScope.launch {
            appPreferencesRepository.setHapticFeedback(!_state.value.isHapticFeedbackEnabled)
        }
    }

    fun toggle24h() {
        viewModelScope.launch {
            appPreferencesRepository.set24HourClock(!_state.value.is24HourClock)
        }
    }

    fun toggleCelsius() {
        viewModelScope.launch {
            appPreferencesRepository.setCelsius(!_state.value.isCelsius)
        }
    }

    fun setUpdateInterval(mins: Int) {
        _state.value = _state.value.copy(widgetUpdateIntervalMinutes = mins)
    }

    fun setSelectedCity(city: String) {
        viewModelScope.launch {
            appPreferencesRepository.setSelectedCity(city)
        }
    }

    fun setUseGpsLocation(useGps: Boolean) {
        viewModelScope.launch {
            appPreferencesRepository.setUseGpsLocation(useGps)
        }
    }
}
