package com.genova.musicplayer.domain.player

import com.genova.musicplayer.domain.library.Track
import java.util.concurrent.CopyOnWriteArraySet
import kotlin.random.Random

/**
 * Global singleton holding the playback session state and notifying observers via callbacks.
 */
object PlayerStateStore {

    private val lock = Any()
    private val listeners = CopyOnWriteArraySet<(PlaybackSession) -> Unit>()
    private val queueManager = PlaybackQueueManager()

    @Volatile
    private var session: PlaybackSession = PlaybackSession()

    fun getState(): PlaybackSession = session

    fun addListener(listener: (PlaybackSession) -> Unit) {
        listeners.add(listener)
    }

    fun removeListener(listener: (PlaybackSession) -> Unit) {
        listeners.remove(listener)
    }

    fun startPreparing(
        track: Track,
        activeList: List<Track>,
        index: Int,
        originLabel: String = "Biblioteca"
    ) {
        updateSession { current ->
            val resolvedIndex = if (index in activeList.indices) index else activeList.indexOfFirst { it.id == track.id }
            if (resolvedIndex in activeList.indices) {
                queueManager.replaceQueue(current, activeList, resolvedIndex, originLabel)
            } else {
                queueManager.replaceQueue(current, listOf(track), 0, originLabel)
            }
        }
    }

    fun appendToQueue(tracks: List<Track>, fallbackOrigin: String = "Fila personalizada"): PlaybackSession {
        return updateSession { current ->
            queueManager.appendTracks(current, tracks, fallbackOrigin)
        }
    }

    fun enqueueAsLastAndPlay(track: Track, fallbackOrigin: String = "Biblioteca"): PlaybackSession {
        return updateSession { current ->
            queueManager.enqueueAsLastAndPlay(current, track, fallbackOrigin)
        }
    }

    fun removeFromQueue(index: Int): PlaybackSession {
        return updateSession { current ->
            queueManager.removeAt(current, index)
        }
    }

    fun moveQueueItem(fromIndex: Int, toIndex: Int): PlaybackSession {
        return updateSession { current ->
            queueManager.moveItem(current, fromIndex, toIndex)
        }
    }

    fun playQueueIndex(index: Int): PlaybackSession {
        return updateSession { current ->
            queueManager.selectIndex(current, index)
        }
    }

    fun clearUpcomingQueue(): PlaybackSession {
        return updateSession { current ->
            queueManager.clearUpcoming(current)
        }
    }

    fun setAutoPlayAfterQueue(enabled: Boolean) {
        updateSession { current ->
            if (current.autoPlayAfterQueue == enabled) current
            else current.copy(autoPlayAfterQueue = enabled)
        }
    }

    fun onPrepared(durationMs: Long) {
        updateSession { current ->
            if (current.status == PlayerStatus.IDLE) {
                current
            } else {
                current.copy(
                    status = PlayerStatus.PLAYING,
                    durationMs = if (durationMs > 0L) durationMs else current.durationMs,
                    errorMessage = null
                )
            }
        }
    }

    fun onPaused() {
        updateSession { current ->
            if (current.status == PlayerStatus.PLAYING || current.status == PlayerStatus.PREPARING) {
                current.copy(status = PlayerStatus.PAUSED)
            } else {
                current
            }
        }
    }

    fun onResumed() {
        updateSession { current ->
            if (current.status == PlayerStatus.PAUSED || current.status == PlayerStatus.COMPLETED) {
                current.copy(status = PlayerStatus.PLAYING, errorMessage = null)
            } else {
                current
            }
        }
    }

    fun onPositionUpdated(positionMs: Long) {
        updateSession { current ->
            val clamped = positionMs.coerceIn(0L, current.durationMs.coerceAtLeast(0L))
            if (current.positionMs == clamped) current else current.copy(positionMs = clamped)
        }
    }

    fun onCompleted() {
        updateSession { current ->
            current.copy(
                status = PlayerStatus.COMPLETED,
                positionMs = current.durationMs
            )
        }
    }

    fun onError(message: String) {
        updateSession { current ->
            current.copy(
                status = PlayerStatus.ERROR,
                errorMessage = message
            )
        }
    }

    fun toggleShuffle(): Boolean {
        val updated = updateSession { current ->
            current.copy(isShuffleEnabled = !current.isShuffleEnabled)
        }
        return updated.isShuffleEnabled
    }

    fun cycleRepeatMode(): RepeatMode {
        val updated = updateSession { current ->
            val nextMode = when (current.repeatMode) {
                RepeatMode.OFF -> RepeatMode.REPEAT_ALL
                RepeatMode.REPEAT_ALL -> RepeatMode.REPEAT_ONE
                RepeatMode.REPEAT_ONE -> RepeatMode.OFF
            }
            current.copy(repeatMode = nextMode)
        }
        return updated.repeatMode
    }

    fun updateAudioOutput(outputName: String) {
        updateSession { current ->
            if (current.audioOutputName == outputName) current
            else current.copy(audioOutputName = outputName)
        }
    }

    fun resolveNextIndex(fromCompletion: Boolean, random: Random = Random.Default): Int? {
        return queueManager.resolveNextIndex(session, fromCompletion, random)
    }

    fun resolvePreviousIndex(): Int? {
        return queueManager.resolvePreviousIndex(session)
    }

    fun reset() {
        updateSession { PlaybackSession() }
    }

    private inline fun updateSession(transform: (PlaybackSession) -> PlaybackSession): PlaybackSession {
        val next: PlaybackSession
        synchronized(lock) {
            next = transform(session)
            session = next
        }
        listeners.forEach { it(next) }
        return next
    }
}
