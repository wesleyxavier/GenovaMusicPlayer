package com.genova.musicplayer.ui

import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.ListView
import android.widget.PopupMenu
import android.widget.TextView
import android.widget.Toast
import com.genova.musicplayer.R
import com.genova.musicplayer.data.AlbumArtCacheLoader
import com.genova.musicplayer.data.FolderPreferencesRepository
import com.genova.musicplayer.data.MediaStoreTrackRepository
import com.genova.musicplayer.data.PlaylistPreferencesRepository
import com.genova.musicplayer.data.TrackPlayStatsRepository
import com.genova.musicplayer.domain.library.Track
import com.genova.musicplayer.domain.player.PlaybackSession
import com.genova.musicplayer.domain.player.PlayerStateStore
import com.genova.musicplayer.domain.playlist.Playlist
import com.genova.musicplayer.domain.playlist.SmartPlaylistEngine
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
    private lateinit var statsRepository: TrackPlayStatsRepository
    private lateinit var miniPlayerController: MiniPlayerController
    private lateinit var adapter: TrackListAdapter

    private lateinit var tvBadge: TextView
    private lateinit var ivArt: ImageView
    private lateinit var tvTitle: TextView
    private lateinit var tvCount: TextView
    private lateinit var tvEmpty: TextView
    private lateinit var btnPlay: Button
    private lateinit var btnShuffle: Button
    private lateinit var btnAddTracks: Button
    private lateinit var btnManualOrder: Button
    private lateinit var btnAddToQueue: Button
    private lateinit var etFilter: EditText
    private lateinit var listTracks: ListView

    private var playlistId: String = ""
    private var smartPlaylistType: String? = null
    private var currentPlaylistName: String = ""
    private var allLibraryTracks: List<Track> = emptyList()
    private var resolvedTracks: List<Track> = emptyList()
    private var visibleTracks: List<Track> = emptyList()
    private var isManualOrderMode: Boolean = false

    private val smartEngine = SmartPlaylistEngine()
    private val ioExecutor = Executors.newSingleThreadExecutor()
    private val mainHandler = Handler(Looper.getMainLooper())

    private val playerListener: (PlaybackSession) -> Unit = { session ->
        mainHandler.post {
            if (!isFinishing && !isDestroyed) {
                adapter.submitList(visibleTracks, session.currentTrack?.id)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_playlist_detail)

        playlistId = intent.getStringExtra(EXTRA_PLAYLIST_ID).orEmpty()
        smartPlaylistType = intent.getStringExtra(EXTRA_SMART_PLAYLIST_TYPE)
        playlistsViewModel = PlaylistsViewModel(PlaylistPreferencesRepository(this))
        folderRepository = FolderPreferencesRepository(this)
        trackRepository = MediaStoreTrackRepository(contentResolver)
        statsRepository = TrackPlayStatsRepository(this)
        miniPlayerController = MiniPlayerController(this)

        tvBadge = findViewById(R.id.tv_playlist_detail_badge)
        ivArt = findViewById(R.id.iv_playlist_detail_art)
        tvTitle = findViewById(R.id.tv_playlist_detail_title)
        tvCount = findViewById(R.id.tv_playlist_detail_count)
        tvEmpty = findViewById(R.id.tv_playlist_detail_empty)
        btnPlay = findViewById(R.id.btn_playlist_play)
        btnShuffle = findViewById(R.id.btn_playlist_shuffle)
        btnAddTracks = findViewById(R.id.btn_playlist_add_tracks)
        btnManualOrder = findViewById(R.id.btn_playlist_manual_order)
        btnAddToQueue = findViewById(R.id.btn_playlist_add_to_queue)
        etFilter = findViewById(R.id.et_playlist_filter)
        listTracks = findViewById(R.id.list_playlist_tracks)

        adapter = TrackListAdapter(this) { track, anchor ->
            showTrackContextMenu(track, anchor)
        }
        listTracks.adapter = adapter
        listTracks.setOnItemClickListener { _, _, position, _ ->
            if (position in visibleTracks.indices) {
                startPlayback(visibleTracks, position, enableShuffle = false)
            }
        }

        findViewById<ImageButton>(R.id.btn_playlist_back).setOnClickListener { finish() }
        findViewById<ImageButton>(R.id.btn_playlist_share).setOnClickListener { sharePlaylistSummary() }
        findViewById<ImageButton>(R.id.btn_playlist_detail_more).setOnClickListener { anchor ->
            showPlaylistOptions(anchor)
        }

        btnPlay.setOnClickListener {
            if (visibleTracks.isNotEmpty()) {
                startPlayback(visibleTracks, 0, enableShuffle = false)
            }
        }
        btnShuffle.setOnClickListener {
            if (visibleTracks.isNotEmpty()) {
                startPlayback(visibleTracks, visibleTracks.indices.random(), enableShuffle = true)
            }
        }
        btnAddTracks.setOnClickListener { showAddTracksBatchDialog() }
        btnManualOrder.setOnClickListener { toggleManualOrderMode() }
        btnAddToQueue.setOnClickListener { addPlaylistToQueue() }

        etFilter.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) = Unit
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                applyFilterAndBind()
            }
            override fun afterTextChanged(s: Editable?) = Unit
        })

        if (isSmartMode()) {
            tvBadge.setText(R.string.playlist_smart_badge)
            btnAddTracks.visibility = View.GONE
            btnManualOrder.visibility = View.GONE
            findViewById<ImageButton>(R.id.btn_playlist_detail_more).visibility = View.GONE
        }

        playlistsViewModel.onStateChanged = { state -> renderState(state) }
    }

    override fun onResume() {
        super.onResume()
        miniPlayerController.bind()
        PlayerStateStore.addListener(playerListener)
        playlistsViewModel.loadPlaylists()
    }

    override fun onPause() {
        super.onPause()
        miniPlayerController.unbind()
        PlayerStateStore.removeListener(playerListener)
    }

    override fun onDestroy() {
        super.onDestroy()
        ioExecutor.shutdownNow()
    }

    private fun isSmartMode(): Boolean = !smartPlaylistType.isNullOrBlank()

    private fun renderState(state: PlaylistsUiState) {
        state.feedback?.let { feedback ->
            val message = when (feedback) {
                PlaylistFeedback.EmptyName -> getString(R.string.playlist_error_empty_name)
                PlaylistFeedback.NameTooLong -> getString(R.string.playlist_error_name_too_long)
                is PlaylistFeedback.DuplicateName -> getString(R.string.playlist_error_duplicate_name, feedback.name)
                is PlaylistFeedback.TrackAdded -> getString(R.string.playlist_track_added, feedback.playlistName)
                is PlaylistFeedback.TracksAddedBatch -> getString(R.string.playlist_tracks_added_batch, feedback.count)
                PlaylistFeedback.TrackRemoved -> getString(R.string.playlist_track_removed)
                PlaylistFeedback.MembershipsUpdated -> getString(R.string.action_done)
            }
            Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
            playlistsViewModel.clearFeedback()
        }

        if (isSmartMode()) {
            val isMostPlayed = smartPlaylistType == SMART_MOST_PLAYED
            currentPlaylistName = getString(
                if (isMostPlayed) R.string.smart_most_played_title else R.string.smart_recently_added_title
            )
            tvTitle.text = currentPlaylistName
            loadSmartTracks(isMostPlayed)
            return
        }

        val playlist = state.playlists.firstOrNull { it.id == playlistId }
        if (playlist == null) {
            finish()
            return
        }
        currentPlaylistName = playlist.name
        tvTitle.text = playlist.name
        resolvePlaylistTracks(playlist)
    }

    private fun loadSmartTracks(isMostPlayed: Boolean) {
        ioExecutor.execute {
            val folders = folderRepository.loadFolders()
            val libraryTracks = if (folders.isNotEmpty()) trackRepository.queryTracks(folders) else emptyList()
            val tracks = if (isMostPlayed) {
                smartEngine.buildMostPlayed(libraryTracks, statsRepository.loadPlayCounts())
            } else {
                smartEngine.buildRecentlyAdded(libraryTracks)
            }
            mainHandler.post {
                if (!isFinishing && !isDestroyed) {
                    allLibraryTracks = libraryTracks
                    resolvedTracks = tracks
                    updateHeaderSummary()
                    applyFilterAndBind()
                }
            }
        }
    }

    private fun resolvePlaylistTracks(playlist: Playlist) {
        ioExecutor.execute {
            val folders = folderRepository.loadFolders()
            val libraryTracks = if (folders.isNotEmpty()) {
                trackRepository.queryTracks(folders)
            } else {
                emptyList()
            }
            val byId = libraryTracks.associateBy { it.id }
            val ordered = playlist.trackIds.mapNotNull { byId[it] }
            mainHandler.post {
                if (!isFinishing && !isDestroyed) {
                    allLibraryTracks = libraryTracks
                    resolvedTracks = ordered
                    updateHeaderSummary()
                    applyFilterAndBind()
                }
            }
        }
    }

    private fun updateHeaderSummary() {
        val totalDuration = formatTotalDuration(resolvedTracks.sumOf { it.durationMs.coerceAtLeast(0L) })
        val hasHiFi = resolvedTracks.any { it.isHiFi }
        val formats = if (hasHiFi) "FLAC / Áudio HD" else "Áudio local"
        tvCount.text = getString(
            R.string.playlist_detail_summary_format,
            resolvedTracks.size,
            totalDuration,
            formats
        )
        val first = resolvedTracks.firstOrNull()
        if (first != null) {
            ivArt.imageTintList = null
            ivArt.setPadding(0, 0, 0, 0)
            val sizePx = (96 * resources.displayMetrics.density).toInt().coerceAtLeast(96)
            AlbumArtCacheLoader.loadThumb(this, first.contentUri, sizePx, ivArt)
        }
    }

    private fun applyFilterAndBind() {
        val query = etFilter.text?.toString()?.trim().orEmpty().lowercase(Locale.getDefault())
        visibleTracks = if (query.isEmpty()) {
            resolvedTracks
        } else {
            resolvedTracks.filter {
                it.title.lowercase(Locale.getDefault()).contains(query) ||
                    it.artist.lowercase(Locale.getDefault()).contains(query)
            }
        }
        val activeId = PlayerStateStore.getState().currentTrack?.id
        adapter.submitList(visibleTracks, activeId)
        tvEmpty.visibility = if (visibleTracks.isEmpty()) View.VISIBLE else View.GONE
        listTracks.visibility = if (visibleTracks.isEmpty()) View.GONE else View.VISIBLE
        btnPlay.isEnabled = visibleTracks.isNotEmpty()
        btnShuffle.isEnabled = visibleTracks.isNotEmpty()
        btnAddToQueue.isEnabled = resolvedTracks.isNotEmpty()
    }

    private fun toggleManualOrderMode() {
        if (etFilter.text?.isNotEmpty() == true) {
            Toast.makeText(this, R.string.playlist_clear_filter_to_reorder, Toast.LENGTH_SHORT).show()
            return
        }
        isManualOrderMode = !isManualOrderMode
        btnManualOrder.setText(
            if (isManualOrderMode) R.string.playlist_manual_order_active else R.string.playlist_manual_order_button
        )
        btnManualOrder.setBackgroundResource(
            if (isManualOrderMode) R.drawable.bg_pill_primary else R.drawable.bg_pill_surface
        )
        btnManualOrder.setTextColor(
            getColor(if (isManualOrderMode) R.color.nv_on_primary_container else R.color.nv_on_surface)
        )
    }

    private fun showAddTracksBatchDialog() {
        val currentIds = playlistsViewModel.getState().playlists
            .firstOrNull { it.id == playlistId }
            ?.trackIds
            ?.toSet()
            ?: emptySet()
        val candidates = allLibraryTracks.filterNot { currentIds.contains(it.id) }
        if (candidates.isEmpty()) {
            Toast.makeText(this, R.string.library_empty_no_tracks, Toast.LENGTH_SHORT).show()
            return
        }
        val labels = candidates.map { "${it.title} — ${it.artist}" }.toTypedArray()
        val checked = BooleanArray(candidates.size)
        AlertDialog.Builder(this)
            .setTitle(R.string.playlist_select_tracks_title)
            .setMultiChoiceItems(labels, checked) { _, which, isChecked ->
                checked[which] = isChecked
            }
            .setPositiveButton(R.string.action_done) { _, _ ->
                val selectedIds = candidates.indices.filter { checked[it] }.map { candidates[it].id }
                if (selectedIds.isNotEmpty()) {
                    playlistsViewModel.addTracksToPlaylist(playlistId, selectedIds)
                }
            }
            .setNegativeButton(R.string.action_cancel, null)
            .show()
    }

    private fun addPlaylistToQueue() {
        if (resolvedTracks.isEmpty()) {
            Toast.makeText(this, R.string.queue_empty_playlist_toast, Toast.LENGTH_SHORT).show()
            return
        }
        val wasIdle = PlayerStateStore.getState().currentTrack == null
        PlayerStateStore.appendToQueue(resolvedTracks, currentPlaylistName)
        if (wasIdle) {
            startForegroundService(
                Intent(this, PlaybackService::class.java).apply {
                    action = PlaybackService.ACTION_PLAY
                }
            )
        }
        Toast.makeText(
            this,
            getString(R.string.queue_added_playlist_toast, resolvedTracks.size),
            Toast.LENGTH_SHORT
        ).show()
    }

    private fun showTrackContextMenu(track: Track, anchor: View) {
        val popup = PopupMenu(this, anchor)
        val index = resolvedTracks.indexOfFirst { it.id == track.id }
        popup.menu.add(0, MENU_ADD_TO_QUEUE, 0, R.string.menu_add_to_queue)
        if (!isSmartMode()) {
            if (isManualOrderMode && etFilter.text?.isEmpty() == true) {
                if (index > 0) popup.menu.add(0, MENU_MOVE_UP, 1, R.string.menu_move_up)
                if (index in 0 until resolvedTracks.lastIndex) {
                    popup.menu.add(0, MENU_MOVE_DOWN, 2, R.string.menu_move_down)
                }
            }
            popup.menu.add(0, MENU_REMOVE_FROM_PLAYLIST, 3, R.string.menu_remove_from_playlist)
        }
        popup.menu.add(0, MENU_DETAILS, 4, R.string.menu_track_details)

        popup.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                MENU_ADD_TO_QUEUE -> {
                    val wasIdle = PlayerStateStore.getState().currentTrack == null
                    PlayerStateStore.appendToQueue(listOf(track), currentPlaylistName)
                    if (wasIdle) {
                        startForegroundService(
                            Intent(this, PlaybackService::class.java).apply {
                                action = PlaybackService.ACTION_PLAY
                            }
                        )
                    }
                    Toast.makeText(
                        this,
                        getString(R.string.queue_added_track_toast, track.title),
                        Toast.LENGTH_SHORT
                    ).show()
                    true
                }
                MENU_MOVE_UP -> {
                    if (index > 0) playlistsViewModel.reorderTrack(playlistId, index, index - 1)
                    true
                }
                MENU_MOVE_DOWN -> {
                    if (index in 0 until resolvedTracks.lastIndex) {
                        playlistsViewModel.reorderTrack(playlistId, index, index + 1)
                    }
                    true
                }
                MENU_REMOVE_FROM_PLAYLIST -> {
                    playlistsViewModel.removeTrackFromPlaylist(playlistId, track.id)
                    true
                }
                MENU_DETAILS -> {
                    showTrackDetailsDialog(track)
                    true
                }
                else -> false
            }
        }
        popup.show()
    }

    private fun showPlaylistOptions(anchor: View) {
        val playlist = playlistsViewModel.getState().playlists.firstOrNull { it.id == playlistId } ?: return
        val popup = PopupMenu(this, anchor)
        popup.menu.add(
            0,
            MENU_TOGGLE_PIN,
            0,
            if (playlist.isPinned) R.string.playlist_unpin_action else R.string.playlist_pin_action
        )
        popup.menu.add(0, MENU_RENAME_PLAYLIST, 1, R.string.action_rename)
        popup.menu.add(0, MENU_DELETE_PLAYLIST, 2, R.string.action_delete)
        popup.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                MENU_TOGGLE_PIN -> {
                    playlistsViewModel.togglePin(playlist.id)
                    true
                }
                MENU_RENAME_PLAYLIST -> {
                    val input = EditText(this).apply {
                        hint = getString(R.string.playlist_name_hint)
                        setText(playlist.name)
                        setSelection(playlist.name.length)
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
                MENU_DELETE_PLAYLIST -> {
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

    private fun sharePlaylistSummary() {
        if (resolvedTracks.isEmpty()) return
        val text = buildString {
            appendLine(currentPlaylistName)
            resolvedTracks.forEachIndexed { idx, track ->
                appendLine("${idx + 1}. ${track.title} — ${track.artist}")
            }
        }
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, text)
        }
        startActivity(Intent.createChooser(intent, getString(R.string.share_chooser_title)))
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
        PlayerStateStore.startPreparing(track, tracks, index, currentPlaylistName)
        val serviceIntent = Intent(this, PlaybackService::class.java).apply {
            action = PlaybackService.ACTION_PLAY
        }
        startForegroundService(serviceIntent)
        startActivity(Intent(this, NowPlayingActivity::class.java))
    }

    private fun formatTotalDuration(durationMs: Long): String {
        val totalMinutes = (durationMs / 60_000L).coerceAtLeast(0L)
        val hours = totalMinutes / 60L
        val mins = totalMinutes % 60L
        return if (hours > 0L) "${hours}h ${mins}min" else "${mins}min"
    }

    companion object {
        const val EXTRA_PLAYLIST_ID = "extra_playlist_id"
        const val EXTRA_SMART_PLAYLIST_TYPE = "extra_smart_playlist_type"
        const val SMART_MOST_PLAYED = "smart_most_played"
        const val SMART_RECENTLY_ADDED = "smart_recently_added"
        private const val MENU_ADD_TO_QUEUE = 1
        private const val MENU_MOVE_UP = 2
        private const val MENU_MOVE_DOWN = 3
        private const val MENU_REMOVE_FROM_PLAYLIST = 4
        private const val MENU_DETAILS = 5
        private const val MENU_TOGGLE_PIN = 10
        private const val MENU_RENAME_PLAYLIST = 11
        private const val MENU_DELETE_PLAYLIST = 12
    }
}
