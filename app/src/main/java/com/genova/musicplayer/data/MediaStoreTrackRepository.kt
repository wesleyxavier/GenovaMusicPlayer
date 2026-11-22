package com.genova.musicplayer.data

import android.content.ContentResolver
import android.content.ContentUris
import android.os.Build
import android.provider.MediaStore
import com.genova.musicplayer.domain.folder.AuthorizedFolder
import com.genova.musicplayer.domain.folder.FolderPathNormalizer
import com.genova.musicplayer.domain.library.Track

/**
 * Queries MediaStore.Audio.Media.EXTERNAL_CONTENT_URI and filters tracks strictly
 * by the user-authorized folders using complete-segment path matching.
 */
class MediaStoreTrackRepository(
    private val contentResolver: ContentResolver
) {

    fun queryTracks(authorizedFolders: List<AuthorizedFolder>): List<Track> {
        if (authorizedFolders.isEmpty()) return emptyList()

        val projectionList = mutableListOf(
            MediaStore.Audio.Media._ID,
            MediaStore.Audio.Media.TITLE,
            MediaStore.Audio.Media.ARTIST,
            MediaStore.Audio.Media.ALBUM,
            MediaStore.Audio.Media.DURATION,
            MediaStore.Audio.Media.DATE_ADDED,
            MediaStore.Audio.Media.DATA,
            MediaStore.Audio.Media.SIZE,
            MediaStore.Audio.Media.MIME_TYPE
        )
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            projectionList.add(MediaStore.Audio.Media.BITRATE)
        }

        val selection = "${MediaStore.Audio.Media.IS_MUSIC} != 0"
        val sortOrder = "${MediaStore.Audio.Media.TITLE} ASC"
        val tracks = mutableListOf<Track>()

        contentResolver.query(
            MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
            projectionList.toTypedArray(),
            selection,
            null,
            sortOrder
        )?.use { cursor ->
            val idCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media._ID)
            val titleCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE)
            val artistCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST)
            val albumCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM)
            val durationCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION)
            val dateAddedCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATE_ADDED)
            val dataCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA)
            val sizeCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.SIZE)
            val mimeCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.MIME_TYPE)
            val bitrateCol = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                cursor.getColumnIndex(MediaStore.Audio.Media.BITRATE)
            } else {
                -1
            }

            while (cursor.moveToNext()) {
                val rawPath = cursor.getString(dataCol) ?: continue
                val normalizedFilePath = FolderPathNormalizer.normalize(rawPath) ?: continue

                val isAuthorized = authorizedFolders.any { folder ->
                    FolderPathNormalizer.isPathWithinFolder(normalizedFilePath, folder.normalizedPath)
                }
                if (!isAuthorized) continue

                val id = cursor.getLong(idCol)
                val contentUri = ContentUris.withAppendedId(
                    MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                    id
                ).toString()

                val rawTitle = cursor.getString(titleCol).orEmpty().ifBlank {
                    normalizedFilePath.substringAfterLast('/')
                }
                val rawArtist = cursor.getString(artistCol).orEmpty().let {
                    if (it.isBlank() || it == "<unknown>") "Artista desconhecido" else it
                }
                val rawAlbum = cursor.getString(albumCol).orEmpty().let {
                    if (it.isBlank() || it == "<unknown>") "Álbum desconhecido" else it
                }
                val durationMs = cursor.getLong(durationCol).coerceAtLeast(0L)
                val dateAddedSec = cursor.getLong(dateAddedCol).coerceAtLeast(0L)
                val sizeBytes = cursor.getLong(sizeCol).coerceAtLeast(0L)
                val mimeType = cursor.getString(mimeCol).orEmpty().ifBlank { "audio/mpeg" }
                val bitrateKbps = if (bitrateCol >= 0 && !cursor.isNull(bitrateCol)) {
                    (cursor.getInt(bitrateCol) / 1000).takeIf { it > 0 }
                } else {
                    null
                }

                tracks.add(
                    Track(
                        id = id,
                        contentUri = contentUri,
                        title = rawTitle,
                        artist = rawArtist,
                        album = rawAlbum,
                        durationMs = durationMs,
                        dateAddedEpochSec = dateAddedSec,
                        filePath = normalizedFilePath,
                        sizeBytes = sizeBytes,
                        mimeType = mimeType,
                        bitrateKbps = bitrateKbps
                    )
                )
            }
        }

        return tracks
    }
}
