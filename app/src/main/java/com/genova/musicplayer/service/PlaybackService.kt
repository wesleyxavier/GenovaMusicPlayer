package com.genova.musicplayer.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.media.AudioAttributes
import android.media.AudioDeviceInfo
import android.media.AudioFocusRequest
import android.media.AudioManager
import android.media.MediaMetadata
import android.media.MediaPlayer
import android.media.session.MediaSession
import android.media.session.PlaybackState
import android.net.Uri
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import com.genova.musicplayer.R
import com.genova.musicplayer.data.FolderPreferencesRepository
import com.genova.musicplayer.data.MediaStoreTrackRepository
import com.genova.musicplayer.data.TrackPlayStatsRepository
import com.genova.musicplayer.domain.library.Track
import com.genova.musicplayer.domain.player.PlayerStateStore
import com.genova.musicplayer.domain.player.PlayerStatus
import com.genova.musicplayer.ui.NowPlayingActivity
import java.util.concurrent.Executors

class PlaybackService : Service() {

    private var mediaPlayer: MediaPlayer? = null
    private lateinit var mediaSession: MediaSession
    private lateinit var audioManager: AudioManager
    private lateinit var notificationManager: NotificationManager
    private var audioFocusRequest: AudioFocusRequest? = null
    private var hasAudioFocus = false
    private var isForegroundStarted = false
    private lateinit var statsRepository: TrackPlayStatsRepository
    private val autoPlayExecutor = Executors.newSingleThreadExecutor()

    private val mainHandler = Handler(Looper.getMainLooper())
    private val progressTicker = object : Runnable {
        override fun run() {
            val player = mediaPlayer
            val status = PlayerStateStore.getState().status
            if (player != null && status == PlayerStatus.PLAYING) {
                try {
                    PlayerStateStore.onPositionUpdated(player.currentPosition.toLong())
                    updateMediaSessionPlaybackState(PlaybackState.STATE_PLAYING, player.currentPosition.toLong())
                } catch (_: IllegalStateException) {
                }
                mainHandler.postDelayed(this, 500L)
            }
        }
    }

