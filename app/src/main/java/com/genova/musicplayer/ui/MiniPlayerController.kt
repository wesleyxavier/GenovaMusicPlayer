package com.genova.musicplayer.ui

import android.app.Activity
import android.content.Intent
import android.os.Handler
import android.os.Looper
import android.view.View
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import com.genova.musicplayer.R
import com.genova.musicplayer.data.AlbumArtCacheLoader
import com.genova.musicplayer.domain.player.PlaybackSession
import com.genova.musicplayer.domain.player.PlayerStateStore
import com.genova.musicplayer.domain.player.PlayerStatus
import com.genova.musicplayer.service.PlaybackService

class MiniPlayerController(private val activity: Activity) {

    private val container: View? = activity.findViewById(R.id.mini_player_container)
    private val ivArt: ImageView? = activity.findViewById(R.id.iv_mini_player_art)
    private val tvTitle: TextView? = activity.findViewById(R.id.tv_mini_player_title)
    private val tvArtist: TextView? = activity.findViewById(R.id.tv_mini_player_artist)
    private val btnPlayPause: ImageButton? = activity.findViewById(R.id.btn_mini_player_play_pause)
    private val btnNext: ImageButton? = activity.findViewById(R.id.btn_mini_player_next)
    private val progressBar: ProgressBar? = activity.findViewById(R.id.progress_mini_player)

    private val mainHandler = Handler(Looper.getMainLooper())
    private val thumbSizePx = (40 * activity.resources.displayMetrics.density).toInt().coerceAtLeast(40)
    private var lastLoadedTrackId: Long? = null

    private val listener: (PlaybackSession) -> Unit = { session ->
        mainHandler.post {
            if (!activity.isFinishing && !activity.isDestroyed) {
                render(session)
            }
        }
    }

    init {
        container?.setOnClickListener {
            if (PlayerStateStore.getState().currentTrack != null) {
                activity.startActivity(Intent(activity, NowPlayingActivity::class.java))
            }
        }
        btnPlayPause?.setOnClickListener {
            dispatchAction(PlaybackService.ACTION_TOGGLE_PLAY_PAUSE)
        }
        btnNext?.setOnClickListener {
            dispatchAction(PlaybackService.ACTION_NEXT)
        }
    }

    fun bind() {
        PlayerStateStore.addListener(listener)
        render(PlayerStateStore.getState())
    }

    fun unbind() {
        PlayerStateStore.removeListener(listener)
    }

    private fun render(session: PlaybackSession) {
        val root = container ?: return
        val track = session.currentTrack
        if (track == null || session.status == PlayerStatus.IDLE) {
            root.visibility = View.GONE
            lastLoadedTrackId = null
            return
        }

        root.visibility = View.VISIBLE
        tvTitle?.text = track.title
        tvArtist?.text = track.artist

        if (lastLoadedTrackId != track.id) {
            lastLoadedTrackId = track.id
            ivArt?.let { imageView ->
                AlbumArtCacheLoader.loadThumb(activity, track.contentUri, thumbSizePx, imageView)
            }
        }

        val isPlaying = session.status == PlayerStatus.PLAYING || session.status == PlayerStatus.PREPARING
        btnPlayPause?.setImageResource(if (isPlaying) R.drawable.ic_pause else R.drawable.ic_play)

        val duration = session.durationMs.coerceAtLeast(1L)
        val progress = ((session.positionMs.coerceIn(0L, duration) * 1000L) / duration).toInt()
        progressBar?.progress = progress
    }

    private fun dispatchAction(action: String) {
        val intent = Intent(activity, PlaybackService::class.java).apply {
            this.action = action
        }
        activity.startService(intent)
    }
}
