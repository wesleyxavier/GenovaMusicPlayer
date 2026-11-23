package com.genova.musicplayer.ui

import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ListView
import android.widget.PopupMenu
import android.widget.TextView
import android.widget.Toast
import com.genova.musicplayer.R
import com.genova.musicplayer.data.FolderPreferencesRepository
import com.genova.musicplayer.data.MediaStoreTrackRepository
import com.genova.musicplayer.data.PlaylistPreferencesRepository
import com.genova.musicplayer.domain.library.Track
import com.genova.musicplayer.domain.player.PlaybackSession
import com.genova.musicplayer.domain.player.PlayerStateStore
import com.genova.musicplayer.domain.playlist.Playlist
import com.genova.musicplayer.service.PlaybackService
import com.genova.musicplayer.viewmodel.PlaylistFeedback
import com.genova.musicplayer.viewmodel.PlaylistsUiState
import com.genova.musicplayer.viewmodel.PlaylistsViewModel
import java.util.Locale
import java.util.concurrent.Executors

class PlaylistDetailActivity : Activity() {

    private lateinit var playlistsViewModel: PlaylistsViewModel
    private lateinit var folderRepository: FolderPreferencesRepository
    private lateinit var trackRepository: MediaStoreTrackRepository
    private lateinit var adapter: TrackListAdapter

    private lateinit var tvTitle: TextView
    private lateinit var tvCount: TextView
    private lateinit var tvEmpty: TextView
    private lateinit var btnShuffle: Button
    private lateinit var listTracks: ListView

    private var playlistId: String = ""
    private var resolvedTracks: List<Track> = emptyList()

    private val ioExecutor = Executors.newSingleThreadExecutor()
    private val mainHandler = Handler(Looper.getMainLooper())

    private val playerListener: (PlaybackSession) -> Unit = { session ->
        mainHandler.post {
            if (!isFinishing && !isDestroyed) {
                adapter.submitList(resolvedTracks, session.currentTrack?.id)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_playlist_detail)

        playlistId = intent.getStringExtra(EXTRA_PLAYLIST_ID).orEmpty()
        playlistsViewModel = PlaylistsViewModel(PlaylistPreferencesRepository(this))
        folderRepository = FolderPreferencesRepository(this)
        trackRepository = MediaStoreTrackRepository(contentResolver)

        tvTitle = findViewById(R.id.tv_playlist_detail_title)
        tvCount = findViewById(R.id.tv_playlist_detail_count)
        tvEmpty = findViewById(R.id.tv_playlist_detail_empty)
        btnShuffle = findViewById(R.id.btn_playlist_shuffle)
        listTracks = findViewById(R.id.list_playlist_tracks)

        adapter = TrackListAdapter(this) { track, anchor ->
            showTrackContextMenu(track, anchor)
        }
        listTracks.adapter = adapter
        listTracks.setOnItemClickListener { _, _, position, _ ->
            if (position in resolvedTracks.indices) {
                startPlayback(resolvedTracks, position, enableShuffle = false)
            }
        }

        findViewById<ImageButton>(R.id.btn_playlist_back).setOnClickListener { finish() }
        findViewById<ImageButton>(R.id.btn_playlist_detail_more).setOnClickListener { anchor ->
            showPlaylistOptions(anchor)
        }
        btnShuffle.setOnClickListener {
            if (resolvedTracks.isNotEmpty()) {
                startPlayback(resolvedTracks, resolvedTracks.indices.random(), enableShuffle = true)
            }
        }

        playlistsViewModel.onStateChanged = { state -> renderState(state) }
    }

    override fun onResume() {
        super.onResume()
        PlayerStateStore.addListener(playerListener)
        playlistsViewModel.loadPlaylists()
    }

    override fun onPause() {
        super.onPause()
        PlayerStateStore.removeListener(playerListener)
    }

    override fun onDestroy() {
        super.onDestroy()
        ioExecutor.shutdownNow()
    }

    private fun renderState(state: PlaylistsUiState) {
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
            playlistsViewModel.clearFeedback()
        }

        val playlist = state.playlists.firstOrNull { it.id == playlistId }
        if (playlist == null) {
            finish()
            return
        }

        tvTitle.text = playlist.name
        loadResolvedTracks(playlist)
    }

