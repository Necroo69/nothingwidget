package com.necroo69.nothingwidget.ui.screens.settings

import android.Manifest
import android.text.format.DateFormat
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.necroo69.nothingwidget.data.repository.DeviceLocation
import com.necroo69.nothingwidget.domain.model.WeatherStatus
import com.necroo69.nothingwidget.ui.theme.NothingDotFontFamily
import com.necroo69.nothingwidget.ui.theme.NothingRed
import java.util.Date

/**
 * Choose where weather comes from: the device's approximate location (asks for
 * ACCESS_COARSE_LOCATION) or a city search. Shared by Settings and onboarding.
 */
@Composable
fun WeatherLocationPicker(state: SettingsUiState, viewModel: SettingsViewModel) {
    val context = LocalContext.current
    val focusManager = LocalFocusManager.current
    val search = {
        focusManager.clearFocus()
        viewModel.searchCities()
    }
    val permissionLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        if (granted) viewModel.useDeviceLocation() else viewModel.onLocationPermissionDenied()
    }

    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        // Current location and data status
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant)
                .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(16.dp))
                .padding(14.dp)
        ) {
            val location = state.weatherLocation
            Text(
                text = location?.name?.uppercase() ?: "NO LOCATION SET",
                color = MaterialTheme.colorScheme.onSurface,
                fontSize = 13.sp,
                fontFamily = NothingDotFontFamily,
                fontWeight = FontWeight.Bold
            )
            val status = when {
                location == null -> "Weather widgets need a location."
                state.weather.status == WeatherStatus.READY -> {
                    val updated = state.weather.updatedAtMillis?.let { DateFormat.getTimeFormat(context).format(Date(it)) }
                    val source = if (location.isDeviceLocation) "Device location (updated when you open the app)" else "City"
                    "$source · ${state.weather.temperature}°${state.weather.unitSymbol} · updated $updated"
                }
                else -> if (location.isDeviceLocation) "Device location · waiting for data" else "City · waiting for data"
            }
            Text(status, color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 11.sp, fontFamily = NothingDotFontFamily)
        }

        Button(
            onClick = {
                focusManager.clearFocus()
                if (DeviceLocation.hasPermission(context)) viewModel.useDeviceLocation()
                else permissionLauncher.launch(Manifest.permission.ACCESS_COARSE_LOCATION)
            },
            enabled = !state.isLocating,
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 48.dp)
                .testTag("use_my_location_btn"),
            colors = ButtonDefaults.buttonColors(containerColor = NothingRed, contentColor = Color.White),
            shape = RoundedCornerShape(12.dp)
        ) {
            if (state.isLocating) {
                CircularProgressIndicator(modifier = Modifier.size(18.dp), color = Color.White, strokeWidth = 2.dp)
            } else {
                Icon(Icons.Default.LocationOn, contentDescription = null)
            }
            Text(
                text = "  USE MY LOCATION",
                fontSize = 12.sp,
                fontFamily = NothingDotFontFamily,
                fontWeight = FontWeight.Bold
            )
        }

        OutlinedTextField(
            value = state.cityQuery,
            onValueChange = viewModel::updateCityQuery,
            modifier = Modifier
                .fillMaxWidth()
                .testTag("city_search_field"),
            singleLine = true,
            placeholder = { Text("Or search a city", fontFamily = NothingDotFontFamily) },
            trailingIcon = {
                if (state.isSearching) {
                    CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp, color = NothingRed)
                } else {
                    IconButton(onClick = search) {
                        Icon(Icons.Default.Search, contentDescription = "Search cities")
                    }
                }
            },
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
            keyboardActions = KeyboardActions(onSearch = { search() }),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = NothingRed,
                unfocusedBorderColor = MaterialTheme.colorScheme.outline
            ),
            shape = RoundedCornerShape(12.dp)
        )

        state.cityResults.forEach { result ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 48.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .clickable {
                        focusManager.clearFocus()
                        viewModel.pickCity(result)
                    }
                    .padding(horizontal = 14.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(result.name, color = MaterialTheme.colorScheme.onSurface, fontSize = 12.sp, fontFamily = NothingDotFontFamily)
            }
        }

        state.locationMessage?.let { message ->
            Text(message, color = NothingRed, fontSize = 11.sp, fontFamily = NothingDotFontFamily)
        }
    }
}
