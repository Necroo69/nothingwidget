package com.necroo69.nothingwidget.ui.screens.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.necroo69.nothingwidget.data.repository.AppPreferencesRepository
import com.necroo69.nothingwidget.ui.components.NothingGlassCard
import com.necroo69.nothingwidget.ui.theme.NothingDotFontFamily
import com.necroo69.nothingwidget.ui.theme.NothingRed

@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel,
    onBackClick: () -> Unit,
    onOpenOnboarding: () -> Unit
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(horizontal = 8.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBackClick) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = MaterialTheme.colorScheme.onSurface)
                }
                Text(
                    text = "SYSTEM & ENGINE SETTINGS",
                    color = MaterialTheme.colorScheme.onSurface,
                    fontSize = 18.sp,
                    fontFamily = NothingDotFontFamily,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
            }
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp)
                .navigationBarsPadding()
                .verticalScroll(rememberScrollState())
                .testTag("settings_screen_scroll"),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            SectionLabel("WEATHER")

            WeatherLocationPicker(state = state, viewModel = viewModel)

            SettingToggleItem("USE CELSIUS (°C)", "Off shows Fahrenheit. Applies to widgets right away", state.isCelsius) {
                viewModel.toggleCelsius()
            }

            Text(
                text = "WEATHER REFRESH RATE",
                color = MaterialTheme.colorScheme.onSurface,
                fontSize = 13.sp,
                fontFamily = NothingDotFontFamily,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                AppPreferencesRepository.WEATHER_REFRESH_OPTIONS.forEach { minutes ->
                    val isSelected = minutes == state.weatherRefreshMinutes
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .heightIn(min = 48.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(if (isSelected) NothingRed else MaterialTheme.colorScheme.surfaceVariant)
                            .border(1.dp, if (isSelected) NothingRed else MaterialTheme.colorScheme.outline, RoundedCornerShape(12.dp))
                            .clickable { viewModel.setWeatherRefreshMinutes(minutes) }
                            .semantics { selected = isSelected }
                            .padding(vertical = 10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = if (minutes < 60) "${minutes} MIN" else "${minutes / 60} H",
                            color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface,
                            fontSize = 11.sp,
                            fontFamily = NothingDotFontFamily,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
            Text(
                text = "How often weather widgets fetch new data while a weather widget is placed. Clocks and dates tick on their own.",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 11.sp,
                fontFamily = NothingDotFontFamily
            )

            SectionLabel("PREFERENCES")

            // Setting Item: Dark Theme
            SettingToggleItem("ENABLE DARK THEME", "Use Nothing OS pitch black dark mode in the app", state.isDarkTheme) {
                viewModel.toggleTheme()
            }

            // Setting Item: 24h Clock
            SettingToggleItem("24-HOUR FORMAT", "Display clocks in 24-hour matrix format", state.is24HourClock) {
                viewModel.toggle24h()
            }

            SectionLabel("WIDGETS")

            SettingActionItem("REFRESH ALL WIDGETS NOW", state.actionMessage ?: "Redraw every placed widget and fetch fresh weather", "refresh_all_btn") {
                viewModel.refreshAllWidgets()
            }

            SettingActionItem("HOW TO ADD WIDGETS", "Replay the introduction and home-screen steps", "how_to_add_btn") {
                onOpenOnboarding()
            }

            // Engine Info Card
            NothingGlassCard(
                modifier = Modifier.fillMaxWidth(),
                cornerRadius = 20.dp
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(Icons.Default.Info, contentDescription = null, tint = NothingRed)
                        Text("NOTHING OS ENGINE v2.5", color = MaterialTheme.colorScheme.onSurface, fontSize = 13.sp, fontFamily = NothingDotFontFamily, fontWeight = FontWeight.Bold)
                    }
                    Text(
                        text = "Clocks and dates tick on the launcher with no background work. Weather comes from Open-Meteo through WorkManager. Your widget styles are stored on this device with Room.",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 11.sp,
                        fontFamily = NothingDotFontFamily
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun SectionLabel(text: String) {
    Text(
        text = text,
        color = NothingRed,
        fontSize = 11.sp,
        fontFamily = NothingDotFontFamily,
        fontWeight = FontWeight.Bold,
        letterSpacing = 1.sp
    )
}

@Composable
private fun SettingActionItem(title: String, subtitle: String, testTag: String, onClick: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 48.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(16.dp))
            .clickable(onClick = onClick)
            .testTag(testTag)
            .padding(14.dp)
    ) {
        Text(
            text = title,
            color = MaterialTheme.colorScheme.onSurface,
            fontSize = 13.sp,
            fontFamily = NothingDotFontFamily,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = subtitle,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontSize = 11.sp,
            fontFamily = NothingDotFontFamily
        )
    }
}

@Composable
fun SettingToggleItem(title: String, subtitle: String, checked: Boolean, onCheckedChange: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(16.dp))
            .padding(14.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                color = MaterialTheme.colorScheme.onSurface,
                fontSize = 13.sp,
                fontFamily = NothingDotFontFamily,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = subtitle,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 11.sp,
                fontFamily = NothingDotFontFamily
            )
        }
        Switch(
            checked = checked,
            onCheckedChange = { onCheckedChange() },
            colors = SwitchDefaults.colors(checkedThumbColor = NothingRed)
        )
    }
}
