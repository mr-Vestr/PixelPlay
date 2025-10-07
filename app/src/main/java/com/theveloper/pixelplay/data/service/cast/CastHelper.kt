package com.theveloper.pixelplay.data.service.cast

import android.net.Uri
import com.google.android.gms.cast.MediaInfo
import com.google.android.gms.cast.MediaMetadata
import com.google.android.gms.common.images.WebImage
import com.theveloper.pixelplay.data.model.Song
import com.theveloper.pixelplay.data.service.http.MediaFileHttpServerService

fun Song.toMediaInfo(): MediaInfo {
    val baseAddress = MediaFileHttpServerService.serverAddress
    val songUrl = "$baseAddress/song/${this.id}"
    val albumArtUrl = "$baseAddress/art/${this.id}"

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