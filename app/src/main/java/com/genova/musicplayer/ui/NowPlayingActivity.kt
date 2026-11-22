package com.genova.musicplayer.ui

import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import android.view.View
import android.widget.Button
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.SeekBar
import android.widget.TextView
import android.widget.Toast
import com.genova.musicplayer.R
import com.genova.musicplayer.data.AlbumArtCacheLoader
import com.genova.musicplayer.domain.player.PlaybackSession
import com.genova.musicplayer.domain.player.PlayerStateStore
import com.genova.musicplayer.domain.player.PlayerStatus
import com.genova.musicplayer.domain.player.RepeatMode
import com.genova.musicplayer.service.PlaybackService
import java.util.Locale

class NowPlayingActivity : Activity() {

    private lateinit var ivAlbumArt: ImageView
    private lateinit var tvHifiBadge: TextView
    private lateinit var tvTitle: TextView
    private lateinit var tvArtist: TextView
    private lateinit var tvError: TextView
    private lateinit var seekBar: SeekBar
    private lateinit var tvElapsed: TextView
    private lateinit var tvTotal: TextView
    private lateinit var btnShuffle: ImageButton
    private lateinit var dotShuffle: View
    private lateinit var btnPlayPause: ImageButton
    private lateinit var btnRepeat: ImageButton
    private lateinit var dotRepeat: View
    private lateinit var tvAudioOutput: TextView

    private val mainHandler = Handler(Looper.getMainLooper())
    private var isUserSeeking = false
    private var lastLoadedTrackId: Long? = null
    private var artSizePx: Int = 640

    private val sessionListener: (PlaybackSession) -> Unit = { session ->
        mainHandler.post {
            if (!isFinishing && !isDestroyed) {
                render(session)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_now_playing)

        artSizePx = (320 * resources.displayMetrics.density).toInt().coerceAtLeast(320)

        ivAlbumArt = findViewById(R.id.iv_album_art_large)
        tvHifiBadge = findViewById(R.id.tv_hifi_badge)
        tvTitle = findViewById(R.id.tv_np_title)
        tvArtist = findViewById(R.id.tv_np_artist)
        tvError = findViewById(R.id.tv_np_error)
        seekBar = findViewById(R.id.seek_progress)
        tvElapsed = findViewById(R.id.tv_elapsed_time)
        tvTotal = findViewById(R.id.tv_total_time)
        btnShuffle = findViewById(R.id.btn_shuffle)
        dotShuffle = findViewById(R.id.dot_shuffle)
        btnPlayPause = findViewById(R.id.btn_play_pause)
        btnRepeat = findViewById(R.id.btn_repeat)
        dotRepeat = findViewById(R.id.dot_repeat)
        tvAudioOutput = findViewById(R.id.tv_audio_output)

        findViewById<ImageButton>(R.id.btn_back).setOnClickListener { finish() }
        findViewById<ImageButton>(R.id.btn_now_playing_more).setOnClickListener {
            showTechnicalDetails()
        }
        findViewById<ImageButton>(R.id.btn_share).setOnClickListener {
            shareCurrentTrack()
        }

        btnShuffle.setOnClickListener { dispatchServiceAction(PlaybackService.ACTION_TOGGLE_SHUFFLE) }
        findViewById<ImageButton>(R.id.btn_previous).setOnClickListener {
            dispatchServiceAction(PlaybackService.ACTION_PREVIOUS)
        }
        btnPlayPause.setOnClickListener {
            dispatchServiceAction(PlaybackService.ACTION_TOGGLE_PLAY_PAUSE)
        }
        findViewById<ImageButton>(R.id.btn_next).setOnClickListener {
            dispatchServiceAction(PlaybackService.ACTION_NEXT)
        }
        btnRepeat.setOnClickListener { dispatchServiceAction(PlaybackService.ACTION_CYCLE_REPEAT) }

        findViewById<Button>(R.id.btn_switch_output).setOnClickListener {
            startActivity(Intent(Settings.ACTION_BLUETOOTH_SETTINGS))
        }

        findViewById<View>(R.id.btn_queue_placeholder).setOnClickListener {
            Toast.makeText(this, R.string.coming_soon_toast, Toast.LENGTH_SHORT).show()
        }
        findViewById<View>(R.id.btn_lyrics_placeholder).setOnClickListener {
            Toast.makeText(this, R.string.coming_soon_toast, Toast.LENGTH_SHORT).show()
        }

        seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if (fromUser) {
                    tvElapsed.text = formatTime(progress.toLong())
                }
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {
                isUserSeeking = true
            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {
                isUserSeeking = false
                val targetMs = seekBar?.progress?.toLong() ?: 0L
                val intent = Intent(this@NowPlayingActivity, PlaybackService::class.java).apply {
                    action = PlaybackService.ACTION_SEEK
                    putExtra(PlaybackService.EXTRA_SEEK_POSITION_MS, targetMs)
                }
                startService(intent)
            }
        })
    }

