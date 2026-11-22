package com.genova.musicplayer.viewmodel

import com.genova.musicplayer.domain.library.LibraryFilter
import com.genova.musicplayer.domain.library.Track
import com.genova.musicplayer.domain.library.TrackFilterEngine

data class LibraryUiState(
    val visibleTracks: List<Track> = emptyList(),
    val totalTracksCount: Int = 0,
    val searchQuery: String = "",
    val activeFilter: LibraryFilter = LibraryFilter.ALL,
    val hasAuthorizedFolders: Boolean = false,
    val hasAudioPermission: Boolean = false,
    val isLoading: Boolean = false,
    val activeTrackId: Long? = null,
    val isPlaying: Boolean = false
)

/**
 * Pure Kotlin ViewModel for the Music Library & Search screen.
 */
class LibraryViewModel {

    var onStateChanged: ((LibraryUiState) -> Unit)? = null

    private var allTracks: List<Track> = emptyList()
    private var state: LibraryUiState = LibraryUiState()

    fun getState(): LibraryUiState = state

    fun setPermissionState(granted: Boolean) {
        if (state.hasAudioPermission != granted) {
            updateState(state.copy(hasAudioPermission = granted))
        }
    }

    fun setLoading(loading: Boolean, hasFolders: Boolean) {
        updateState(
            state.copy(
                isLoading = loading,
                hasAuthorizedFolders = hasFolders
            )
        )
    }

    fun setTracks(tracks: List<Track>, hasFolders: Boolean) {
        allTracks = tracks
        val filtered = TrackFilterEngine.filterAndSort(
            tracks = allTracks,
            query = state.searchQuery,
            filter = state.activeFilter
        )
        updateState(
            state.copy(
                visibleTracks = filtered,
                totalTracksCount = allTracks.size,
                hasAuthorizedFolders = hasFolders,
                isLoading = false
            )
        )
    }

    fun onSearchQueryChanged(query: String) {
        if (state.searchQuery == query) return
        val filtered = TrackFilterEngine.filterAndSort(
            tracks = allTracks,
            query = query,
            filter = state.activeFilter
        )
        updateState(
            state.copy(
                searchQuery = query,
                visibleTracks = filtered
            )
        )
    }

    fun onFilterSelected(filter: LibraryFilter) {
        if (state.activeFilter == filter) return
        val filtered = TrackFilterEngine.filterAndSort(
            tracks = allTracks,
            query = state.searchQuery,
            filter = filter
        )
        updateState(
            state.copy(
                activeFilter = filter,
                visibleTracks = filtered
            )
        )
    }

    fun updatePlaybackIndicator(activeTrackId: Long?, isPlaying: Boolean) {
        if (state.activeTrackId != activeTrackId || state.isPlaying != isPlaying) {
            updateState(
                state.copy(
                    activeTrackId = activeTrackId,
                    isPlaying = isPlaying
                )
            )
        }
    }

    private fun updateState(newState: LibraryUiState) {
        state = newState
        onStateChanged?.invoke(newState)
    }
}
