package com.example.nothingwidget.ui.screens.studio

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.nothingwidget.data.repository.BatteryRepository
import com.example.nothingwidget.data.repository.WidgetRepository
import com.example.nothingwidget.domain.model.BatteryInfo
import com.example.nothingwidget.domain.model.NothingWidgetConfig
import com.example.nothingwidget.domain.model.WidgetSize
import com.example.nothingwidget.domain.model.WidgetType
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class StudioBuilderState(
    val selectedType: WidgetType = WidgetType.DIGITAL_CLOCK,
    val selectedSize: WidgetSize = WidgetSize.SIZE_2X2,
    val customTitle: String = "MY COMPOSITE CARD",
    val accentHex: String = "#D71921",
    val cornerRadius: Int = 24,
    val isSavedSuccess: Boolean = false
)

class WidgetStudioViewModel(
    private val widgetRepository: WidgetRepository,
    batteryRepository: BatteryRepository
) : ViewModel() {

    private val _builderState = MutableStateFlow(StudioBuilderState())
    val builderState: StateFlow<StudioBuilderState> = _builderState.asStateFlow()

    val batteryInfo: StateFlow<BatteryInfo> = batteryRepository.batteryState.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = batteryRepository.currentBatteryInfo()
    )

    fun setWidgetType(type: WidgetType) {
        _builderState.value = _builderState.value.copy(
            selectedType = type,
            customTitle = "MY ${type.displayName.uppercase()}"
        )
    }

    fun setWidgetSize(size: WidgetSize) {
        _builderState.value = _builderState.value.copy(selectedSize = size)
    }

    fun setAccentHex(hex: String) {
        _builderState.value = _builderState.value.copy(accentHex = hex)
    }

    fun setCustomTitle(title: String) {
        _builderState.value = _builderState.value.copy(customTitle = title)
    }

    fun buildAndSaveWidget(onComplete: () -> Unit) {
        val curr = _builderState.value
        val id = "studio_${System.currentTimeMillis()}"
        val newWidget = NothingWidgetConfig(
            id = id,
            type = curr.selectedType,
            size = curr.selectedSize,
            title = curr.customTitle,
            accentColorHex = curr.accentHex,
            cornerRadiusDp = curr.cornerRadius,
            showDotMatrixBackground = true,
            showGlyphBorder = true
        )

        viewModelScope.launch {
            widgetRepository.saveConfig(newWidget)
            _builderState.value = curr.copy(isSavedSuccess = true)
            onComplete()
        }
    }
}
