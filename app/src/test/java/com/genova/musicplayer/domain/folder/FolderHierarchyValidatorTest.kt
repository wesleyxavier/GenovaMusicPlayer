package com.genova.musicplayer.domain.folder

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

class FolderHierarchyValidatorTest {

    private fun folder(path: String): AuthorizedFolder = AuthorizedFolder(
        normalizedPath = path,
        treeUriString = "content://com.android.externalstorage.documents/tree/primary:$path",
        displayName = FolderPathNormalizer.displayName(path),
        addedAtEpochMs = 1000L
    )

    @Test
    fun `1 - rejects equal folder`() {
        val existing = listOf(folder("/storage/emulated/0/Music"))
        val result = FolderHierarchyValidator.addFolder(
            existingFolders = existing,
            rawPath = "/storage/emulated/0/Music",
            treeUriString = "uri",
            addedAtEpochMs = 2000L
        )
        val rejected = assertIs<FolderAdditionResult.RejectedDuplicate>(result)
        assertEquals("/storage/emulated/0/Music", rejected.existingPath)
    }

    @Test
    fun `2 - rejects child folder when parent already exists`() {
        val existing = listOf(folder("/storage/emulated/0/Music"))
        val result = FolderHierarchyValidator.addFolder(
            existingFolders = existing,
            rawPath = "/storage/emulated/0/Music/Rock",
            treeUriString = "uri",
            addedAtEpochMs = 2000L
        )
        val rejected = assertIs<FolderAdditionResult.RejectedCoveredByParent>(result)
        assertEquals("/storage/emulated/0/Music", rejected.parentPath)
    }

    @Test
    fun `3 - accepts parent folder and removes existing child folders`() {
        val rock = folder("/storage/emulated/0/Music/Rock")
        val jazz = folder("/storage/emulated/0/Music/Jazz")
        val podcasts = folder("/storage/emulated/0/Podcasts")
        val existing = listOf(rock, jazz, podcasts)

        val result = FolderHierarchyValidator.addFolder(
            existingFolders = existing,
            rawPath = "/storage/emulated/0/Music",
            treeUriString = "uri-music",
            addedAtEpochMs = 2000L
        )
        val added = assertIs<FolderAdditionResult.Added>(result)
        assertEquals(listOf(rock, jazz), added.removedChildren)
        assertEquals(
            listOf("/storage/emulated/0/Podcasts", "/storage/emulated/0/Music"),
            added.folders.map { it.normalizedPath }
        )
    }

    @Test
    fun `4 - keeps sibling folders`() {
        val existing = listOf(folder("/storage/emulated/0/Music"))
        val result = FolderHierarchyValidator.addFolder(
            existingFolders = existing,
            rawPath = "/storage/emulated/0/Podcasts",
            treeUriString = "uri-podcasts",
            addedAtEpochMs = 2000L
        )
        val added = assertIs<FolderAdditionResult.Added>(result)
        assertEquals(2, added.folders.size)
        assertEquals(emptyList(), added.removedChildren)
    }

    @Test
    fun `5 - keeps folders with shared text prefix like Music vs Music2`() {
        val existing = listOf(folder("/storage/emulated/0/Music"))
        val result = FolderHierarchyValidator.addFolder(
            existingFolders = existing,
            rawPath = "/storage/emulated/0/Music2",
            treeUriString = "uri-music2",
            addedAtEpochMs = 2000L
        )
        val added = assertIs<FolderAdditionResult.Added>(result)
        assertEquals(
            listOf("/storage/emulated/0/Music", "/storage/emulated/0/Music2"),
            added.folders.map { it.normalizedPath }
        )
    }

    @Test
    fun `6 - normalizes trailing slash as equal`() {
        val existing = listOf(folder("/storage/emulated/0/Music"))
        val result = FolderHierarchyValidator.addFolder(
            existingFolders = existing,
            rawPath = "/storage/emulated/0/Music///",
            treeUriString = "uri",
            addedAtEpochMs = 2000L
        )
        val rejected = assertIs<FolderAdditionResult.RejectedDuplicate>(result)
        assertEquals("/storage/emulated/0/Music", rejected.existingPath)
    }

    @Test
    fun `7 - rejects empty or blank path`() {
        val resultEmpty = FolderHierarchyValidator.addFolder(emptyList(), "", "uri", 2000L)
        val resultBlank = FolderHierarchyValidator.addFolder(emptyList(), "   ", "uri", 2000L)
        assertIs<FolderAdditionResult.RejectedInvalidPath>(resultEmpty)
        assertIs<FolderAdditionResult.RejectedInvalidPath>(resultBlank)
    }
}
