package com.genova.musicplayer.data

import android.content.Context
import android.net.Uri
import android.os.Environment
import android.provider.DocumentsContract
import com.genova.musicplayer.domain.folder.AuthorizedFolder
import com.genova.musicplayer.domain.folder.FolderPathNormalizer
import org.json.JSONArray
import org.json.JSONObject

/**
 * Persists authorized folder list and last scan timestamp using SharedPreferences + org.json.JSONArray.
 */
class FolderPreferencesRepository(context: Context) {

    private val prefs = context.applicationContext
        .getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    fun loadFolders(): List<AuthorizedFolder> {
        val rawJson = prefs.getString(KEY_AUTHORIZED_FOLDERS, null) ?: return emptyList()
        return try {
            val array = JSONArray(rawJson)
            buildList(array.length()) {
                for (i in 0 until array.length()) {
                    val obj = array.getJSONObject(i)
                    val path = obj.optString("normalizedPath", "")
                    if (path.isNotBlank()) {
                        add(
                            AuthorizedFolder(
                                normalizedPath = path,
                                treeUriString = obj.optString("treeUriString", ""),
                                displayName = obj.optString(
                                    "displayName",
                                    FolderPathNormalizer.displayName(path)
                                ),
                                addedAtEpochMs = obj.optLong("addedAtEpochMs", 0L)
                            )
                        )
                    }
                }
            }
        } catch (_: Exception) {
            emptyList()
        }
    }

    fun saveFolders(folders: List<AuthorizedFolder>) {
        val array = JSONArray()
        for (folder in folders) {
            val obj = JSONObject().apply {
                put("normalizedPath", folder.normalizedPath)
                put("treeUriString", folder.treeUriString)
                put("displayName", folder.displayName)
                put("addedAtEpochMs", folder.addedAtEpochMs)
            }
            array.put(obj)
        }
        prefs.edit().putString(KEY_AUTHORIZED_FOLDERS, array.toString()).apply()
    }

    fun getLastScanEpochMs(): Long {
        return prefs.getLong(KEY_LAST_SCAN_EPOCH_MS, 0L)
    }

    fun saveLastScanEpochMs(epochMs: Long) {
        prefs.edit().putLong(KEY_LAST_SCAN_EPOCH_MS, epochMs).apply()
    }

    /**
     * Converts an ACTION_OPEN_DOCUMENT_TREE URI (e.g. primary:Music/Rock) into a normalized
     * filesystem path compatible with MediaStore DATA filtering.
     */
    fun resolveNormalizedPathFromTreeUri(treeUri: Uri): String? {
        val docId = try {
            DocumentsContract.getTreeDocumentId(treeUri)
        } catch (_: Exception) {
            null
        } ?: return null

        val parts = docId.split(":", limit = 2)
        if (parts.isEmpty()) return null

        val volume = parts[0]
        val relative = if (parts.size > 1) parts[1] else ""
        val primaryRoot = Environment.getExternalStorageDirectory().absolutePath

        val rawPath = when {
            volume.equals("primary", ignoreCase = true) -> {
                if (relative.isBlank()) primaryRoot else "$primaryRoot/$relative"
            }
            volume.equals("home", ignoreCase = true) -> {
                if (relative.isBlank()) "$primaryRoot/Documents" else "$primaryRoot/Documents/$relative"
            }
            else -> {
                if (relative.isBlank()) "/storage/$volume" else "/storage/$volume/$relative"
            }
        }
        return FolderPathNormalizer.normalize(rawPath)
    }

    companion object {
        private const val PREFS_NAME = "genova_music_prefs"
        private const val KEY_AUTHORIZED_FOLDERS = "authorized_folders"
        private const val KEY_LAST_SCAN_EPOCH_MS = "last_scan_epoch_ms"
    }
}
