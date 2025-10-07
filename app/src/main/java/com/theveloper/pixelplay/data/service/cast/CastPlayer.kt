package com.theveloper.pixelplay.data.service.cast

import androidx.core.net.toUri
import com.google.android.gms.cast.MediaInfo
import com.google.android.gms.cast.MediaQueueItem
import com.google.android.gms.cast.MediaSeekOptions
import com.google.android.gms.cast.MediaStatus
import com.google.android.gms.cast.framework.CastSession
import com.google.android.gms.cast.framework.media.RemoteMediaClient
import com.google.android.gms.common.images.WebImage
import com.theveloper.pixelplay.data.model.Song
import com.theveloper.pixelplay.data.service.http.MediaFileHttpServerService
import com.theveloper.pixelplay.data.service.player.Playback
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import timber.log.Timber

class CastPlayer(private val castSession: CastSession) : Playback, RemoteMediaClient.Callback(), RemoteMediaClient.ProgressListener {
    private val scope = CoroutineScope(Dispatchers.Main + SupervisorJob())

    override val isInitialized = true
    override val audioSessionId = 0
    override var callbacks: Playback.PlaybackCallbacks? = null

    private val remoteMediaClient: RemoteMediaClient = castSession.remoteMediaClient!!
    private var allSongs: List<Song> = emptyList()

    init {
        remoteMediaClient.registerCallback(this)
        remoteMediaClient.addProgressListener(this, 1000)
    }

    override val isPlaying: Boolean
        get() = remoteMediaClient.isPlaying
    override val currentSong: Song?
        get() = remoteMediaClient.mediaStatus?.currentItemId?.let { findSongInQueue(it) }
    override val currentQueue: List<Song>
        get() = allSongs

    override fun playSongs(songs: List<Song>, startSong: Song) {
        this.allSongs = songs
        val serverAddress = MediaFileHttpServerService.serverAddress
        if (serverAddress == null) {
            Timber.e("Cannot play songs on cast device without a running HTTP server.")
            return
        }

        val queueItems = songs.map { it.toMediaQueueItem(serverAddress) }.toTypedArray()
        val startIndex = songs.indexOf(startSong).coerceAtLeast(0)

        remoteMediaClient.queueLoad(queueItems, startIndex, MediaStatus.REPEAT_MODE_REPEAT_OFF, 0, null)
    }

    override fun start(): Boolean {
        remoteMediaClient.play()
        return true
    }

    override fun stop() {
        remoteMediaClient.stop()
    }

    override fun release() {
        remoteMediaClient.unregisterCallback(this)
        remoteMediaClient.removeProgressListener(this)
        scope.cancel()
    }

    override fun pause(): Boolean {
        remoteMediaClient.pause()
        return true
    }

    override fun duration(): Int = remoteMediaClient.streamDuration.toInt()

    override fun position(): Int = remoteMediaClient.approximateStreamPosition.toInt()

    override fun seek(whereto: Int, force: Boolean): Int {
        val options = MediaSeekOptions.Builder().setPosition(whereto.toLong()).build()
        remoteMediaClient.seek(options)
        return whereto
    }

    override fun next() {
        remoteMediaClient.queueNext(null)
    }

    override fun previous() {
        remoteMediaClient.queuePrev(null)
    }

    override fun toggleShuffle() {
        val currentMode = remoteMediaClient.mediaStatus?.queueRepeatMode ?: MediaStatus.REPEAT_MODE_REPEAT_OFF
        val newMode = if (currentMode == MediaStatus.REPEAT_MODE_REPEAT_ALL_AND_SHUFFLE) {
            MediaStatus.REPEAT_MODE_REPEAT_ALL // Turn shuffle off, keep repeat all
        } else {
            MediaStatus.REPEAT_MODE_REPEAT_ALL_AND_SHUFFLE // Turn shuffle on
        }
        remoteMediaClient.queueSetRepeatMode(newMode, null)
    }

