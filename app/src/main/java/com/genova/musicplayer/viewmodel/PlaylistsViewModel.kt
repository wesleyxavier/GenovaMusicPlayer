package com.genova.musicplayer.viewmodel

import com.genova.musicplayer.data.PlaylistPreferencesRepository
import com.genova.musicplayer.domain.playlist.Playlist
import com.genova.musicplayer.domain.playlist.PlaylistManager
import com.genova.musicplayer.domain.playlist.PlaylistValidationResult
import java.util.UUID

sealed class PlaylistFeedback {
    data object EmptyName : PlaylistFeedback()
    data class DuplicateName(val name: String) : PlaylistFeedback()
    data class TrackAdded(val playlistName: String) : PlaylistFeedback()
    data object TrackRemoved : PlaylistFeedback()
}

data class PlaylistsUiState(
    val playlists: List<Playlist> = emptyList(),
    val feedback: PlaylistFeedback? = null
)

class PlaylistsViewModel(
    private val repository: PlaylistPreferencesRepository,
    private val manager: PlaylistManager = PlaylistManager()
) {
    private var state = PlaylistsUiState()
    var onStateChanged: ((PlaylistsUiState) -> Unit)? = null

    fun getState(): PlaylistsUiState = state

    fun loadPlaylists() {
        val loaded = repository.loadAll()
        updateState(state.copy(playlists = loaded))
    }

    fun createPlaylist(rawName: String): Playlist? {
        val result = manager.createPlaylist(
            existing = state.playlists,
            rawName = rawName,
            id = UUID.randomUUID().toString(),
            createdAtEpochMs = System.currentTimeMillis()
        )
        return handleValidationResult(result)
    }

    fun renamePlaylist(playlistId: String, rawNewName: String): Playlist? {
        val result = manager.renamePlaylist(
            existing = state.playlists,
            playlistId = playlistId,
            rawNewName = rawNewName
        )
        return handleValidationResult(result)
    }

    fun deletePlaylist(playlistId: String) {
        val remaining = manager.deletePlaylist(state.playlists, playlistId)
        repository.saveAll(remaining)
        updateState(state.copy(playlists = remaining))
    }

    fun addTrackToPlaylist(playlistId: String, trackId: Long) {
        val result = manager.addTrackToPlaylist(state.playlists, playlistId, trackId)
        if (result is PlaylistValidationResult.Success) {
            repository.saveAll(result.playlists)
            updateState(
                state.copy(
                    playlists = result.playlists,
                    feedback = PlaylistFeedback.TrackAdded(result.affectedPlaylist.name)
                )
            )
        }
    }

    fun removeTrackFromPlaylist(playlistId: String, trackId: Long) {
        val result = manager.removeTrackFromPlaylist(state.playlists, playlistId, trackId)
        if (result is PlaylistValidationResult.Success) {
            repository.saveAll(result.playlists)
            updateState(
                state.copy(
                    playlists = result.playlists,
                    feedback = PlaylistFeedback.TrackRemoved
                )
            )
        }
    }

    fun clearFeedback() {
        if (state.feedback != null) {
            updateState(state.copy(feedback = null))
        }
    }

    private fun handleValidationResult(result: PlaylistValidationResult): Playlist? {
        return when (result) {
            is PlaylistValidationResult.Success -> {
                repository.saveAll(result.playlists)
                updateState(state.copy(playlists = result.playlists))
                result.affectedPlaylist
            }
            PlaylistValidationResult.EmptyName -> {
                updateState(state.copy(feedback = PlaylistFeedback.EmptyName))
                null
            }
            is PlaylistValidationResult.DuplicateName -> {
                updateState(state.copy(feedback = PlaylistFeedback.DuplicateName(result.normalizedName)))
                null
            }
            is PlaylistValidationResult.PlaylistNotFound -> null
        }
    }

    private fun updateState(newState: PlaylistsUiState) {
        state = newState
        onStateChanged?.invoke(newState)
    }
}
