package com.genova.musicplayer.ui

import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ListView
import android.widget.PopupMenu
import android.widget.TextView
import android.widget.Toast
import com.genova.musicplayer.R
import com.genova.musicplayer.data.PlaylistPreferencesRepository
import com.genova.musicplayer.domain.playlist.Playlist
import com.genova.musicplayer.viewmodel.PlaylistFeedback
import com.genova.musicplayer.viewmodel.PlaylistsUiState
import com.genova.musicplayer.viewmodel.PlaylistsViewModel

class PlaylistsActivity : Activity() {

    private lateinit var viewModel: PlaylistsViewModel
    private lateinit var adapter: PlaylistListAdapter
    private lateinit var tvEmpty: TextView
    private lateinit var listPlaylists: ListView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_playlists)

        viewModel = PlaylistsViewModel(PlaylistPreferencesRepository(this))
        tvEmpty = findViewById(R.id.tv_playlists_empty)
        listPlaylists = findViewById(R.id.list_playlists)

        adapter = PlaylistListAdapter(this) { playlist, anchor ->
            showPlaylistMenu(playlist, anchor)
        }
        listPlaylists.adapter = adapter
        listPlaylists.setOnItemClickListener { _, _, position, _ ->
            val playlists = viewModel.getState().playlists
            if (position in playlists.indices) {
                openPlaylistDetail(playlists[position])
            }
        }

        findViewById<Button>(R.id.btn_create_playlist).setOnClickListener {
            showNameDialog(titleRes = R.string.playlist_create_title, initialName = "") { name ->
                viewModel.createPlaylist(name)
            }
        }

        findViewById<ImageButton>(R.id.btn_search_playlists).setOnClickListener {
            navigateToLibrary(focusSearch = true)
        }
        findViewById<View>(R.id.tab_music).setOnClickListener {
            navigateToLibrary(focusSearch = false)
        }
        findViewById<View>(R.id.tab_folders).setOnClickListener {
            navigateToFolders()
        }

        viewModel.onStateChanged = { state -> render(state) }
    }

    override fun onResume() {
        super.onResume()
        viewModel.loadPlaylists()
    }

    private fun render(state: PlaylistsUiState) {
        adapter.submitList(state.playlists)
        tvEmpty.visibility = if (state.playlists.isEmpty()) View.VISIBLE else View.GONE
        listPlaylists.visibility = if (state.playlists.isEmpty()) View.GONE else View.VISIBLE

        state.feedback?.let { feedback ->
            val message = when (feedback) {
                PlaylistFeedback.EmptyName -> getString(R.string.playlist_error_empty_name)
                is PlaylistFeedback.DuplicateName -> {
                    getString(R.string.playlist_error_duplicate_name, feedback.name)
                }
                is PlaylistFeedback.TrackAdded -> {
                    getString(R.string.playlist_track_added, feedback.playlistName)
                }
                PlaylistFeedback.TrackRemoved -> getString(R.string.playlist_track_removed)
            }
            Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
            viewModel.clearFeedback()
        }
    }

    private fun showPlaylistMenu(playlist: Playlist, anchor: View) {
        val popup = PopupMenu(this, anchor)
        popup.menu.add(0, MENU_RENAME, 0, R.string.action_rename)
        popup.menu.add(0, MENU_DELETE, 1, R.string.action_delete)
        popup.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                MENU_RENAME -> {
                    showNameDialog(
                        titleRes = R.string.playlist_rename_title,
                        initialName = playlist.name
                    ) { newName ->
                        viewModel.renamePlaylist(playlist.id, newName)
                    }
                    true
                }
                MENU_DELETE -> {
                    confirmDeletePlaylist(playlist)
                    true
                }
                else -> false
            }
        }
        popup.show()
    }

    private fun showNameDialog(titleRes: Int, initialName: String, onConfirm: (String) -> Unit) {
        val input = EditText(this).apply {
            hint = getString(R.string.playlist_name_hint)
            setText(initialName)
            setSelection(text.length)
        }
        AlertDialog.Builder(this)
            .setTitle(titleRes)
            .setView(input)
            .setPositiveButton(R.string.action_save) { _, _ ->
                onConfirm(input.text.toString())
            }
            .setNegativeButton(R.string.action_cancel, null)
            .show()
    }

    private fun confirmDeletePlaylist(playlist: Playlist) {
        AlertDialog.Builder(this)
            .setTitle(R.string.playlist_delete_title)
            .setMessage(getString(R.string.playlist_delete_message, playlist.name))
            .setPositiveButton(R.string.action_delete) { _, _ ->
                viewModel.deletePlaylist(playlist.id)
            }
            .setNegativeButton(R.string.action_cancel, null)
            .show()
    }

    private fun openPlaylistDetail(playlist: Playlist) {
        val intent = Intent(this, PlaylistDetailActivity::class.java).apply {
            putExtra(PlaylistDetailActivity.EXTRA_PLAYLIST_ID, playlist.id)
        }
        startActivity(intent)
    }

    private fun navigateToLibrary(focusSearch: Boolean) {
        val intent = Intent(this, LibraryActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
            putExtra(LibraryActivity.EXTRA_FOCUS_SEARCH, focusSearch)
        }
        startActivity(intent)
        overridePendingTransition(0, 0)
    }

    private fun navigateToFolders() {
        val intent = Intent(this, FoldersActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
        }
        startActivity(intent)
        overridePendingTransition(0, 0)
    }

    companion object {
        private const val MENU_RENAME = 1
        private const val MENU_DELETE = 2
    }
}
