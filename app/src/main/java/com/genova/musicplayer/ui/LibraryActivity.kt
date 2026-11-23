package com.genova.musicplayer.ui

import android.Manifest
import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.SystemClock
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.ListView
import android.widget.PopupMenu
import android.widget.TextView
import android.widget.Toast
import com.genova.musicplayer.R
import com.genova.musicplayer.data.FolderPreferencesRepository
import com.genova.musicplayer.data.MediaStoreTrackRepository
import com.genova.musicplayer.data.PlaylistPreferencesRepository
import com.genova.musicplayer.domain.library.LibraryFilter
import com.genova.musicplayer.domain.library.Track
import com.genova.musicplayer.domain.player.PlaybackSession
import com.genova.musicplayer.domain.player.PlayerStateStore
import com.genova.musicplayer.domain.player.PlayerStatus
import com.genova.musicplayer.service.PlaybackService
import com.genova.musicplayer.viewmodel.LibraryUiState
import com.genova.musicplayer.viewmodel.LibraryViewModel
import com.genova.musicplayer.viewmodel.PlaylistFeedback
import com.genova.musicplayer.viewmodel.PlaylistsViewModel
import java.util.Locale
import java.util.concurrent.Executors

class LibraryActivity : Activity() {

    private lateinit var folderRepository: FolderPreferencesRepository
    private lateinit var trackRepository: MediaStoreTrackRepository
    private lateinit var viewModel: LibraryViewModel
    private lateinit var adapter: TrackListAdapter

    private lateinit var etSearch: EditText
    private lateinit var btnClearSearch: ImageButton
    private lateinit var chipAll: TextView
    private lateinit var chipNowPlaying: TextView
    private lateinit var chipRecent: TextView
    private lateinit var miniPlayerController: MiniPlayerController
    private lateinit var tvTrackCount: TextView
    private lateinit var btnShuffleAll: Button
    private lateinit var listTracks: ListView
    private lateinit var layoutEmpty: LinearLayout
    private lateinit var tvEmptyMessage: TextView
    private lateinit var btnEmptyAction: Button

    private val ioExecutor = Executors.newSingleThreadExecutor()
    private val mainHandler = Handler(Looper.getMainLooper())

