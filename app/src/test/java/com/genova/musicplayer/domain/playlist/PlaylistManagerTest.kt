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

    @Test
    fun `createPlaylist rejects names over 60 characters and supports pin and coverStyle`() {
        val longName = "A".repeat(61)
        assertIs<PlaylistValidationResult.NameTooLong>(
            manager.createPlaylist(emptyList(), longName, "pl-1", 1000L)
        )

        val valid = assertIs<PlaylistValidationResult.Success>(
            manager.createPlaylist(emptyList(), "A".repeat(60), "pl-2", 2000L, isPinned = true, coverStyle = 3)
        )
        assertEquals(true, valid.affectedPlaylist.isPinned)
        assertEquals(3, valid.affectedPlaylist.coverStyle)
    }

    @Test
    fun `sortAndFilter keeps pinned playlists first across all sort orders`() {
        val list = listOf(
            Playlist("1", "Zebra", 100L, trackIds = listOf(1L, 2L), isPinned = false),
            Playlist("2", "Alpha", 200L, trackIds = emptyList(), isPinned = true),
            Playlist("3", "Beta", 300L, trackIds = listOf(1L), isPinned = false)
        )

        val alpha = manager.sortAndFilter(list, "", PlaylistSortOrder.ALPHABETICAL)
        assertEquals(listOf("2", "3", "1"), alpha.map { it.id })

        val most = manager.sortAndFilter(list, "", PlaylistSortOrder.MOST_TRACKS)
        assertEquals(listOf("2", "1", "3"), most.map { it.id })

        val filtered = manager.sortAndFilter(list, "be", PlaylistSortOrder.RECENT)
        assertEquals(listOf("3"), filtered.map { it.id })
    }

    @Test
    fun `reorderTrack batch add and updateTrackMemberships work as expected`() {
        val initial = listOf(
            Playlist("1", "P1", 100L, trackIds = listOf(10L, 20L, 30L)),
            Playlist("2", "P2", 200L, trackIds = listOf(10L))
        )

        val reordered = assertIs<PlaylistValidationResult.Success>(
            manager.reorderTrack(initial, "1", fromIndex = 2, toIndex = 0)
        )
        assertEquals(listOf(30L, 10L, 20L), reordered.affectedPlaylist.trackIds)

        val batchAdded = assertIs<PlaylistValidationResult.Success>(
            manager.addTracksToPlaylist(reordered.playlists, "2", listOf(10L, 40L, 50L))
        )
        assertEquals(listOf(10L, 40L, 50L), batchAdded.playlists[1].trackIds)

        val memberships = manager.updateTrackMemberships(batchAdded.playlists, trackId = 10L, selectedPlaylistIds = setOf("2"))
        assertEquals(listOf(30L, 20L), memberships[0].trackIds)
        assertEquals(listOf(10L, 40L, 50L), memberships[1].trackIds)
    }
}
