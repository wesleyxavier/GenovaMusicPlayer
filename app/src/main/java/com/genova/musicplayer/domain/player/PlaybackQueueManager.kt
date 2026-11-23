package com.genova.musicplayer.domain.player

import com.genova.musicplayer.domain.library.Track
import kotlin.random.Random

/**
 * Pure Kotlin queue operations for playback sessions without Android framework dependencies.
 */
class PlaybackQueueManager {

    fun replaceQueue(
        session: PlaybackSession,
        tracks: List<Track>,
        startIndex: Int,
        originLabel: String
    ): PlaybackSession {
        if (tracks.isEmpty() || startIndex !in tracks.indices) {
            return session
        }
        val selected = tracks[startIndex]
        return session.copy(
            currentTrack = selected,
            activeList = tracks,
            currentIndex = startIndex,
            queueOriginLabel = originLabel,
            status = PlayerStatus.PREPARING,
            positionMs = 0L,
            durationMs = selected.durationMs,
            errorMessage = null
        )
    }

    fun appendTracks(
        session: PlaybackSession,
        tracksToAdd: List<Track>,
        fallbackOrigin: String
    ): PlaybackSession {
        if (tracksToAdd.isEmpty()) return session
        if (session.activeList.isEmpty() || session.currentTrack == null || session.currentIndex !in session.activeList.indices) {
            val first = tracksToAdd.first()
            return session.copy(
                currentTrack = first,
                activeList = tracksToAdd,
                currentIndex = 0,
                queueOriginLabel = fallbackOrigin,
                status = PlayerStatus.PREPARING,
                positionMs = 0L,
                durationMs = first.durationMs,
                errorMessage = null
            )
        }
        return session.copy(activeList = session.activeList + tracksToAdd)
    }

    fun enqueueAsLastAndPlay(
        session: PlaybackSession,
        track: Track,
        fallbackOrigin: String
    ): PlaybackSession {
        if (session.activeList.isEmpty() || session.currentTrack == null) {
            return session.copy(
                currentTrack = track,
                activeList = listOf(track),
                currentIndex = 0,
                queueOriginLabel = fallbackOrigin,
                status = PlayerStatus.PREPARING,
                positionMs = 0L,
                durationMs = track.durationMs,
                errorMessage = null
            )
        }
        val updated = session.activeList + track
        return session.copy(
            currentTrack = track,
            activeList = updated,
            currentIndex = updated.lastIndex,
            status = PlayerStatus.PREPARING,
            positionMs = 0L,
            durationMs = track.durationMs,
            errorMessage = null
        )
    }

    fun removeAt(session: PlaybackSession, index: Int): PlaybackSession {
        val list = session.activeList
        if (index !in list.indices) return session

        val updated = list.toMutableList().apply { removeAt(index) }
        if (updated.isEmpty()) {
            return session.copy(
                currentTrack = null,
                activeList = emptyList(),
                currentIndex = -1,
                status = PlayerStatus.IDLE,
                positionMs = 0L,
                durationMs = 0L,
                errorMessage = null
            )
        }

        val currentIdx = session.currentIndex
        return when {
            index < currentIdx -> {
                val newIdx = (currentIdx - 1).coerceIn(0, updated.lastIndex)
                session.copy(
                    activeList = updated,
                    currentIndex = newIdx,
                    currentTrack = updated[newIdx]
                )
            }
            index == currentIdx -> {
                val newIdx = index.coerceAtMost(updated.lastIndex)
                val nextTrack = updated[newIdx]
                session.copy(
                    activeList = updated,
                    currentIndex = newIdx,
                    currentTrack = nextTrack,
                    status = PlayerStatus.PREPARING,
                    positionMs = 0L,
                    durationMs = nextTrack.durationMs,
                    errorMessage = null
                )
            }
            else -> session.copy(activeList = updated)
        }
    }

    fun moveItem(session: PlaybackSession, fromIndex: Int, toIndex: Int): PlaybackSession {
        val list = session.activeList
        if (fromIndex !in list.indices || toIndex !in list.indices || fromIndex == toIndex) {
            return session
        }

        val updated = list.toMutableList()
        val moved = updated.removeAt(fromIndex)
        updated.add(toIndex, moved)

        val currentIdx = session.currentIndex
        val newCurrentIdx = when {
            currentIdx == fromIndex -> toIndex
            fromIndex < currentIdx && toIndex >= currentIdx -> currentIdx - 1
            fromIndex > currentIdx && toIndex <= currentIdx -> currentIdx + 1
            else -> currentIdx
        }

        return session.copy(
            activeList = updated,
            currentIndex = newCurrentIdx,
            currentTrack = updated.getOrNull(newCurrentIdx)
        )
    }

    fun selectIndex(session: PlaybackSession, index: Int): PlaybackSession {
        val list = session.activeList
        if (index !in list.indices) return session
        val selected = list[index]
        return session.copy(
            currentTrack = selected,
            currentIndex = index,
            status = PlayerStatus.PREPARING,
            positionMs = 0L,
            durationMs = selected.durationMs,
            errorMessage = null
        )
    }

    fun clearUpcoming(session: PlaybackSession): PlaybackSession {
        val current = session.currentTrack
        return if (current != null && session.currentIndex in session.activeList.indices) {
            session.copy(
                activeList = listOf(current),
                currentIndex = 0
            )
        } else {
            session.copy(
                activeList = emptyList(),
                currentIndex = -1,
                currentTrack = null,
                status = PlayerStatus.IDLE,
                positionMs = 0L,
                durationMs = 0L
            )
        }
    }

    fun resolveNextIndex(
        session: PlaybackSession,
        fromCompletion: Boolean,
        random: Random = Random.Default
    ): Int? {
        val list = session.activeList
        if (list.isEmpty()) return null
        if (fromCompletion && session.repeatMode == RepeatMode.REPEAT_ONE) {
            return session.currentIndex.takeIf { it in list.indices } ?: 0
        }
        if (session.isShuffleEnabled && list.size > 1) {
            var candidate = random.nextInt(list.size)
            if (candidate == session.currentIndex) {
                candidate = (candidate + 1) % list.size
            }
            return candidate
        }
        val next = session.currentIndex + 1
        return when {
            next < list.size -> next
            session.repeatMode == RepeatMode.REPEAT_ALL -> 0
            !fromCompletion && list.isNotEmpty() -> 0
            else -> null
        }
    }

    fun resolvePreviousIndex(session: PlaybackSession): Int? {
        val list = session.activeList
        if (list.isEmpty()) return null
        val prev = session.currentIndex - 1
        return if (prev >= 0) prev else list.lastIndex
    }
}
