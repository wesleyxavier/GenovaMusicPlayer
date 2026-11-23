package com.genova.musicplayer.domain.playlist

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

class PlaylistManagerTest {

    private val manager = PlaylistManager()

    @Test
    fun `createPlaylist trims valid name and appends to list`() {
        val result = manager.createPlaylist(
            existing = emptyList(),
            rawName = "  Treino Noturno  ",
            id = "pl-1",
            createdAtEpochMs = 1000L
        )
        val success = assertIs<PlaylistValidationResult.Success>(result)
        assertEquals("Treino Noturno", success.affectedPlaylist.name)
        assertEquals(1, success.playlists.size)
        assertEquals(emptyList(), success.affectedPlaylist.trackIds)
    }

    @Test
    fun `createPlaylist rejects empty or blank name`() {
        val result = manager.createPlaylist(
            existing = emptyList(),
            rawName = "   ",
            id = "pl-1",
            createdAtEpochMs = 1000L
        )
        assertIs<PlaylistValidationResult.EmptyName>(result)
    }

    @Test
    fun `createPlaylist rejects duplicate name ignoring case and whitespace`() {
        val initial = listOf(Playlist(id = "pl-1", name = "Favoritas", createdAtEpochMs = 1000L))
        val result = manager.createPlaylist(
            existing = initial,
            rawName = "  favoritas ",
            id = "pl-2",
            createdAtEpochMs = 2000L
        )
        val duplicate = assertIs<PlaylistValidationResult.DuplicateName>(result)
        assertEquals("favoritas", duplicate.normalizedName)
    }

    @Test
    fun `renamePlaylist validates empty and duplicate names`() {
        val initial = listOf(
            Playlist(id = "pl-1", name = "Rock", createdAtEpochMs = 1000L),
            Playlist(id = "pl-2", name = "Jazz", createdAtEpochMs = 2000L)
        )

        assertIs<PlaylistValidationResult.EmptyName>(
            manager.renamePlaylist(initial, "pl-1", "  ")
        )
        assertIs<PlaylistValidationResult.DuplicateName>(
            manager.renamePlaylist(initial, "pl-1", "jazz")
        )

        val renamed = assertIs<PlaylistValidationResult.Success>(
            manager.renamePlaylist(initial, "pl-1", "Rock Clássico")
        )
        assertEquals("Rock Clássico", renamed.affectedPlaylist.name)
        assertEquals("Jazz", renamed.playlists[1].name)
    }

    @Test
    fun `deletePlaylist removes target playlist only`() {
        val initial = listOf(
            Playlist(id = "pl-1", name = "Rock", createdAtEpochMs = 1000L),
            Playlist(id = "pl-2", name = "Jazz", createdAtEpochMs = 2000L)
        )
        val remaining = manager.deletePlaylist(initial, "pl-1")
        assertEquals(1, remaining.size)
        assertEquals("pl-2", remaining.first().id)
    }

    @Test
    fun `addTrackToPlaylist avoids duplicates and removeTrackFromPlaylist preserves order`() {
        val initial = listOf(
            Playlist(id = "pl-1", name = "Mix", createdAtEpochMs = 1000L, trackIds = listOf(10L, 20L))
        )
        val dupAdd = assertIs<PlaylistValidationResult.Success>(
            manager.addTrackToPlaylist(initial, "pl-1", 10L)
        )
        assertEquals(listOf(10L, 20L), dupAdd.affectedPlaylist.trackIds)

        val added = assertIs<PlaylistValidationResult.Success>(
            manager.addTrackToPlaylist(dupAdd.playlists, "pl-1", 30L)
        )
        assertEquals(listOf(10L, 20L, 30L), added.affectedPlaylist.trackIds)

        val removed = assertIs<PlaylistValidationResult.Success>(
            manager.removeTrackFromPlaylist(added.playlists, "pl-1", 20L)
        )
        assertEquals(listOf(10L, 30L), removed.affectedPlaylist.trackIds)
    }
}
