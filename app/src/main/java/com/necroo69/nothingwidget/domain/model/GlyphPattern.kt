package com.necroo69.nothingwidget.domain.model

data class GlyphPattern(
    val id: String,
    val name: String,
    val description: String,
    val topRingIntensity: Float = 1.0f,
    val cameraRingIntensity: Float = 0.8f,
    val diagonalStripIntensity: Float = 0.6f,
    val bottomExclamationIntensity: Float = 1.0f,
    val pulseFrequencyHz: Float = 2.0f,
    val isAudioReactive: Boolean = false,
    val brightnessLevel: Float = 0.9f
)
