package com.genova.musicplayer.domain.player

import com.genova.musicplayer.domain.library.Track
import kotlin.random.Random
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class PlayerStateTransitionsTest {

    private val sampleTracks = listOf(
        Track(
            id = 1L,
            contentUri = "content://media/external/audio/media/1",
            title = "Faixa Um",
            artist = "Artista A",
            album = "Álbum 1",
            durationMs = 180_000L,
            dateAddedEpochSec = 100L,
            filePath = "/storage/emulated/0/Music/a.flac",
            sizeBytes = 24_000_000L,
            mimeType = "audio/flac"
        ),
        Track(
            id = 2L,
            contentUri = "content://media/external/audio/media/2",
            title = "Faixa Dois",
            artist = "Artista B",
            album = "Álbum 2",
            durationMs = 210_000L,
            dateAddedEpochSec = 200L,
            filePath = "/storage/emulated/0/Music/b.mp3",
            sizeBytes = 5_000_000L,
            mimeType = "audio/mpeg",
            bitrateKbps = 192
        )
    )

    @BeforeTest
    fun setUp() {
        PlayerStateStore.reset()
    }

    @Test
    fun `idle to preparing to playing to paused to resumed transitions`() {
        assertEquals(PlayerStatus.IDLE, PlayerStateStore.getState().status)

        PlayerStateStore.startPreparing(sampleTracks[0], sampleTracks, 0)
        assertEquals(PlayerStatus.PREPARING, PlayerStateStore.getState().status)
        assertTrue(sampleTracks[0].isHiFi)

        PlayerStateStore.onPrepared(182_000L)
        assertEquals(PlayerStatus.PLAYING, PlayerStateStore.getState().status)
        assertEquals(182_000L, PlayerStateStore.getState().durationMs)

        PlayerStateStore.onPaused()
        assertEquals(PlayerStatus.PAUSED, PlayerStateStore.getState().status)

        PlayerStateStore.onResumed()
        assertEquals(PlayerStatus.PLAYING, PlayerStateStore.getState().status)
    }

    @Test
    fun `repeat mode cycles OFF to REPEAT_ALL to REPEAT_ONE to OFF`() {
        assertEquals(RepeatMode.OFF, PlayerStateStore.getState().repeatMode)
        assertEquals(RepeatMode.REPEAT_ALL, PlayerStateStore.cycleRepeatMode())
        assertEquals(RepeatMode.REPEAT_ONE, PlayerStateStore.cycleRepeatMode())
        assertEquals(RepeatMode.OFF, PlayerStateStore.cycleRepeatMode())
    }

    @Test
    fun `shuffle toggles and avoids repeating same index when list has multiple tracks`() {
        PlayerStateStore.startPreparing(sampleTracks[0], sampleTracks, 0)
        assertFalse(PlayerStateStore.getState().isShuffleEnabled)

        assertTrue(PlayerStateStore.toggleShuffle())
        val next = PlayerStateStore.resolveNextIndex(fromCompletion = false, random = Random(0))
        assertEquals(1, next)
    }

    @Test
    fun `completion respects repeat modes at end of list`() {
        PlayerStateStore.startPreparing(sampleTracks[1], sampleTracks, 1)
        PlayerStateStore.onPrepared(210_000L)

        // Repeat OFF at last track -> null (completed)
        assertNull(PlayerStateStore.resolveNextIndex(fromCompletion = true))

        // Repeat ALL at last track -> wraps to 0
        PlayerStateStore.cycleRepeatMode()
        assertEquals(0, PlayerStateStore.resolveNextIndex(fromCompletion = true))

        // Repeat ONE -> stays on 1
        PlayerStateStore.cycleRepeatMode()
        assertEquals(1, PlayerStateStore.resolveNextIndex(fromCompletion = true))
    }

    @Test
    fun `error state stores message and clears on next prepare`() {
        PlayerStateStore.startPreparing(sampleTracks[0], sampleTracks, 0)
        PlayerStateStore.onError("Não foi possível reproduzir esta faixa.")
        assertEquals(PlayerStatus.ERROR, PlayerStateStore.getState().status)
        assertEquals("Não foi possível reproduzir esta faixa.", PlayerStateStore.getState().errorMessage)

        PlayerStateStore.startPreparing(sampleTracks[1], sampleTracks, 1)
        assertEquals(PlayerStatus.PREPARING, PlayerStateStore.getState().status)
        assertNull(PlayerStateStore.getState().errorMessage)
    }
}
