package com.genova.musicplayer.domain.folder

/**
 * Normalizes file system paths and splits them into complete path segments
 * so hierarchy checks never rely on raw string prefixes (e.g. /Music vs /Music2).
 */
object FolderPathNormalizer {

    fun normalize(rawPath: String): String? {
        val trimmed = rawPath.trim()
        if (trimmed.isEmpty()) return null

        val forwardSlashes = trimmed.replace('\\', '/')
        val collapsed = forwardSlashes.replace(Regex("/+"), "/")
        val withoutTrailing = if (collapsed.length > 1 && collapsed.endsWith("/")) {
            collapsed.dropLast(1)
        } else {
            collapsed
        }

        if (withoutTrailing.isBlank()) return null
        return if (withoutTrailing.startsWith("/")) withoutTrailing else "/$withoutTrailing"
    }

    fun segments(normalizedPath: String): List<String> {
        return normalizedPath.split('/').filter { it.isNotEmpty() }
    }

    fun displayName(normalizedPath: String): String {
        val segs = segments(normalizedPath)
        return segs.lastOrNull() ?: "/"
    }

    /**
     * Returns true if [candidatePath] is equal to or inside [folderPath] by full segment boundaries.
     */
    fun isPathWithinFolder(candidatePath: String, folderPath: String): Boolean {
        val normCandidate = normalize(candidatePath) ?: return false
        val normFolder = normalize(folderPath) ?: return false
        val candidateSegments = segments(normCandidate)
        val folderSegments = segments(normFolder)
        if (folderSegments.isEmpty()) return true
        if (candidateSegments.size < folderSegments.size) return false
        return candidateSegments.subList(0, folderSegments.size) == folderSegments
    }
}
