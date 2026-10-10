package com.example.nothingwidget

import android.appwidget.AppWidgetManager
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
import com.example.nothingwidget.widgets.ACTION_EDIT_WIDGET
import com.example.nothingwidget.widgets.EXTRA_PRESET_ID
import com.example.nothingwidget.widgets.requestWidgetUpdate
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        enableEdgeToEdge()
        
        val appPrefsRepo = AppPreferencesRepository(this)
        // Only on first creation, so a rotation doesn't push the customizer again.
        val editPlacedWidget = if (savedInstanceState == null) placedWidgetToEdit() else null
        
        setContent {
            val isDarkTheme by appPrefsRepo.isDarkThemeFlow.collectAsState(initial = true)
            val is24HourClock by appPrefsRepo.is24HourClockFlow.collectAsState(initial = true)
            
            NothingWidgetsTheme(darkTheme = isDarkTheme) {
                CompositionLocalProvider(LocalIs24HourClock provides is24HourClock) {
                    Surface(
                        modifier = Modifier.fillMaxSize()
                    ) {
                        val navController = rememberNavController()
                        AppNavGraph(navController = navController, editPlacedWidget = editPlacedWidget)
                    }
                }
            }
        }
    }

    /** (appWidgetId, template id) if this launch came from tapping a placed widget. */
    private fun placedWidgetToEdit(): Pair<Int, String>? {
        if (intent?.action != ACTION_EDIT_WIDGET) return null
        val appWidgetId = intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID)
        val presetId = intent.getStringExtra(EXTRA_PRESET_ID)
        if (appWidgetId == AppWidgetManager.INVALID_APPWIDGET_ID || presetId == null) return null
        return appWidgetId to presetId
    }

    override fun onStart() {
        super.onStart()
        // The battery widget otherwise refreshes only every 30 min (updatePeriodMillis), so give
        // it a fresh reading whenever the user opens the app.
        requestWidgetUpdate(this, WidgetType.BATTERY_CIRCLE)
    }
}