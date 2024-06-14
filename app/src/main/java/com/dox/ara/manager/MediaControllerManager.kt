package com.dox.ara.manager

import android.content.ComponentName
import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFocusRequest
import android.media.AudioManager
import androidx.core.content.ContextCompat
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import com.dox.ara.service.AssistantSpeechService
import com.dox.ara.service.MusicPlaybackService
import dagger.hilt.android.qualifiers.ApplicationContext
import timber.log.Timber
import javax.inject.Inject

class MediaControllerManager @Inject constructor(@ApplicationContext private val context: Context) {
    companion object {
        private var musicController: MediaController? = null
        private var speechController: MediaController? = null
    }

    init {
        initializeControllers()
    }

    private val audioManager: AudioManager by lazy {
        context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
    }

    private val speechPlayerListener = object : Player.Listener {
        override fun onPlaybackStateChanged(state: Int) {
            when (state) {
                Player.STATE_ENDED,
                Player.STATE_IDLE -> abandonAudioFocus()

                Player.STATE_BUFFERING,
                Player.STATE_READY -> requestAudioFocus()
            }
        }
    }

    private fun initializeControllers() {
        // Initialize Music Controller
        val musicSessionToken = SessionToken(context, ComponentName(context, MusicPlaybackService::class.java))
        val musicControllerFuture = MediaController.Builder(context, musicSessionToken).buildAsync()
        musicControllerFuture.addListener({
            musicController = musicControllerFuture.get()
        }, ContextCompat.getMainExecutor(context))

        // Initialize Speech Controller
        val speechSessionToken = SessionToken(context, ComponentName(context, AssistantSpeechService::class.java))
        val speechControllerFuture = MediaController.Builder(context, speechSessionToken).buildAsync()
        speechControllerFuture.addListener({
            speechController = speechControllerFuture.get()
            speechController?.addListener(speechPlayerListener)
        }, ContextCompat.getMainExecutor(context))
    }

    private val audioFocusRequest: AudioFocusRequest by lazy {
        AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN_TRANSIENT_MAY_DUCK)
            .setAudioAttributes(
                AudioAttributes.Builder()
                    .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                    .setUsage(AudioAttributes.USAGE_ASSISTANT)
                    .build()
            )
            .build()
    }

    fun playMusic(mediaItem: MediaItem) {
        if(musicController == null){
            Timber.e("[${::playMusic.name}] Music controller is not initialized")
        }
        musicController?.stop()
        musicController?.setMediaItem(mediaItem)
        musicController?.play()
    }

    fun playSpeech(mediaItem: MediaItem) {
        if(speechController == null){
            Timber.e("[${::playSpeech.name}] Speech controller is not initialized")
        }
        speechController?.stop()
        speechController?.setMediaItem(mediaItem)
        speechController?.play()
    }

    fun stopMusic() {
        musicController?.stop()
    }

    fun stopSpeech() {
        speechController?.stop()
    }

    fun isMusicPlaying(): Boolean {
        return musicController?.isPlaying == true
    }

    fun isSpeechPlaying(): Boolean {
        return speechController?.isPlaying == true
    }

    fun isMusicStopped(): Boolean {
        return musicController == null || musicController?.playbackState == Player.STATE_IDLE
                || musicController?.playbackState == Player.STATE_ENDED
    }

    fun toggleMusicPlayPause() {
        if (musicController?.isPlaying == true) {
            musicController?.pause()
        } else {
            musicController?.play()
        }
    }

    fun toggleSpeechPlayPause() {
        if (speechController?.isPlaying == true) {
            speechController?.pause()
        } else {
            speechController?.play()
        }
    }


    private fun requestAudioFocus() {
        audioManager.requestAudioFocus(audioFocusRequest)
    }

    private fun abandonAudioFocus() {
        audioManager.abandonAudioFocusRequest(audioFocusRequest)
    }

    fun release() {
        musicController?.release()
        speechController?.release()
        musicController = null
        speechController = null
    }
}

