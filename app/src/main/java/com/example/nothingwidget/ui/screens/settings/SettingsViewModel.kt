package com.example.nothingwidget.ui.screens.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.nothingwidget.data.repository.AppPreferencesRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class SettingsUiState(
    val isDarkTheme: Boolean = true,
    val isHapticFeedbackEnabled: Boolean = true,
    val is24HourClock: Boolean = true,
    val isCelsius: Boolean = true,
    val widgetUpdateIntervalMinutes: Int = 15,
    val selectedCity: String = "London"
)

class SettingsViewModel(
    private val appPreferencesRepository: AppPreferencesRepository,
    // Redraws placed clock widgets; called after the 12/24-hour setting has been saved.
    private val refreshClockWidgets: () -> Unit = {}
) : ViewModel() {

    private val _state = MutableStateFlow(SettingsUiState())
    val state: StateFlow<SettingsUiState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            appPreferencesRepository.isDarkThemeFlow.collect { isDark ->
                _state.value = _state.value.copy(isDarkTheme = isDark)
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
    }

    fun toggleTheme() {
        viewModelScope.launch {
            appPreferencesRepository.setDarkTheme(!_state.value.isDarkTheme)
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
            refreshClockWidgets()
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
}
