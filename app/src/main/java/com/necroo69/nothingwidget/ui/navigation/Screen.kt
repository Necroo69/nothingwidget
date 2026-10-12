package com.necroo69.nothingwidget.ui.navigation

sealed class Screen(val route: String) {
    object Gallery : Screen("gallery")
    // widgetId is the gallery template. appWidgetId, when set, means "edit this one placed widget".
    object Customizer : Screen("customizer/{widgetId}?appWidgetId={appWidgetId}") {
        fun createRoute(widgetId: String) = "customizer/$widgetId"
        fun createRoute(widgetId: String, appWidgetId: Int) = "customizer/$widgetId?appWidgetId=$appWidgetId"
    }
    object Studio : Screen("studio")
    object Glyph : Screen("glyph")
    object Settings : Screen("settings")
    object Onboarding : Screen("onboarding")
}

/** Where the app should open when it was launched from a home-screen widget. */
sealed interface LaunchTarget {
    /** Customize one placed widget. [presetId] is its provider's default template. */
    data class EditWidget(val appWidgetId: Int, val presetId: String) : LaunchTarget

    /** A weather widget with no location was tapped. */
    data object WeatherLocation : LaunchTarget
}
