package com.example.nothingwidget.ui.screens.customizer

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.nothingwidget.data.repository.BatteryRepository
import com.example.nothingwidget.data.repository.WidgetRepository
import com.example.nothingwidget.domain.model.BatteryInfo
import com.example.nothingwidget.domain.model.NothingWidgetConfig
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class WidgetCustomizerViewModel(
    private val widgetRepository: WidgetRepository,
    batteryRepository: BatteryRepository
) : ViewModel() {

    private val _config = MutableStateFlow<NothingWidgetConfig?>(null)
    val config: StateFlow<NothingWidgetConfig?> = _config.asStateFlow()

    // The placed home-screen widget being edited, or null when editing a gallery template.
    private val _placedWidgetId = MutableStateFlow<Int?>(null)
    val placedWidgetId: StateFlow<Int?> = _placedWidgetId.asStateFlow()

    val batteryInfo: StateFlow<BatteryInfo> = batteryRepository.batteryState.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = batteryRepository.currentBatteryInfo()
    )

    fun loadWidget(widgetId: String) {
        _placedWidgetId.value = null
        viewModelScope.launch {
            val loaded = widgetRepository.getConfigById(widgetId)
            _config.value = loaded
        }
    }

    /** Edit one placed widget. [fallbackPresetId] is its provider's default template. */
    fun loadPlacedWidget(appWidgetId: Int, fallbackPresetId: String) {
        _placedWidgetId.value = appWidgetId
        viewModelScope.launch {
            _config.value = widgetRepository.getConfigForWidget(appWidgetId, fallbackPresetId)
        }
    }

    fun updateAccentColor(colorHex: String) {
        _config.value = _config.value?.copy(accentColorHex = colorHex)
    }

    fun updateCornerRadius(radiusDp: Int) {
        _config.value = _config.value?.copy(cornerRadiusDp = radiusDp)
    }

    fun toggleMonochrome() {
        _config.value?.let {
            _config.value = it.copy(isMonochrome = !it.isMonochrome)
        }
    }

    fun toggleDotBackground() {
        _config.value?.let {
            _config.value = it.copy(showDotMatrixBackground = !it.showDotMatrixBackground)
        }
    }

    fun toggleGlyphBorder() {
        _config.value?.let {
            _config.value = it.copy(showGlyphBorder = !it.showGlyphBorder)
        }
    }

    fun updateCustomSubtitle(subtitle: String) {
        _config.value = _config.value?.copy(customSubtitle = subtitle)
    }

    fun saveConfig(onSuccess: () -> Unit) {
        val current = _config.value ?: return
        val placedWidgetId = _placedWidgetId.value
        viewModelScope.launch {
            if (placedWidgetId != null) {
                // current.id is the widget's template; it's the fallback if no instance row exists yet.
                widgetRepository.saveWidgetConfig(placedWidgetId, current.id, current)
            } else {
                widgetRepository.saveConfig(current)
            }
            onSuccess()
        }
    }
}
