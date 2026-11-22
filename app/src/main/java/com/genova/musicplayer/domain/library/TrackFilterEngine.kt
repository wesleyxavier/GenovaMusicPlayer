package com.genova.musicplayer.domain.library

/**
 * Pure Kotlin engine for real-time search by title/artist and ALL/RECENT filtering.
 */
object TrackFilterEngine {

    fun filterAndSort(
        tracks: List<Track>,
        query: String,
        filter: LibraryFilter
    ): List<Track> {
        val normalizedQuery = query.trim().lowercase()

        val filtered = if (normalizedQuery.isEmpty()) {
            tracks
        } else {
            tracks.filter { track ->
                track.title.lowercase().contains(normalizedQuery) ||
                    track.artist.lowercase().contains(normalizedQuery)
            }
        }

        return when (filter) {
            LibraryFilter.ALL -> filtered.sortedBy { it.title.lowercase() }
            LibraryFilter.RECENT -> filtered.sortedByDescending { it.dateAddedEpochSec }
        }
    }
}
