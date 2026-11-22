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

    @Volatile
    private var session: PlaybackSession = PlaybackSession()

    fun getState(): PlaybackSession = session

    fun addListener(listener: (PlaybackSession) -> Unit) {
        listeners.add(listener)
    }

    fun removeListener(listener: (PlaybackSession) -> Unit) {
        listeners.remove(listener)
    }

    fun startPreparing(track: Track, activeList: List<Track>, index: Int) {
        updateSession { current ->
            current.copy(
                currentTrack = track,
                activeList = activeList,
                currentIndex = index,
                status = PlayerStatus.PREPARING,
                positionMs = 0L,
                durationMs = track.durationMs,
                errorMessage = null
            )
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
        val current = session
        val list = current.activeList
        if (list.isEmpty()) return null
        if (fromCompletion && current.repeatMode == RepeatMode.REPEAT_ONE) {
            return current.currentIndex.takeIf { it in list.indices } ?: 0
        }
        if (current.isShuffleEnabled && list.size > 1) {
            var candidate = random.nextInt(list.size)
            if (candidate == current.currentIndex) {
                candidate = (candidate + 1) % list.size
            }
            return candidate
        }
        val next = current.currentIndex + 1
        return when {
            next < list.size -> next
            current.repeatMode == RepeatMode.REPEAT_ALL -> 0
            !fromCompletion && list.isNotEmpty() -> 0
            else -> null
        }
    }

    fun resolvePreviousIndex(): Int? {
        val current = session
        val list = current.activeList
        if (list.isEmpty()) return null
        val prev = current.currentIndex - 1
        return if (prev >= 0) prev else list.lastIndex
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
