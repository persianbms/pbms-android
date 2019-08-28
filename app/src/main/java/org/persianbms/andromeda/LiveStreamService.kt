package org.persianbms.andromeda

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.media.AudioAttributes
import android.media.AudioFocusRequest
import android.media.AudioManager
import android.net.Uri
import android.os.Build
import android.os.IBinder
import android.os.SystemClock
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.MediaSessionCompat
import android.support.v4.media.session.MediaSessionCompat.Callback
import android.support.v4.media.session.PlaybackStateCompat
import androidx.core.app.NotificationCompat
import com.google.android.exoplayer2.C
import com.google.android.exoplayer2.ExoPlayerFactory
import com.google.android.exoplayer2.SimpleExoPlayer
import com.google.android.exoplayer2.source.hls.HlsMediaSource
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory

class LiveStreamService : Service(), AudioManager.OnAudioFocusChangeListener {

    private var audioFocusRequest: AudioFocusRequest? = null
    private lateinit var exoPlayer: SimpleExoPlayer
    private val focusLock = Object()
    private var haveAudioFocus = false
    private lateinit var mediaSession: MediaSessionCompat
    private var mediaSessionCallback = object: Callback() {
        override fun onPlay() {
            handlePlayAction()
        }

        override fun onPause() {
            handlePauseAction()
        }

        override fun onStop() {
            handleStopAction()
        }
    }
    private var resumeFocusOnGain = false

    enum class Action {
        Play,
        Pause,
        Stop
    }

    companion object{
        private const val CHANNEL_ID = "live_stream_01"
        private const val NOTIFICATION_ID = 4112   // arbitrary number
        private const val CANCEL_REQUEST_ID = 9456
        private const val PAUSE_REQUEST_ID = 28859
        private const val PLAY_REQUEST_ID = 54326
        private const val ACTION_ARG = "action"

        @Volatile private var singleton: LiveStreamService? = null
        @Volatile private var _listener: OnLiveStreamStateChangedListener? = null

        var listener: OnLiveStreamStateChangedListener?
            get() = _listener
            set(value) {
                _listener = value
                singleton?.let { sngltn ->
                    _listener?.onLiveStreamStreamStateChanged(sngltn.isPlaying())
                }
            }

        @JvmStatic
        fun get(): LiveStreamService? {
            return singleton
        }

        @JvmStatic
        fun newPlayIntent(ctx: Context): Intent {
            val i = Intent(ctx, LiveStreamService::class.java)
            i.putExtra(ACTION_ARG, Action.Play.name)
            return i
        }

        @JvmStatic
        fun newPauseIntent(ctx: Context): Intent {
            val i = Intent(ctx, LiveStreamService::class.java)
            i.putExtra(ACTION_ARG, Action.Pause.name)
            return i
        }

        @JvmStatic
        fun newStopIntent(ctx: Context): Intent {
            val i = Intent(ctx, LiveStreamService::class.java)
            i.putExtra(ACTION_ARG, Action.Stop.name)
            return i
        }
    }

    private fun createPlaybackState(@PlaybackStateCompat.State state: Int): PlaybackStateCompat {
        return PlaybackStateCompat.Builder().
            setActions(PlaybackStateCompat.ACTION_PLAY or
                    PlaybackStateCompat.ACTION_STOP or
                    PlaybackStateCompat.ACTION_PAUSE).
            setState(state,
                PlaybackStateCompat.PLAYBACK_POSITION_UNKNOWN,
                1.0f,
                SystemClock.elapsedRealtime()).
            build()
    }

    private fun handlePlayAction() {
        playStream()
        val state = createPlaybackState(PlaybackStateCompat.STATE_PLAYING)
        mediaSession.setPlaybackState(state)
        mediaSession.isActive = true
        updateNotification(mediaSession, Action.Play)

        _listener?.onLiveStreamStreamStateChanged(true)
    }

    private fun handlePauseAction() {
        exoPlayer.stop()
        updateNotification(mediaSession, Action.Pause)
        val state = createPlaybackState(PlaybackStateCompat.STATE_PAUSED)
        mediaSession.setPlaybackState(state)
        synchronized(focusLock) {
            resumeFocusOnGain = false
        }

        _listener?.onLiveStreamStreamStateChanged(false)
    }

