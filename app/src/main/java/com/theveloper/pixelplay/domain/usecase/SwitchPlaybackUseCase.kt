package com.theveloper.pixelplay.domain.usecase

import android.content.Context
import android.content.Intent
import com.google.android.gms.cast.framework.CastContext
import com.google.android.gms.cast.framework.CastSession
import com.google.android.gms.cast.framework.SessionManager
import com.google.android.gms.cast.framework.SessionManagerListener
import com.theveloper.pixelplay.data.model.Song
import com.theveloper.pixelplay.data.service.cast.CastPlayer
import com.theveloper.pixelplay.data.service.http.MediaFileHttpServerService
import com.theveloper.pixelplay.data.service.player.DualPlayerEngine
import com.theveloper.pixelplay.data.service.player.Playback
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SwitchPlaybackUseCase @Inject constructor(
    @ApplicationContext private val context: Context,
    private val dualPlayerEngine: DualPlayerEngine
) {
    private val _activePlayer = MutableStateFlow<Playback>(dualPlayerEngine)
    val activePlayer: StateFlow<Playback> = _activePlayer.asStateFlow()

    private val sessionManager: SessionManager
    private var castSessionManagerListener: SessionManagerListener<CastSession>? = null
    private val coroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    init {
        sessionManager = CastContext.getSharedInstance(context).sessionManager
        setupCastListener()
    }

    private fun setupCastListener() {
        castSessionManagerListener = object : SessionManagerListener<CastSession> {
            override fun onSessionStarted(session: CastSession, sessionId: String) = switchToRemotePlayback(session)
            override fun onSessionResumed(session: CastSession, wasSuspended: Boolean) = switchToRemotePlayback(session)
            override fun onSessionEnded(session: CastSession, error: Int) = switchToLocalPlayback()
            override fun onSessionSuspended(session: CastSession, reason: Int) = switchToLocalPlayback()
        }
        sessionManager.addSessionManagerListener(castSessionManagerListener!!, CastSession::class.java)
    }

    private fun switchToRemotePlayback(session: CastSession) {
        coroutineScope.launch {
            if (!ensureHttpServerRunning()) {
                Timber.e("Failed to start HTTP server for casting.")
                return@launch
            }
            val wasPlaying = _activePlayer.value.isPlaying
            val progress = _activePlayer.value.position()
            val currentSong = (_activePlayer.value as? DualPlayerEngine)?.masterPlayer?.currentMediaItem?.let { mediaItem ->
                // This is a simplification. A real implementation would need a robust way to get the full Song object.
                // For now, we assume the ViewModel will handle setting the data source on the new player.
                 Song.emptySong().copy(id = mediaItem.mediaId)
            }

            _activePlayer.value.stop()
            val castPlayer = CastPlayer(session)
            castPlayer.callbacks = _activePlayer.value.callbacks
            _activePlayer.value = castPlayer

            currentSong?.let {
                castPlayer.setDataSource(it, true) { success ->
                    if (success && wasPlaying) {
                        castPlayer.seek(progress, true)
                        castPlayer.start()
                    }
                }
            }
        }
    }

    private fun switchToLocalPlayback() {
        val wasPlaying = _activePlayer.value.isPlaying
        val progress = _activePlayer.value.position()
        val currentSong = (_activePlayer.value as? CastPlayer)?.let {
             // This is a simplification. A real implementation would need a robust way to get the full Song object.
             Song.emptySong()
        }

        _activePlayer.value.release()
        dualPlayerEngine.callbacks = _activePlayer.value.callbacks
        _activePlayer.value = dualPlayerEngine

        currentSong?.let {
            dualPlayerEngine.setDataSource(it, true) { success ->
                if (success && wasPlaying) {
                    dualPlayerEngine.seek(progress, true)
                    dualPlayerEngine.start()
                }
            }
        }
    }

    private suspend fun ensureHttpServerRunning(): Boolean {
        if (MediaFileHttpServerService.isServerRunning && MediaFileHttpServerService.serverAddress != null) {
            return true
        }

        context.startService(Intent(context, MediaFileHttpServerService::class.java).apply {
            action = MediaFileHttpServerService.ACTION_START_SERVER
        })

        val startTime = System.currentTimeMillis()
        val timeout = 5000L // 5 seconds
        while (!MediaFileHttpServerService.isServerRunning || MediaFileHttpServerService.serverAddress == null) {
            if (System.currentTimeMillis() - startTime > timeout) {
                Timber.e("HTTP server start timed out.")
                return false
            }
            delay(100)
        }
        return true
    }

    fun onCleared() {
        castSessionManagerListener?.let {
            sessionManager.removeSessionManagerListener(it, CastSession::class.java)
        }
    }
}