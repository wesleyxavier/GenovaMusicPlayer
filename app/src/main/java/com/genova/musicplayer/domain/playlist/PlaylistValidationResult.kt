package com.genova.musicplayer.domain.playlist

sealed class PlaylistValidationResult {
    data class Success(
        val playlists: List<Playlist>,
        val affectedPlaylist: Playlist
    ) : PlaylistValidationResult()

    data object EmptyName : PlaylistValidationResult()

    data object NameTooLong : PlaylistValidationResult()

    data class DuplicateName(val normalizedName: String) : PlaylistValidationResult()

    data class PlaylistNotFound(val playlistId: String) : PlaylistValidationResult()
}
