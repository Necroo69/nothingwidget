package com.example.nothingwidget.ui.screens.gallery

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.nothingwidget.data.repository.AudioRepository
import com.example.nothingwidget.data.repository.BatteryRepository
import com.example.nothingwidget.data.repository.QuickSettingsRepository
import com.example.nothingwidget.data.repository.StepTrackerRepository
import com.example.nothingwidget.data.repository.WeatherRepository
import com.example.nothingwidget.data.repository.WidgetRepository
import com.example.nothingwidget.domain.model.AudioTrackState
import com.example.nothingwidget.domain.model.BatteryInfo
import com.example.nothingwidget.domain.model.NothingWidgetConfig
import com.example.nothingwidget.domain.model.QuickToggleState
import com.example.nothingwidget.domain.model.WeatherInfo
import com.example.nothingwidget.domain.model.WidgetType
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class GalleryUiState(
    val widgets: List<NothingWidgetConfig> = emptyList(),
    val selectedCategory: String = "All",
    val weatherInfo: WeatherInfo = WeatherInfo(),
    val batteryInfo: BatteryInfo = BatteryInfo(),
    val toggleState: QuickToggleState = QuickToggleState(),
    val audioState: AudioTrackState = AudioTrackState(),
    val stepCount: Int = 8420
)

class WidgetGalleryViewModel(
    private val widgetRepository: WidgetRepository,
    private val weatherRepository: WeatherRepository,
    private val batteryRepository: BatteryRepository,
    private val quickSettingsRepository: QuickSettingsRepository,
    private val stepTrackerRepository: StepTrackerRepository,
    private val audioRepository: AudioRepository
) : ViewModel() {

    private val _selectedCategory = MutableStateFlow("All")

    val uiState: StateFlow<GalleryUiState> = combine(
        widgetRepository.allConfigs,
        _selectedCategory,
        weatherRepository.weatherState,
        batteryRepository.batteryState,
        quickSettingsRepository.toggleState,
        audioRepository.audioState
    ) { args: Array<Any> ->
        @Suppress("UNCHECKED_CAST")
        val configs = args[0] as List<NothingWidgetConfig>
        val category = args[1] as String
        val weather = args[2] as WeatherInfo
        val battery = args[3] as BatteryInfo
        val toggles = args[4] as QuickToggleState
        val audio = args[5] as AudioTrackState

        val filtered = if (category == "All") {
            configs
        } else {
            configs.filter { it.type.category.equals(category, ignoreCase = true) }
        }
        GalleryUiState(
            widgets = filtered,
            selectedCategory = category,
            weatherInfo = weather,
            batteryInfo = battery,
            toggleState = toggles,
            audioState = audio
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = GalleryUiState()
    )

    fun selectCategory(category: String) {
        _selectedCategory.value = category
    }

    fun toggleQuickToggle(toggleType: String) {
        when (toggleType) {
            "wifi" -> quickSettingsRepository.toggleWifi()
            "bluetooth" -> quickSettingsRepository.toggleBluetooth()
            "flashlight" -> quickSettingsRepository.toggleFlashlight()
            "airplane" -> quickSettingsRepository.toggleAirplaneMode()
        }
    }

    fun toggleAudioPlay() {
        audioRepository.togglePlayPause()
    }

    fun nextAudio() {
        audioRepository.nextTrack()
    }

    fun prevAudio() {
        audioRepository.previousTrack()
    }
}
