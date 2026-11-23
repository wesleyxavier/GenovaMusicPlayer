package com.genova.musicplayer.ui

import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.ListView
import android.widget.PopupMenu
import android.widget.ProgressBar
import android.widget.Switch
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
import com.genova.musicplayer.domain.player.PlayerStatus
import com.genova.musicplayer.domain.player.RepeatMode
import com.genova.musicplayer.domain.playlist.Playlist
import com.genova.musicplayer.service.PlaybackService
import com.genova.musicplayer.viewmodel.PlaylistFeedback
import com.genova.musicplayer.viewmodel.PlaylistsViewModel
import com.genova.musicplayer.viewmodel.QueueUiState
import com.genova.musicplayer.viewmodel.QueueViewModel
import java.util.Locale
import java.util.concurrent.Executors

class QueueActivity : Activity() {

    private lateinit var viewModel: QueueViewModel
    private lateinit var statsRepository: TrackPlayStatsRepository
    private lateinit var adapter: QueueTrackAdapter

    private lateinit var tvTotalBadge: TextView
    private lateinit var tvOrigin: TextView
    private lateinit var tvShuffleBadge: TextView
    private lateinit var ivCurrentArt: ImageView
    private lateinit var tvCurrentTitle: TextView
    private lateinit var tvCurrentArtist: TextView
    private lateinit var tvCurrentQuality: TextView
    private lateinit var tvCurrentTime: TextView
    private lateinit var progressCurrent: ProgressBar
    private lateinit var btnPlayPause: ImageButton
    private lateinit var tvRemainingSummary: TextView
    private lateinit var switchAutoPlay: Switch
    private lateinit var tvEmpty: TextView
    private lateinit var listUpcoming: ListView
    private lateinit var btnShuffle: ImageButton
    private lateinit var btnRepeat: ImageButton

    private val ioExecutor = Executors.newSingleThreadExecutor()
    private val mainHandler = Handler(Looper.getMainLooper())
    private var lastLoadedTrackId: Long? = null
    private var artSizePx: Int = 104

