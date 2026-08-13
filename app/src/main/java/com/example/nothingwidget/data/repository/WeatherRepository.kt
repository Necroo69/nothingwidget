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
            condition = WeatherCondition.fromWmoCode(code)
        )
    }

    suspend fun fetchWeather(lat: Double, lon: Double): WeatherResponse {
        return api.getCurrentWeather(latitude = lat, longitude = lon)
    }
}
