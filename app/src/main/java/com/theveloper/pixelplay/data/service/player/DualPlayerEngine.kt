package com.theveloper.pixelplay.data.service.player

import android.content.Context
import androidx.annotation.OptIn
import androidx.core.net.toUri
import androidx.media3.common.AudioAttributes
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.DefaultRenderersFactory
import androidx.media3.exoplayer.ExoPlayer
import com.theveloper.pixelplay.data.model.Song
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

@OptIn(UnstableApi::class)
@Singleton
class DualPlayerEngine @Inject constructor(
    @ApplicationContext private val context: Context,
) : Playback {
    private val scope = CoroutineScope(Dispatchers.Main + SupervisorJob())
    private var progressJob: Job? = null

    private val exoPlayer: ExoPlayer
    private var allSongs: List<Song> = emptyList()

    override val isInitialized: Boolean = true
    override val isPlaying: Boolean
        get() = exoPlayer.isPlaying
    override val audioSessionId: Int
        get() = exoPlayer.audioSessionId
    override val currentSong: Song?
        get() = exoPlayer.currentMediaItem?.mediaId?.let { findSongById(it) }
    override val currentQueue: List<Song>
        get() = allSongs
    override var callbacks: Playback.PlaybackCallbacks? = null

    init {
        exoPlayer = buildPlayer()
        exoPlayer.addListener(PlayerListener())
    }

    private inner class PlayerListener : Player.Listener {
        override fun onPlaybackStateChanged(playbackState: Int) {
            callbacks?.onPlaybackStatusChanged(playbackState)
            if (playbackState == Player.STATE_ENDED) {
                callbacks?.onCompletion()
            }
            if (playbackState == Player.STATE_READY) {
                callbacks?.onDurationChanged(exoPlayer.duration.coerceAtLeast(0L))
            }
        }

        override fun onPlayerError(error: PlaybackException) {
            callbacks?.onPlaybackStatusChanged(Player.STATE_IDLE)
        }

        override fun onIsPlayingChanged(isPlaying: Boolean) {
            callbacks?.onIsPlayingChanged(isPlaying)
            if (isPlaying) {
                startProgressUpdates()
            } else {
                stopProgressUpdates()
            }
        }

        override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
            val song = mediaItem?.mediaId?.let { findSongById(it) }
            callbacks?.onSongChanged(song)
        }

        override fun onShuffleModeEnabledChanged(shuffleModeEnabled: Boolean) {
            callbacks?.onShuffleModeChanged(shuffleModeEnabled)
        }

        override fun onRepeatModeChanged(repeatMode: Int) {
            callbacks?.onRepeatModeChanged(repeatMode)
        }

        override fun onTimelineChanged(timeline: androidx.media3.common.Timeline, reason: Int) {
            val newQueue = mutableListOf<Song>()
            for (i in 0 until timeline.windowCount) {
                val mediaItem = exoPlayer.getMediaItemAt(i)
                findSongById(mediaItem.mediaId)?.let { newQueue.add(it) }
            }
            callbacks?.onQueueChanged(newQueue)
        }
    }

    override fun playSongs(songs: List<Song>, startSong: Song) {
        this.allSongs = songs
        val mediaItems = songs.map { it.toMediaItem() }
        val startIndex = songs.indexOf(startSong).coerceAtLeast(0)

        exoPlayer.setMediaItems(mediaItems, startIndex, 0L)
        exoPlayer.prepare()
        exoPlayer.play()
    }

    override fun start(): Boolean {
        exoPlayer.play()
        return true
    }

    override fun stop() {
        exoPlayer.stop()
    }

    override fun pause(): Boolean {
        exoPlayer.pause()
        return true
    }

    override fun duration(): Int = exoPlayer.duration.takeIf { it != C.TIME_UNSET }?.toInt() ?: 0

    override fun position(): Int = exoPlayer.currentPosition.toInt()

    override fun seek(whereto: Int, force: Boolean): Int {
        exoPlayer.seekTo(whereto.toLong())
        return whereto
    }

    override fun next() {
        if (exoPlayer.hasNextMediaItem()) {
            exoPlayer.seekToNextMediaItem()
        }
    }

    override fun previous() {
        if (exoPlayer.hasPreviousMediaItem()) {
            exoPlayer.seekToPreviousMediaItem()
        }
    }

    override fun toggleShuffle() {
        exoPlayer.shuffleModeEnabled = !exoPlayer.shuffleModeEnabled
    }

    override fun cycleRepeatMode() {
        exoPlayer.repeatMode = when (exoPlayer.repeatMode) {
            Player.REPEAT_MODE_OFF -> Player.REPEAT_MODE_ONE
            Player.REPEAT_MODE_ONE -> Player.REPEAT_MODE_ALL
            Player.REPEAT_MODE_ALL -> Player.REPEAT_MODE_OFF
            else -> Player.REPEAT_MODE_OFF
        }
    }

    private fun buildPlayer(): ExoPlayer {
        val renderersFactory = DefaultRenderersFactory(context)
            .setExtensionRendererMode(DefaultRenderersFactory.EXTENSION_renderer_mode_on)

        val audioAttributes = AudioAttributes.Builder()
            .setContentType(C.AUDIO_CONTENT_TYPE_MUSIC)
            .setUsage(C.USAGE_MEDIA)
            .build()

        return ExoPlayer.Builder(context, renderersFactory).build().apply {
            setAudioAttributes(audioAttributes, true)
            setHandleAudioBecomingNoisy(true)
        }
    }

    private fun startProgressUpdates() {
        stopProgressUpdates()
        progressJob = scope.launch {
            while (isActive) {
                callbacks?.onPositionChanged(exoPlayer.currentPosition)
                delay(1000)
            }
        }
    }

    private fun stopProgressUpdates() {
        progressJob?.cancel()
        progressJob = null
    }

    override fun release() {
        stopProgressUpdates()
        exoPlayer.release()
    }

    private fun findSongById(id: String): Song? {
        return allSongs.find { it.id == id }
    }
}

private fun Song.toMediaItem(): MediaItem {
    val metadata = MediaMetadata.Builder()
        .setTitle(title)
        .setArtist(artist)
        .setArtworkUri(albumArtUriString?.toUri())
        .build()

    return MediaItem.Builder()
        .setMediaId(id)
        .setUri(contentUriString.toUri())
        .setMediaMetadata(metadata)
        .build()
}