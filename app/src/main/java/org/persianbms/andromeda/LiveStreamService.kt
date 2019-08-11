package org.persianbms.andromeda

import android.app.Service
import android.content.Intent
import android.net.Uri
import android.os.IBinder
import android.support.v4.media.session.MediaSessionCompat
import android.support.v4.media.session.MediaSessionCompat.Callback
import android.support.v4.media.session.PlaybackStateCompat
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.ExoPlayerFactory
import com.google.android.exoplayer2.source.hls.HlsMediaSource
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory

class LiveStreamService : Service() {

    private var exoPlayer: ExoPlayer? = null
    private var mediaSession: MediaSessionCompat? = null
    private var mediaSessionCallback = object: Callback() {
        override fun onPlay() {
            L.i("callback onPlay")
        }

        override fun onStop() {
            L.i("callback onStop")
        }
    }

    override fun onBind(p0: Intent?): IBinder? {
        return null
    }

    override fun onCreate() {
        super.onCreate()

//        val session = MediaSessionCompat(this, "PBMS Live Stream")
//        session.setFlags(MediaSessionCompat.FLAG_HANDLES_MEDIA_BUTTONS)
//        val playbackState = PlaybackStateCompat.Builder().
//            setActions(PlaybackStateCompat.ACTION_PLAY or PlaybackStateCompat.ACTION_STOP).
//            build()
//        session.setPlaybackState(playbackState)
//        session.setCallback(mediaSessionCallback)
//        mediaSession = session

        val session = MediaSessionCompat(this, "PBMS Live Stream")
        session.setFlags(MediaSessionCompat.FLAG_HANDLES_MEDIA_BUTTONS)
        val playbackState = PlaybackStateCompat.Builder().
            setActions(PlaybackStateCompat.ACTION_PLAY or PlaybackStateCompat.ACTION_STOP).
            build()
        session.setPlaybackState(playbackState)
        session.setCallback(mediaSessionCallback)
        mediaSession = session

        val player = ExoPlayerFactory.newSimpleInstance(this)
        player.playWhenReady = false
        player.setForegroundMode(false)
        val streamUri = Uri.parse(Constants.LIVE_STREAM_ADDRESS)
        val src = HlsMediaSource.Factory(DefaultDataSourceFactory(this, "pbms-android")).
            createMediaSource(streamUri)
        player.prepare(src, true, false)
        player.playWhenReady
        exoPlayer = player
    }
}