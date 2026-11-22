package com.genova.musicplayer.domain.folder

/**
 * Enforces the mandatory Folder Hierarchy Rule in pure Kotlin:
 * - Rejects empty/invalid paths
 * - Rejects duplicates or descendants of already-included folders
 * - Automatically removes existing child folders when a parent folder is added
 * - Compares by complete path segments (so /Music and /Music2 are siblings)
 */
object FolderHierarchyValidator {

    fun addFolder(
        existingFolders: List<AuthorizedFolder>,
        rawPath: String,
        treeUriString: String,
        addedAtEpochMs: Long
    ): FolderAdditionResult {
        val normalizedNew = FolderPathNormalizer.normalize(rawPath)
            ?: return FolderAdditionResult.RejectedInvalidPath

        val newSegments = FolderPathNormalizer.segments(normalizedNew)
        val removedChildren = mutableListOf<AuthorizedFolder>()

        for (existing in existingFolders) {
            val normalizedExisting = FolderPathNormalizer.normalize(existing.normalizedPath)
                ?: continue
            val existingSegments = FolderPathNormalizer.segments(normalizedExisting)

            if (newSegments == existingSegments) {
                return FolderAdditionResult.RejectedDuplicate(normalizedExisting)
            }

            if (isSegmentPrefix(prefix = existingSegments, target = newSegments)) {
                return FolderAdditionResult.RejectedCoveredByParent(normalizedExisting)
            }

            if (isSegmentPrefix(prefix = newSegments, target = existingSegments)) {
                removedChildren.add(existing)
            }
        }

        val newFolder = AuthorizedFolder(
            normalizedPath = normalizedNew,
            treeUriString = treeUriString,
            displayName = FolderPathNormalizer.displayName(normalizedNew),
            addedAtEpochMs = addedAtEpochMs
        )

        val remaining = existingFolders.filterNot { it in removedChildren } + newFolder
        return FolderAdditionResult.Added(
            folders = remaining,
            removedChildren = removedChildren
        )
    }

    private fun isSegmentPrefix(prefix: List<String>, target: List<String>): Boolean {
        if (prefix.size >= target.size) return false
        return target.subList(0, prefix.size) == prefix
    }
}