    override fun onResume() {
        super.onResume()
        PlayerStateStore.addListener(sessionListener)
        render(PlayerStateStore.getState())
    }

    override fun onPause() {
        super.onPause()
        PlayerStateStore.removeListener(sessionListener)
    }

    private fun render(session: PlaybackSession) {
        val track = session.currentTrack
        if (track == null) {
            tvTitle.setText(R.string.playback_error_no_track)
            tvArtist.text = ""
            tvHifiBadge.visibility = View.GONE
        } else {
            tvTitle.text = track.title
            tvArtist.text = track.artist
            tvHifiBadge.visibility = if (track.isHiFi) View.VISIBLE else View.GONE
            if (lastLoadedTrackId != track.id) {
                lastLoadedTrackId = track.id
                AlbumArtCacheLoader.loadThumb(this, track.contentUri, artSizePx, ivAlbumArt)
            }
        }

        if (session.status == PlayerStatus.ERROR && !session.errorMessage.isNullOrBlank()) {
            tvError.text = session.errorMessage
            tvError.visibility = View.VISIBLE
        } else {
            tvError.visibility = View.GONE
        }

        val duration = session.durationMs.coerceAtLeast(0L)
        seekBar.max = duration.toInt()
        if (!isUserSeeking) {
            seekBar.progress = session.positionMs.coerceIn(0L, duration).toInt()
            tvElapsed.text = formatTime(session.positionMs)
        }
        tvTotal.text = formatTime(duration)

        val isPlaying = session.status == PlayerStatus.PLAYING
        btnPlayPause.setImageResource(if (isPlaying) R.drawable.ic_pause else R.drawable.ic_play)

        val activeTint = getColor(R.color.nv_primary)
        val inactiveTint = getColor(R.color.nv_on_surface_variant)

        btnShuffle.setColorFilter(if (session.isShuffleEnabled) activeTint else inactiveTint)
        dotShuffle.visibility = if (session.isShuffleEnabled) View.VISIBLE else View.INVISIBLE

        when (session.repeatMode) {
            RepeatMode.OFF -> {
                btnRepeat.setImageResource(R.drawable.ic_repeat)
                btnRepeat.setColorFilter(inactiveTint)
                dotRepeat.visibility = View.INVISIBLE
            }
            RepeatMode.REPEAT_ALL -> {
                btnRepeat.setImageResource(R.drawable.ic_repeat)
                btnRepeat.setColorFilter(activeTint)
                dotRepeat.visibility = View.VISIBLE
            }
            RepeatMode.REPEAT_ONE -> {
                btnRepeat.setImageResource(R.drawable.ic_repeat_one)
                btnRepeat.setColorFilter(activeTint)
                dotRepeat.visibility = View.VISIBLE
            }
        }

        tvAudioOutput.text = session.audioOutputName
    }

    private fun dispatchServiceAction(action: String) {
        val intent = Intent(this, PlaybackService::class.java).apply {
            this.action = action
        }
        startService(intent)
    }

    private fun shareCurrentTrack() {
        val track = PlayerStateStore.getState().currentTrack ?: return
        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = track.mimeType
            putExtra(Intent.EXTRA_STREAM, Uri.parse(track.contentUri))
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        startActivity(Intent.createChooser(shareIntent, getString(R.string.share_chooser_title)))
    }

    private fun showTechnicalDetails() {
        val track = PlayerStateStore.getState().currentTrack ?: return
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
            .setTitle(R.string.details_title)
            .setMessage(message)
            .setPositiveButton(R.string.details_close, null)
            .show()
    }

    private fun formatTime(ms: Long): String {
        val totalSeconds = (ms / 1000L).coerceAtLeast(0L)
        val minutes = totalSeconds / 60L
        val seconds = totalSeconds % 60L
        return String.format(Locale.US, "%02d:%02d", minutes, seconds)
    }
}