    override fun cycleRepeatMode() {
        val currentMode = remoteMediaClient.mediaStatus?.queueRepeatMode ?: MediaStatus.REPEAT_MODE_REPEAT_OFF
        // This cycle ignores shuffle. Shuffle is handled by its own toggle.
        val newMode = when (currentMode) {
            MediaStatus.REPEAT_MODE_REPEAT_OFF -> MediaStatus.REPEAT_MODE_REPEAT_ALL
            MediaStatus.REPEAT_MODE_REPEAT_ALL -> MediaStatus.REPEAT_MODE_REPEAT_SINGLE
            MediaStatus.REPEAT_MODE_REPEAT_SINGLE -> MediaStatus.REPEAT_MODE_REPEAT_OFF
            MediaStatus.REPEAT_MODE_REPEAT_ALL_AND_SHUFFLE -> MediaStatus.REPEAT_MODE_REPEAT_SINGLE // If shuffling, next mode is repeat one.
            else -> MediaStatus.REPEAT_MODE_REPEAT_OFF
        }
        remoteMediaClient.queueSetRepeatMode(newMode, null)
    }

    // RemoteMediaClient.Callback implementation
    override fun onStatusUpdated() {
        val status = remoteMediaClient.mediaStatus ?: return
        callbacks?.onIsPlayingChanged(status.playerState == MediaStatus.PLAYER_STATE_PLAYING)
        callbacks?.onShuffleModeChanged(status.queueRepeatMode == MediaStatus.REPEAT_MODE_REPEAT_ALL_AND_SHUFFLE)
        callbacks?.onRepeatModeChanged(status.queueRepeatMode)
        callbacks?.onDurationChanged(remoteMediaClient.streamDuration)

        val currentItemId = status.currentItemId
        if (currentItemId != 0) {
            val song = findSongInQueue(currentItemId)
            callbacks?.onSongChanged(song)
        } else {
            callbacks?.onSongChanged(null)
        }

        val remoteQueue = status.queueItems
        val songQueue = remoteQueue.mapNotNull { findSongInQueue(it.itemId) }
        callbacks?.onQueueChanged(songQueue)
    }

    override fun onMediaSessionEnded() {
        callbacks?.onCompletion()
    }

    // RemoteMediaClient.ProgressListener implementation
    override fun onProgressUpdated(progressMs: Long, durationMs: Long) {
        callbacks?.onPositionChanged(progressMs)
    }

    private fun findSongInQueue(itemId: Int): Song? {
        val item = remoteMediaClient.mediaQueue.getItemById(itemId) ?: return null
        val contentId = item.media?.contentId ?: return null
        val songId = contentId.substringAfterLast('/')
        return allSongs.find { it.id == songId }
    }
}

private fun Song.toMediaQueueItem(serverAddress: String): MediaQueueItem {
    return MediaQueueItem.Builder(this.toMediaInfo(serverAddress)).build()
}

private fun Song.toMediaInfo(serverAddress: String): MediaInfo {
    val songUrl = "$serverAddress/song/${this.id}"
    val albumArtUrl = "$serverAddress/art/${this.id}"

    val metadata = com.google.android.gms.cast.MediaMetadata(com.google.android.gms.cast.MediaMetadata.MEDIA_TYPE_MUSIC_TRACK).apply {
        putString(com.google.android.gms.cast.MediaMetadata.KEY_TITLE, this@toMediaInfo.title)
        putString(com.google.android.gms.cast.MediaMetadata.KEY_ARTIST, this@toMediaInfo.artist)
        putString(com.google.android.gms.cast.MediaMetadata.KEY_ALBUM_TITLE, this@toMediaInfo.album)
        addImage(WebImage(albumArtUrl.toUri()))
    }

    return MediaInfo.Builder(songUrl)
        .setStreamType(MediaInfo.STREAM_TYPE_BUFFERED)
        .setContentType("audio/mpeg")
        .setMetadata(metadata)
        .build()
}