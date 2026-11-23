package com.genova.musicplayer.domain.playlist

class PlaylistManager {

    fun createPlaylist(
        existing: List<Playlist>,
        rawName: String,
        id: String,
        createdAtEpochMs: Long,
        isPinned: Boolean = false,
        coverStyle: Int = 0
    ): PlaylistValidationResult {
        val normalized = rawName.trim()
        if (normalized.isEmpty()) {
            return PlaylistValidationResult.EmptyName
        }
        if (normalized.length > MAX_NAME_LENGTH) {
            return PlaylistValidationResult.NameTooLong
        }
        val duplicate = existing.any { it.name.trim().equals(normalized, ignoreCase = true) }
        if (duplicate) {
            return PlaylistValidationResult.DuplicateName(normalized)
        }
        val created = Playlist(
            id = id,
            name = normalized,
            createdAtEpochMs = createdAtEpochMs,
            trackIds = emptyList(),
            isPinned = isPinned,
            coverStyle = coverStyle
        )
        return PlaylistValidationResult.Success(
            playlists = existing + created,
            affectedPlaylist = created
        )
    }

    fun renamePlaylist(
        existing: List<Playlist>,
        playlistId: String,
        rawNewName: String
    ): PlaylistValidationResult {
        val normalized = rawNewName.trim()
        if (normalized.isEmpty()) {
            return PlaylistValidationResult.EmptyName
        }
        if (normalized.length > MAX_NAME_LENGTH) {
            return PlaylistValidationResult.NameTooLong
        }
        val targetIndex = existing.indexOfFirst { it.id == playlistId }
        if (targetIndex < 0) {
            return PlaylistValidationResult.PlaylistNotFound(playlistId)
        }
        val duplicate = existing.any {
            it.id != playlistId && it.name.trim().equals(normalized, ignoreCase = true)
        }
        if (duplicate) {
            return PlaylistValidationResult.DuplicateName(normalized)
        }
        val updated = existing[targetIndex].copy(name = normalized)
        val updatedList = existing.toMutableList().apply { set(targetIndex, updated) }
        return PlaylistValidationResult.Success(
            playlists = updatedList,
            affectedPlaylist = updated
        )
    }

    fun togglePin(
        existing: List<Playlist>,
        playlistId: String
    ): PlaylistValidationResult {
        val targetIndex = existing.indexOfFirst { it.id == playlistId }
        if (targetIndex < 0) {
            return PlaylistValidationResult.PlaylistNotFound(playlistId)
        }
        val target = existing[targetIndex]
        val updated = target.copy(isPinned = !target.isPinned)
        val updatedList = existing.toMutableList().apply { set(targetIndex, updated) }
        return PlaylistValidationResult.Success(
            playlists = updatedList,
            affectedPlaylist = updated
        )
    }

    fun deletePlaylist(existing: List<Playlist>, playlistId: String): List<Playlist> {
        return existing.filterNot { it.id == playlistId }
    }

    fun addTrackToPlaylist(
        existing: List<Playlist>,
        playlistId: String,
        trackId: Long
    ): PlaylistValidationResult {
        return addTracksToPlaylist(existing, playlistId, listOf(trackId))
    }

    fun addTracksToPlaylist(
        existing: List<Playlist>,
        playlistId: String,
        trackIds: List<Long>
    ): PlaylistValidationResult {
        val targetIndex = existing.indexOfFirst { it.id == playlistId }
        if (targetIndex < 0) {
            return PlaylistValidationResult.PlaylistNotFound(playlistId)
        }
        val target = existing[targetIndex]
        val currentSet = target.trackIds.toHashSet()
        val toAppend = trackIds.filter { currentSet.add(it) }
        val updated = target.copy(trackIds = target.trackIds + toAppend)
        val updatedList = existing.toMutableList().apply { set(targetIndex, updated) }
        return PlaylistValidationResult.Success(
            playlists = updatedList,
            affectedPlaylist = updated
        )
    }

    fun updateTrackMemberships(
        existing: List<Playlist>,
        trackId: Long,
        selectedPlaylistIds: Set<String>
    ): List<Playlist> {
        return existing.map { playlist ->
            val shouldContain = selectedPlaylistIds.contains(playlist.id)
            val alreadyContains = playlist.trackIds.contains(trackId)
            when {
                shouldContain && !alreadyContains -> playlist.copy(trackIds = playlist.trackIds + trackId)
                !shouldContain && alreadyContains -> playlist.copy(trackIds = playlist.trackIds.filterNot { it == trackId })
                else -> playlist
            }
        }
    }

    fun removeTrackFromPlaylist(
        existing: List<Playlist>,
        playlistId: String,
        trackId: Long
    ): PlaylistValidationResult {
        val targetIndex = existing.indexOfFirst { it.id == playlistId }
        if (targetIndex < 0) {
            return PlaylistValidationResult.PlaylistNotFound(playlistId)
        }
        val target = existing[targetIndex]
        val updated = target.copy(trackIds = target.trackIds.filterNot { it == trackId })
        val updatedList = existing.toMutableList().apply { set(targetIndex, updated) }
        return PlaylistValidationResult.Success(
            playlists = updatedList,
            affectedPlaylist = updated
        )
    }

    fun reorderTrack(
        existing: List<Playlist>,
        playlistId: String,
        fromIndex: Int,
        toIndex: Int
    ): PlaylistValidationResult {
        val targetIndex = existing.indexOfFirst { it.id == playlistId }
        if (targetIndex < 0) {
            return PlaylistValidationResult.PlaylistNotFound(playlistId)
        }
        val target = existing[targetIndex]
        val tracks = target.trackIds
        if (fromIndex !in tracks.indices || toIndex !in tracks.indices || fromIndex == toIndex) {
            return PlaylistValidationResult.Success(playlists = existing, affectedPlaylist = target)
        }
        val mutable = tracks.toMutableList()
        val moved = mutable.removeAt(fromIndex)
        mutable.add(toIndex, moved)
        val updated = target.copy(trackIds = mutable)
        val updatedList = existing.toMutableList().apply { set(targetIndex, updated) }
        return PlaylistValidationResult.Success(
            playlists = updatedList,
            affectedPlaylist = updated
        )
    }

    fun sortAndFilter(
        playlists: List<Playlist>,
        query: String,
        sortOrder: PlaylistSortOrder
    ): List<Playlist> {
        val normalizedQuery = query.trim().lowercase()
        val filtered = if (normalizedQuery.isEmpty()) {
            playlists
        } else {
            playlists.filter { it.name.lowercase().contains(normalizedQuery) }
        }
        val comparator = when (sortOrder) {
            PlaylistSortOrder.RECENT -> compareByDescending<Playlist> { it.isPinned }
                .thenByDescending { it.createdAtEpochMs }
            PlaylistSortOrder.ALPHABETICAL -> compareByDescending<Playlist> { it.isPinned }
                .thenBy { it.name.lowercase() }
            PlaylistSortOrder.MOST_TRACKS -> compareByDescending<Playlist> { it.isPinned }
                .thenByDescending { it.trackIds.size }
                .thenByDescending { it.createdAtEpochMs }
        }
        return filtered.sortedWith(comparator)
    }

    companion object {
        const val MAX_NAME_LENGTH = 60
    }
}
