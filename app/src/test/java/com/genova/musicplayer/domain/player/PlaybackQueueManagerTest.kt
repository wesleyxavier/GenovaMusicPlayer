package com.genova.musicplayer.domain.player

import com.genova.musicplayer.domain.library.Track
import kotlin.random.Random
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class PlaybackQueueManagerTest {

    private val manager = PlaybackQueueManager()

    private fun sampleTrack(id: Long): Track {
        return Track(
            id = id,
            title = "Track $id",
            artist = "Artist $id",
            album = "Album",
            durationMs = 180_000L,
            contentUri = "content://media/$id",
            filePath = "/Music/track_$id.flac",
            mimeType = "audio/flac",
            sizeBytes = 10_000_000L,
            bitrateKbps = 1411,
            sampleRateHz = 44100,
            dateAddedEpochSec = id,
            isHiFi = true
        )
    }

    @Test
    fun `replaceQueue sets activeList currentIndex and origin`() {
        val tracks = listOf(sampleTrack(1), sampleTrack(2), sampleTrack(3))
        val session = manager.replaceQueue(PlaybackSession(), tracks, 1, "Minha Playlist")

        assertEquals(tracks, session.activeList)
        assertEquals(1, session.currentIndex)
        assertEquals(2L, session.currentTrack?.id)
        assertEquals("Minha Playlist", session.queueOriginLabel)
    }

    @Test
    fun `appendTracks initializes idle session or appends to active queue`() {
        val t1 = sampleTrack(1)
        val t2 = sampleTrack(2)
        val t3 = sampleTrack(3)

        val started = manager.appendTracks(PlaybackSession(), listOf(t1), "Fila personalizada")
        assertEquals(0, started.currentIndex)
        assertEquals(listOf(t1), started.activeList)

        val appended = manager.appendTracks(started, listOf(t2, t3), "Outro")
        assertEquals(0, appended.currentIndex)
        assertEquals(listOf(t1, t2, t3), appended.activeList)
        assertEquals("Fila personalizada", appended.queueOriginLabel)
    }

    @Test
    fun `removeAt adjusts currentIndex when removing before at and after`() {
        val tracks = listOf(sampleTrack(1), sampleTrack(2), sampleTrack(3), sampleTrack(4))
        var session = manager.replaceQueue(PlaybackSession(), tracks, 2, "Biblioteca")

        session = manager.removeAt(session, 0)
        assertEquals(1, session.currentIndex)
        assertEquals(3L, session.currentTrack?.id)

        session = manager.removeAt(session, 2)
        assertEquals(1, session.currentIndex)
        assertEquals(listOf(2L, 3L), session.activeList.map { it.id })

        session = manager.removeAt(session, 1)
        assertEquals(0, session.currentIndex)
        assertEquals(2L, session.currentTrack?.id)
    }

    @Test
    fun `moveItem preserves active track index across reorder`() {
        val tracks = listOf(sampleTrack(1), sampleTrack(2), sampleTrack(3), sampleTrack(4))
        var session = manager.replaceQueue(PlaybackSession(), tracks, 1, "Biblioteca")

        session = manager.moveItem(session, fromIndex = 3, toIndex = 0)
        assertEquals(listOf(4L, 1L, 2L, 3L), session.activeList.map { it.id })
        assertEquals(2, session.currentIndex)
        assertEquals(2L, session.currentTrack?.id)

        session = manager.moveItem(session, fromIndex = 2, toIndex = 0)
        assertEquals(listOf(2L, 4L, 1L, 3L), session.activeList.map { it.id })
        assertEquals(0, session.currentIndex)
    }

    @Test
    fun `selectIndex switches current track immediately`() {
        val tracks = listOf(sampleTrack(1), sampleTrack(2), sampleTrack(3))
        val initial = manager.replaceQueue(PlaybackSession(), tracks, 0, "Biblioteca")
        val selected = manager.selectIndex(initial, 2)

        assertEquals(2, selected.currentIndex)
        assertEquals(3L, selected.currentTrack?.id)
        assertEquals(PlayerStatus.PREPARING, selected.status)
    }

    @Test
    fun `clearUpcoming keeps only current track`() {
        val tracks = listOf(sampleTrack(1), sampleTrack(2), sampleTrack(3))
        val initial = manager.replaceQueue(PlaybackSession(), tracks, 1, "Biblioteca")
        val cleared = manager.clearUpcoming(initial)

        assertEquals(listOf(2L), cleared.activeList.map { it.id })
        assertEquals(0, cleared.currentIndex)
        assertEquals(2L, cleared.currentTrack?.id)
    }

    @Test
    fun `resolveNextIndex respects repeat off and repeat all`() {
        val tracks = listOf(sampleTrack(1), sampleTrack(2))
        val atEnd = manager.replaceQueue(PlaybackSession(), tracks, 1, "Biblioteca")

        assertNull(manager.resolveNextIndex(atEnd, fromCompletion = true, random = Random(0)))
        val repeatAll = atEnd.copy(repeatMode = RepeatMode.REPEAT_ALL)
        assertEquals(0, manager.resolveNextIndex(repeatAll, fromCompletion = true, random = Random(0)))
    }

    @Test
    fun `enqueueAsLastAndPlay creates single-track queue when idle and appends as last when active`() {
        val t1 = sampleTrack(1)
        val t2 = sampleTrack(2)

        val first = manager.enqueueAsLastAndPlay(PlaybackSession(), t1, "Biblioteca")
        assertEquals(listOf(1L), first.activeList.map { it.id })
        assertEquals(0, first.currentIndex)
        assertEquals(1L, first.currentTrack?.id)

        val second = manager.enqueueAsLastAndPlay(first, t2, "Biblioteca")
        assertEquals(listOf(1L, 2L), second.activeList.map { it.id })
        assertEquals(1, second.currentIndex)
        assertEquals(2L, second.currentTrack?.id)
    }
}
