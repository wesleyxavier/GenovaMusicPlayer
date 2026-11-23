package com.genova.musicplayer.data

import android.content.Context
import com.genova.musicplayer.domain.playlist.Playlist
import org.json.JSONArray
import org.json.JSONObject

class PlaylistPreferencesRepository(context: Context) {

    private val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    fun loadAll(): List<Playlist> {
        val raw = prefs.getString(KEY_PLAYLISTS_JSON, null) ?: return emptyList()
        return try {
            val array = JSONArray(raw)
            buildList(array.length()) {
                for (i in 0 until array.length()) {
                    val obj = array.optJSONObject(i) ?: continue
                    val id = obj.optString(FIELD_ID).takeIf { it.isNotBlank() } ?: continue
                    val name = obj.optString(FIELD_NAME).takeIf { it.isNotBlank() } ?: continue
                    val createdAt = obj.optLong(FIELD_CREATED_AT, 0L)
                    val trackIdsArray = obj.optJSONArray(FIELD_TRACK_IDS)
                    val trackIds = if (trackIdsArray != null) {
                        buildList(trackIdsArray.length()) {
                            for (j in 0 until trackIdsArray.length()) {
                                add(trackIdsArray.optLong(j))
                            }
                        }
                    } else {
                        emptyList()
                    }
                    add(
                        Playlist(
                            id = id,
                            name = name,
                            createdAtEpochMs = createdAt,
                            trackIds = trackIds
                        )
                    )
                }
            }
        } catch (_: Exception) {
            emptyList()
        }
    }

    fun saveAll(playlists: List<Playlist>) {
        val array = JSONArray()
        playlists.forEach { playlist ->
            val trackArray = JSONArray()
            playlist.trackIds.forEach { trackArray.put(it) }
            val obj = JSONObject().apply {
                put(FIELD_ID, playlist.id)
                put(FIELD_NAME, playlist.name)
                put(FIELD_CREATED_AT, playlist.createdAtEpochMs)
                put(FIELD_TRACK_IDS, trackArray)
            }
            array.put(obj)
        }
        prefs.edit().putString(KEY_PLAYLISTS_JSON, array.toString()).apply()
    }

    companion object {
        private const val PREFS_NAME = "genova_playlists_prefs"
        private const val KEY_PLAYLISTS_JSON = "playlists_json"
        private const val FIELD_ID = "id"
        private const val FIELD_NAME = "name"
        private const val FIELD_CREATED_AT = "createdAtEpochMs"
        private const val FIELD_TRACK_IDS = "trackIds"
    }
}
