package com.necroo69.nothingwidget.domain.model

data class AudioTrackState(
    val title: String = "Mono Pulse (Nothing Remix)",
    val artist: String = "Swedish House Mafia",
    val album: String = "Nothing Glyph Session",
    val isPlaying: Boolean = false,
    val progressMs: Long = 84000,
    val durationMs: Long = 210000,
    val volumePercent: Int = 75,
    val spectrumFrequencies: List<Float> = listOf(0.4f, 0.7f, 0.9f, 0.5f, 0.8f, 0.3f, 0.6f, 0.95f, 0.4f, 0.2f)
)
