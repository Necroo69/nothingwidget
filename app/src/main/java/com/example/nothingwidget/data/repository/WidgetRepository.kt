package com.example.nothingwidget.data.repository

import com.example.nothingwidget.data.local.WidgetConfigDao
import com.example.nothingwidget.data.local.WidgetConfigEntity
import com.example.nothingwidget.data.local.WidgetInstanceDao
import com.example.nothingwidget.data.local.WidgetInstanceEntity
import com.example.nothingwidget.domain.model.NothingWidgetConfig
import com.example.nothingwidget.domain.model.WidgetSize
import com.example.nothingwidget.domain.model.WidgetType
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class WidgetRepository(
    private val dao: WidgetConfigDao,
    private val instanceDao: WidgetInstanceDao
) {

    val allConfigs: Flow<List<NothingWidgetConfig>> = dao.getAllWidgetConfigs().map { entities ->
        val savedMap = entities.associateBy { it.id }
        
        // 1. Get defaults and replace with saved if available
        val mergedDefaults = getDefaultPresetWidgets().map { defaultWidget ->
            savedMap[defaultWidget.id]?.toDomain() ?: defaultWidget
        }
        
        // 2. Add any newly created studio widgets that are not in defaults
        val defaultIds = getDefaultPresetWidgets().map { it.id }.toSet()
        val customWidgets = entities
            .filter { it.id !in defaultIds }
            .map { it.toDomain() }
            
        mergedDefaults + customWidgets
    }

    val pinnedConfigs: Flow<List<NothingWidgetConfig>> = dao.getPinnedWidgetConfigs().map { entities ->
        entities.map { it.toDomain() }
    }

    suspend fun getConfigById(id: String): NothingWidgetConfig? {
        val entity = dao.getWidgetConfigById(id)
        return entity?.toDomain() ?: getDefaultPresetWidgets().find { it.id == id }
    }

    suspend fun saveConfig(config: NothingWidgetConfig) {
        dao.insertOrUpdateWidgetConfig(WidgetConfigEntity.fromDomain(config))
    }

    suspend fun deleteConfig(id: String) {
        dao.deleteWidgetConfig(id)
    }

    /**
     * The config a placed home-screen widget renders: its template, with the widget's own style
     * on top if it was customized individually. Widgets with no instance row (added from the
     * launcher's picker, or placed before B3) and widgets whose template was deleted use
     * [fallbackPresetId], the provider's default template.
     */
    suspend fun getConfigForWidget(appWidgetId: Int, fallbackPresetId: String): NothingWidgetConfig? {
        val instance = instanceDao.getInstance(appWidgetId)
        val preset = instance?.let { getConfigById(it.presetId) } ?: getConfigById(fallbackPresetId)
            ?: return null
        return instance?.applyTo(preset) ?: preset
    }

    /** Records that [appWidgetId] was placed from template [presetId] and should follow it. */
    suspend fun linkWidgetToPreset(appWidgetId: Int, presetId: String) {
        val preset = getConfigById(presetId) ?: return
        instanceDao.upsertInstance(WidgetInstanceEntity.from(appWidgetId, presetId, isCustomized = false, style = preset))
    }

    /** Saves [config]'s style for this one placed widget. Its template stops affecting it. */
    suspend fun saveWidgetConfig(appWidgetId: Int, fallbackPresetId: String, config: NothingWidgetConfig) {
        val presetId = instanceDao.getInstance(appWidgetId)?.presetId ?: fallbackPresetId
        instanceDao.upsertInstance(WidgetInstanceEntity.from(appWidgetId, presetId, isCustomized = true, style = config))
    }

    suspend fun deleteWidgets(appWidgetIds: IntArray) {
        instanceDao.deleteInstances(appWidgetIds.toList())
    }

    /** Called after a backup restore, when the launcher hands out new ids for restored widgets. */
    suspend fun remapWidgets(oldIds: IntArray, newIds: IntArray) {
        oldIds.zip(newIds).forEach { (old, new) -> instanceDao.remapInstance(old, new) }
    }

    fun getDefaultPresetWidgets(): List<NothingWidgetConfig> {
        return listOf(
            NothingWidgetConfig(
                id = "clock_digital_default",
                type = WidgetType.DIGITAL_CLOCK,
                size = WidgetSize.SIZE_2X2,
                title = "Dot Matrix Digital Clock",
                accentColorHex = "#D71921",
                isMonochrome = true,
                showDotMatrixBackground = true,
                showGlyphBorder = true,
                customSubtitle = "UTC / Local Pulse"
            ),
            NothingWidgetConfig(
                id = "clock_analog_default",
                type = WidgetType.ANALOG_CLOCK,
                size = WidgetSize.SIZE_2X2,
                title = "Glyph Ring Analog Clock",
                accentColorHex = "#FFFFFF",
                isMonochrome = true,
                showDotMatrixBackground = true,
                showGlyphBorder = true,
                customSubtitle = "12-Hour Sweep"
            ),
            NothingWidgetConfig(
                id = "weather_default",
                type = WidgetType.WEATHER,
                size = WidgetSize.SIZE_4X2,
                title = "Nothing Weather",
                accentColorHex = "#D71921",
                isMonochrome = true,
                showDotMatrixBackground = true,
                showGlyphBorder = true,
                customSubtitle = "Live Conditions & Hourly Bar"
            ),
            NothingWidgetConfig(
                id = "battery_default",
                type = WidgetType.BATTERY_CIRCLE,
                size = WidgetSize.SIZE_2X2,
                title = "Battery Gauge & Ear (2)",
                accentColorHex = "#00E676",
                isMonochrome = false,
                showDotMatrixBackground = true,
                showGlyphBorder = true,
                customSubtitle = "System & Connected Earbuds"
            ),
            NothingWidgetConfig(
                id = "step_default",
                type = WidgetType.STEP_TRACKER,
                size = WidgetSize.SIZE_2X2,
                title = "Daily Activity Ring",
                accentColorHex = "#FFD600",
                isMonochrome = false,
                showDotMatrixBackground = true,
                showGlyphBorder = true,
                customSubtitle = "10,000 Step Goal Tracker"
            ),
            NothingWidgetConfig(
                id = "toggles_default",
                type = WidgetType.QUICK_TOGGLES,
                size = WidgetSize.SIZE_4X1,
                title = "Essential Controls",
                accentColorHex = "#D71921",
                isMonochrome = true,
                showDotMatrixBackground = true,
                showGlyphBorder = true,
                customSubtitle = "Wi-Fi • Bluetooth • Flashlight • DND"
            ),
            NothingWidgetConfig(
                id = "audio_default",
                type = WidgetType.AUDIO_PLAYER,
                size = WidgetSize.SIZE_4X2,
                title = "Glyph Audio Visualizer",
                accentColorHex = "#D71921",
                isMonochrome = true,
                showDotMatrixBackground = true,
                showGlyphBorder = true,
                customSubtitle = "Interactive Playback & Dynamic Spectrum"
            ),
            NothingWidgetConfig(
                id = "world_clock_default",
                type = WidgetType.WORLD_CLOCK,
                size = WidgetSize.SIZE_2X1,
                title = "Matrix World Clock",
                accentColorHex = "#FFFFFF",
                isMonochrome = true,
                showDotMatrixBackground = true,
                showGlyphBorder = false,
                customSubtitle = "Tokyo • New York • London"
            ),
            NothingWidgetConfig(
                id = "note_default",
                type = WidgetType.QUICK_NOTE,
                size = WidgetSize.SIZE_2X2,
                title = "Nothing Sticky Note",
                accentColorHex = "#FFD600",
                isMonochrome = false,
                showDotMatrixBackground = true,
                showGlyphBorder = true,
                customSubtitle = "Quick Home Screen Reminder"
            ),
            NothingWidgetConfig(
                id = "date_default",
                type = WidgetType.DATE,
                size = WidgetSize.SIZE_2X1,
                title = "Nothing Date",
                accentColorHex = "#D71921",
                isMonochrome = true,
                showDotMatrixBackground = true,
                showGlyphBorder = true,
                customSubtitle = "Day & Month Matrix"
            )
        )
    }
}
