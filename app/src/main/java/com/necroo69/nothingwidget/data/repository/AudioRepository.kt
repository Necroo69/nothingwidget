package com.necroo69.nothingwidget.data.repository

import com.necroo69.nothingwidget.domain.model.AudioTrackState
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class AudioRepository {
    private val _audioState = MutableStateFlow(AudioTrackState())
    val audioState: Flow<AudioTrackState> = _audioState.asStateFlow()

    fun togglePlayPause() {
        val curr = _audioState.value
        _audioState.value = curr.copy(isPlaying = !curr.isPlaying)
    }

    fun nextTrack() {
        val tracks = listOf(
            Triple("Mono Pulse (Nothing Remix)", "Swedish House Mafia", "Nothing Glyph Session"),
            Triple("Dot Matrix Wave", "Kiasmos", "Transparent Architecture"),
            Triple("Glyph Sync 120BPM", "Jon Hopkins", "Singularity")
        )
        val currentIdx = tracks.indexOfFirst { it.first == _audioState.value.title }
        val nextIdx = (currentIdx + 1) % tracks.size
        val nextTrack = tracks[nextIdx]
        _audioState.value = _audioState.value.copy(
            title = nextTrack.first,
            artist = nextTrack.second,
            album = nextTrack.third,
            progressMs = 0
        )
    }

    fun previousTrack() {
        val tracks = listOf(
            Triple("Mono Pulse (Nothing Remix)", "Swedish House Mafia", "Nothing Glyph Session"),
            Triple("Dot Matrix Wave", "Kiasmos", "Transparent Architecture"),
            Triple("Glyph Sync 120BPM", "Jon Hopkins", "Singularity")
        )
        val currentIdx = tracks.indexOfFirst { it.first == _audioState.value.title }
        val prevIdx = if (currentIdx <= 0) tracks.size - 1 else currentIdx - 1
        val prevTrack = tracks[prevIdx]
        _audioState.value = _audioState.value.copy(
            title = prevTrack.first,
            artist = prevTrack.second,
            album = prevTrack.third,
            progressMs = 0
        )
    }
}
