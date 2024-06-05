package com.dox.ara.manager

import android.content.ComponentName
import android.content.Context
import androidx.core.content.ContextCompat
import androidx.media3.common.MediaItem
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import com.dox.ara.service.MediaPlaybackService
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

class MediaControllerManager @Inject constructor(@ApplicationContext private val context: Context) {
    private var mediaController: MediaController? = null

    init {
        initializeController()
    }

    private fun initializeController() {
        val sessionToken = SessionToken(context, ComponentName(context, MediaPlaybackService::class.java))
        val controllerFuture = MediaController.Builder(context, sessionToken).buildAsync()
        controllerFuture.addListener({
            mediaController = controllerFuture.get()
        }, ContextCompat.getMainExecutor(context))
    }

    fun play(mediaItem: MediaItem) {
        mediaController?.stop()
        mediaController?.setMediaItem(mediaItem)
        mediaController?.play()
    }

    fun pause() {
        mediaController?.pause()
    }

    fun isPlaying(): Boolean {
        return mediaController?.isPlaying == true
    }

    fun togglePlayPause() {
        if (mediaController?.isPlaying == true) {
            mediaController?.pause()
        } else {
            mediaController?.play()
        }
    }

    fun release() {
        mediaController?.release()
        mediaController = null
    }
}
