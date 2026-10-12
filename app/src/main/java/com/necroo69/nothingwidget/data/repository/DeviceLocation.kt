package com.necroo69.nothingwidget.data.repository

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.Geocoder
import android.location.Location
import android.location.LocationManager
import android.os.Build
import android.os.CancellationSignal
import androidx.core.content.ContextCompat
import androidx.core.location.LocationManagerCompat
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull
import kotlin.coroutines.resume

/**
 * One-shot coarse location, without Google Play services. Only works while the app is in the
 * foreground: the app asks for ACCESS_COARSE_LOCATION but not background location, so the
 * weather worker reuses the coordinates saved the last time the app was open.
 */
object DeviceLocation {

    fun hasPermission(context: Context): Boolean =
        ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) ==
            PackageManager.PERMISSION_GRANTED

    @SuppressLint("MissingPermission") // checked by hasPermission()
    suspend fun current(context: Context): Location? {
        if (!hasPermission(context)) return null
        val manager = context.getSystemService(LocationManager::class.java) ?: return null

        // A fix from the last few minutes is good enough for weather and returns instantly.
        newestCachedFix(manager)?.takeIf { System.currentTimeMillis() - it.time < RECENT_FIX_MS }?.let { return it }

        val providers = buildList {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) add(LocationManager.FUSED_PROVIDER)
            add(LocationManager.NETWORK_PROVIDER)
            add(LocationManager.GPS_PROVIDER)
        }.filter { it in manager.allProviders && manager.isProviderEnabled(it) }

        for (provider in providers) {
            val fix = try {
                withTimeoutOrNull(FIX_TIMEOUT_MS) { currentFrom(context, manager, provider) }
            } catch (e: SecurityException) {
                null // GPS needs fine location below API 31
            }
            if (fix != null) return fix
        }

        // No fresh fix (e.g. location services just turned on): fall back to the newest cached one.
        return newestCachedFix(manager)
    }

    @SuppressLint("MissingPermission")
    private fun newestCachedFix(manager: LocationManager): Location? =
        manager.getProviders(true)
            .mapNotNull { provider -> runCatching { manager.getLastKnownLocation(provider) }.getOrNull() }
            .maxByOrNull { it.time }

    @SuppressLint("MissingPermission")
    private suspend fun currentFrom(context: Context, manager: LocationManager, provider: String): Location? =
        suspendCancellableCoroutine { continuation ->
            val signal = CancellationSignal()
            continuation.invokeOnCancellation { signal.cancel() }
            LocationManagerCompat.getCurrentLocation(
                manager, provider, signal, ContextCompat.getMainExecutor(context)
            ) { location -> if (continuation.isActive) continuation.resume(location) }
        }

    /** A short place name for [location], or null if the device has no geocoder or it fails. */
    suspend fun placeName(context: Context, location: Location): String? = withContext(Dispatchers.IO) {
        if (!Geocoder.isPresent()) return@withContext null
        try {
            @Suppress("DEPRECATION") // the async overload is API 33+; this runs off the main thread
            val address = Geocoder(context).getFromLocation(location.latitude, location.longitude, 1)?.firstOrNull()
            address?.locality ?: address?.subAdminArea ?: address?.adminArea
        } catch (e: Exception) {
            null
        }
    }

    private const val FIX_TIMEOUT_MS = 10_000L
    private const val RECENT_FIX_MS = 10 * 60_000L
}
