package com.example.nothingwidget.ui.navigation

sealed class Screen(val route: String) {
    object Onboarding : Screen("onboarding")
    object Gallery : Screen("gallery")
    object Customizer : Screen("customizer/{widgetId}") {
        fun createRoute(widgetId: String) = "customizer/$widgetId"
    }
    object Studio : Screen("studio")
    object Glyph : Screen("glyph")
    object Settings : Screen("settings")
}
