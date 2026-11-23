package com.genova.musicplayer.domain.playlist

data class Playlist(
    val id: String,
    val name: String,
    val createdAtEpochMs: Long,
    val trackIds: List<Long> = emptyList(),
    val isPinned: Boolean = false,
    val coverStyle: Int = 0
)
