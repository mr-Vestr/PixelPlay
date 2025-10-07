package com.theveloper.pixelplay.data.service.cast

import android.net.Uri
import com.google.android.gms.cast.MediaLoadOptions
import com.google.android.gms.cast.MediaStatus
import com.google.android.gms.cast.framework.CastSession
import com.google.android.gms.cast.framework.media.RemoteMediaClient
import com.theveloper.pixelplay.data.model.Song
import com.theveloper.pixelplay.data.service.player.Playback
import timber.log.Timber

class CastPlayer(castSession: CastSession) : Playback, RemoteMediaClient.Callback() {
    override val isInitialized = true
    override val audioSessionId = 0 // Not applicable for Cast
    override var callbacks: Playback.PlaybackCallbacks? = null

    private val remoteMediaClient: RemoteMediaClient? = castSession.remoteMediaClient
    private var isActuallyPlaying = false

    init {
        remoteMediaClient?.registerCallback(this)
        remoteMediaClient?.setPlaybackRate(1.0)
    }

    override val isPlaying: Boolean
        get() = remoteMediaClient?.isPlaying == true || isActuallyPlaying

    override fun setDataSource(song: Song, force: Boolean, completion: (success: Boolean) -> Unit) {
        try {
            val mediaInfo = song.toMediaInfo()
            val mediaLoadOptions = MediaLoadOptions.Builder()
                .setPlayPosition(0)
                .setAutoplay(true)
                .build()

            remoteMediaClient?.load(mediaInfo, mediaLoadOptions)?.setResultCallback { result ->
                if (result.status.isSuccess) {
                    Timber.d("CastPlayer: Media loaded successfully.")
                    completion(true)
                } else {
                    Timber.e("CastPlayer: Error loading media. Code: ${result.status.statusCode}, Message: ${result.status.statusMessage}")
                    completion(false)
                }
            } ?: completion(false)
        } catch (e: Exception) {
            Timber.e(e, "CastPlayer: Exception in setDataSource")
            completion(false)
        }
    }

    override fun setNextDataSource(path: Uri?) {}

    override fun start(): Boolean {
        isActuallyPlaying = true
        remoteMediaClient?.play()
        return true
    }

    override fun stop() {
        isActuallyPlaying = false
        remoteMediaClient?.stop()
    }

    override fun release() {
        remoteMediaClient?.unregisterCallback(this)
    }

    override fun pause(): Boolean {
        isActuallyPlaying = false
        remoteMediaClient?.pause()
        return true
    }

    override fun duration(): Int {
        return remoteMediaClient?.mediaInfo?.streamDuration?.toInt() ?: 0
    }

    override fun position(): Int {
        return remoteMediaClient?.approximateStreamPosition?.toInt() ?: 0
    }

    override fun seek(whereto: Int, force: Boolean): Int {
        remoteMediaClient?.seek(whereto.toLong())
        return whereto
    }

    override fun onStatusUpdated() {
        super.onStatusUpdated()
        val playerState = remoteMediaClient?.playerState ?: return
        callbacks?.onPlaybackStatusChanged(playerState)

        if (playerState == MediaStatus.PLAYER_STATE_IDLE && remoteMediaClient?.idleReason == MediaStatus.IDLE_REASON_FINISHED) {
            callbacks?.onCompletion()
        }
    }
}