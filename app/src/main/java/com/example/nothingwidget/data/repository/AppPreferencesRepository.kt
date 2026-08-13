package com.example.nothingwidget.data.repository

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.doublePreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

class AppPreferencesRepository(private val context: Context) {

    private val THEME_MODE = stringPreferencesKey("theme_mode")
    private val IS_HAPTIC_FEEDBACK = booleanPreferencesKey("is_haptic_feedback")
    private val IS_24_HOUR_CLOCK = booleanPreferencesKey("is_24_hour_clock")
    private val IS_CELSIUS = booleanPreferencesKey("is_celsius")
    private val SELECTED_CITY = stringPreferencesKey("selected_city")
    private val IS_ONBOARDING_COMPLETED = booleanPreferencesKey("is_onboarding_completed")
    private val USE_GPS_LOCATION = booleanPreferencesKey("use_gps_location")
    private val LAST_KNOWN_LAT = doublePreferencesKey("last_known_lat")
    private val LAST_KNOWN_LON = doublePreferencesKey("last_known_lon")
    private val DETECTED_CITY_NAME = stringPreferencesKey("detected_city_name")
    private val LAST_WEATHER_TEMP = doublePreferencesKey("last_weather_temp")
    private val LAST_WEATHER_CODE = androidx.datastore.preferences.core.intPreferencesKey("last_weather_code")

    val themeModeFlow: Flow<String> = context.dataStore.data.map { it[THEME_MODE] ?: "SYSTEM" }
    val isHapticFeedbackFlow: Flow<Boolean> = context.dataStore.data.map { it[IS_HAPTIC_FEEDBACK] ?: true }
    val is24HourClockFlow: Flow<Boolean> = context.dataStore.data.map { it[IS_24_HOUR_CLOCK] ?: true }
    val isCelsiusFlow: Flow<Boolean> = context.dataStore.data.map { it[IS_CELSIUS] ?: true }
    val selectedCityFlow: Flow<String> = context.dataStore.data.map { it[SELECTED_CITY] ?: "London" }
    val isOnboardingCompletedFlow: Flow<Boolean> = context.dataStore.data.map { it[IS_ONBOARDING_COMPLETED] ?: false }
    val useGpsLocationFlow: Flow<Boolean> = context.dataStore.data.map { it[USE_GPS_LOCATION] ?: false }
    val lastKnownLatFlow: Flow<Double> = context.dataStore.data.map { it[LAST_KNOWN_LAT] ?: 0.0 }
    val lastKnownLonFlow: Flow<Double> = context.dataStore.data.map { it[LAST_KNOWN_LON] ?: 0.0 }
    val detectedCityNameFlow: Flow<String> = context.dataStore.data.map { it[DETECTED_CITY_NAME] ?: "" }
    val lastWeatherTempFlow: Flow<Double> = context.dataStore.data.map { it[LAST_WEATHER_TEMP] ?: 0.0 }
    val lastWeatherCodeFlow: Flow<Int> = context.dataStore.data.map { it[LAST_WEATHER_CODE] ?: 0 }

    suspend fun setThemeMode(mode: String) = context.dataStore.edit { it[THEME_MODE] = mode }
    suspend fun setHapticFeedback(enabled: Boolean) = context.dataStore.edit { it[IS_HAPTIC_FEEDBACK] = enabled }
    suspend fun set24HourClock(enabled: Boolean) = context.dataStore.edit { it[IS_24_HOUR_CLOCK] = enabled }
    suspend fun setCelsius(enabled: Boolean) = context.dataStore.edit { it[IS_CELSIUS] = enabled }
    suspend fun setSelectedCity(city: String) = context.dataStore.edit { it[SELECTED_CITY] = city }
    suspend fun setOnboardingCompleted(completed: Boolean) = context.dataStore.edit { it[IS_ONBOARDING_COMPLETED] = completed }
    suspend fun setUseGpsLocation(useGps: Boolean) = context.dataStore.edit { it[USE_GPS_LOCATION] = useGps }
    suspend fun setLastKnownLocation(lat: Double, lon: Double) = context.dataStore.edit {
        it[LAST_KNOWN_LAT] = lat
        it[LAST_KNOWN_LON] = lon
    }
    suspend fun setDetectedCityName(city: String) = context.dataStore.edit { it[DETECTED_CITY_NAME] = city }
    suspend fun setCachedWeather(temp: Double, code: Int) = context.dataStore.edit {
        it[LAST_WEATHER_TEMP] = temp
        it[LAST_WEATHER_CODE] = code
    }
}