    private val playerListener: (PlaybackSession) -> Unit = { session ->
        mainHandler.post {
            viewModel.updatePlaybackIndicator(
                activeTrackId = session.currentTrack?.id,
                isPlaying = session.status == PlayerStatus.PLAYING
            )
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_library)

        folderRepository = FolderPreferencesRepository(this)
        trackRepository = MediaStoreTrackRepository(contentResolver)
        viewModel = LibraryViewModel()

        etSearch = findViewById(R.id.et_search)
        btnClearSearch = findViewById(R.id.btn_clear_search)
        chipAll = findViewById(R.id.chip_all)
        chipNowPlaying = findViewById(R.id.chip_now_playing)
        chipRecent = findViewById(R.id.chip_recent)
        miniPlayerController = MiniPlayerController(this)
        tvTrackCount = findViewById(R.id.tv_track_count)
        btnShuffleAll = findViewById(R.id.btn_shuffle_all)
        listTracks = findViewById(R.id.list_tracks)
        layoutEmpty = findViewById(R.id.layout_empty_state)
        tvEmptyMessage = findViewById(R.id.tv_empty_message)
        btnEmptyAction = findViewById(R.id.btn_empty_action)

        adapter = TrackListAdapter(this) { track, anchor ->
            showTrackContextMenu(track, anchor)
        }
        listTracks.adapter = adapter
        listTracks.setOnItemClickListener { _, _, position, _ ->
            val tracks = viewModel.getState().visibleTracks
            if (position in tracks.indices) {
                playSingleTrack(tracks[position])
            }
        }

        findViewById<ImageButton>(R.id.btn_focus_search).setOnClickListener {
            focusSearchInput()
        }
        btnClearSearch.setOnClickListener {
            etSearch.setText("")
        }
        etSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) = Unit
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val query = s?.toString().orEmpty()
                btnClearSearch.visibility = if (query.isNotEmpty()) View.VISIBLE else View.GONE
                viewModel.onSearchQueryChanged(query)
            }
            override fun afterTextChanged(s: Editable?) = Unit
        })

        chipAll.setOnClickListener { viewModel.onFilterSelected(LibraryFilter.ALL) }
        chipNowPlaying.setOnClickListener {
            if (PlayerStateStore.getState().currentTrack != null) {
                startActivity(Intent(this, NowPlayingActivity::class.java))
            } else {
                Toast.makeText(this, R.string.no_active_playback_toast, Toast.LENGTH_SHORT).show()
            }
        }
        chipRecent.setOnClickListener { viewModel.onFilterSelected(LibraryFilter.RECENT) }

        btnShuffleAll.setOnClickListener {
            val tracks = viewModel.getState().visibleTracks
            if (tracks.isNotEmpty()) {
                val startIndex = tracks.indices.random()
                startPlayback(tracks, startIndex, enableShuffle = true)
            }
        }

        findViewById<View>(R.id.tab_playlists).setOnClickListener {
            navigateToPlaylists()
        }
        findViewById<View>(R.id.tab_folders).setOnClickListener {
            navigateToFolders()
        }

        viewModel.onStateChanged = { state -> render(state) }

        if (intent.getBooleanExtra(EXTRA_FOCUS_SEARCH, false)) {
            focusSearchInput()
        }
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        if (intent?.getBooleanExtra(EXTRA_FOCUS_SEARCH, false) == true) {
            focusSearchInput()
        }
    }

    override fun onResume() {
        super.onResume()
        miniPlayerController.bind()
        PlayerStateStore.addListener(playerListener)
        val currentSession = PlayerStateStore.getState()
        viewModel.updatePlaybackIndicator(
            activeTrackId = currentSession.currentTrack?.id,
            isPlaying = currentSession.status == PlayerStatus.PLAYING
        )
        loadLibrary()
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

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_AUDIO_PERMISSION) {
            loadLibrary()
        }
    }

    private fun loadLibrary() {
        val hasPermission = hasAudioPermission()
        viewModel.setPermissionState(hasPermission)
        val folders = folderRepository.loadFolders()

        if (!hasPermission) {
            viewModel.setTracks(emptyList(), folders.isNotEmpty())
            return
        }

        if (folders.isEmpty()) {
            viewModel.setTracks(emptyList(), hasFolders = false)
            return
        }

        viewModel.setLoading(true, hasFolders = true)
        val startMs = SystemClock.elapsedRealtime()
        ioExecutor.execute {
            val tracks = trackRepository.queryTracks(folders)
            folderRepository.saveLastScanEpochMs(System.currentTimeMillis())
            val elapsedMs = SystemClock.elapsedRealtime() - startMs
            Log.i("GenovaBenchmark", "Loaded ${tracks.size} tracks in ${elapsedMs}ms")
            mainHandler.post {
                if (!isFinishing && !isDestroyed) {
                    viewModel.setTracks(tracks, hasFolders = true)
                }
            }
        }
    }

    private fun render(state: LibraryUiState) {
        tvTrackCount.text = getString(R.string.library_count_format, state.visibleTracks.size)
        btnShuffleAll.isEnabled = state.visibleTracks.isNotEmpty()
        btnShuffleAll.alpha = if (state.visibleTracks.isNotEmpty()) 1.0f else 0.5f

        updateFilterChips(state.activeFilter)
        adapter.submitList(state.visibleTracks, state.activeTrackId)

        when {
            !state.hasAudioPermission -> {
                listTracks.visibility = View.GONE
                layoutEmpty.visibility = View.VISIBLE
                tvEmptyMessage.setText(R.string.library_permission_required)
                btnEmptyAction.setText(R.string.library_grant_permission)
                btnEmptyAction.visibility = View.VISIBLE
                btnEmptyAction.setOnClickListener { requestAudioPermission() }
            }
            !state.hasAuthorizedFolders -> {
                listTracks.visibility = View.GONE
                layoutEmpty.visibility = View.VISIBLE
                tvEmptyMessage.setText(R.string.library_empty_no_folders)
                btnEmptyAction.setText(R.string.library_go_to_folders)
                btnEmptyAction.visibility = View.VISIBLE
                btnEmptyAction.setOnClickListener { navigateToFolders() }
            }
            state.visibleTracks.isEmpty() && !state.isLoading -> {
                listTracks.visibility = View.GONE
                layoutEmpty.visibility = View.VISIBLE
                tvEmptyMessage.setText(
                    if (state.searchQuery.isNotBlank()) R.string.library_empty_search
                    else R.string.library_empty_no_tracks
                )
                btnEmptyAction.visibility = View.GONE
            }
            else -> {
                layoutEmpty.visibility = View.GONE
                listTracks.visibility = View.VISIBLE
            }
        }
    }

    private fun updateFilterChips(activeFilter: LibraryFilter) {
        val activeBg = R.drawable.bg_pill_primary
        val inactiveBg = R.drawable.bg_pill_surface
        val activeText = getColor(R.color.nv_on_primary_container)
        val inactiveText = getColor(R.color.nv_on_surface_variant)

        if (activeFilter == LibraryFilter.ALL) {
            chipAll.setBackgroundResource(activeBg)
            chipAll.setTextColor(activeText)
            chipRecent.setBackgroundResource(inactiveBg)
            chipRecent.setTextColor(inactiveText)
        } else {
            chipRecent.setBackgroundResource(activeBg)
            chipRecent.setTextColor(activeText)
            chipAll.setBackgroundResource(inactiveBg)
            chipAll.setTextColor(inactiveText)
        }
    }

    private fun showTrackContextMenu(track: Track, anchor: View) {
        val popup = PopupMenu(this, anchor)
        popup.menu.add(0, MENU_ADD_TO_QUEUE, 0, R.string.menu_add_to_queue)
        popup.menu.add(0, MENU_ADD_TO_PLAYLIST, 1, R.string.menu_add_to_playlist)
        popup.menu.add(0, MENU_DETAILS, 2, R.string.menu_track_details)
        popup.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                MENU_ADD_TO_QUEUE -> {
                    val wasIdle = PlayerStateStore.getState().currentTrack == null
                    PlayerStateStore.appendToQueue(listOf(track), getString(R.string.queue_origin_library))
                    if (wasIdle) {
                        val serviceIntent = Intent(this, PlaybackService::class.java).apply {
                            action = PlaybackService.ACTION_PLAY
                        }
                        startForegroundService(serviceIntent)
                    }
                    Toast.makeText(
                        this,
                        getString(R.string.queue_added_track_toast, track.title),
                        Toast.LENGTH_SHORT
                    ).show()
                    true
                }
                MENU_ADD_TO_PLAYLIST -> {
                    showAddToPlaylistDialog(track)
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

    private fun showAddToPlaylistDialog(track: Track) {
        val playlistsViewModel = PlaylistsViewModel(PlaylistPreferencesRepository(this))
        PlaylistDialogs.showAddToPlaylistsDialog(
            activity = this,
            track = track,
            viewModel = playlistsViewModel
        )
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

    private fun playSingleTrack(track: Track) {
        PlayerStateStore.enqueueAsLastAndPlay(track, getString(R.string.queue_origin_library))
        val serviceIntent = Intent(this, PlaybackService::class.java).apply {
            action = PlaybackService.ACTION_PLAY
        }
        startForegroundService(serviceIntent)
        startActivity(Intent(this, NowPlayingActivity::class.java))
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

    private fun focusSearchInput() {
        etSearch.requestFocus()
        val imm = getSystemService(INPUT_METHOD_SERVICE) as? InputMethodManager
        imm?.showSoftInput(etSearch, InputMethodManager.SHOW_IMPLICIT)
    }

    private fun hasAudioPermission(): Boolean {
        val permission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            Manifest.permission.READ_MEDIA_AUDIO
        } else {
            Manifest.permission.READ_EXTERNAL_STORAGE
        }
        return checkSelfPermission(permission) == PackageManager.PERMISSION_GRANTED
    }

    private fun requestAudioPermission() {
        val permission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            Manifest.permission.READ_MEDIA_AUDIO
        } else {
            Manifest.permission.READ_EXTERNAL_STORAGE
        }
        requestPermissions(arrayOf(permission), REQUEST_AUDIO_PERMISSION)
    }

    private fun navigateToPlaylists() {
        val intent = Intent(this, PlaylistsActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
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
        const val EXTRA_FOCUS_SEARCH = "extra_focus_search"
        private const val REQUEST_AUDIO_PERMISSION = 2001
        private const val MENU_ADD_TO_QUEUE = 0
        private const val MENU_DETAILS = 1
        private const val MENU_ADD_TO_PLAYLIST = 2
    }
}
