package com.example.nothingwidget.data.repository

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

class AppPreferencesRepository(private val context: Context) {

    private val IS_DARK_THEME = booleanPreferencesKey("is_dark_theme")
    private val IS_24_HOUR_CLOCK = booleanPreferencesKey("is_24_hour_clock")
    private val IS_CELSIUS = booleanPreferencesKey("is_celsius")
    private val WEATHER_REFRESH_MINUTES = intPreferencesKey("weather_refresh_minutes")
    private val HAS_COMPLETED_ONBOARDING = booleanPreferencesKey("has_completed_onboarding")

    val isDarkThemeFlow: Flow<Boolean> = context.dataStore.data.map { it[IS_DARK_THEME] ?: true }
    val is24HourClockFlow: Flow<Boolean> = context.dataStore.data.map { it[IS_24_HOUR_CLOCK] ?: true }
    val isCelsiusFlow: Flow<Boolean> = context.dataStore.data.map { it[IS_CELSIUS] ?: true }
    val weatherRefreshMinutesFlow: Flow<Int> =
        context.dataStore.data.map { it[WEATHER_REFRESH_MINUTES] ?: DEFAULT_WEATHER_REFRESH_MINUTES }
    val hasCompletedOnboardingFlow: Flow<Boolean> = context.dataStore.data.map { it[HAS_COMPLETED_ONBOARDING] ?: false }

    suspend fun setDarkTheme(enabled: Boolean) = context.dataStore.edit { it[IS_DARK_THEME] = enabled }
    suspend fun set24HourClock(enabled: Boolean) = context.dataStore.edit { it[IS_24_HOUR_CLOCK] = enabled }
    suspend fun setCelsius(enabled: Boolean) = context.dataStore.edit { it[IS_CELSIUS] = enabled }
    suspend fun setWeatherRefreshMinutes(minutes: Int) = context.dataStore.edit { it[WEATHER_REFRESH_MINUTES] = minutes }
    suspend fun setCompletedOnboarding() = context.dataStore.edit { it[HAS_COMPLETED_ONBOARDING] = true }

    companion object {
        /** Choices offered in Settings. 15 min is WorkManager's minimum periodic interval. */
        val WEATHER_REFRESH_OPTIONS = listOf(15, 30, 60, 180)
        const val DEFAULT_WEATHER_REFRESH_MINUTES = 30
    }
}
