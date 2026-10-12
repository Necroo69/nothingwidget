package com.necroo69.nothingwidget.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.necroo69.nothingwidget.domain.model.NothingWidgetConfig
import com.necroo69.nothingwidget.domain.model.WidgetSize
import com.necroo69.nothingwidget.domain.model.WidgetType

@Entity(tableName = "widget_configs")
data class WidgetConfigEntity(
    @PrimaryKey val id: String,
    val typeName: String,
    val sizeName: String,
    val title: String,
    val accentColorHex: String,
    val isMonochrome: Boolean,
    val cornerRadiusDp: Int,
    val transparencyPercent: Int,
    val showDotMatrixBackground: Boolean,
    val showGlyphBorder: Boolean,
    val customSubtitle: String,
    val isPinned: Boolean,
    val updateIntervalMinutes: Int,
    val lastUpdatedTimestamp: Long
) {
    fun toDomain(): NothingWidgetConfig {
        return NothingWidgetConfig(
            id = id,
            type = WidgetType.entries.find { it.name == typeName } ?: WidgetType.DIGITAL_CLOCK,
            size = WidgetSize.entries.find { it.name == sizeName } ?: WidgetSize.SIZE_2X2,
            title = title,
            accentColorHex = accentColorHex,
            isMonochrome = isMonochrome,
            cornerRadiusDp = cornerRadiusDp,
            transparencyPercent = transparencyPercent,
            showDotMatrixBackground = showDotMatrixBackground,
            showGlyphBorder = showGlyphBorder,
            customSubtitle = customSubtitle,
            isPinned = isPinned,
            updateIntervalMinutes = updateIntervalMinutes,
            lastUpdatedTimestamp = lastUpdatedTimestamp
        )
    }

    companion object {
        fun fromDomain(config: NothingWidgetConfig): WidgetConfigEntity {
            return WidgetConfigEntity(
                id = config.id,
                typeName = config.type.name,
                sizeName = config.size.name,
                title = config.title,
                accentColorHex = config.accentColorHex,
                isMonochrome = config.isMonochrome,
                cornerRadiusDp = config.cornerRadiusDp,
                transparencyPercent = config.transparencyPercent,
                showDotMatrixBackground = config.showDotMatrixBackground,
                showGlyphBorder = config.showGlyphBorder,
                customSubtitle = config.customSubtitle,
                isPinned = config.isPinned,
                updateIntervalMinutes = config.updateIntervalMinutes,
                lastUpdatedTimestamp = config.lastUpdatedTimestamp
            )
        }
    }
}
