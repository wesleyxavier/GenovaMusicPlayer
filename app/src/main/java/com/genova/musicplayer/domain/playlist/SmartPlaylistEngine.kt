package com.genova.musicplayer.domain.playlist

import com.genova.musicplayer.domain.library.Track

/**
 * Pure Kotlin engine for local offline smart playlists.
 */
class SmartPlaylistEngine {

    fun buildMostPlayed(
        tracks: List<Track>,
        playCounts: Map<Long, Int>,
        limit: Int = DEFAULT_LIMIT
    ): List<Track> {
        if (tracks.isEmpty() || playCounts.isEmpty()) return emptyList()
        return tracks
            .filter { (playCounts[it.id] ?: 0) > 0 }
            .sortedWith(
                compareByDescending<Track> { playCounts[it.id] ?: 0 }
                    .thenBy { it.title.lowercase() }
            )
            .take(limit)
    }

    fun buildRecentlyAdded(
        tracks: List<Track>,
        limit: Int = DEFAULT_LIMIT
    ): List<Track> {
        if (tracks.isEmpty()) return emptyList()
        return tracks
            .sortedWith(
                compareByDescending<Track> { it.dateAddedEpochSec }
                    .thenBy { it.title.lowercase() }
            )
            .take(limit)
    }

    companion object {
        const val DEFAULT_LIMIT = 50
    }
}
