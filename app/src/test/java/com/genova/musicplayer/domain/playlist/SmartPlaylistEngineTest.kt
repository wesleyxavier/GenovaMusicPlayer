package com.genova.musicplayer.domain.playlist

import com.genova.musicplayer.domain.library.Track
import kotlin.test.Test
import kotlin.test.assertEquals

class SmartPlaylistEngineTest {

    private val engine = SmartPlaylistEngine()

    private fun sampleTrack(id: Long, title: String, dateAdded: Long): Track {
        return Track(
            id = id,
            title = title,
            artist = "Artist",
            album = "Album",
            durationMs = 180_000L,
            contentUri = "content://media/$id",
            filePath = "/Music/$id.mp3",
            mimeType = "audio/mpeg",
            sizeBytes = 4_000_000L,
            bitrateKbps = 320,
            sampleRateHz = 44100,
            dateAddedEpochSec = dateAdded,
            isHiFi = false
        )
    }

    @Test
    fun `buildMostPlayed orders by play count descending and limits to 50`() {
        val tracks = (1L..60L).map { sampleTrack(it, "Track $it", it * 10) }
        val counts = tracks.associate { it.id to it.id.toInt() }
        val result = engine.buildMostPlayed(tracks, counts, limit = 50)

        assertEquals(50, result.size)
        assertEquals(60L, result.first().id)
        assertEquals(11L, result.last().id)
    }

    @Test
    fun `buildRecentlyAdded orders by dateAddedEpochSec descending`() {
        val t1 = sampleTrack(1, "Antiga", 100L)
        val t2 = sampleTrack(2, "Nova", 300L)
        val t3 = sampleTrack(3, "Media", 200L)

        val result = engine.buildRecentlyAdded(listOf(t1, t2, t3))
        assertEquals(listOf(2L, 3L, 1L), result.map { it.id })
    }
}
