package com.genova.musicplayer.data

import android.content.Context
import android.content.SharedPreferences
import com.genova.musicplayer.domain.playlist.PlaylistSortOrder
import org.json.JSONObject

class TrackPlayStatsRepository(context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    fun incrementPlayCount(trackId: Long) {
        val counts = loadPlayCounts().toMutableMap()
        counts[trackId] = (counts[trackId] ?: 0) + 1
        val json = JSONObject()
        counts.forEach { (id, count) ->
            json.put(id.toString(), count)
        }
        prefs.edit().putString(KEY_PLAY_COUNTS, json.toString()).apply()
    }

    fun loadPlayCounts(): Map<Long, Int> {
        val raw = prefs.getString(KEY_PLAY_COUNTS, null) ?: return emptyMap()
        return try {
            val json = JSONObject(raw)
            val result = mutableMapOf<Long, Int>()
            val keys = json.keys()
            while (keys.hasNext()) {
                val key = keys.next()
                val trackId = key.toLongOrNull() ?: continue
                val count = json.optInt(key, 0)
                if (count > 0) {
                    result[trackId] = count
                }
            }
            result
        } catch (_: Exception) {
            emptyMap()
        }
    }

    fun isAutoPlayAfterQueueEnabled(): Boolean {
        return prefs.getBoolean(KEY_AUTO_PLAY_AFTER_QUEUE, true)
    }

    fun setAutoPlayAfterQueueEnabled(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_AUTO_PLAY_AFTER_QUEUE, enabled).apply()
    }

    fun isPlaylistGridMode(): Boolean {
        return prefs.getBoolean(KEY_PLAYLIST_GRID_MODE, true)
    }

    fun setPlaylistGridMode(isGrid: Boolean) {
        prefs.edit().putBoolean(KEY_PLAYLIST_GRID_MODE, isGrid).apply()
    }

    fun loadPlaylistSortOrder(): PlaylistSortOrder {
        val raw = prefs.getString(KEY_PLAYLIST_SORT_ORDER, PlaylistSortOrder.RECENT.name)
        return PlaylistSortOrder.values().firstOrNull { it.name == raw } ?: PlaylistSortOrder.RECENT
    }

    fun savePlaylistSortOrder(order: PlaylistSortOrder) {
        prefs.edit().putString(KEY_PLAYLIST_SORT_ORDER, order.name).apply()
    }

    companion object {
        private const val PREFS_NAME = "genova_playback_stats"
        private const val KEY_PLAY_COUNTS = "play_counts_json"
        private const val KEY_AUTO_PLAY_AFTER_QUEUE = "auto_play_after_queue"
        private const val KEY_PLAYLIST_GRID_MODE = "playlist_view_grid"
        private const val KEY_PLAYLIST_SORT_ORDER = "playlist_sort_order"
    }
}