    private val sessionListener: (PlaybackSession) -> Unit = { session ->
        mainHandler.post {
            if (!isFinishing && !isDestroyed) {
                viewModel.onSessionUpdated(session)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_queue)

        artSizePx = (52 * resources.displayMetrics.density).toInt().coerceAtLeast(52)
        viewModel = QueueViewModel()
        statsRepository = TrackPlayStatsRepository(this)

        tvTotalBadge = findViewById(R.id.tv_queue_total_badge)
        tvOrigin = findViewById(R.id.tv_queue_origin)
        tvShuffleBadge = findViewById(R.id.tv_queue_shuffle_badge)
        ivCurrentArt = findViewById(R.id.iv_queue_current_art)
        tvCurrentTitle = findViewById(R.id.tv_queue_current_title)
        tvCurrentArtist = findViewById(R.id.tv_queue_current_artist)
        tvCurrentQuality = findViewById(R.id.tv_queue_current_quality)
        tvCurrentTime = findViewById(R.id.tv_queue_current_time)
        progressCurrent = findViewById(R.id.progress_queue_current)
        btnPlayPause = findViewById(R.id.btn_queue_play_pause)
        tvRemainingSummary = findViewById(R.id.tv_queue_remaining_summary)
        switchAutoPlay = findViewById(R.id.switch_queue_autoplay)
        tvEmpty = findViewById(R.id.tv_queue_empty)
        listUpcoming = findViewById(R.id.list_queue_upcoming)
        btnShuffle = findViewById(R.id.btn_queue_shuffle)
        btnRepeat = findViewById(R.id.btn_queue_repeat)

        adapter = QueueTrackAdapter(
            context = this,
            onMoreClicked = { pos, _, anchor -> showUpcomingItemMenu(pos, anchor) },
            onSwapItems = { fromPos, toPos -> viewModel.moveQueueItem(fromPos, toPos) }
        )
        listUpcoming.adapter = adapter
        listUpcoming.setOnItemClickListener { _, _, position, _ ->
            if (viewModel.playQueueItem(position)) {
                dispatchServiceAction(PlaybackService.ACTION_PLAY)
            }
        }

        findViewById<ImageButton>(R.id.btn_queue_collapse).setOnClickListener { finish() }
        findViewById<Button>(R.id.btn_queue_clear).setOnClickListener { confirmClearQueue() }
        findViewById<Button>(R.id.btn_queue_add).setOnClickListener { showAddToQueueDialog() }
        findViewById<Button>(R.id.btn_queue_save_playlist).setOnClickListener { saveQueueAsPlaylist() }
        btnPlayPause.setOnClickListener { dispatchServiceAction(PlaybackService.ACTION_TOGGLE_PLAY_PAUSE) }
        btnShuffle.setOnClickListener { dispatchServiceAction(PlaybackService.ACTION_TOGGLE_SHUFFLE) }
        btnRepeat.setOnClickListener { dispatchServiceAction(PlaybackService.ACTION_CYCLE_REPEAT) }

        switchAutoPlay.isChecked = statsRepository.isAutoPlayAfterQueueEnabled()
        switchAutoPlay.setOnCheckedChangeListener { _, isChecked ->
            statsRepository.setAutoPlayAfterQueueEnabled(isChecked)
            viewModel.setAutoPlayAfterQueue(isChecked)
        }

        viewModel.onStateChanged = { state -> render(state) }
    }

    override fun onResume() {
        super.onResume()
        PlayerStateStore.addListener(sessionListener)
        viewModel.onSessionUpdated(PlayerStateStore.getState())
    }

    override fun onPause() {
        super.onPause()
        PlayerStateStore.removeListener(sessionListener)
    }

    override fun onDestroy() {
        super.onDestroy()
        ioExecutor.shutdownNow()
    }

    private fun render(state: QueueUiState) {
        val session = state.session
        tvTotalBadge.text = state.queueTracks.size.toString()
        tvOrigin.text = session.queueOriginLabel.ifBlank { getString(R.string.queue_origin_library) }
        tvShuffleBadge.setText(
            if (session.isShuffleEnabled) R.string.queue_shuffle_on else R.string.queue_shuffle_off
        )
        tvShuffleBadge.setTextColor(
            getColor(if (session.isShuffleEnabled) R.color.nv_primary else R.color.nv_on_surface_variant)
        )

        val current = session.currentTrack
        if (current == null) {
            tvCurrentTitle.setText(R.string.playback_error_no_track)
            tvCurrentArtist.text = ""
            tvCurrentQuality.visibility = View.GONE
            tvCurrentTime.text = "00:00 / 00:00"
            progressCurrent.progress = 0
        } else {
            tvCurrentTitle.text = current.title
            tvCurrentArtist.text = current.artist
            tvCurrentQuality.visibility = if (current.isHiFi) View.VISIBLE else View.GONE
            if (lastLoadedTrackId != current.id) {
                lastLoadedTrackId = current.id
                AlbumArtCacheLoader.loadThumb(this, current.contentUri, artSizePx, ivCurrentArt)
            }
            val dur = session.durationMs.coerceAtLeast(1L)
            val pos = session.positionMs.coerceIn(0L, dur)
            tvCurrentTime.text = "${formatDuration(pos)} / ${formatDuration(dur)}"
            progressCurrent.progress = ((pos * 1000L) / dur).toInt()
        }

        val isPlaying = session.status == PlayerStatus.PLAYING || session.status == PlayerStatus.PREPARING
        btnPlayPause.setImageResource(if (isPlaying) R.drawable.ic_pause else R.drawable.ic_play)

        btnShuffle.setColorFilter(
            getColor(if (session.isShuffleEnabled) R.color.nv_primary else R.color.nv_on_surface_variant)
        )
        when (session.repeatMode) {
            RepeatMode.OFF -> {
                btnRepeat.setImageResource(R.drawable.ic_repeat)
                btnRepeat.setColorFilter(getColor(R.color.nv_on_surface_variant))
            }
            RepeatMode.REPEAT_ALL -> {
                btnRepeat.setImageResource(R.drawable.ic_repeat)
                btnRepeat.setColorFilter(getColor(R.color.nv_primary))
            }
            RepeatMode.REPEAT_ONE -> {
                btnRepeat.setImageResource(R.drawable.ic_repeat_one)
                btnRepeat.setColorFilter(getColor(R.color.nv_primary))
            }
        }

        tvRemainingSummary.text = getString(
            R.string.queue_remaining_summary,
            state.remainingCount,
            formatTotalDuration(state.remainingDurationMs)
        )
        adapter.submitList(state.queueTracks, state.currentIndex)
        tvEmpty.visibility = if (state.queueTracks.isEmpty()) View.VISIBLE else View.GONE
        listUpcoming.visibility = if (state.queueTracks.isEmpty()) View.GONE else View.VISIBLE
    }

    private fun showUpcomingItemMenu(position: Int, anchor: View) {
        val queue = viewModel.getState().queueTracks
        if (position !in queue.indices) return

        val popup = PopupMenu(this, anchor)
        popup.menu.add(0, MENU_REMOVE, 0, R.string.menu_remove_from_queue)

        popup.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                MENU_REMOVE -> {
                    viewModel.removeQueueItem(position)
                    true
                }
                else -> false
            }
        }
        popup.show()
    }

    private fun confirmClearQueue() {
        if (viewModel.getState().remainingCount == 0) return
        AlertDialog.Builder(this)
            .setTitle(R.string.queue_clear_confirm_title)
            .setMessage(R.string.queue_clear_confirm_message)
            .setPositiveButton(R.string.queue_clear) { _, _ ->
                viewModel.clearUpcoming()
                Toast.makeText(this, R.string.queue_cleared_toast, Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton(R.string.action_cancel, null)
            .show()
    }

    private fun saveQueueAsPlaylist() {
        val queueTracks = PlayerStateStore.getState().activeList
        if (queueTracks.isEmpty()) {
            Toast.makeText(this, R.string.playback_error_no_track, Toast.LENGTH_SHORT).show()
            return
        }
        val playlistsViewModel = PlaylistsViewModel(PlaylistPreferencesRepository(this))
        playlistsViewModel.loadPlaylists()
        PlaylistDialogs.showCreatePlaylistDialog(
            activity = this,
            onConfirm = { name, isPinned, coverStyle ->
                val created = playlistsViewModel.createPlaylist(name, isPinned, coverStyle)
                if (created != null) {
                    playlistsViewModel.addTracksToPlaylist(created.id, queueTracks.map { it.id })
                    Toast.makeText(
                        this,
                        getString(R.string.playlist_track_added, created.name),
                        Toast.LENGTH_SHORT
                    ).show()
                    true
                } else {
                    val msg = when (val fb = playlistsViewModel.getState().feedback) {
                        PlaylistFeedback.EmptyName -> getString(R.string.playlist_error_empty_name)
                        is PlaylistFeedback.DuplicateName -> getString(R.string.playlist_error_duplicate_name, fb.name)
                        PlaylistFeedback.NameTooLong -> getString(R.string.playlist_error_name_too_long)
                        else -> null
                    }
                    if (msg != null) Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
                    false
                }
            }
        )
    }

    private fun showAddToQueueDialog() {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_add_to_queue, null)
        val btnTabTracks = dialogView.findViewById<Button>(R.id.btn_add_queue_tab_tracks)
        val btnTabPlaylists = dialogView.findViewById<Button>(R.id.btn_add_queue_tab_playlists)
        val etFilter = dialogView.findViewById<EditText>(R.id.et_add_queue_filter)
        val listItems = dialogView.findViewById<ListView>(R.id.list_add_queue_items)

        val playlistsRepo = PlaylistPreferencesRepository(this)
        val playlists: List<Playlist> = playlistsRepo.loadAll()

        val dialog = AlertDialog.Builder(this)
            .setTitle(R.string.queue_add_dialog_title)
            .setView(dialogView)
            .setNegativeButton(R.string.details_close, null)
            .create()

        var showingTracks = true
        var allTracks: List<Track> = emptyList()
        var visibleTracks: List<Track> = emptyList()
        var visiblePlaylists: List<Playlist> = playlists

        fun refreshList() {
            val query = etFilter.text?.toString()?.trim().orEmpty().lowercase(Locale.getDefault())
            if (showingTracks) {
                btnTabTracks.setBackgroundResource(R.drawable.bg_pill_primary)
                btnTabTracks.setTextColor(getColor(R.color.nv_on_primary_container))
                btnTabPlaylists.setBackgroundResource(R.drawable.bg_pill_surface)
                btnTabPlaylists.setTextColor(getColor(R.color.nv_on_surface_variant))

                visibleTracks = if (query.isEmpty()) allTracks else allTracks.filter {
                    it.title.lowercase(Locale.getDefault()).contains(query) ||
                        it.artist.lowercase(Locale.getDefault()).contains(query)
                }
                val labels = visibleTracks.map { "${it.title} — ${it.artist}" }
                listItems.adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, labels)
            } else {
                btnTabPlaylists.setBackgroundResource(R.drawable.bg_pill_primary)
                btnTabPlaylists.setTextColor(getColor(R.color.nv_on_primary_container))
                btnTabTracks.setBackgroundResource(R.drawable.bg_pill_surface)
                btnTabTracks.setTextColor(getColor(R.color.nv_on_surface_variant))

                visiblePlaylists = if (query.isEmpty()) playlists else playlists.filter {
                    it.name.lowercase(Locale.getDefault()).contains(query)
                }
                val labels = visiblePlaylists.map { "${it.name} (${it.trackIds.size})" }
                listItems.adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, labels)
            }
        }

        btnTabTracks.setOnClickListener {
            showingTracks = true
            refreshList()
        }
        btnTabPlaylists.setOnClickListener {
            showingTracks = false
            refreshList()
        }
        etFilter.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) = Unit
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) = refreshList()
            override fun afterTextChanged(s: Editable?) = Unit
        })

        listItems.setOnItemClickListener { _, _, position, _ ->
            if (showingTracks) {
                val track = visibleTracks.getOrNull(position) ?: return@setOnItemClickListener
                val wasIdle = PlayerStateStore.getState().currentTrack == null
                viewModel.appendTracks(listOf(track), getString(R.string.queue_origin_custom))
                if (wasIdle) dispatchServiceAction(PlaybackService.ACTION_PLAY)
                Toast.makeText(this, getString(R.string.queue_added_track_toast, track.title), Toast.LENGTH_SHORT).show()
            } else {
                val playlist = visiblePlaylists.getOrNull(position) ?: return@setOnItemClickListener
                val trackMap = allTracks.associateBy { it.id }
                val playlistTracks = playlist.trackIds.mapNotNull { trackMap[it] }
                if (playlistTracks.isEmpty()) {
                    Toast.makeText(this, R.string.queue_empty_playlist_toast, Toast.LENGTH_SHORT).show()
                    return@setOnItemClickListener
                }
                val wasIdle = PlayerStateStore.getState().currentTrack == null
                viewModel.appendTracks(playlistTracks, playlist.name)
                if (wasIdle) dispatchServiceAction(PlaybackService.ACTION_PLAY)
                Toast.makeText(
                    this,
                    getString(R.string.queue_added_playlist_toast, playlistTracks.size),
                    Toast.LENGTH_SHORT
                ).show()
            }
            dialog.dismiss()
        }

        dialog.show()
        ioExecutor.execute {
            val folders = FolderPreferencesRepository(this).loadFolders()
            val loaded = if (folders.isNotEmpty()) {
                MediaStoreTrackRepository(contentResolver).queryTracks(folders)
            } else {
                emptyList()
            }
            mainHandler.post {
                if (!isFinishing && !isDestroyed) {
                    allTracks = loaded
                    refreshList()
                }
            }
        }
    }

    private fun dispatchServiceAction(action: String) {
        val intent = Intent(this, PlaybackService::class.java).apply {
            this.action = action
        }
        startService(intent)
    }

    private fun formatDuration(durationMs: Long): String {
        val totalSeconds = (durationMs / 1000L).coerceAtLeast(0L)
        val minutes = totalSeconds / 60L
        val seconds = totalSeconds % 60L
        return String.format(Locale.US, "%02d:%02d", minutes, seconds)
    }

    private fun formatTotalDuration(durationMs: Long): String {
        val totalMinutes = (durationMs / 60_000L).coerceAtLeast(0L)
        val hours = totalMinutes / 60L
        val minutes = totalMinutes % 60L
        return if (hours > 0L) "${hours}h ${minutes}min" else "${minutes}min"
    }

    companion object {
        private const val MENU_REMOVE = 1
    }
}