    private fun loadResolvedTracks(playlist: Playlist) {
        val folders = folderRepository.loadFolders()
        ioExecutor.execute {
            val allTracksById = if (folders.isEmpty()) {
                emptyMap()
            } else {
                trackRepository.queryTracks(folders).associateBy { it.id }
            }
            val ordered = playlist.trackIds.mapNotNull { allTracksById[it] }
            mainHandler.post {
                if (!isFinishing && !isDestroyed) {
                    resolvedTracks = ordered
                    tvCount.text = getString(R.string.playlist_track_count_format, ordered.size)
                    btnShuffle.isEnabled = ordered.isNotEmpty()
                    btnShuffle.alpha = if (ordered.isNotEmpty()) 1.0f else 0.5f
                    tvEmpty.visibility = if (ordered.isEmpty()) View.VISIBLE else View.GONE
                    listTracks.visibility = if (ordered.isEmpty()) View.GONE else View.VISIBLE
                    adapter.submitList(ordered, PlayerStateStore.getState().currentTrack?.id)
                }
            }
        }
    }

    private fun showTrackContextMenu(track: Track, anchor: View) {
        val popup = PopupMenu(this, anchor)
        popup.menu.add(0, MENU_REMOVE_TRACK, 0, R.string.menu_remove_from_playlist)
        popup.menu.add(0, MENU_TRACK_DETAILS, 1, R.string.menu_track_details)
        popup.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                MENU_REMOVE_TRACK -> {
                    playlistsViewModel.removeTrackFromPlaylist(playlistId, track.id)
                    true
                }
                MENU_TRACK_DETAILS -> {
                    showTrackDetailsDialog(track)
                    true
                }
                else -> false
            }
        }
        popup.show()
    }

    private fun showPlaylistOptions(anchor: View) {
        val playlist = playlistsViewModel.getState().playlists.firstOrNull { it.id == playlistId }
            ?: return
        val popup = PopupMenu(this, anchor)
        popup.menu.add(0, MENU_RENAME, 0, R.string.action_rename)
        popup.menu.add(0, MENU_DELETE, 1, R.string.action_delete)
        popup.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                MENU_RENAME -> {
                    val input = EditText(this).apply {
                        hint = getString(R.string.playlist_name_hint)
                        setText(playlist.name)
                        setSelection(text.length)
                    }
                    AlertDialog.Builder(this)
                        .setTitle(R.string.playlist_rename_title)
                        .setView(input)
                        .setPositiveButton(R.string.action_save) { _, _ ->
                            playlistsViewModel.renamePlaylist(playlist.id, input.text.toString())
                        }
                        .setNegativeButton(R.string.action_cancel, null)
                        .show()
                    true
                }
                MENU_DELETE -> {
                    AlertDialog.Builder(this)
                        .setTitle(R.string.playlist_delete_title)
                        .setMessage(getString(R.string.playlist_delete_message, playlist.name))
                        .setPositiveButton(R.string.action_delete) { _, _ ->
                            playlistsViewModel.deletePlaylist(playlist.id)
                        }
                        .setNegativeButton(R.string.action_cancel, null)
                        .show()
                    true
                }
                else -> false
            }
        }
        popup.show()
    }

    private fun showTrackDetailsDialog(track: Track) {
        val notAvailable = getString(R.string.details_not_available)
        val bitrate = track.bitrateKbps?.let { "$it kbps" } ?: notAvailable
        val sampleRate = track.sampleRateHz?.let { "$it Hz" } ?: notAvailable
        val sizeMb = String.format(Locale.US, "%.2f MB", track.sizeBytes / (1024.0 * 1024.0))

        val message = buildString {
            appendLine(getString(R.string.details_format, track.mimeType))
            appendLine(getString(R.string.details_bitrate, bitrate))
            appendLine(getString(R.string.details_sample_rate, sampleRate))
            appendLine(getString(R.string.details_size, sizeMb))
            append(getString(R.string.details_path, track.filePath))
        }

        AlertDialog.Builder(this)
            .setTitle(track.title)
            .setMessage(message)
            .setPositiveButton(R.string.details_close, null)
            .show()
    }

    private fun startPlayback(tracks: List<Track>, index: Int, enableShuffle: Boolean) {
        val track = tracks[index]
        if (enableShuffle && !PlayerStateStore.getState().isShuffleEnabled) {
            PlayerStateStore.toggleShuffle()
        }
        PlayerStateStore.startPreparing(track, tracks, index)

        val serviceIntent = Intent(this, PlaybackService::class.java).apply {
            action = PlaybackService.ACTION_PLAY
        }
        startForegroundService(serviceIntent)

        startActivity(Intent(this, NowPlayingActivity::class.java))
    }

    companion object {
        const val EXTRA_PLAYLIST_ID = "extra_playlist_id"
        private const val MENU_REMOVE_TRACK = 1
        private const val MENU_TRACK_DETAILS = 2
        private const val MENU_RENAME = 3
        private const val MENU_DELETE = 4
    }
}
