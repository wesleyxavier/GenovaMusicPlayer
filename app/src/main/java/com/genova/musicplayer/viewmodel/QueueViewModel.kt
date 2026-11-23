package com.genova.musicplayer.viewmodel

import com.genova.musicplayer.domain.library.Track
import com.genova.musicplayer.domain.player.PlaybackSession
import com.genova.musicplayer.domain.player.PlayerStateStore

data class QueueUiState(
    val session: PlaybackSession = PlaybackSession(),
    val queueTracks: List<Track> = emptyList(),
    val currentIndex: Int = -1,
    val remainingCount: Int = 0,
    val remainingDurationMs: Long = 0L
)

class QueueViewModel {

    var onStateChanged: ((QueueUiState) -> Unit)? = null

    private var currentState = QueueUiState()

    fun getState(): QueueUiState = currentState

    fun onSessionUpdated(session: PlaybackSession) {
        val upcoming = if (session.currentIndex in session.activeList.indices) {
            session.activeList.drop(session.currentIndex + 1)
        } else {
            session.activeList
        }
        val remainingDuration = upcoming.sumOf { it.durationMs.coerceAtLeast(0L) }
        currentState = QueueUiState(
            session = session,
            queueTracks = session.activeList,
            currentIndex = session.currentIndex,
            remainingCount = upcoming.size,
            remainingDurationMs = remainingDuration
        )
        onStateChanged?.invoke(currentState)
    }

    fun playQueueItem(position: Int): Boolean {
        val session = PlayerStateStore.getState()
        if (position !in session.activeList.indices) return false
        PlayerStateStore.playQueueIndex(position)
        return true
    }

    fun removeQueueItem(position: Int) {
        PlayerStateStore.removeFromQueue(position)
    }

    fun moveQueueItem(fromPosition: Int, toPosition: Int) {
        PlayerStateStore.moveQueueItem(fromPosition, toPosition)
    }

    fun clearUpcoming() {
        PlayerStateStore.clearUpcomingQueue()
    }

    fun appendTracks(tracks: List<Track>, originLabel: String) {
        PlayerStateStore.appendToQueue(tracks, originLabel)
    }

    fun setAutoPlayAfterQueue(enabled: Boolean) {
        PlayerStateStore.setAutoPlayAfterQueue(enabled)
    }
}
