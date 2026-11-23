package com.genova.musicplayer.domain.playlist

data class Playlist(
    val id: String,
    val name: String,
    val createdAtEpochMs: Long,
    val trackIds: List<Long> = emptyList()
)
