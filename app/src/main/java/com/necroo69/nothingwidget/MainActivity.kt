package com.necroo69.nothingwidget

import android.appwidget.AppWidgetManager
import android.content.Intent
import android.graphics.Color as AndroidColor
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.SystemBarStyle
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.core.view.WindowCompat
import androidx.lifecycle.lifecycleScope
import androidx.navigation.compose.rememberNavController
import com.necroo69.nothingwidget.data.repository.AppPreferencesRepository
import com.necroo69.nothingwidget.domain.model.WidgetType
import com.necroo69.nothingwidget.ui.components.LocalIs24HourClock
import com.necroo69.nothingwidget.ui.navigation.AppNavGraph
import com.necroo69.nothingwidget.ui.navigation.LaunchTarget
import com.necroo69.nothingwidget.ui.theme.NothingWidgetsTheme
import com.necroo69.nothingwidget.widgets.ACTION_EDIT_WIDGET
import com.necroo69.nothingwidget.widgets.ACTION_SET_WEATHER_LOCATION
import com.necroo69.nothingwidget.widgets.EXTRA_PRESET_ID
import com.necroo69.nothingwidget.widgets.requestWidgetUpdate
import com.necroo69.nothingwidget.worker.WeatherScheduler
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        enableEdgeToEdge()
        
        val appPrefsRepo = AppPreferencesRepository(this)
        // Only on first creation, so a rotation doesn't navigate again.
        val launchTarget = if (savedInstanceState == null) launchTargetOf(intent) else null
        
        setContent {
            val isDarkTheme by appPrefsRepo.isDarkThemeFlow.collectAsState(initial = true)
            val is24HourClock by appPrefsRepo.is24HourClockFlow.collectAsState(initial = true)
            // Null until DataStore has loaded; the start screen is chosen once, from the first value.
            val hasCompletedOnboarding by appPrefsRepo.hasCompletedOnboardingFlow
                .map<Boolean, Boolean?> { it }
                .collectAsState(initial = null)
            var startOnboarding by rememberSaveable { mutableStateOf<Boolean?>(null) }
            if (startOnboarding == null) hasCompletedOnboarding?.let { startOnboarding = !it }
            
            // enableEdgeToEdge() picks bar icon colors from the *system* theme; follow the app's
            // instead, or icons are dark-on-black when the app is dark and the system is light.
            LaunchedEffect(isDarkTheme) {
                val style = if (isDarkTheme) SystemBarStyle.dark(AndroidColor.TRANSPARENT)
                else SystemBarStyle.light(AndroidColor.TRANSPARENT, AndroidColor.TRANSPARENT)
                enableEdgeToEdge(statusBarStyle = style, navigationBarStyle = style)
            }

            NothingWidgetsTheme(darkTheme = isDarkTheme) {
                CompositionLocalProvider(LocalIs24HourClock provides is24HourClock) {
                    Surface(
                        modifier = Modifier.fillMaxSize()
                    ) {
                        startOnboarding?.let { onboarding ->
                            val navController = rememberNavController()
                            AppNavGraph(navController = navController, startOnboarding = onboarding, launchTarget = launchTarget)
                        }
                    }
                }
            }
        }
    }

    /** Where to open if this launch came from tapping a placed widget. */
    private fun launchTargetOf(intent: Intent?): LaunchTarget? = when (intent?.action) {
        ACTION_EDIT_WIDGET -> {
            val appWidgetId = intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID)
            val presetId = intent.getStringExtra(EXTRA_PRESET_ID)
            if (appWidgetId == AppWidgetManager.INVALID_APPWIDGET_ID || presetId == null) null
            else LaunchTarget.EditWidget(appWidgetId, presetId)
        }
        ACTION_SET_WEATHER_LOCATION -> LaunchTarget.WeatherLocation
        else -> null
    }

    override fun onStart() {
        super.onStart()
        // The battery widget otherwise refreshes only every 30 min (updatePeriodMillis), so give
        // it a fresh reading whenever the user opens the app.
        requestWidgetUpdate(this, WidgetType.BATTERY_CIRCLE)
        // The only time a device location can be read; also fetches weather if it is stale.
        lifecycleScope.launch { WeatherScheduler.onAppForeground(applicationContext) }
    }
}