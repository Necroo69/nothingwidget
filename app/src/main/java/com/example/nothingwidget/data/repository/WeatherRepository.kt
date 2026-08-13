package com.example.nothingwidget.data.repository

import com.example.nothingwidget.data.remote.OpenMeteoApi
import com.example.nothingwidget.data.remote.WeatherResponse
import com.example.nothingwidget.domain.model.WeatherCondition
import com.example.nothingwidget.domain.model.WeatherInfo
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory

class WeatherRepository(
    private val appPrefsRepo: AppPreferencesRepository
) {
    private val api: OpenMeteoApi by lazy {
        Retrofit.Builder()
            .baseUrl("https://api.open-meteo.com/")
            .addConverterFactory(MoshiConverterFactory.create())
            .build()
            .create(OpenMeteoApi::class.java)
    }

    val weatherState: Flow<WeatherInfo> = combine(
        appPrefsRepo.useGpsLocationFlow,
        appPrefsRepo.selectedCityFlow,
        appPrefsRepo.detectedCityNameFlow,
        appPrefsRepo.lastWeatherTempFlow,
        appPrefsRepo.lastWeatherCodeFlow
    ) { useGps, selectedCity, detectedCity, temp, code ->
        val city = if (useGps && detectedCity.isNotBlank()) detectedCity else selectedCity
        WeatherInfo(
            cityName = city.ifBlank { "London" },
            temperatureC = temp.toInt(),
            condition = mapWeatherCodeToCondition(code)
        )
    }

    suspend fun fetchWeather(lat: Double, lon: Double): WeatherResponse {
        return api.getCurrentWeather(latitude = lat, longitude = lon)
    }
    
    private fun mapWeatherCodeToCondition(code: Int): WeatherCondition {
        return when (code) {
            0 -> WeatherCondition.SUNNY
            1, 2, 3 -> WeatherCondition.PARTLY_CLOUDY
            45, 48 -> WeatherCondition.FOGGY
            51, 53, 55, 56, 57 -> WeatherCondition.RAINY
            61, 63, 65, 66, 67 -> WeatherCondition.RAINY
            71, 73, 75, 77 -> WeatherCondition.SNOWY
            80, 81, 82 -> WeatherCondition.RAINY
            85, 86 -> WeatherCondition.SNOWY
            95, 96, 99 -> WeatherCondition.THUNDERSTORM
            else -> WeatherCondition.CLOUDY
        }
    }
}
