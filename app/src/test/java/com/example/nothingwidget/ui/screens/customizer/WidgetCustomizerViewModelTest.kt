package com.example.nothingwidget.ui.screens.customizer

import com.example.nothingwidget.data.repository.FakeWidgetConfigDao
import com.example.nothingwidget.data.repository.WidgetRepository
import com.example.nothingwidget.util.MainDispatcherRule
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class WidgetCustomizerViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private lateinit var dao: FakeWidgetConfigDao
    private lateinit var repository: WidgetRepository
    private lateinit var viewModel: WidgetCustomizerViewModel

    @Before
    fun setUp() {
        dao = FakeWidgetConfigDao()
        repository = WidgetRepository(dao)
        viewModel = WidgetCustomizerViewModel(repository)
    }

    @Test
    fun loadWidget_populatesConfigFromRepository() = runTest(mainDispatcherRule.dispatcher) {
        viewModel.loadWidget("weather_default")
        advanceUntilIdle()

        assertNotNull(viewModel.config.value)
        assertEquals("weather_default", viewModel.config.value?.id)
    }

    @Test
    fun updateAccentColor_updatesState() = runTest(mainDispatcherRule.dispatcher) {
        viewModel.loadWidget("weather_default")
        advanceUntilIdle()

        viewModel.updateAccentColor("#00FF00")

        assertEquals("#00FF00", viewModel.config.value?.accentColorHex)
    }

    @Test
    fun toggleMonochrome_flipsState() = runTest(mainDispatcherRule.dispatcher) {
        viewModel.loadWidget("weather_default")
        advanceUntilIdle()
        val before = viewModel.config.value?.isMonochrome!!

        viewModel.toggleMonochrome()

        assertEquals(!before, viewModel.config.value?.isMonochrome)
    }

    @Test
    fun updateBeforeLoad_isNoOpAndKeepsConfigNull() = runTest(mainDispatcherRule.dispatcher) {
        // No widget loaded yet — mutators must not crash or fabricate a config.
        viewModel.updateAccentColor("#00FF00")
        viewModel.toggleMonochrome()

        assertEquals(null, viewModel.config.value)
    }

    @Test
    fun saveConfig_persistsToRepositoryAndInvokesCallback() = runTest(mainDispatcherRule.dispatcher) {
        viewModel.loadWidget("weather_default")
        advanceUntilIdle()
        viewModel.updateAccentColor("#123456")

        var callbackInvoked = false
        viewModel.saveConfig { callbackInvoked = true }
        advanceUntilIdle()

        assertTrue(callbackInvoked)
        assertEquals("#123456", dao.getWidgetConfigById("weather_default")?.accentColorHex)
    }

    @Test
    fun saveConfig_withoutLoadedConfig_doesNotInvokeCallback() = runTest(mainDispatcherRule.dispatcher) {
        var callbackInvoked = false
        viewModel.saveConfig { callbackInvoked = true }
        advanceUntilIdle()

        assertTrue(!callbackInvoked)
    }
}
