package com.theveloper.pixelplay.data.service.cast

import android.net.Uri
import com.google.android.gms.cast.MediaInfo
import com.google.android.gms.cast.MediaMetadata
import com.google.android.gms.common.images.WebImage
import com.theveloper.pixelplay.data.model.Song
import com.theveloper.pixelplay.data.service.http.MediaFileHttpServerService

fun Song.toMediaInfo(): MediaInfo {
    val serverAddress = MediaFileHttpServerService.serverAddress
    // A placeholder IP is used here as the actual IP will be provided by the running server.
    // This function relies on the server being active when it's called.
    val songUrl = "${serverAddress ?: "http://127.0.0.1:8080"}/song?id=${this.id}"
    val albumArtUrl = "${serverAddress ?: "http://127.0.0.1:8080"}/cover?id=${this.albumId}"

    val metadata = MediaMetadata(MediaMetadata.MEDIA_TYPE_MUSIC_TRACK).apply {
        putString(MediaMetadata.KEY_TITLE, this@toMediaInfo.title)
        putString(MediaMetadata.KEY_ARTIST, this@toMediaInfo.artist)
        putString(MediaMetadata.KEY_ALBUM_TITLE, this@toMediaInfo.album)
        addImage(WebImage(Uri.parse(albumArtUrl)))
    }
    return MediaInfo.Builder(songUrl)
        .setStreamType(MediaInfo.STREAM_TYPE_BUFFERED)
        .setContentType("audio/mpeg")
        .setMetadata(metadata)
        .build()
}