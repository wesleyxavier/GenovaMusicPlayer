package com.genova.musicplayer.domain.library

/**
 * Pure domain representation of a local audio track indexed from MediaStore.
 */
data class Track(
    val id: Long,
    val contentUri: String,
    val title: String,
    val artist: String,
    val album: String,
    val durationMs: Long,
    val dateAddedEpochSec: Long,
    val filePath: String,
    val sizeBytes: Long,
    val mimeType: String,
    val bitrateKbps: Int? = null,
    val sampleRateHz: Int? = null,
    val isHiFi: Boolean = deriveHiFi(mimeType, bitrateKbps)
) {
    companion object {
        fun deriveHiFi(mimeType: String, bitrateKbps: Int?): Boolean {
            val normalized = mimeType.lowercase()
            if (normalized.contains("flac") ||
                normalized.contains("wav") ||
                normalized.contains("alac") ||
                normalized.contains("x-wav")
            ) {
                return true
            }
            return (bitrateKbps ?: 0) >= 320
        }
    }
}
