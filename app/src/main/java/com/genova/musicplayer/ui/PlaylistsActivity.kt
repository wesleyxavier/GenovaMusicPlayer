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
import android.widget.GridView
import android.widget.ImageButton
import android.widget.ListView
import android.widget.PopupMenu
import android.widget.TextView
import android.widget.Toast
import com.genova.musicplayer.R
import com.genova.musicplayer.data.FolderPreferencesRepository
import com.genova.musicplayer.data.MediaStoreTrackRepository
import com.genova.musicplayer.data.PlaylistPreferencesRepository
import com.genova.musicplayer.data.TrackPlayStatsRepository
import com.genova.musicplayer.domain.library.Track
import com.genova.musicplayer.domain.player.PlayerStateStore
import com.genova.musicplayer.domain.playlist.Playlist
import com.genova.musicplayer.domain.playlist.PlaylistSortOrder
import com.genova.musicplayer.domain.playlist.SmartPlaylistEngine
import com.genova.musicplayer.service.PlaybackService
import com.genova.musicplayer.viewmodel.PlaylistFeedback
import com.genova.musicplayer.viewmodel.PlaylistsUiState
import com.genova.musicplayer.viewmodel.PlaylistsViewModel
import java.util.concurrent.Executors

class PlaylistsActivity : Activity() {

    private lateinit var viewModel: PlaylistsViewModel
    private lateinit var statsRepository: TrackPlayStatsRepository
    private lateinit var folderRepository: FolderPreferencesRepository
    private lateinit var trackRepository: MediaStoreTrackRepository
    private lateinit var miniPlayerController: MiniPlayerController
    private lateinit var adapter: PlaylistListAdapter

    private lateinit var etSearch: EditText
    private lateinit var btnSort: Button
    private lateinit var btnToggleView: ImageButton
    private lateinit var tvEmpty: TextView
    private lateinit var gridPlaylists: GridView
    private lateinit var listPlaylists: ListView
    private lateinit var tvSmartMostPlayedCount: TextView
    private lateinit var tvSmartRecentCount: TextView

    private val smartEngine = SmartPlaylistEngine()
    private val ioExecutor = Executors.newSingleThreadExecutor()
    private val mainHandler = Handler(Looper.getMainLooper())

