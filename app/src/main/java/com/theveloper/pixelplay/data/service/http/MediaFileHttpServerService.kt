package com.theveloper.pixelplay.data.service.http

import android.app.Service
import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.Uri
import android.os.IBinder
import android.webkit.MimeTypeMap
import androidx.core.net.toUri
import com.theveloper.pixelplay.data.repository.MusicRepository
import dagger.hilt.android.AndroidEntryPoint
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import timber.log.Timber
import java.net.Inet4Address
import javax.inject.Inject

@AndroidEntryPoint
class MediaFileHttpServerService : Service() {

    @Inject
    lateinit var musicRepository: MusicRepository

    private var server: NettyApplicationEngine? = null
    private val serviceJob = SupervisorJob()
    private val serviceScope = CoroutineScope(Dispatchers.IO + serviceJob)

    companion object {
        const val ACTION_START_SERVER = "ACTION_START_SERVER"
        const val ACTION_STOP_SERVER = "ACTION_STOP_SERVER"
        var isServerRunning = false
        var serverAddress: String? = null
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_START_SERVER -> startServer()
            ACTION_STOP_SERVER -> stopSelf()
        }
        return START_NOT_STICKY
    }

    private fun getMimeType(uri: Uri): String? {
        return if (uri.scheme == "content") {
            contentResolver.getType(uri)
        } else {
            val extension = MimeTypeMap.getFileExtensionFromUrl(uri.toString())
            MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension)
        }
    }

    private fun startServer() {
        if (server?.application?.isActive == true) {
            Timber.d("Server is already running at $serverAddress")
            return
        }

        serviceScope.launch {
            try {
                val ipAddress = getWifiIpAddress(applicationContext)
                if (ipAddress == null) {
                    Timber.e("Could not get Wi-Fi IP address. Stopping server.")
                    stopSelf()
                    return@launch
                }
                serverAddress = "http://$ipAddress:8080"
                Timber.i("Starting server at: $serverAddress")

                server = embeddedServer(Netty, port = 8080, host = "0.0.0.0") {
                    routing {
                        get("/song/{songId}") {
                            val songId = call.parameters["songId"]
                            if (songId == null) {
                                call.respond(HttpStatusCode.BadRequest, "Song ID is missing")
                                return@get
                            }

                            val song = musicRepository.getSong(songId).firstOrNull()
                            if (song == null) {
                                call.respond(HttpStatusCode.NotFound, "Song not found")
                                return@get
                            }

                            val songUri = song.contentUriString.toUri()
                            val mimeType = getMimeType(songUri) ?: "audio/*"
                            Timber.d("Serving song $songId with MIME type: $mimeType")

                            contentResolver.openInputStream(songUri)?.use { inputStream ->
                                call.respondOutputStream(contentType = ContentType.parse(mimeType)) {
                                    inputStream.copyTo(this)
                                }
                            } ?: call.respond(HttpStatusCode.InternalServerError, "Could not open song file")
                        }
                        get("/art/{songId}") {
                            val songId = call.parameters["songId"]
                            if (songId == null) {
                                call.respond(HttpStatusCode.BadRequest, "Song ID is missing")
                                return@get
                            }

                            val song = musicRepository.getSong(songId).firstOrNull()
                            if (song?.albumArtUriString == null) {
                                call.respond(HttpStatusCode.NotFound, "Album art not found")
                                return@get
                            }

                            val artUri = song.albumArtUriString.toUri()
                            val mimeType = getMimeType(artUri) ?: "image/*"
                            Timber.d("Serving art for $songId with MIME type: $mimeType")

                            contentResolver.openInputStream(artUri)?.use { inputStream ->
                                call.respondOutputStream(contentType = ContentType.parse(mimeType)) {
                                    inputStream.copyTo(this)
                                }
                            } ?: call.respond(HttpStatusCode.InternalServerError, "Could not open album art file")
                        }
                    }
                }.start(wait = false)
                isServerRunning = true
            } catch (e: Exception) {
                Timber.e(e, "Error starting Ktor server")
                stopSelf()
            }
        }
    }

    private fun getWifiIpAddress(context: Context): String? {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val networks = connectivityManager.allNetworks
        for (network in networks) {
            val networkCapabilities = connectivityManager.getNetworkCapabilities(network)
            if (networkCapabilities?.hasTransport(android.net.NetworkCapabilities.TRANSPORT_WIFI) == true) {
                val linkProperties = connectivityManager.getLinkProperties(network)
                linkProperties?.linkAddresses?.forEach { linkAddress ->
                    if (linkAddress.address is Inet4Address) {
                        return linkAddress.address.hostAddress
                    }
                }
            }
        }
        Timber.w("No Wi-Fi IP Address found.")
        return null
    }


    override fun onDestroy() {
        super.onDestroy()
        Timber.i("Stopping server.")
        server?.stop(1000, 2000)
        isServerRunning = false
        serverAddress = null
        serviceJob.cancel()
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }
}