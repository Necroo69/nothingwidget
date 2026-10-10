package com.example.nothingwidget

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.core.view.WindowCompat
import androidx.navigation.compose.rememberNavController
import com.example.nothingwidget.data.repository.AppPreferencesRepository
import com.example.nothingwidget.domain.model.WidgetType
import com.example.nothingwidget.ui.components.LocalIs24HourClock
import com.example.nothingwidget.ui.navigation.AppNavGraph
import com.example.nothingwidget.ui.theme.NothingWidgetsTheme
import com.example.nothingwidget.widgets.requestWidgetUpdate
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        enableEdgeToEdge()
        
        val appPrefsRepo = AppPreferencesRepository(this)
        
        setContent {
            val isDarkTheme by appPrefsRepo.isDarkThemeFlow.collectAsState(initial = true)
            val is24HourClock by appPrefsRepo.is24HourClockFlow.collectAsState(initial = true)
            
            NothingWidgetsTheme(darkTheme = isDarkTheme) {
                CompositionLocalProvider(LocalIs24HourClock provides is24HourClock) {
                    Surface(
                        modifier = Modifier.fillMaxSize()
                    ) {
                        val navController = rememberNavController()
                        AppNavGraph(navController = navController)
                    }
                }
            }
        }
    }

    override fun onStart() {
        super.onStart()
        // The battery widget otherwise refreshes only every 30 min (updatePeriodMillis), so give
        // it a fresh reading whenever the user opens the app.
        requestWidgetUpdate(this, WidgetType.BATTERY_CIRCLE)
    }
}