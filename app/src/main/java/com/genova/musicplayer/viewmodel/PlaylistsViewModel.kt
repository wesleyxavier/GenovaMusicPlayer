package com.genova.musicplayer.viewmodel

import com.genova.musicplayer.data.PlaylistPreferencesRepository
import com.genova.musicplayer.domain.playlist.Playlist
import com.genova.musicplayer.domain.playlist.PlaylistManager
import com.genova.musicplayer.domain.playlist.PlaylistSortOrder
import com.genova.musicplayer.domain.playlist.PlaylistValidationResult
import java.util.UUID

sealed class PlaylistFeedback {
    data object EmptyName : PlaylistFeedback()
    data object NameTooLong : PlaylistFeedback()
    data class DuplicateName(val name: String) : PlaylistFeedback()
    data class TrackAdded(val playlistName: String) : PlaylistFeedback()
    data class TracksAddedBatch(val count: Int) : PlaylistFeedback()
    data object TrackRemoved : PlaylistFeedback()
    data object MembershipsUpdated : PlaylistFeedback()
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

    fun getSortedAndFiltered(query: String, sortOrder: PlaylistSortOrder): List<Playlist> {
        return manager.sortAndFilter(state.playlists, query, sortOrder)
    }

    fun createPlaylist(
        rawName: String,
        isPinned: Boolean = false,
        coverStyle: Int = 0
    ): Playlist? {
        val result = manager.createPlaylist(
            existing = state.playlists,
            rawName = rawName,
            id = UUID.randomUUID().toString(),
            createdAtEpochMs = System.currentTimeMillis(),
            isPinned = isPinned,
            coverStyle = coverStyle
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

    fun togglePin(playlistId: String) {
        val result = manager.togglePin(state.playlists, playlistId)
        handleValidationResult(result)
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

    fun addTracksToPlaylist(playlistId: String, trackIds: List<Long>) {
        val beforeCount = state.playlists.firstOrNull { it.id == playlistId }?.trackIds?.size ?: 0
        val result = manager.addTracksToPlaylist(state.playlists, playlistId, trackIds)
        if (result is PlaylistValidationResult.Success) {
            val addedCount = (result.affectedPlaylist.trackIds.size - beforeCount).coerceAtLeast(0)
            repository.saveAll(result.playlists)
            updateState(
                state.copy(
                    playlists = result.playlists,
                    feedback = PlaylistFeedback.TracksAddedBatch(addedCount)
                )
            )
        }
    }

    fun updateTrackMemberships(trackId: Long, selectedPlaylistIds: Set<String>) {
        val updated = manager.updateTrackMemberships(state.playlists, trackId, selectedPlaylistIds)
        repository.saveAll(updated)
        updateState(
            state.copy(
                playlists = updated,
                feedback = PlaylistFeedback.MembershipsUpdated
            )
        )
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

    fun reorderTrack(playlistId: String, fromIndex: Int, toIndex: Int) {
        val result = manager.reorderTrack(state.playlists, playlistId, fromIndex, toIndex)
        if (result is PlaylistValidationResult.Success) {
            repository.saveAll(result.playlists)
            updateState(state.copy(playlists = result.playlists))
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
            PlaylistValidationResult.NameTooLong -> {
                updateState(state.copy(feedback = PlaylistFeedback.NameTooLong))
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
