package com.theveloper.pixelplay.domain.use_case

import android.content.Context
import android.content.Intent
import com.google.android.gms.cast.framework.CastContext
import com.google.android.gms.cast.framework.CastSession
import com.google.android.gms.cast.framework.SessionManager
import com.google.android.gms.cast.framework.SessionManagerListener
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
    private var castSessionManagerListener: SessionManagerListener<CastSession>

    init {
        sessionManager = CastContext.getSharedInstance(context).sessionManager
        castSessionManagerListener = object : SessionManagerListener<CastSession> {
            override fun onSessionStarted(session: CastSession, sessionId: String) = switchToRemote(session)
            override fun onSessionResumed(session: CastSession, wasSuspended: Boolean) = switchToRemote(session)
            override fun onSessionEnded(session: CastSession, error: Int) = switchToLocal()
            override fun onSessionSuspended(session: CastSession, reason: Int) = switchToLocal()
        }
        sessionManager.addSessionManagerListener(castSessionManagerListener, CastSession::class.java)
    }

    fun switchToRemote(session: CastSession) {
        CoroutineScope(SupervisorJob() + Dispatchers.Main).launch {
            if (!ensureHttpServerRunning()) {
                Timber.e("Failed to start HTTP server for casting.")
                // Optionally, post an error message to the UI via a shared flow
                return@launch
            }

            val oldPlayer = _activePlayer.value
            if (oldPlayer is CastPlayer) return@launch // Already on a cast player

            // 1. Capture State
            val wasPlaying = oldPlayer.isPlaying
            val currentQueue = oldPlayer.currentQueue
            val currentSong = oldPlayer.currentSong
            val currentPosition = oldPlayer.position()

            // 2. Stop and release old player resources if necessary (optional here, but good practice)
            oldPlayer.stop()

            // 3. Create and set new player
            val newPlayer = CastPlayer(session)
            newPlayer.callbacks = oldPlayer.callbacks // Transfer callbacks
            _activePlayer.value = newPlayer

            // 4. Transfer state to new player
            if (currentSong != null && currentQueue.isNotEmpty()) {
                newPlayer.playSongs(currentQueue, currentSong)
                // Small delay to allow the media to be loaded before seeking
                delay(1000)
                newPlayer.seek(currentPosition, true)
                if (wasPlaying) {
                    newPlayer.start()
                }
            }
        }
    }

    fun switchToLocal() {
        val oldPlayer = _activePlayer.value
        if (oldPlayer is DualPlayerEngine) return // Already on the local player

        // 1. Capture State
        val wasPlaying = oldPlayer.isPlaying
        val currentQueue = oldPlayer.currentQueue
        val currentSong = oldPlayer.currentSong
        val currentPosition = oldPlayer.position()

        // 2. Release old player
        oldPlayer.release()

        // 3. Set new player
        dualPlayerEngine.callbacks = oldPlayer.callbacks // Transfer callbacks
        _activePlayer.value = dualPlayerEngine

        // 4. Transfer state
        if (currentSong != null && currentQueue.isNotEmpty()) {
            dualPlayerEngine.playSongs(currentQueue, currentSong)
            dualPlayerEngine.seek(currentPosition, true)
            if (wasPlaying) {
                dualPlayerEngine.start()
            } else {
                // Ensure UI updates position even if paused
                dualPlayerEngine.callbacks?.onPositionChanged(currentPosition.toLong())
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
        sessionManager.removeSessionManagerListener(castSessionManagerListener, CastSession::class.java)
        _activePlayer.value.release()
    }
}