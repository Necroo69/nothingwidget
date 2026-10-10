package com.example.nothingwidget.ui.navigation

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
}
