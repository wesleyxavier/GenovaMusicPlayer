package com.genova.musicplayer.domain.playlist

class PlaylistManager {

    fun createPlaylist(
        existing: List<Playlist>,
        rawName: String,
        id: String,
        createdAtEpochMs: Long
    ): PlaylistValidationResult {
        val normalized = rawName.trim()
        if (normalized.isEmpty()) {
            return PlaylistValidationResult.EmptyName
        }
        val duplicate = existing.any { it.name.trim().equals(normalized, ignoreCase = true) }
        if (duplicate) {
            return PlaylistValidationResult.DuplicateName(normalized)
        }
        val created = Playlist(
            id = id,
            name = normalized,
            createdAtEpochMs = createdAtEpochMs,
            trackIds = emptyList()
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

    fun deletePlaylist(existing: List<Playlist>, playlistId: String): List<Playlist> {
        return existing.filterNot { it.id == playlistId }
    }

    fun addTrackToPlaylist(
        existing: List<Playlist>,
        playlistId: String,
        trackId: Long
    ): PlaylistValidationResult {
        val targetIndex = existing.indexOfFirst { it.id == playlistId }
        if (targetIndex < 0) {
            return PlaylistValidationResult.PlaylistNotFound(playlistId)
        }
        val target = existing[targetIndex]
        val updatedTrackIds = if (target.trackIds.contains(trackId)) {
            target.trackIds
        } else {
            target.trackIds + trackId
        }
        val updated = target.copy(trackIds = updatedTrackIds)
        val updatedList = existing.toMutableList().apply { set(targetIndex, updated) }
        return PlaylistValidationResult.Success(
            playlists = updatedList,
            affectedPlaylist = updated
        )
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
}
