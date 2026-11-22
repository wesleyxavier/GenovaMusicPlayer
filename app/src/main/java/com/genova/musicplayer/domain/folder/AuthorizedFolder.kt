package com.genova.musicplayer.domain.folder

/**
 * Pure domain representation of a user-authorized music folder.
 */
data class AuthorizedFolder(
    val normalizedPath: String,
    val treeUriString: String,
    val displayName: String,
    val addedAtEpochMs: Long,
    val trackCount: Int = 0,
    val totalSizeBytes: Long = 0L
)

/**
 * Outcome of attempting to add a new folder under the Hierarchy Rule.
 */
sealed class FolderAdditionResult {
    data class Added(
        val folders: List<AuthorizedFolder>,
        val removedChildren: List<AuthorizedFolder>
    ) : FolderAdditionResult()

    data class RejectedDuplicate(val existingPath: String) : FolderAdditionResult()
    data class RejectedCoveredByParent(val parentPath: String) : FolderAdditionResult()
    data object RejectedInvalidPath : FolderAdditionResult()
}
