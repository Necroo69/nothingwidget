package com.example.nothingwidget.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.nothingwidget.data.local.AppDatabase
import com.example.nothingwidget.data.repository.AppPreferencesRepository
import com.example.nothingwidget.data.repository.AudioRepository
import com.example.nothingwidget.data.repository.BatteryRepository
import com.example.nothingwidget.data.repository.QuickSettingsRepository
import com.example.nothingwidget.data.repository.StepTrackerRepository
import com.example.nothingwidget.data.repository.WeatherRepository
import com.example.nothingwidget.data.repository.WidgetRepository
import com.example.nothingwidget.ui.screens.customizer.WidgetCustomizerScreen
import com.example.nothingwidget.ui.screens.customizer.WidgetCustomizerViewModel
import com.example.nothingwidget.ui.screens.gallery.WidgetGalleryScreen
import com.example.nothingwidget.ui.screens.gallery.WidgetGalleryViewModel
import com.example.nothingwidget.ui.screens.glyph.GlyphStudioScreen
import com.example.nothingwidget.ui.screens.glyph.GlyphStudioViewModel
import com.example.nothingwidget.ui.screens.onboarding.OnboardingScreen
import com.example.nothingwidget.ui.screens.settings.SettingsScreen
import com.example.nothingwidget.ui.screens.settings.SettingsViewModel
import com.example.nothingwidget.ui.screens.studio.WidgetStudioScreen
import com.example.nothingwidget.ui.screens.studio.WidgetStudioViewModel

@Composable
fun AppNavGraph(navController: NavHostController, startDestination: String = Screen.Gallery.route) {
    val context = LocalContext.current

    val db = remember { AppDatabase.getInstance(context) }
    val widgetRepo = remember { WidgetRepository(db.widgetConfigDao()) }
    val appPrefsRepo = remember { AppPreferencesRepository(context) }
    val weatherRepo = remember { WeatherRepository(appPrefsRepo) }
    val batteryRepo = remember { BatteryRepository(context) }
    val quickSettingsRepo = remember { QuickSettingsRepository() }
    val stepRepo = remember { StepTrackerRepository(db.stepDao()) }
    val audioRepo = remember { AudioRepository() }

    val galleryViewModel = remember {
        WidgetGalleryViewModel(
            widgetRepository = widgetRepo,
            weatherRepository = weatherRepo,
            batteryRepository = batteryRepo,
            quickSettingsRepository = quickSettingsRepo,
            stepTrackerRepository = stepRepo,
            audioRepository = audioRepo
        )
    }

    val customizerViewModel = remember { WidgetCustomizerViewModel(widgetRepo) }
    val studioViewModel = remember { WidgetStudioViewModel(widgetRepo) }
    val glyphViewModel = remember { GlyphStudioViewModel() }
    val settingsViewModel = remember { SettingsViewModel(appPrefsRepo) }

    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        composable(Screen.Onboarding.route) {
            OnboardingScreen(
                appPrefsRepo = appPrefsRepo,
                onComplete = {
                    navController.navigate(Screen.Gallery.route) {
                        popUpTo(0) // Clear backstack so user can't go back to onboarding
                    }
                }
            )
        }

        composable(Screen.Gallery.route) {
            WidgetGalleryScreen(
                viewModel = galleryViewModel,
                onNavigateToCustomizer = { id -> navController.navigate(Screen.Customizer.createRoute(id)) },
                onNavigateToStudio = { navController.navigate(Screen.Studio.route) },
                onNavigateToGlyph = { navController.navigate(Screen.Glyph.route) },
                onNavigateToSettings = { navController.navigate(Screen.Settings.route) }
            )
        }

        composable(
            route = Screen.Customizer.route,
            arguments = listOf(navArgument("widgetId") { type = NavType.StringType })
        ) { backStackEntry ->
            val widgetId = backStackEntry.arguments?.getString("widgetId") ?: ""
            WidgetCustomizerScreen(
                widgetId = widgetId,
                viewModel = customizerViewModel,
                onBackClick = { navController.popBackStack() }
            )
        }

        composable(Screen.Studio.route) {
            WidgetStudioScreen(
                viewModel = studioViewModel,
                onBackClick = { navController.popBackStack() }
            )
        }

        composable(Screen.Glyph.route) {
            GlyphStudioScreen(
                viewModel = glyphViewModel,
                onBackClick = { navController.popBackStack() }
            )
        }

        composable(Screen.Settings.route) {
            SettingsScreen(
                viewModel = settingsViewModel,
                onBackClick = { navController.popBackStack() },
                onNavigateToOnboarding = { navController.navigate(Screen.Onboarding.route) }
            )
        }
    }
}
