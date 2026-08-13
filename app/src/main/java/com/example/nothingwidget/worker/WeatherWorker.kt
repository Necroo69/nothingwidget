package com.example.nothingwidget.worker

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Geocoder
import android.location.Location
import androidx.core.content.ContextCompat
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.nothingwidget.data.repository.AppPreferencesRepository
import com.example.nothingwidget.data.repository.WeatherRepository
import com.example.nothingwidget.widgets.WeatherWidget
import com.google.android.gms.location.LocationServices
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.util.Locale

class WeatherWorker(
    private val context: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        try {
            val appPrefsRepo = AppPreferencesRepository(context)
            val weatherRepo = WeatherRepository(appPrefsRepo)
            val useGps = appPrefsRepo.useGpsLocationFlow.first()

            var targetLat = 0.0
            var targetLon = 0.0
            var detectedCity = ""

            if (useGps && ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)
                var location = fusedLocationClient.lastLocation.await()
                
                android.util.Log.d("WeatherWorker", "lastKnownLocation result: $location")

                if (location == null) {
                    try {
                        val request = com.google.android.gms.location.LocationRequest.Builder(com.google.android.gms.location.Priority.PRIORITY_BALANCED_POWER_ACCURACY, 1000)
                            .setMaxUpdates(1)
                            .build()
                        
                        location = kotlinx.coroutines.withTimeoutOrNull(5000) {
                            kotlinx.coroutines.suspendCancellableCoroutine { cont ->
                                val callback = object : com.google.android.gms.location.LocationCallback() {
                                    override fun onLocationResult(result: com.google.android.gms.location.LocationResult) {
                                        fusedLocationClient.removeLocationUpdates(this)
                                        if (cont.isActive) {
                                            cont.resume(result.lastLocation) {}
                                        }
                                    }
                                }
                                fusedLocationClient.requestLocationUpdates(request, callback, android.os.Looper.getMainLooper())
                                cont.invokeOnCancellation {
                                    fusedLocationClient.removeLocationUpdates(callback)
                                }
                            }
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                    android.util.Log.d("WeatherWorker", "Fresh location request result: $location")
                }
                
                val lastLat = appPrefsRepo.lastKnownLatFlow.first()
                val lastLon = appPrefsRepo.lastKnownLonFlow.first()

                if (location != null) {
                    val results = FloatArray(1)
                    Location.distanceBetween(lastLat, lastLon, location.latitude, location.longitude, results)
                    val distanceMeters = results[0]

                    if (distanceMeters > 50000 || lastLat == 0.0) { // 50km
                        targetLat = location.latitude
                        targetLon = location.longitude
                        appPrefsRepo.setLastKnownLocation(targetLat, targetLon)
                        
                        try {
                            val geocoder = Geocoder(context, Locale.getDefault())
                            val addresses = geocoder.getFromLocation(targetLat, targetLon, 1)
                            if (!addresses.isNullOrEmpty()) {
                                detectedCity = addresses[0].locality ?: addresses[0].subAdminArea ?: addresses[0].adminArea ?: "Unknown"
                                appPrefsRepo.setDetectedCityName(detectedCity)
                            }
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    } else {
                        targetLat = lastLat
                        targetLon = lastLon
                    }
                } else if (lastLat != 0.0 && lastLon != 0.0) {
                    targetLat = lastLat
                    targetLon = lastLon
                    android.util.Log.d("WeatherWorker", "Falling back to cached coords: lat=$targetLat lon=$targetLon")
                } else {
                    val selectedCity = appPrefsRepo.selectedCityFlow.first()
                    val fallback = getCoordinatesForCity(selectedCity)
                    targetLat = fallback.first
                    targetLon = fallback.second
                    android.util.Log.d("WeatherWorker", "Absolute fallback to city: $selectedCity")
                }
            } else {
                val selectedCity = appPrefsRepo.selectedCityFlow.first()
                val coords = getCoordinatesForCity(selectedCity)
                targetLat = coords.first
                targetLon = coords.second
            }
            
            android.util.Log.d("WeatherWorker", "Final coordinates used: lat=$targetLat lon=$targetLon")

            val weatherResponse = weatherRepo.fetchWeather(targetLat, targetLon)
            val currentTemp = weatherResponse.currentWeather?.temperature ?: 0.0
            val currentCode = weatherResponse.currentWeather?.weatherCode ?: 0
            
            android.util.Log.d("WeatherWorker", "OpenMeteo response: temp=$currentTemp code=$currentCode")
            
            appPrefsRepo.setCachedWeather(currentTemp, currentCode)

            val updateIntent = Intent(context, WeatherWidget::class.java).apply {
                action = "android.appwidget.action.APPWIDGET_UPDATE"
            }
            context.sendBroadcast(updateIntent)
            
            android.util.Log.d("WeatherWorker", "Widget update broadcast sent")
            
            Result.success()
        } catch (e: Exception) {
            e.printStackTrace()
            Result.retry()
        }
    }

    private fun getCoordinatesForCity(city: String): Pair<Double, Double> {
        return when (city.lowercase()) {
            "tokyo" -> Pair(35.6895, 139.6917)
            "new york" -> Pair(40.7128, -74.0060)
            "berlin" -> Pair(52.5200, 13.4050)
            else -> Pair(51.5074, -0.1278) // London
        }
    }
}