    private val noisyReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent?.action == AudioManager.ACTION_AUDIO_BECOMING_NOISY) {
                pausePlayback()
                refreshAudioOutputRoute()
            }
        }
    }

    private val audioFocusListener = AudioManager.OnAudioFocusChangeListener { focusChange ->
        when (focusChange) {
            AudioManager.AUDIOFOCUS_GAIN -> {
                hasAudioFocus = true
                mediaPlayer?.setVolume(1.0f, 1.0f)
            }
            AudioManager.AUDIOFOCUS_LOSS,
            AudioManager.AUDIOFOCUS_LOSS_TRANSIENT -> {
                hasAudioFocus = false
                pausePlayback()
            }
            AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK -> {
                mediaPlayer?.setVolume(0.25f, 0.25f)
            }
        }
    }

    override fun onCreate() {
        super.onCreate()
        audioManager = getSystemService(AUDIO_SERVICE) as AudioManager
        notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        statsRepository = TrackPlayStatsRepository(this)
        PlayerStateStore.setAutoPlayAfterQueue(statsRepository.isAutoPlayAfterQueueEnabled())

        createNotificationChannel()
        initMediaSession()
        registerReceiver(noisyReceiver, IntentFilter(AudioManager.ACTION_AUDIO_BECOMING_NOISY))
        refreshAudioOutputRoute()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        refreshAudioOutputRoute()
        when (intent?.action) {
            ACTION_PLAY -> playCurrentTrackFromStore()
            ACTION_PAUSE -> pausePlayback()
            ACTION_RESUME -> resumePlayback()
            ACTION_TOGGLE_PLAY_PAUSE -> {
                if (PlayerStateStore.getState().status == PlayerStatus.PLAYING) {
                    pausePlayback()
                } else {
                    resumePlayback()
                }
            }
            ACTION_NEXT -> skipToNext(fromCompletion = false)
            ACTION_PREVIOUS -> skipToPrevious()
            ACTION_SEEK -> {
                val positionMs = intent.getLongExtra(EXTRA_SEEK_POSITION_MS, 0L)
                seekTo(positionMs)
            }
            ACTION_TOGGLE_SHUFFLE -> {
                PlayerStateStore.toggleShuffle()
                updateNotification()
            }
            ACTION_CYCLE_REPEAT -> {
                PlayerStateStore.cycleRepeatMode()
                updateNotification()
            }
            ACTION_STOP -> stopServiceGracefully()
        }
        return START_NOT_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        super.onDestroy()
        mainHandler.removeCallbacks(progressTicker)
        try {
            unregisterReceiver(noisyReceiver)
        } catch (_: IllegalArgumentException) {
        }
        releaseMediaPlayer()
        abandonAudioFocus()
        autoPlayExecutor.shutdownNow()
        mediaSession.isActive = false
        mediaSession.release()
    }

    private fun initMediaSession() {
        mediaSession = MediaSession(this, "GenovaMusicSession").apply {
            setCallback(object : MediaSession.Callback() {
                override fun onPlay() = resumePlayback()
                override fun onPause() = pausePlayback()
                override fun onSkipToNext() = skipToNext(fromCompletion = false)
                override fun onSkipToPrevious() = skipToPrevious()
                override fun onSeekTo(pos: Long) = seekTo(pos)
                override fun onStop() = stopServiceGracefully()
            })
            isActive = true
        }
    }

    private fun playCurrentTrackFromStore() {
        val track = PlayerStateStore.getState().currentTrack
        if (track == null) {
            PlayerStateStore.onError(getString(R.string.playback_error_no_track))
            return
        }

        if (!requestAudioFocus()) {
            PlayerStateStore.onPaused()
            updateNotification()
            return
        }

        mainHandler.removeCallbacks(progressTicker)
        releaseMediaPlayer()

        val player = MediaPlayer().apply {
            setAudioAttributes(
                AudioAttributes.Builder()
                    .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                    .setUsage(AudioAttributes.USAGE_MEDIA)
                    .build()
            )
            setOnPreparedListener { mp ->
                val duration = mp.duration.toLong().coerceAtLeast(track.durationMs)
                mp.start()
                statsRepository.incrementPlayCount(track.id)
                PlayerStateStore.onPrepared(duration)
                updateMediaSessionMetadata(track, duration)
                updateMediaSessionPlaybackState(PlaybackState.STATE_PLAYING, 0L)
                startOrUpdateForeground()
                mainHandler.post(progressTicker)
            }
            setOnCompletionListener {
                mainHandler.removeCallbacks(progressTicker)
                skipToNext(fromCompletion = true)
            }
            setOnErrorListener { _, _, _ ->
                mainHandler.removeCallbacks(progressTicker)
                releaseMediaPlayer()
                PlayerStateStore.onError(getString(R.string.playback_error_generic))
                updateMediaSessionPlaybackState(PlaybackState.STATE_ERROR, 0L)
                updateNotification()
                true
            }
        }

        mediaPlayer = player
        try {
            player.setDataSource(applicationContext, Uri.parse(track.contentUri))
            player.prepareAsync()
            updateMediaSessionMetadata(track, track.durationMs)
            startOrUpdateForeground()
        } catch (_: Exception) {
            releaseMediaPlayer()
            PlayerStateStore.onError(getString(R.string.playback_error_generic))
            updateNotification()
        }
    }

    private fun pausePlayback() {
        mainHandler.removeCallbacks(progressTicker)
        val player = mediaPlayer
        if (player != null && PlayerStateStore.getState().status == PlayerStatus.PLAYING) {
            try {
                player.pause()
                PlayerStateStore.onPositionUpdated(player.currentPosition.toLong())
            } catch (_: IllegalStateException) {
            }
        }
        PlayerStateStore.onPaused()
        updateMediaSessionPlaybackState(
            PlaybackState.STATE_PAUSED,
            PlayerStateStore.getState().positionMs
        )
        updateNotification()
    }

    private fun resumePlayback() {
        val state = PlayerStateStore.getState()
        val track = state.currentTrack ?: return
        if (state.status == PlayerStatus.ERROR || mediaPlayer == null) {
            playCurrentTrackFromStore()
            return
        }
        if (!requestAudioFocus()) return

        val player = mediaPlayer ?: return
        try {
            if (state.status == PlayerStatus.COMPLETED) {
                player.seekTo(0)
                PlayerStateStore.onPositionUpdated(0L)
            }
            player.start()
            PlayerStateStore.onResumed()
            updateMediaSessionMetadata(track, state.durationMs)
            updateMediaSessionPlaybackState(PlaybackState.STATE_PLAYING, player.currentPosition.toLong())
            startOrUpdateForeground()
            mainHandler.post(progressTicker)
        } catch (_: IllegalStateException) {
            playCurrentTrackFromStore()
        }
    }

    private fun skipToNext(fromCompletion: Boolean) {
        val state = PlayerStateStore.getState()
        val nextIndex = PlayerStateStore.resolveNextIndex(fromCompletion)
        val list = state.activeList
        if (nextIndex == null || nextIndex !in list.indices) {
            if (fromCompletion && state.autoPlayAfterQueue) {
                tryAutoPlayNextFromLibrary(state.currentTrack?.id)
                return
            }
            PlayerStateStore.onCompleted()
            updateMediaSessionPlaybackState(
                PlaybackState.STATE_STOPPED,
                PlayerStateStore.getState().durationMs
            )
            updateNotification()
            return
        }
        PlayerStateStore.playQueueIndex(nextIndex)
        playCurrentTrackFromStore()
    }

    private fun tryAutoPlayNextFromLibrary(currentTrackId: Long?) {
        autoPlayExecutor.execute {
            val folders = FolderPreferencesRepository(applicationContext).loadFolders()
            val tracks = if (folders.isNotEmpty()) {
                MediaStoreTrackRepository(contentResolver).queryTracks(folders)
            } else {
                emptyList()
            }
            val candidate = tracks.firstOrNull { it.id != currentTrackId } ?: tracks.firstOrNull()
            mainHandler.post {
                if (candidate != null) {
                    val updated = PlayerStateStore.appendToQueue(listOf(candidate))
                    val targetIdx = updated.activeList.lastIndex
                    if (targetIdx in updated.activeList.indices) {
                        PlayerStateStore.playQueueIndex(targetIdx)
                        playCurrentTrackFromStore()
                        return@post
                    }
                }
                PlayerStateStore.onCompleted()
                updateMediaSessionPlaybackState(
                    PlaybackState.STATE_STOPPED,
                    PlayerStateStore.getState().durationMs
                )
                updateNotification()
            }
        }
    }

    private fun skipToPrevious() {
        val state = PlayerStateStore.getState()
        if (state.positionMs > 3_000L && mediaPlayer != null) {
            seekTo(0L)
            return
        }
        val prevIndex = PlayerStateStore.resolvePreviousIndex() ?: return
        val list = state.activeList
        if (prevIndex !in list.indices) return
        PlayerStateStore.startPreparing(list[prevIndex], list, prevIndex)
        playCurrentTrackFromStore()
    }

    private fun seekTo(positionMs: Long) {
        val player = mediaPlayer ?: return
        val duration = PlayerStateStore.getState().durationMs.coerceAtLeast(0L)
        val clamped = positionMs.coerceIn(0L, duration)
        try {
            player.seekTo(clamped.toInt())
            PlayerStateStore.onPositionUpdated(clamped)
            val pbState = if (PlayerStateStore.getState().status == PlayerStatus.PLAYING) {
                PlaybackState.STATE_PLAYING
            } else {
                PlaybackState.STATE_PAUSED
            }
            updateMediaSessionPlaybackState(pbState, clamped)
        } catch (_: IllegalStateException) {
        }
    }

    private fun stopServiceGracefully() {
        mainHandler.removeCallbacks(progressTicker)
        releaseMediaPlayer()
        abandonAudioFocus()
        PlayerStateStore.reset()
        stopForeground(STOP_FOREGROUND_REMOVE)
        isForegroundStarted = false
        stopSelf()
    }

    private fun requestAudioFocus(): Boolean {
        if (hasAudioFocus) return true
        val request = AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN)
            .setAudioAttributes(
                AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_MEDIA)
                    .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                    .build()
            )
            .setOnAudioFocusChangeListener(audioFocusListener)
            .build()
        audioFocusRequest = request
        val result = audioManager.requestAudioFocus(request)
        hasAudioFocus = result == AudioManager.AUDIOFOCUS_REQUEST_GRANTED
        return hasAudioFocus
    }

    private fun abandonAudioFocus() {
        audioFocusRequest?.let { audioManager.abandonAudioFocusRequest(it) }
        hasAudioFocus = false
    }

    private fun releaseMediaPlayer() {
        mediaPlayer?.let { player ->
            try {
                if (player.isPlaying) player.stop()
            } catch (_: IllegalStateException) {
            }
            try {
                player.release()
            } catch (_: Exception) {
            }
        }
        mediaPlayer = null
    }

    private fun refreshAudioOutputRoute() {
        val devices = audioManager.getDevices(AudioManager.GET_DEVICES_OUTPUTS)
        val bluetooth = devices.firstOrNull {
            it.type == AudioDeviceInfo.TYPE_BLUETOOTH_A2DP ||
                it.type == AudioDeviceInfo.TYPE_BLUETOOTH_SCO ||
                it.type == AudioDeviceInfo.TYPE_BLE_HEADSET
        }
        val wired = devices.firstOrNull {
            it.type == AudioDeviceInfo.TYPE_WIRED_HEADSET ||
                it.type == AudioDeviceInfo.TYPE_WIRED_HEADPHONES ||
                it.type == AudioDeviceInfo.TYPE_USB_HEADSET
        }
        val label = when {
            bluetooth != null -> bluetooth.productName?.toString()?.takeIf { it.isNotBlank() }
                ?: getString(R.string.audio_output_bluetooth)
            wired != null -> getString(R.string.audio_output_wired)
            else -> getString(R.string.audio_output_speaker)
        }
        PlayerStateStore.updateAudioOutput(label)
    }

    private fun updateMediaSessionMetadata(track: Track, durationMs: Long) {
        val metadata = MediaMetadata.Builder()
            .putString(MediaMetadata.METADATA_KEY_TITLE, track.title)
            .putString(MediaMetadata.METADATA_KEY_ARTIST, track.artist)
            .putString(MediaMetadata.METADATA_KEY_ALBUM, track.album)
            .putLong(MediaMetadata.METADATA_KEY_DURATION, durationMs)
            .build()
        mediaSession.setMetadata(metadata)
    }

    private fun updateMediaSessionPlaybackState(state: Int, positionMs: Long) {
        val actions = PlaybackState.ACTION_PLAY or
            PlaybackState.ACTION_PAUSE or
            PlaybackState.ACTION_PLAY_PAUSE or
            PlaybackState.ACTION_SKIP_TO_NEXT or
            PlaybackState.ACTION_SKIP_TO_PREVIOUS or
            PlaybackState.ACTION_SEEK_TO or
            PlaybackState.ACTION_STOP
        val playbackState = PlaybackState.Builder()
            .setActions(actions)
            .setState(state, positionMs, if (state == PlaybackState.STATE_PLAYING) 1.0f else 0.0f)
            .build()
        mediaSession.setPlaybackState(playbackState)
    }

    private fun startOrUpdateForeground() {
        val notification = buildNotification()
        if (!isForegroundStarted) {
            startForeground(NOTIFICATION_ID, notification)
            isForegroundStarted = true
        } else {
            notificationManager.notify(NOTIFICATION_ID, notification)
        }
    }

    private fun updateNotification() {
        if (isForegroundStarted) {
            notificationManager.notify(NOTIFICATION_ID, buildNotification())
        }
    }

    private fun buildNotification(): Notification {
        val session = PlayerStateStore.getState()
        val track = session.currentTrack
        val isPlaying = session.status == PlayerStatus.PLAYING

        val contentIntent = PendingIntent.getActivity(
            this,
            0,
            Intent(this, NowPlayingActivity::class.java),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val prevAction = Notification.Action.Builder(
            R.drawable.ic_skip_previous,
            getString(R.string.cd_previous),
            servicePendingIntent(ACTION_PREVIOUS, 1)
        ).build()

        val playPauseAction = Notification.Action.Builder(
            if (isPlaying) R.drawable.ic_pause else R.drawable.ic_play,
            getString(R.string.cd_play_pause),
            servicePendingIntent(ACTION_TOGGLE_PLAY_PAUSE, 2)
        ).build()

        val nextAction = Notification.Action.Builder(
            R.drawable.ic_skip_next,
            getString(R.string.cd_next),
            servicePendingIntent(ACTION_NEXT, 3)
        ).build()

        return Notification.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_music_note)
            .setContentTitle(track?.title ?: getString(R.string.app_name))
            .setContentText(track?.artist ?: "")
            .setContentIntent(contentIntent)
            .setOngoing(isPlaying)
            .setOnlyAlertOnce(true)
            .addAction(prevAction)
            .addAction(playPauseAction)
            .addAction(nextAction)
            .setStyle(
                Notification.MediaStyle()
                    .setMediaSession(mediaSession.sessionToken)
                    .setShowActionsInCompactView(0, 1, 2)
            )
            .build()
    }

    private fun servicePendingIntent(action: String, requestCode: Int): PendingIntent {
        val intent = Intent(this, PlaybackService::class.java).apply { this.action = action }
        return PendingIntent.getService(
            this,
            requestCode,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    private fun createNotificationChannel() {
        val channel = NotificationChannel(
            CHANNEL_ID,
            getString(R.string.notification_channel_name),
            NotificationManager.IMPORTANCE_LOW
        ).apply {
            description = getString(R.string.notification_channel_desc)
            setShowBadge(false)
        }
        notificationManager.createNotificationChannel(channel)
    }

    companion object {
        const val ACTION_PLAY = "com.genova.musicplayer.action.PLAY"
        const val ACTION_PAUSE = "com.genova.musicplayer.action.PAUSE"
        const val ACTION_RESUME = "com.genova.musicplayer.action.RESUME"
        const val ACTION_TOGGLE_PLAY_PAUSE = "com.genova.musicplayer.action.TOGGLE_PLAY_PAUSE"
        const val ACTION_NEXT = "com.genova.musicplayer.action.NEXT"
        const val ACTION_PREVIOUS = "com.genova.musicplayer.action.PREVIOUS"
        const val ACTION_SEEK = "com.genova.musicplayer.action.SEEK"
        const val ACTION_TOGGLE_SHUFFLE = "com.genova.musicplayer.action.TOGGLE_SHUFFLE"
        const val ACTION_CYCLE_REPEAT = "com.genova.musicplayer.action.CYCLE_REPEAT"
        const val ACTION_STOP = "com.genova.musicplayer.action.STOP"
        const val EXTRA_SEEK_POSITION_MS = "extra_seek_position_ms"

        private const val CHANNEL_ID = "genova_playback_channel"
        private const val NOTIFICATION_ID = 4001
    }
}
