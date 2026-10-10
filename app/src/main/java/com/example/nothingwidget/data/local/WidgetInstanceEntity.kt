package com.example.nothingwidget.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.nothingwidget.domain.model.NothingWidgetConfig

/**
 * One widget placed on the home screen, keyed by the launcher's appWidgetId.
 *
 * [presetId] is the gallery template it was placed from. While [isCustomized] is false the
 * widget follows that template, so gallery edits still reach it. Once the user edits this
 * widget on its own, [isCustomized] becomes true and the style columns below take over.
 */
@Entity(tableName = "widget_instances")
data class WidgetInstanceEntity(
    @PrimaryKey val appWidgetId: Int,
    val presetId: String,
    val isCustomized: Boolean,
    val accentColorHex: String,
    val isMonochrome: Boolean,
    val cornerRadiusDp: Int,
    val transparencyPercent: Int,
    val showDotMatrixBackground: Boolean,
    val showGlyphBorder: Boolean,
    val customSubtitle: String
) {
    /** [preset] with this widget's own style applied, or [preset] unchanged if not customized. */
    fun applyTo(preset: NothingWidgetConfig): NothingWidgetConfig {
        if (!isCustomized) return preset
        return preset.copy(
            accentColorHex = accentColorHex,
            isMonochrome = isMonochrome,
            cornerRadiusDp = cornerRadiusDp,
            transparencyPercent = transparencyPercent,
            showDotMatrixBackground = showDotMatrixBackground,
            showGlyphBorder = showGlyphBorder,
            customSubtitle = customSubtitle
        )
    }

    companion object {
        fun from(
            appWidgetId: Int,
            presetId: String,
            isCustomized: Boolean,
            style: NothingWidgetConfig
        ) = WidgetInstanceEntity(
            appWidgetId = appWidgetId,
            presetId = presetId,
            isCustomized = isCustomized,
            accentColorHex = style.accentColorHex,
            isMonochrome = style.isMonochrome,
            cornerRadiusDp = style.cornerRadiusDp,
            transparencyPercent = style.transparencyPercent,
            showDotMatrixBackground = style.showDotMatrixBackground,
            showGlyphBorder = style.showGlyphBorder,
            customSubtitle = style.customSubtitle
        )
    }
}
