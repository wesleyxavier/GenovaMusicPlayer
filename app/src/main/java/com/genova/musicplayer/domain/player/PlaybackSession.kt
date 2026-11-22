package com.genova.musicplayer.domain.player

import com.genova.musicplayer.domain.library.Track

/**
 * Explicit MediaPlayer lifecycle states.
 */
enum class PlayerStatus {
    IDLE,
    PREPARING,
    PLAYING,
    PAUSED,
    COMPLETED,
    ERROR
}

/**
 * Repeat modes supported in MVP Part 1.
 */
enum class RepeatMode {
    OFF,
    REPEAT_ALL,
    REPEAT_ONE
}

/**
 * Immutable snapshot of the global playback state.
 */
data class PlaybackSession(
    val currentTrack: Track? = null,
    val activeList: List<Track> = emptyList(),
    val currentIndex: Int = -1,
    val status: PlayerStatus = PlayerStatus.IDLE,
    val positionMs: Long = 0L,
    val durationMs: Long = 0L,
    val isShuffleEnabled: Boolean = false,
    val repeatMode: RepeatMode = RepeatMode.OFF,
    val audioOutputName: String = "Alto-falante",
    val errorMessage: String? = null
)
