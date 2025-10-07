package com.theveloper.pixelplay.data.service.cast

import android.net.Uri
import com.google.android.gms.cast.MediaInfo
import com.google.android.gms.cast.MediaMetadata
import com.google.android.gms.common.images.WebImage
import com.theveloper.pixelplay.data.model.Song
import com.theveloper.pixelplay.data.service.http.MediaFileHttpServerService

fun Song.toMediaInfo(): MediaInfo {
    // Use the running HTTP server's address
    val serverAddress = MediaFileHttpServerService.serverAddress
    val songUrl = "$serverAddress/song?id=${this.id}"
    val albumArtUrl = "$serverAddress/art?id=${this.albumId}"

    val metadata = MediaMetadata(MediaMetadata.MEDIA_TYPE_MUSIC_TRACK).apply {
        putString(MediaMetadata.KEY_TITLE, this@toMediaInfo.title)
        putString(MediaMetadata.KEY_ARTIST, this@toMediaInfo.artist)
        putString(MediaMetadata.KEY_ALBUM_TITLE, this@toMediaInfo.album)
        this@toMediaInfo.albumArtUriString?.let {
             addImage(WebImage(Uri.parse(albumArtUrl)))
        }
    }

    return MediaInfo.Builder(songUrl)
        .setStreamType(MediaInfo.STREAM_TYPE_BUFFERED)
        .setContentType("audio/mpeg") // This should ideally be dynamic based on the file
        .setMetadata(metadata)
        .build()
}