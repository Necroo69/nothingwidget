package com.necroo69.nothingwidget.ui.screens.glyph

import androidx.lifecycle.ViewModel
import com.necroo69.nothingwidget.domain.model.GlyphPattern
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class GlyphStudioViewModel : ViewModel() {

    private val _patterns = listOf(
        GlyphPattern("p1", "CHARGING INDICATOR", "Dynamic battery percentage fill ring"),
        GlyphPattern("p2", "AUDIO VISUALIZER", "Dynamic audio frequency reactive pulse", isAudioReactive = true),
        GlyphPattern("p3", "ESSENTIAL NOTIFICATION", "Persistent corner LED indicator"),
        GlyphPattern("p4", "CAMERA FLASH RING", "360-degree soft fill light ring")
    )

    private val _selectedPattern = MutableStateFlow(_patterns.first())
    val selectedPattern: StateFlow<GlyphPattern> = _selectedPattern.asStateFlow()

    val availablePatterns: List<GlyphPattern> = _patterns

    fun selectPattern(pattern: GlyphPattern) {
        _selectedPattern.value = pattern
    }

    fun updateTopRing(intensity: Float) {
        _selectedPattern.value = _selectedPattern.value.copy(topRingIntensity = intensity)
    }

    fun updateCameraRing(intensity: Float) {
        _selectedPattern.value = _selectedPattern.value.copy(cameraRingIntensity = intensity)
    }

    fun updateDiagonalStrip(intensity: Float) {
        _selectedPattern.value = _selectedPattern.value.copy(diagonalStripIntensity = intensity)
    }

    fun updateBottomExclamation(intensity: Float) {
        _selectedPattern.value = _selectedPattern.value.copy(bottomExclamationIntensity = intensity)
    }
}
