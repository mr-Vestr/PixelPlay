package com.theveloper.pixelplay.data.service.player

import android.content.Context
import android.net.Uri
import androidx.annotation.OptIn
import androidx.core.net.toUri
import androidx.media3.common.AudioAttributes
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.DefaultRenderersFactory
import androidx.media3.exoplayer.ExoPlayer
import com.theveloper.pixelplay.data.model.Song
import com.theveloper.pixelplay.data.model.TransitionSettings
import com.theveloper.pixelplay.utils.envelope
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

@OptIn(UnstableApi::class)
@Singleton
class DualPlayerEngine @Inject constructor(
    @ApplicationContext private val context: Context,
) : Playback {
    private val scope = CoroutineScope(Dispatchers.Main + SupervisorJob())
    private var transitionJob: Job? = null

    private val playerA: ExoPlayer
    private val playerB: ExoPlayer

    val masterPlayer: Player
        get() = playerA

    override val isInitialized: Boolean = true
    override val isPlaying: Boolean
        get() = playerA.isPlaying
    override val audioSessionId: Int
        get() = playerA.audioSessionId
    override var callbacks: Playback.PlaybackCallbacks? = null

    init {
        playerA = buildPlayer()
        playerB = buildPlayer()
        playerA.addListener(PlayerListener())
    }

    private inner class PlayerListener : Player.Listener {
        override fun onPlaybackStateChanged(playbackState: Int) {
            callbacks?.onPlaybackStatusChanged(playbackState)
            if (playbackState == Player.STATE_ENDED) {
                callbacks?.onCompletion()
            }
        }

        override fun onPlayerError(error: PlaybackException) {
            callbacks?.onPlaybackStatusChanged(Player.STATE_IDLE)
        }
    }

    override fun setDataSource(song: Song, force: Boolean, completion: (success: Boolean) -> Unit) {
        try {
            val mediaItem = MediaItem.fromUri(song.contentUriString.toUri())
            playerA.setMediaItem(mediaItem)
            playerA.prepare()
            playerA.addListener(object : Player.Listener {
                override fun onPlaybackStateChanged(playbackState: Int) {
                    if (playbackState == Player.STATE_READY) {
                        completion(true)
                        playerA.removeListener(this)
                    }
                }

                override fun onPlayerError(error: PlaybackException) {
                    completion(false)
                    playerA.removeListener(this)
                }
            })
        } catch (e: Exception) {
            e.printStackTrace()
            completion(false)
        }
    }

    override fun setNextDataSource(path: Uri?) {
        if (path != null) {
            prepareNext(MediaItem.fromUri(path))
        } else {
            cancelNext()
        }
    }

    override fun start(): Boolean {
        playerA.play()
        return true
    }

    override fun stop() {
        playerA.stop()
    }

    override fun pause(): Boolean {
        playerA.pause()
        return true
    }

    override fun duration(): Int = playerA.duration.takeIf { it != C.TIME_UNSET }?.toInt() ?: 0

    override fun position(): Int = playerA.currentPosition.toInt()

    override fun seek(whereto: Int, force: Boolean): Int {
        playerA.seekTo(whereto.toLong())
        return whereto
    }

    private fun buildPlayer(): ExoPlayer {
        val renderersFactory = DefaultRenderersFactory(context)
            .setExtensionRendererMode(DefaultRenderersFactory.EXTENSION_RENDERER_MODE_ON)

        val audioAttributes = AudioAttributes.Builder()
            .setContentType(C.AUDIO_CONTENT_TYPE_MUSIC)
            .setUsage(C.USAGE_MEDIA)
            .build()

        return ExoPlayer.Builder(context, renderersFactory).build().apply {
            setAudioAttributes(audioAttributes, true)
            setHandleAudioBecomingNoisy(true)
        }
    }

    fun prepareNext(mediaItem: MediaItem) {
        playerB.stop()
        playerB.clearMediaItems()
        playerB.setMediaItem(mediaItem)
        playerB.prepare()
    }

    fun cancelNext() {
        if (playerB.mediaItemCount > 0) {
            playerB.stop()
            playerB.clearMediaItems()
        }
    }

    fun performTransition(settings: TransitionSettings) {
        transitionJob?.cancel()
        transitionJob = scope.launch {
            when (settings.mode) {
                com.theveloper.pixelplay.data.model.TransitionMode.FADE_IN_OUT -> performFadeInOutTransition(settings)
                com.theveloper.pixelplay.data.model.TransitionMode.OVERLAP, com.theveloper.pixelplay.data.model.TransitionMode.SMOOTH -> performOverlapTransition(settings)
                com.theveloper.pixelplay.data.model.TransitionMode.NONE -> {
                    // No transition logic needed
                }
            }
        }
    }

    private suspend fun performFadeInOutTransition(settings: TransitionSettings) {
        if (playerB.mediaItemCount == 0) return
        val halfDuration = settings.durationMs.toLong() / 2
        if (halfDuration <= 0) return

        var elapsed = 0L
        while (elapsed < halfDuration) {
            val progress = elapsed.toFloat() / halfDuration
            playerA.volume = 1f - envelope(progress, settings.curveOut)
            delay(50L)
            elapsed += 50L
        }
        playerA.volume = 0f
        playerA.stop()

        playerB.volume = 0f
        playerB.play()
        elapsed = 0L
        while (elapsed < halfDuration) {
            val progress = elapsed.toFloat() / halfDuration
            playerB.volume = envelope(progress, settings.curveIn)
            delay(50L)
            elapsed += 50L
        }
        playerB.volume = 1f

        if (playerA.hasNextMediaItem()) {
            playerA.seekToNextMediaItem()
            playerA.seekTo(playerB.currentPosition)
            playerA.volume = 1f
            playerA.play()
        }

        playerB.stop()
        playerB.clearMediaItems()
    }

    private suspend fun performOverlapTransition(settings: TransitionSettings) {
        if (playerB.mediaItemCount == 0) return

        playerB.volume = 0f
        playerB.play()

        val duration = settings.durationMs.toLong()
        var elapsed = 0L
        while (elapsed < duration) {
            val progress = elapsed.toFloat() / duration
            playerA.volume = 1f - envelope(progress, settings.curveOut)
            playerB.volume = envelope(progress, settings.curveIn)
            delay(50L)
            elapsed += 50L
        }
        playerA.volume = 0f
        playerB.volume = 1f

        playerA.stop()

        if (playerA.hasNextMediaItem()) {
            playerA.seekToNextMediaItem()
            playerA.seekTo(playerB.currentPosition)
            playerA.volume = 1f
            playerA.play()
        }

        playerB.stop()
        playerB.clearMediaItems()
    }

    override fun release() {
        transitionJob?.cancel()
        playerA.release()
        playerB.release()
    }
}