    private var allTracks: List<Track> = emptyList()
    private var tracksById: Map<Long, Track> = emptyMap()
    private var mostPlayedTracks: List<Track> = emptyList()
    private var recentlyAddedTracks: List<Track> = emptyList()
    private var isGridMode: Boolean = true
    private var sortOrder: PlaylistSortOrder = PlaylistSortOrder.RECENT

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_playlists)

        viewModel = PlaylistsViewModel(PlaylistPreferencesRepository(this))
        statsRepository = TrackPlayStatsRepository(this)
        folderRepository = FolderPreferencesRepository(this)
        trackRepository = MediaStoreTrackRepository(contentResolver)
        miniPlayerController = MiniPlayerController(this)

        isGridMode = statsRepository.isPlaylistGridMode()
        sortOrder = statsRepository.loadPlaylistSortOrder()

        etSearch = findViewById(R.id.et_playlists_search)
        btnSort = findViewById(R.id.btn_playlists_sort)
        btnToggleView = findViewById(R.id.btn_playlists_toggle_view)
        tvEmpty = findViewById(R.id.tv_playlists_empty)
        gridPlaylists = findViewById(R.id.grid_playlists)
        listPlaylists = findViewById(R.id.list_playlists)
        tvSmartMostPlayedCount = findViewById(R.id.tv_smart_most_played_count)
        tvSmartRecentCount = findViewById(R.id.tv_smart_recent_count)

        adapter = PlaylistListAdapter(
            context = this,
            onQuickPlayClick = { playlist -> quickPlayPlaylist(playlist) },
            onMoreClick = { playlist, anchor -> showPlaylistMenu(playlist, anchor) }
        )
        gridPlaylists.adapter = adapter
        listPlaylists.adapter = adapter

        val onItemClick: (Int) -> Unit = { position ->
            val visible = getVisiblePlaylists()
            if (position in visible.indices) {
                openPlaylistDetail(visible[position])
            }
        }
        gridPlaylists.setOnItemClickListener { _, _, position, _ -> onItemClick(position) }
        listPlaylists.setOnItemClickListener { _, _, position, _ -> onItemClick(position) }

        val openCreateDialog: () -> Unit = {
            PlaylistDialogs.showCreatePlaylistDialog(this) { name, isPinned, coverStyle ->
                val created = viewModel.createPlaylist(name, isPinned, coverStyle)
                created != null
            }
        }
        findViewById<View>(R.id.card_create_playlist).setOnClickListener { openCreateDialog() }
        findViewById<Button>(R.id.btn_create_playlist).setOnClickListener { openCreateDialog() }

        btnSort.setOnClickListener { anchor -> showSortMenu(anchor) }
        btnToggleView.setOnClickListener {
            isGridMode = !isGridMode
            statsRepository.setPlaylistGridMode(isGridMode)
            updateViewModeUi()
            render(viewModel.getState())
        }

        etSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) = Unit
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                render(viewModel.getState())
            }
            override fun afterTextChanged(s: Editable?) = Unit
        })

        findViewById<ImageButton>(R.id.btn_search_playlists).setOnClickListener {
            etSearch.requestFocus()
        }
        findViewById<View>(R.id.card_smart_most_played).setOnClickListener {
            openSmartPlaylist(PlaylistDetailActivity.SMART_MOST_PLAYED)
        }
        findViewById<View>(R.id.card_smart_recently_added).setOnClickListener {
            openSmartPlaylist(PlaylistDetailActivity.SMART_RECENTLY_ADDED)
        }

        findViewById<View>(R.id.tab_music).setOnClickListener {
            navigateToLibrary(focusSearch = false)
        }
        findViewById<View>(R.id.tab_folders).setOnClickListener {
            navigateToFolders()
        }

        updateSortButtonLabel()
        updateViewModeUi()
        viewModel.onStateChanged = { state -> render(state) }
    }

    override fun onResume() {
        super.onResume()
        miniPlayerController.bind()
        viewModel.loadPlaylists()
        loadTracksAndSmartPlaylists()
    }

    override fun onPause() {
        super.onPause()
        miniPlayerController.unbind()
    }

    override fun onDestroy() {
        super.onDestroy()
        ioExecutor.shutdownNow()
    }

    private fun loadTracksAndSmartPlaylists() {
        ioExecutor.execute {
            val folders = folderRepository.loadFolders()
            val tracks = if (folders.isNotEmpty()) {
                trackRepository.queryTracks(folders)
            } else {
                emptyList()
            }
            val counts = statsRepository.loadPlayCounts()
            val mostPlayed = smartEngine.buildMostPlayed(tracks, counts)
            val recent = smartEngine.buildRecentlyAdded(tracks)
            mainHandler.post {
                if (!isFinishing && !isDestroyed) {
                    allTracks = tracks
                    tracksById = tracks.associateBy { it.id }
                    mostPlayedTracks = mostPlayed
                    recentlyAddedTracks = recent
                    tvSmartMostPlayedCount.text = getString(R.string.playlist_track_count_format, mostPlayed.size)
                    tvSmartRecentCount.text = getString(R.string.playlist_track_count_format, recent.size)
                    render(viewModel.getState())
                }
            }
        }
    }

    private fun getVisiblePlaylists(): List<Playlist> {
        return viewModel.getSortedAndFiltered(etSearch.text?.toString().orEmpty(), sortOrder)
    }

    private fun render(state: PlaylistsUiState) {
        val visible = getVisiblePlaylists()
        adapter.submitList(visible, tracksById, isGridMode)

        if (visible.isEmpty()) {
            tvEmpty.visibility = View.VISIBLE
            gridPlaylists.visibility = View.GONE
            listPlaylists.visibility = View.GONE
        } else {
            tvEmpty.visibility = View.GONE
            gridPlaylists.visibility = if (isGridMode) View.VISIBLE else View.GONE
            listPlaylists.visibility = if (isGridMode) View.GONE else View.VISIBLE
        }

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
            viewModel.clearFeedback()
        }
    }

    private fun quickPlayPlaylist(playlist: Playlist) {
        val resolved = playlist.trackIds.mapNotNull { tracksById[it] }
        if (resolved.isEmpty()) {
            Toast.makeText(this, R.string.queue_empty_playlist_toast, Toast.LENGTH_SHORT).show()
            return
        }
        PlayerStateStore.startPreparing(resolved.first(), resolved, 0, playlist.name)
        val serviceIntent = Intent(this, PlaybackService::class.java).apply {
            action = PlaybackService.ACTION_PLAY
        }
        startForegroundService(serviceIntent)
        startActivity(Intent(this, NowPlayingActivity::class.java))
    }

    private fun showSortMenu(anchor: View) {
        val popup = PopupMenu(this, anchor)
        popup.menu.add(0, 1, 0, R.string.playlist_sort_recent)
        popup.menu.add(0, 2, 1, R.string.playlist_sort_alpha)
        popup.menu.add(0, 3, 2, R.string.playlist_sort_most_tracks)
        popup.setOnMenuItemClickListener { item ->
            sortOrder = when (item.itemId) {
                2 -> PlaylistSortOrder.ALPHABETICAL
                3 -> PlaylistSortOrder.MOST_TRACKS
                else -> PlaylistSortOrder.RECENT
            }
            statsRepository.savePlaylistSortOrder(sortOrder)
            updateSortButtonLabel()
            render(viewModel.getState())
            true
        }
        popup.show()
    }

    private fun updateSortButtonLabel() {
        btnSort.setText(
            when (sortOrder) {
                PlaylistSortOrder.RECENT -> R.string.playlist_sort_recent
                PlaylistSortOrder.ALPHABETICAL -> R.string.playlist_sort_alpha
                PlaylistSortOrder.MOST_TRACKS -> R.string.playlist_sort_most_tracks
            }
        )
    }

    private fun updateViewModeUi() {
        btnToggleView.setImageResource(if (isGridMode) R.drawable.ic_list_view else R.drawable.ic_grid_view)
        btnToggleView.contentDescription = getString(
            if (isGridMode) R.string.cd_playlist_view_list else R.string.cd_playlist_view_grid
        )
    }

    private fun showPlaylistMenu(playlist: Playlist, anchor: View) {
        val popup = PopupMenu(this, anchor)
        popup.menu.add(0, MENU_ADD_TO_QUEUE, 0, R.string.menu_add_to_queue)
        popup.menu.add(
            0,
            MENU_TOGGLE_PIN,
            1,
            if (playlist.isPinned) R.string.playlist_unpin_action else R.string.playlist_pin_action
        )
        popup.menu.add(0, MENU_RENAME, 2, R.string.action_rename)
        popup.menu.add(0, MENU_DELETE, 3, R.string.action_delete)
        popup.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                MENU_ADD_TO_QUEUE -> {
                    val resolved = playlist.trackIds.mapNotNull { tracksById[it] }
                    if (resolved.isEmpty()) {
                        Toast.makeText(this, R.string.queue_empty_playlist_toast, Toast.LENGTH_SHORT).show()
                    } else {
                        val wasIdle = PlayerStateStore.getState().currentTrack == null
                        PlayerStateStore.appendToQueue(resolved, playlist.name)
                        if (wasIdle) {
                            startForegroundService(
                                Intent(this, PlaybackService::class.java).apply {
                                    action = PlaybackService.ACTION_PLAY
                                }
                            )
                        }
                        Toast.makeText(
                            this,
                            getString(R.string.queue_added_playlist_toast, resolved.size),
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                    true
                }
                MENU_TOGGLE_PIN -> {
                    viewModel.togglePin(playlist.id)
                    true
                }
                MENU_RENAME -> {
                    showRenameDialog(playlist)
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

    private fun showRenameDialog(playlist: Playlist) {
        val input = EditText(this).apply {
            hint = getString(R.string.playlist_name_hint)
            setText(playlist.name)
            setSelection(playlist.name.length)
        }
        AlertDialog.Builder(this)
            .setTitle(R.string.playlist_rename_title)
            .setView(input)
            .setPositiveButton(R.string.action_save) { _, _ ->
                viewModel.renamePlaylist(playlist.id, input.text.toString())
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

    private fun openSmartPlaylist(smartType: String) {
        val intent = Intent(this, PlaylistDetailActivity::class.java).apply {
            putExtra(PlaylistDetailActivity.EXTRA_SMART_PLAYLIST_TYPE, smartType)
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
        private const val MENU_ADD_TO_QUEUE = 1
        private const val MENU_TOGGLE_PIN = 2
        private const val MENU_RENAME = 3
        private const val MENU_DELETE = 4
    }
}
