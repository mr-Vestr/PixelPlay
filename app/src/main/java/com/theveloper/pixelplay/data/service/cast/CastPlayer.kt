package com.theveloper.pixelplay.data.service.cast

import com.google.android.gms.cast.MediaQueueItem
import com.google.android.gms.cast.MediaSeekOptions
import com.google.android.gms.cast.MediaStatus
import com.google.android.gms.cast.framework.CastSession
import com.google.android.gms.cast.framework.media.RemoteMediaClient
import com.theveloper.pixelplay.data.model.Song
import org.json.JSONObject

class CastPlayer(castSession: CastSession) {

    val remoteMediaClient: RemoteMediaClient? = castSession.remoteMediaClient

    fun loadQueue(songs: List<Song>, startSong: Song, startPosition: Long) {
        if (songs.isEmpty()) return

        val queueItems = songs.map { it.toMediaQueueItem() }.toTypedArray()
        val startIndex = songs.indexOf(startSong).coerceAtLeast(0)

        val repeatMode = remoteMediaClient?.mediaStatus?.queueRepeatMode ?: MediaStatus.REPEAT_MODE_REPEAT_OFF

        remoteMediaClient?.queueLoad(
            queueItems,
            startIndex,
            repeatMode,
            startPosition,
            null
        )
    }

    fun play() {
        remoteMediaClient?.play()
    }

    fun pause() {
        remoteMediaClient?.pause()
    }

    fun seekTo(position: Long) {
        val options = MediaSeekOptions.Builder()
            .setPosition(position)
            .setResumeState(MediaSeekOptions.RESUME_STATE_UNCHANGED)
            .build()
        remoteMediaClient?.seek(options)
    }

    fun next() {
        remoteMediaClient?.queueNext(null)
    }

    fun previous() {
        remoteMediaClient?.queuePrev(null)
    }

    fun setRepeatMode(repeatMode: Int) {
        remoteMediaClient?.queueSetRepeatMode(repeatMode, null)
    }

    fun setShuffleMode(enabled: Boolean) {
        val repeatMode = if (enabled) {
            MediaStatus.REPEAT_MODE_REPEAT_ALL_AND_SHUFFLE
        } else {
            // When disabling shuffle, revert to REPEAT_OFF. A more advanced implementation
            // might restore the previous non-shuffle repeat mode.
            MediaStatus.REPEAT_MODE_REPEAT_OFF
        }
        remoteMediaClient?.queueSetRepeatMode(repeatMode, null)
    }

    private fun Song.toMediaQueueItem(): MediaQueueItem {
        // The customData can be used to store the original song ID for later retrieval
        // from the MediaQueueItem, which is crucial for state restoration.
        val customData = JSONObject().apply {
            put("songId", this@toMediaQueueItem.id)
        }
        return MediaQueueItem.Builder(this.toMediaInfo())
            .setCustomData(customData)
            .build()
    }
}