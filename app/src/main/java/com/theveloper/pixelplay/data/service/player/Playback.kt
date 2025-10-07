package com.theveloper.pixelplay.data.service.player

import android.net.Uri
import com.theveloper.pixelplay.data.model.Song

interface Playback {
    val isInitialized: Boolean
    val isPlaying: Boolean
    val audioSessionId: Int
    fun setDataSource(song: Song, force: Boolean, completion: (success: Boolean) -> Unit)
    fun setNextDataSource(path: Uri?)
    var callbacks: PlaybackCallbacks?
    fun start(): Boolean
    fun stop()
    fun release()
    fun pause(): Boolean
    fun duration(): Int
    fun position(): Int
    fun seek(whereto: Int, force: Boolean): Int

    interface PlaybackCallbacks {
        fun onCompletion()
        fun onPlaybackStatusChanged(state: Int)
    }
}