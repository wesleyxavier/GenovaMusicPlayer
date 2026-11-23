package com.genova.musicplayer.ui

import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.widget.Button
import android.widget.ImageButton
import android.widget.ListView
import android.widget.TextView
import android.widget.Toast
import com.genova.musicplayer.R
import com.genova.musicplayer.data.FolderPreferencesRepository
import com.genova.musicplayer.data.MediaStoreTrackRepository
import com.genova.musicplayer.domain.folder.AuthorizedFolder
import com.genova.musicplayer.viewmodel.FolderFeedback
import com.genova.musicplayer.viewmodel.FoldersUiState
import com.genova.musicplayer.viewmodel.FoldersViewModel
import java.util.Locale
import java.util.concurrent.Executors

class FoldersActivity : Activity() {

    private lateinit var repository: FolderPreferencesRepository
    private lateinit var trackRepository: MediaStoreTrackRepository
    private lateinit var viewModel: FoldersViewModel
    private lateinit var adapter: FolderListAdapter

    private lateinit var tvScanStatus: TextView
    private lateinit var tvAudioUsage: TextView
    private lateinit var tvEmpty: TextView
    private lateinit var listFolders: ListView

    private val ioExecutor = Executors.newSingleThreadExecutor()
    private val mainHandler = Handler(Looper.getMainLooper())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_folders)

        repository = FolderPreferencesRepository(this)
        trackRepository = MediaStoreTrackRepository(contentResolver)
        viewModel = FoldersViewModel(repository)

        tvScanStatus = findViewById(R.id.tv_scan_status)
        tvAudioUsage = findViewById(R.id.tv_audio_usage)
        tvEmpty = findViewById(R.id.tv_folders_empty)
        listFolders = findViewById(R.id.list_folders)

        adapter = FolderListAdapter(this) { folder ->
            confirmRemoveFolder(folder)
        }
        listFolders.adapter = adapter

        findViewById<Button>(R.id.btn_add_folder).setOnClickListener {
            openFolderPicker()
        }
        findViewById<ImageButton>(R.id.btn_search_folders).setOnClickListener {
            navigateToLibrary(focusSearch = true)
        }
        findViewById<View>(R.id.tab_music).setOnClickListener {
            navigateToLibrary(focusSearch = false)
        }
        findViewById<View>(R.id.tab_playlists).setOnClickListener {
            navigateToPlaylists()
        }

        viewModel.onStateChanged = { state ->
            render(state)
        }
        viewModel.loadFolders()
        refreshDiagnostics()
    }

    override fun onDestroy() {
        super.onDestroy()
        ioExecutor.shutdownNow()
    }

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_OPEN_DOCUMENT_TREE && resultCode == RESULT_OK) {
            val treeUri: Uri = data?.data ?: return
            try {
                contentResolver.takePersistableUriPermission(
                    treeUri,
                    Intent.FLAG_GRANT_READ_URI_PERMISSION
                )
            } catch (_: SecurityException) {
                // Some providers do not support persistable permissions; continue with resolved path
            }
            val resolvedPath = repository.resolveNormalizedPathFromTreeUri(treeUri)
            viewModel.onFolderPicked(resolvedPath, treeUri.toString())
            refreshDiagnostics()
        }
    }

    private fun openFolderPicker() {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT_TREE).apply {
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            addFlags(Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION)
        }
        @Suppress("DEPRECATION")
        startActivityForResult(intent, REQUEST_OPEN_DOCUMENT_TREE)
    }

    private fun confirmRemoveFolder(folder: AuthorizedFolder) {
        AlertDialog.Builder(this)
            .setTitle(R.string.folder_remove_title)
            .setMessage(getString(R.string.folder_remove_message, folder.displayName))
            .setPositiveButton(R.string.action_remove) { _, _ ->
                viewModel.onRemoveFolderConfirmed(folder.normalizedPath)
                refreshDiagnostics()
            }
            .setNegativeButton(R.string.action_cancel, null)
            .show()
    }

    private fun refreshDiagnostics() {
        val folders = viewModel.getState().folders
        if (folders.isEmpty()) {
            viewModel.applyTrackDiagnostics(emptyList(), repository.getLastScanEpochMs())
            return
        }
        ioExecutor.execute {
            val tracks = trackRepository.queryTracks(folders)
            val now = System.currentTimeMillis()
            repository.saveLastScanEpochMs(now)
            mainHandler.post {
                if (!isFinishing && !isDestroyed) {
                    viewModel.applyTrackDiagnostics(tracks, now)
                }
            }
        }
    }

    private fun render(state: FoldersUiState) {
        adapter.submitList(state.folders)
        tvEmpty.visibility = if (state.folders.isEmpty()) View.VISIBLE else View.GONE
        listFolders.visibility = if (state.folders.isEmpty()) View.GONE else View.VISIBLE

        tvScanStatus.text = formatScanStatus(state.lastScanEpochMs)
        tvAudioUsage.text = getString(
            R.string.diag_usage_format,
            formatSize(state.totalAudioSizeBytes),
            state.totalTrackCount
        )

        state.feedback?.let { feedback ->
            val message = when (feedback) {
                is FolderFeedback.Added -> {
                    if (feedback.removedChildrenCount > 0) {
                        getString(R.string.folder_added_consolidated, feedback.removedChildrenCount)
                    } else {
                        getString(R.string.folder_added_success, feedback.displayName)
                    }
                }
                is FolderFeedback.RejectedDuplicate -> {
                    getString(R.string.folder_rejected_duplicate, feedback.existingPath)
                }
                is FolderFeedback.RejectedCoveredByParent -> {
                    getString(R.string.folder_rejected_parent, feedback.parentPath)
                }
                FolderFeedback.RejectedInvalidPath -> {
                    getString(R.string.folder_rejected_invalid)
                }
            }
            Toast.makeText(this, message, Toast.LENGTH_LONG).show()
            viewModel.clearFeedback()
        }
    }

    private fun formatScanStatus(epochMs: Long): String {
        if (epochMs <= 0L) return getString(R.string.diag_scan_never)
        val diffMin = ((System.currentTimeMillis() - epochMs) / 60_000L).coerceAtLeast(0L)
        return when {
            diffMin < 1L -> getString(R.string.diag_scan_just_now)
            diffMin < 60L -> getString(R.string.diag_scan_minutes_ago, diffMin)
            else -> getString(R.string.diag_scan_hours_ago, diffMin / 60L)
        }
    }

    private fun formatSize(bytes: Long): String {
        val mb = bytes / (1024.0 * 1024.0)
        return if (mb >= 1024.0) {
            String.format(Locale.US, "%.1f GB", mb / 1024.0)
        } else {
            String.format(Locale.US, "%.1f MB", mb)
        }
    }

    private fun navigateToPlaylists() {
        val intent = Intent(this, PlaylistsActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
        }
        startActivity(intent)
        overridePendingTransition(0, 0)
    }

    private fun navigateToLibrary(focusSearch: Boolean) {
        val intent = Intent(this, LibraryActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
            putExtra(LibraryActivity.EXTRA_FOCUS_SEARCH, focusSearch)
        }
        startActivity(intent)
        overridePendingTransition(0, 0)
    }

    companion object {
        private const val REQUEST_OPEN_DOCUMENT_TREE = 1001
    }
}
