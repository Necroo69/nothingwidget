package com.example.nothingwidget

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.core.view.WindowCompat
import androidx.navigation.compose.rememberNavController
import com.example.nothingwidget.data.repository.AppPreferencesRepository
import com.example.nothingwidget.ui.navigation.AppNavGraph
import com.example.nothingwidget.ui.navigation.Screen
import com.example.nothingwidget.ui.theme.NothingWidgetsTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        enableEdgeToEdge()
        
        val appPrefsRepo = AppPreferencesRepository(this)
        
        setContent {
            val themeMode by appPrefsRepo.themeModeFlow.collectAsState(initial = "SYSTEM")
            val isOnboardingCompleted by appPrefsRepo.isOnboardingCompletedFlow.collectAsState(initial = null)
            
            val systemDark = isSystemInDarkTheme()
            val isDarkTheme = when (themeMode) {
                "DARK" -> true
                "LIGHT" -> false
                else -> systemDark
            }
            
            NothingWidgetsTheme(darkTheme = isDarkTheme) {
                Surface(
                    modifier = Modifier.fillMaxSize()
                ) {
                    if (isOnboardingCompleted == null) {
                        Box(modifier = Modifier.fillMaxSize())
                    } else {
                        val navController = rememberNavController()
                        val startDestination = if (isOnboardingCompleted == true) Screen.Gallery.route else Screen.Onboarding.route
                        AppNavGraph(
                            navController = navController,
                            startDestination = startDestination
                        )
                    }
                }
            }
        }
    }
}