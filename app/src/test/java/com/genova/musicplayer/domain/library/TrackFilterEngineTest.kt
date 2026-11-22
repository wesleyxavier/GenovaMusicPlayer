package com.genova.musicplayer.domain.library

import kotlin.test.Test
import kotlin.test.assertEquals

class TrackFilterEngineTest {

    private val tracks = listOf(
        Track(
            id = 1L,
            contentUri = "content://1",
            title = "Midnight Echoes",
            artist = "Nocturne Band",
            album = "Night",
            durationMs = 200_000L,
            dateAddedEpochSec = 100L,
            filePath = "/storage/emulated/0/Music/a.mp3",
            sizeBytes = 4_000_000L,
            mimeType = "audio/mpeg"
        ),
        Track(
            id = 2L,
            contentUri = "content://2",
            title = "Aurora",
            artist = "Silva",
            album = "Dawn",
            durationMs = 180_000L,
            dateAddedEpochSec = 300L,
            filePath = "/storage/emulated/0/Music/b.flac",
            sizeBytes = 20_000_000L,
            mimeType = "audio/flac"
        ),
        Track(
            id = 3L,
            contentUri = "content://3",
            title = "Solar Wind",
            artist = "Nocturne Band",
            album = "Cosmos",
            durationMs = 240_000L,
            dateAddedEpochSec = 200L,
            filePath = "/storage/emulated/0/Music/c.mp3",
            sizeBytes = 5_000_000L,
            mimeType = "audio/mpeg"
        )
    )

    @Test
    fun `ALL filter sorts alphabetically by title`() {
        val result = TrackFilterEngine.filterAndSort(tracks, "", LibraryFilter.ALL)
        assertEquals(listOf(2L, 1L, 3L), result.map { it.id })
    }

    @Test
    fun `RECENT filter sorts by dateAddedEpochSec descending`() {
        val result = TrackFilterEngine.filterAndSort(tracks, "", LibraryFilter.RECENT)
        assertEquals(listOf(2L, 3L, 1L), result.map { it.id })
    }

    @Test
    fun `query filters case-insensitively by title or artist`() {
        val byArtist = TrackFilterEngine.filterAndSort(tracks, "nocturne", LibraryFilter.ALL)
        assertEquals(listOf(1L, 3L), byArtist.map { it.id })

        val byTitle = TrackFilterEngine.filterAndSort(tracks, "  AURORA ", LibraryFilter.RECENT)
        assertEquals(listOf(2L), byTitle.map { it.id })
    }
}
