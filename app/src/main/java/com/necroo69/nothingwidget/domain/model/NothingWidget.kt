package com.necroo69.nothingwidget.domain.model

data class NothingWidgetConfig(
    val id: String,
    val type: WidgetType,
    val size: WidgetSize,
    val title: String,
    val accentColorHex: String = "#D71921", // Nothing Red default
    val isMonochrome: Boolean = true,
    val cornerRadiusDp: Int = 24,
    val transparencyPercent: Int = 10,
    val showDotMatrixBackground: Boolean = true,
    val showGlyphBorder: Boolean = true,
    val customSubtitle: String = "",
    val isPinned: Boolean = false,
    val updateIntervalMinutes: Int = 15,
    val lastUpdatedTimestamp: Long = System.currentTimeMillis()
)
