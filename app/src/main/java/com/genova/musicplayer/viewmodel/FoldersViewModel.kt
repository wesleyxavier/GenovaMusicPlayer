package com.genova.musicplayer.viewmodel

import com.genova.musicplayer.data.FolderPreferencesRepository
import com.genova.musicplayer.domain.folder.AuthorizedFolder
import com.genova.musicplayer.domain.folder.FolderAdditionResult
import com.genova.musicplayer.domain.folder.FolderHierarchyValidator
import com.genova.musicplayer.domain.folder.FolderPathNormalizer
import com.genova.musicplayer.domain.library.Track

data class FoldersUiState(
    val folders: List<AuthorizedFolder> = emptyList(),
    val lastScanEpochMs: Long = 0L,
    val totalTrackCount: Int = 0,
    val totalAudioSizeBytes: Long = 0L,
    val feedback: FolderFeedback? = null
)

sealed class FolderFeedback {
    data class Added(val displayName: String, val removedChildrenCount: Int) : FolderFeedback()
    data class RejectedDuplicate(val existingPath: String) : FolderFeedback()
    data class RejectedCoveredByParent(val parentPath: String) : FolderFeedback()
    data object RejectedInvalidPath : FolderFeedback()
}

/**
 * Pure Kotlin ViewModel for the Folders screen (no AndroidX ViewModel dependency).
 */
class FoldersViewModel(
    private val repository: FolderPreferencesRepository
) {
    var onStateChanged: ((FoldersUiState) -> Unit)? = null

    private var state: FoldersUiState = FoldersUiState()

    fun getState(): FoldersUiState = state

    fun loadFolders() {
        val folders = repository.loadFolders()
        val lastScan = repository.getLastScanEpochMs()
        updateState(
            state.copy(
                folders = folders,
                lastScanEpochMs = lastScan
            )
        )
    }

    fun applyTrackDiagnostics(tracks: List<Track>, scanEpochMs: Long) {
        val updatedFolders = state.folders.map { folder ->
            val matching = tracks.filter { track ->
                FolderPathNormalizer.isPathWithinFolder(track.filePath, folder.normalizedPath)
            }
            folder.copy(
                trackCount = matching.size,
                totalSizeBytes = matching.sumOf { it.sizeBytes }
            )
        }
        val totalSize = tracks.sumOf { it.sizeBytes }
        updateState(
            state.copy(
                folders = updatedFolders,
                lastScanEpochMs = scanEpochMs,
                totalTrackCount = tracks.size,
                totalAudioSizeBytes = totalSize
            )
        )
    }

    fun onFolderPicked(rawPath: String?, treeUriString: String, nowEpochMs: Long = System.currentTimeMillis()) {
        if (rawPath.isNullOrBlank()) {
            updateState(state.copy(feedback = FolderFeedback.RejectedInvalidPath))
            return
        }

        val result = FolderHierarchyValidator.addFolder(
            existingFolders = state.folders,
            rawPath = rawPath,
            treeUriString = treeUriString,
            addedAtEpochMs = nowEpochMs
        )

        when (result) {
            is FolderAdditionResult.Added -> {
                repository.saveFolders(result.folders)
                val addedName = result.folders.lastOrNull()?.displayName ?: rawPath
                updateState(
                    state.copy(
                        folders = result.folders,
                        feedback = FolderFeedback.Added(
                            displayName = addedName,
                            removedChildrenCount = result.removedChildren.size
                        )
                    )
                )
            }
            is FolderAdditionResult.RejectedDuplicate -> {
                updateState(
                    state.copy(feedback = FolderFeedback.RejectedDuplicate(result.existingPath))
                )
            }
            is FolderAdditionResult.RejectedCoveredByParent -> {
                updateState(
                    state.copy(feedback = FolderFeedback.RejectedCoveredByParent(result.parentPath))
                )
            }
            FolderAdditionResult.RejectedInvalidPath -> {
                updateState(state.copy(feedback = FolderFeedback.RejectedInvalidPath))
            }
        }
    }

    fun onRemoveFolderConfirmed(normalizedPath: String) {
        val updated = state.folders.filterNot { it.normalizedPath == normalizedPath }
        repository.saveFolders(updated)
        updateState(
            state.copy(
                folders = updated,
                feedback = null
            )
        )
    }

    fun clearFeedback() {
        if (state.feedback != null) {
            updateState(state.copy(feedback = null))
        }
    }

    private fun updateState(newState: FoldersUiState) {
        state = newState
        onStateChanged?.invoke(newState)
    }
}
