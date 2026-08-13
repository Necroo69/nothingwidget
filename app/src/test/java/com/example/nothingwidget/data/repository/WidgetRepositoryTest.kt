package com.example.nothingwidget.data.repository

import com.example.nothingwidget.data.local.WidgetConfigEntity
import com.example.nothingwidget.domain.model.WidgetSize
import com.example.nothingwidget.domain.model.WidgetType
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class WidgetRepositoryTest {

    private lateinit var dao: FakeWidgetConfigDao
    private lateinit var repository: WidgetRepository

    @Before
    fun setUp() {
        dao = FakeWidgetConfigDao()
        repository = WidgetRepository(dao)
    }

    @Test
    fun allConfigs_emptyDatabase_returnsAllDefaultPresets() = runTest {
        val defaults = repository.getDefaultPresetWidgets()

        val result = repository.allConfigs.first()

        assertEquals(defaults.map { it.id }, result.map { it.id })
    }

    @Test
    fun allConfigs_savedConfig_overridesMatchingDefault() = runTest {
        // Regression guard for BUG-01: saving one widget must not drop the others.
        val edited = entityFor("weather_default", accentColorHex = "#00FF00")
        dao.insertOrUpdateWidgetConfig(edited)

        val result = repository.allConfigs.first()

        // Still every default preset, none dropped.
        assertEquals(repository.getDefaultPresetWidgets().size, result.size)
        val weather = result.first { it.id == "weather_default" }
        assertEquals("#00FF00", weather.accentColorHex)
    }

    @Test
    fun allConfigs_customStudioWidget_isAppendedAfterDefaults() = runTest {
        val custom = entityFor("studio_custom_1", accentColorHex = "#123456")
        dao.insertOrUpdateWidgetConfig(custom)

        val result = repository.allConfigs.first()

        assertEquals(repository.getDefaultPresetWidgets().size + 1, result.size)
        assertTrue(result.any { it.id == "studio_custom_1" })
    }

    @Test
    fun getConfigById_returnsSavedConfigWhenPresent() = runTest {
        dao.insertOrUpdateWidgetConfig(entityFor("weather_default", accentColorHex = "#ABCDEF"))

        val result = repository.getConfigById("weather_default")

        assertNotNull(result)
        assertEquals("#ABCDEF", result?.accentColorHex)
    }

    @Test
    fun getConfigById_fallsBackToDefaultPresetWhenNotSaved() = runTest {
        val result = repository.getConfigById("weather_default")

        assertNotNull(result)
        assertEquals(WidgetType.WEATHER, result?.type)
    }

    @Test
    fun getConfigById_returnsNullForUnknownId() = runTest {
        assertNull(repository.getConfigById("does_not_exist"))
    }

    private fun entityFor(id: String, accentColorHex: String) = WidgetConfigEntity(
        id = id,
        typeName = WidgetType.WEATHER.name,
        sizeName = WidgetSize.SIZE_4X2.name,
        title = "Test Widget",
        accentColorHex = accentColorHex,
        isMonochrome = true,
        cornerRadiusDp = 24,
        transparencyPercent = 10,
        showDotMatrixBackground = true,
        showGlyphBorder = true,
        customSubtitle = "",
        isPinned = false,
        updateIntervalMinutes = 15,
        lastUpdatedTimestamp = 0L
    )
}
