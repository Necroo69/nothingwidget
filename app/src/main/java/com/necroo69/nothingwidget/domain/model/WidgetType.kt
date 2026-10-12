package com.necroo69.nothingwidget.domain.model

enum class WidgetType(val displayName: String, val category: String) {
    DIGITAL_CLOCK("Dot Matrix Digital Clock", "Clock"),
    ANALOG_CLOCK("Glyph Analog Clock", "Clock"),
    WEATHER("Nothing Weather", "Weather"),
    BATTERY_CIRCLE("Battery Gauge", "System"),
    STEP_TRACKER("Step & Activity Ring", "Fitness"),
    QUICK_TOGGLES("Essential Toggles", "System"),
    AUDIO_PLAYER("Audio Visualizer", "Media"),
    WORLD_CLOCK("World Matrix Clock", "Clock"),
    QUICK_NOTE("Nothing Sticky Note", "Productivity"),
    DATE("Nothing Date", "Calendar")
}

enum class WidgetSize(val label: String, val spanX: Int, val spanY: Int) {
    SIZE_1X1("1x1 Small", 1, 1),
    SIZE_2X1("2x1 Compact", 2, 1),
    SIZE_2X2("2x2 Standard", 2, 2),
    SIZE_4X1("4x1 Wide", 4, 1),
    SIZE_4X2("4x2 Hero", 4, 2)
}
