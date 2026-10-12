package com.necroo69.nothingwidget.ui.navigation

import android.appwidget.AppWidgetManager
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.necroo69.nothingwidget.data.local.AppDatabase
import com.necroo69.nothingwidget.data.repository.AppPreferencesRepository
import com.necroo69.nothingwidget.data.repository.AudioRepository
import com.necroo69.nothingwidget.data.repository.BatteryRepository
import com.necroo69.nothingwidget.data.repository.QuickSettingsRepository
import com.necroo69.nothingwidget.data.repository.StepTrackerRepository
import com.necroo69.nothingwidget.data.repository.WeatherRepository
import com.necroo69.nothingwidget.data.repository.WidgetRepository
import com.necroo69.nothingwidget.ui.screens.customizer.WidgetCustomizerScreen
import com.necroo69.nothingwidget.ui.screens.customizer.WidgetCustomizerViewModel
import com.necroo69.nothingwidget.ui.screens.gallery.WidgetGalleryScreen
import com.necroo69.nothingwidget.ui.screens.gallery.WidgetGalleryViewModel
import com.necroo69.nothingwidget.ui.screens.glyph.GlyphStudioScreen
import com.necroo69.nothingwidget.ui.screens.glyph.GlyphStudioViewModel
import com.necroo69.nothingwidget.ui.screens.settings.SettingsScreen
import com.necroo69.nothingwidget.ui.screens.settings.SettingsViewModel
import com.necroo69.nothingwidget.ui.screens.studio.WidgetStudioScreen
import com.necroo69.nothingwidget.ui.screens.studio.WidgetStudioViewModel
import com.necroo69.nothingwidget.ui.screens.onboarding.OnboardingScreen
import com.necroo69.nothingwidget.widgets.AndroidWidgetEngine

@Composable
fun AppNavGraph(
    navController: NavHostController,
    // True on first run: start at the onboarding screen instead of the gallery.
    startOnboarding: Boolean,
    // Set when the app was opened by tapping a placed widget.
    launchTarget: LaunchTarget? = null
) {
    val context = LocalContext.current

    val db = remember { AppDatabase.getInstance(context) }
    val widgetRepo = remember { WidgetRepository(db.widgetConfigDao(), db.widgetInstanceDao()) }
    val weatherRepo = remember { WeatherRepository(context) }
    val batteryRepo = remember { BatteryRepository(context) }
    val quickSettingsRepo = remember { QuickSettingsRepository() }
    val stepRepo = remember { StepTrackerRepository(db.stepDao()) }
    val audioRepo = remember { AudioRepository() }
    val appPrefsRepo = remember { AppPreferencesRepository(context) }

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

    val customizerViewModel = remember { WidgetCustomizerViewModel(widgetRepo, batteryRepo, weatherRepo) }
    val studioViewModel = remember { WidgetStudioViewModel(widgetRepo, batteryRepo, weatherRepo) }
    val glyphViewModel = remember { GlyphStudioViewModel() }
    val settingsViewModel = remember { SettingsViewModel(appPrefsRepo, weatherRepo, AndroidWidgetEngine(context)) }

    NavHost(
        navController = navController,
        // A widget tap skips onboarding: the user has clearly already added a widget.
        startDestination = if (startOnboarding && launchTarget == null) Screen.Onboarding.route else Screen.Gallery.route
    ) {
        composable(Screen.Onboarding.route) {
            OnboardingScreen(
                settingsViewModel = settingsViewModel,
                onFinished = {
                    // Replayed from Settings: go back there. First run: replace with the gallery.
                    if (navController.previousBackStackEntry != null) {
                        navController.popBackStack()
                    } else {
                        navController.navigate(Screen.Gallery.route) {
                            popUpTo(Screen.Onboarding.route) { inclusive = true }
                        }
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
            arguments = listOf(
                navArgument("widgetId") { type = NavType.StringType },
                navArgument("appWidgetId") {
                    type = NavType.IntType
                    defaultValue = AppWidgetManager.INVALID_APPWIDGET_ID
                }
            )
        ) { backStackEntry ->
            val widgetId = backStackEntry.arguments?.getString("widgetId") ?: ""
            val appWidgetId = backStackEntry.arguments?.getInt("appWidgetId") ?: AppWidgetManager.INVALID_APPWIDGET_ID
            WidgetCustomizerScreen(
                widgetId = widgetId,
                appWidgetId = appWidgetId,
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
                onOpenOnboarding = { navController.navigate(Screen.Onboarding.route) }
            )
        }
    }

    // Runs after NavHost has set its graph, so navigate() can resolve the route.
    LaunchedEffect(launchTarget) {
        when (launchTarget) {
            is LaunchTarget.EditWidget ->
                navController.navigate(Screen.Customizer.createRoute(launchTarget.presetId, launchTarget.appWidgetId))
            LaunchTarget.WeatherLocation -> navController.navigate(Screen.Settings.route)
            null -> Unit
        }
    }
}