    private fun handleStopAction() {
        exoPlayer.stop()
        exoPlayer.release()

        val state = createPlaybackState(PlaybackStateCompat.STATE_STOPPED)
        mediaSession.setPlaybackState(state)
        mediaSession.isActive = false

        stopForeground(true)

        if (haveAudioFocus) {
            val mgr = getSystemService(Context.AUDIO_SERVICE) as AudioManager
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                mgr.abandonAudioFocusRequest(audioFocusRequest!!)
            } else {
                @Suppress("DEPRECATION")
                mgr.abandonAudioFocus(this)
            }
            haveAudioFocus = false
        }

        _listener?.onLiveStreamStreamStateChanged(false)
    }

    private fun isPlaying(): Boolean {
        return when (exoPlayer.playbackState) {
            SimpleExoPlayer.STATE_BUFFERING -> true
            SimpleExoPlayer.STATE_ENDED -> false
            SimpleExoPlayer.STATE_IDLE -> false
            SimpleExoPlayer.STATE_READY -> true
            else -> false
        }
    }

    override fun onBind(p0: Intent?): IBinder? {
        return null
    }

    override fun onCreate() {
        super.onCreate()
        singleton = this

        mediaSession = MediaSessionCompat(this, "pbms")
        mediaSession.setFlags(MediaSessionCompat.FLAG_HANDLES_MEDIA_BUTTONS or
            MediaSessionCompat.FLAG_HANDLES_TRANSPORT_CONTROLS)
        val playbackState = PlaybackStateCompat.Builder().
            setActions(PlaybackStateCompat.ACTION_PLAY or
                    PlaybackStateCompat.ACTION_STOP or
                    PlaybackStateCompat.ACTION_PAUSE).
            setState(PlaybackStateCompat.STATE_STOPPED,
                PlaybackStateCompat.PLAYBACK_POSITION_UNKNOWN,
                1.0f,
                SystemClock.elapsedRealtime()).
            build()
        mediaSession.setPlaybackState(playbackState)
        mediaSession.setCallback(mediaSessionCallback)
        val metadataBldr = MediaMetadataCompat.Builder().
            putString(MediaMetadataCompat.METADATA_KEY_ARTIST, getString(R.string.persianbms))
        val icon = BitmapFactory.decodeResource(resources, R.drawable.pbms_logo_transparent_102w)
        metadataBldr.putBitmap(MediaMetadataCompat.METADATA_KEY_ART, icon)
        metadataBldr.putBitmap(MediaMetadataCompat.METADATA_KEY_DISPLAY_ICON, icon)
        mediaSession.setMetadata(metadataBldr.build())

        val audioAttrs = com.google.android.exoplayer2.audio.AudioAttributes.Builder().
            setUsage(C.USAGE_MEDIA).
            setContentType(C.CONTENT_TYPE_SPEECH).
            build()

        exoPlayer = ExoPlayerFactory.newSimpleInstance(this)
        exoPlayer.playWhenReady = false
        exoPlayer.setForegroundMode(false)
        exoPlayer.setAudioAttributes(audioAttrs, false)
    }

    override fun onDestroy() {
        super.onDestroy()

        mediaSession.release()
        singleton = null
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        L.i("LiveStreamService.onStartCommand")
        if (intent == null) {
            throw RuntimeException("intent should not be null")
        }

        val action = intent.extras?.getString(ACTION_ARG)
            ?: throw RuntimeException("action should not be null")

        when (action) {
            Action.Play.name -> handlePlayAction()
            Action.Pause.name -> handlePauseAction()
            Action.Stop.name -> handleStopAction()
            else -> throw RuntimeException("unknown action: $action")
        }

        return START_NOT_STICKY
    }

    private fun playNow() {
        val streamUri = Uri.parse(Constants.LIVE_STREAM_ADDRESS)
        val ddsf = DefaultDataSourceFactory(this, "pbms-android")
        val src = HlsMediaSource.Factory(ddsf).createMediaSource(streamUri)
        exoPlayer.prepare(src, true, false)
        exoPlayer.playWhenReady = true
    }

    private fun playStream() {
        val mgr = getSystemService(Context.AUDIO_SERVICE) as AudioManager

        val result: Int
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            audioFocusRequest = AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN).run {
                setOnAudioFocusChangeListener(this@LiveStreamService)
                setAudioAttributes(AudioAttributes.Builder().run {
                    setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                    build()
                })
                build()
            }
            result = mgr.requestAudioFocus(audioFocusRequest!!)
        } else {
            @Suppress("DEPRECATION")
            result = mgr.requestAudioFocus(this, AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN)
        }

        synchronized(focusLock) {
            when (result) {
                AudioManager.AUDIOFOCUS_REQUEST_FAILED -> {
                    L.i("LiveStreamService: focus request failed")
                    return
                }
                AudioManager.AUDIOFOCUS_REQUEST_GRANTED -> {
                    haveAudioFocus = true
                    playNow()
                }
                else -> {
                    L.i("LiveStreamService: unexpected requestAudioFocus result ($result). Skipping audio playback.")
                    return
                }
            }
        }
    }

    private fun updateNotification(session: MediaSessionCompat, action: Action) {
        val mgr = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        if (Build.VERSION.SDK_INT >= 26) {
            val chanName = getString(R.string.live_stream)
            val chan = NotificationChannel(CHANNEL_ID, chanName, NotificationManager.IMPORTANCE_LOW)
            chan.description = getString(R.string.live_stream)
            mgr.getNotificationChannel(CHANNEL_ID)
            mgr.createNotificationChannel(chan)
        }

        if (action == Action.Stop) {
            return
        }

        val icon = BitmapFactory.decodeResource(resources, R.drawable.pbms_logo_transparent_102w)
        val style = androidx.media.app.NotificationCompat.MediaStyle()
        style.setMediaSession(session.sessionToken)
        style.setShowActionsInCompactView(0)
        val deleteIntent = PendingIntent.getService(this,
            CANCEL_REQUEST_ID,
            newStopIntent(applicationContext),
            PendingIntent.FLAG_UPDATE_CURRENT)
        val contentIntent = PendingIntent.getActivity(this,
            0,
            MainActivity.newIntent(this),
            PendingIntent.FLAG_UPDATE_CURRENT)
        val bldr = NotificationCompat.Builder(this, CHANNEL_ID).
            setContentTitle(getString(R.string.persianbms)).
            setContentText(getString(R.string.live_stream)).
            setLargeIcon(icon).
            setSmallIcon(R.drawable.pbms_logo_transparent_102w).
            setContentIntent(contentIntent).
            setDeleteIntent(deleteIntent).
            setCategory(NotificationCompat.CATEGORY_TRANSPORT).
            setVisibility(NotificationCompat.VISIBILITY_PUBLIC).
            setStyle(style)
        if (action == Action.Play) {
            val pauseIntent = PendingIntent.getService(this,
                PAUSE_REQUEST_ID,
                newPauseIntent(applicationContext),
                PendingIntent.FLAG_UPDATE_CURRENT)
            val pauseAction = NotificationCompat.Action.Builder(R.drawable.ic_outline_pause_24dp,
                getString(R.string.pause),
                pauseIntent).build()
            bldr.addAction(pauseAction)
            val notification = bldr.build()
            startForeground(NOTIFICATION_ID, notification)
        } else if (action == Action.Pause) {
            val playIntent = PendingIntent.getService(this,
                PLAY_REQUEST_ID,
                newPlayIntent(applicationContext),
                PendingIntent.FLAG_UPDATE_CURRENT)
            val playAction = NotificationCompat.Action.Builder(R.drawable.ic_outline_play_24dp,
                getString(R.string.play),
                playIntent).build()
            bldr.addAction(playAction)
            val notification = bldr.build()
            stopForeground(false)
            mgr.notify(NOTIFICATION_ID, notification)
        }
    }

    //region onAudioFocusChange(Int)
    override fun onAudioFocusChange(focusChange: Int) {
        if (focusChange == AudioManager.AUDIOFOCUS_GAIN) {
            L.i("onAudioFocusChange AUDIOFOCUS_GAIN")
            haveAudioFocus = true
            if (resumeFocusOnGain) {
                synchronized(focusLock) {
                    resumeFocusOnGain = false
                }
                exoPlayer.playWhenReady = true
            }
            return
        }

        if (focusChange == AudioManager.AUDIOFOCUS_LOSS) {
            L.i("onAudioFocusChange AUDIOFOCUS_LOSS")
            haveAudioFocus = false
            synchronized(focusLock) {
                resumeFocusOnGain = false
            }
            handleStopAction()
            return
        }

        if (focusChange == AudioManager.AUDIOFOCUS_LOSS_TRANSIENT ||
                focusChange == AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK) {
            L.i("onAudioFocusChange AUDIOFOCUS_LOSS_TRANSIENT_* $focusChange")
            synchronized(focusLock) {
                resumeFocusOnGain = isPlaying()
            }
            exoPlayer.playWhenReady = false
        }
    }
    //endregion

    interface OnLiveStreamStateChangedListener {
        fun onLiveStreamStreamStateChanged(playing: Boolean)
    }

}