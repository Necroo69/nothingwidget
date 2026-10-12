package com.example.nothingwidget.ui.screens.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.nothingwidget.data.repository.AppPreferencesRepository
import com.example.nothingwidget.data.repository.WeatherRepository
import com.example.nothingwidget.domain.model.WeatherInfo
import com.example.nothingwidget.domain.model.WeatherLocation
import com.example.nothingwidget.domain.model.WidgetType
import com.example.nothingwidget.widgets.WidgetEngine
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class SettingsUiState(
    val isDarkTheme: Boolean = true,
    val is24HourClock: Boolean = true,
    val isCelsius: Boolean = true,
    val weatherRefreshMinutes: Int = AppPreferencesRepository.DEFAULT_WEATHER_REFRESH_MINUTES,
    val weatherLocation: WeatherLocation? = null,
    val weather: WeatherInfo = WeatherInfo(),
    val cityQuery: String = "",
    val cityResults: List<WeatherLocation> = emptyList(),
    val isSearching: Boolean = false,
    val isLocating: Boolean = false,
    /** A problem with the last location action, shown under the picker. */
    val locationMessage: String? = null,
    /** Confirmation for the last one-shot action (e.g. refresh all). */
    val actionMessage: String? = null
)

class SettingsViewModel(
    private val appPreferencesRepository: AppPreferencesRepository,
    private val weatherRepository: WeatherRepository,
    private val widgetEngine: WidgetEngine
) : ViewModel() {

    private val _state = MutableStateFlow(SettingsUiState())
    val state: StateFlow<SettingsUiState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            appPreferencesRepository.isDarkThemeFlow.collect { v -> _state.update { it.copy(isDarkTheme = v) } }
        }
        viewModelScope.launch {
            appPreferencesRepository.is24HourClockFlow.collect { v -> _state.update { it.copy(is24HourClock = v) } }
        }
        viewModelScope.launch {
            appPreferencesRepository.isCelsiusFlow.collect { v -> _state.update { it.copy(isCelsius = v) } }
        }
        viewModelScope.launch {
            appPreferencesRepository.weatherRefreshMinutesFlow.collect { v -> _state.update { it.copy(weatherRefreshMinutes = v) } }
        }
        viewModelScope.launch {
            weatherRepository.locationFlow.collect { v -> _state.update { it.copy(weatherLocation = v) } }
        }
        viewModelScope.launch {
            weatherRepository.weatherState.collect { v -> _state.update { it.copy(weather = v) } }
        }
    }

    fun toggleTheme() {
        viewModelScope.launch { appPreferencesRepository.setDarkTheme(!_state.value.isDarkTheme) }
    }

    fun toggle24h() {
        viewModelScope.launch {
            appPreferencesRepository.set24HourClock(!_state.value.is24HourClock)
            widgetEngine.redraw(WidgetType.DIGITAL_CLOCK)
        }
    }

    fun toggleCelsius() {
        viewModelScope.launch {
            appPreferencesRepository.setCelsius(!_state.value.isCelsius)
            // Forecasts are cached in °C, so this is a redraw, not a new request.
            widgetEngine.redraw(WidgetType.WEATHER)
        }
    }

    fun setWeatherRefreshMinutes(minutes: Int) {
        viewModelScope.launch {
            appPreferencesRepository.setWeatherRefreshMinutes(minutes)
            widgetEngine.rescheduleWeather()
        }
    }

    fun refreshAllWidgets() {
        widgetEngine.refreshAll()
        _state.update { it.copy(actionMessage = "Refreshing all widgets…") }
    }

    fun updateCityQuery(query: String) {
        _state.update { it.copy(cityQuery = query) }
    }

    fun searchCities() {
        val query = _state.value.cityQuery.trim()
        if (query.length < 2) {
            _state.update { it.copy(locationMessage = "Type at least two letters.") }
            return
        }
        _state.update { it.copy(isSearching = true, locationMessage = null, cityResults = emptyList()) }
        viewModelScope.launch {
            try {
                val results = weatherRepository.searchCities(query)
                _state.update {
                    it.copy(
                        cityResults = results,
                        locationMessage = if (results.isEmpty()) "No places found for \"$query\"." else null
                    )
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _state.update { it.copy(locationMessage = "Couldn't search. Check your connection and try again.") }
            } finally {
                _state.update { it.copy(isSearching = false) }
            }
        }
    }

    fun pickCity(location: WeatherLocation) {
        viewModelScope.launch {
            weatherRepository.setLocation(location)
            _state.update { it.copy(cityResults = emptyList(), cityQuery = "", locationMessage = null) }
            widgetEngine.refreshWeather()
        }
    }

    /** Call once ACCESS_COARSE_LOCATION is granted. */
    fun useDeviceLocation() {
        _state.update { it.copy(isLocating = true, locationMessage = null) }
        viewModelScope.launch {
            val located = weatherRepository.locateDevice()
            _state.update {
                it.copy(
                    isLocating = false,
                    locationMessage = if (located == null) "Couldn't get your location. Turn on location, or search for a city." else null
                )
            }
            if (located != null) widgetEngine.refreshWeather()
        }
    }

    fun onLocationPermissionDenied() {
        _state.update { it.copy(locationMessage = "Location permission denied. Search for a city instead.") }
    }

    fun completeOnboarding() {
        viewModelScope.launch { appPreferencesRepository.setCompletedOnboarding() }
    }
}
