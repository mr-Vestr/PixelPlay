package com.theveloper.pixelplay.data.service.player

import com.theveloper.pixelplay.data.model.Song

interface Playback {
    val isInitialized: Boolean
    val isPlaying: Boolean
    val audioSessionId: Int
    val currentSong: Song?
    val currentQueue: List<Song>

    fun playSongs(songs: List<Song>, startSong: Song)

    var callbacks: PlaybackCallbacks?

    fun start(): Boolean
    fun stop()
    fun release()
    fun pause(): Boolean
    fun duration(): Int
    fun position(): Int
    fun seek(whereto: Int, force: Boolean): Int

    fun next()
    fun previous()

    fun toggleShuffle()
    fun cycleRepeatMode()

    interface PlaybackCallbacks {
        fun onCompletion()
        fun onPlaybackStatusChanged(state: Int)
        fun onSongChanged(song: Song?)
        fun onIsPlayingChanged(isPlaying: Boolean)
        fun onShuffleModeChanged(isEnabled: Boolean)
        fun onRepeatModeChanged(repeatMode: Int)
        fun onPositionChanged(position: Long)
        fun onDurationChanged(duration: Long)
        fun onQueueChanged(queue: List<Song>)
    }
}