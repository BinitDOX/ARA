package com.dox.ara.service

import android.content.Intent
import androidx.annotation.OptIn
import androidx.media3.common.AudioAttributes
import androidx.media3.common.C
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.session.DefaultMediaNotificationProvider
import androidx.media3.session.MediaSession
import androidx.media3.session.MediaSessionService
import com.dox.ara.R
import com.dox.ara.utility.Constants


class AssistantSpeechService : MediaSessionService() {
    private var mediaSession: MediaSession? = null
    private val notificationId = 20


    @OptIn(UnstableApi::class)
    override fun onCreate() {
        super.onCreate()

        // Initialize ExoPlayer
        val exoPlayer = ExoPlayer.Builder(this).build()
        exoPlayer.setAudioAttributes(AudioAttributes.Builder()
            .setContentType(C.AUDIO_CONTENT_TYPE_SPEECH)
            .setUsage(C.USAGE_ASSISTANT)
            .build(), false)

        // Initialize MediaSession
        mediaSession = MediaSession.Builder(this, exoPlayer)
            .setId("assistant_speech_session")
            //.setSessionActivity(/* pendingIntent to launch activity */) TODO: Link to chat
            .build()

        val notificationProvider = DefaultMediaNotificationProvider.Builder(this)
            .setNotificationId(notificationId)
            .setChannelId(Constants.SPEECH_NOTIFICATION_CHANNEL_ID)
            .setChannelName(R.string.speech_notification_channel_name)
            .build()

        setMediaNotificationProvider(notificationProvider)
    }

    override fun onTaskRemoved(rootIntent: Intent?) {
        val player = mediaSession?.player!!
        if (!player.playWhenReady
            || player.mediaItemCount == 0
            || player.playbackState == Player.STATE_ENDED) {
            // Stop the service if not playing, continue playing in the background otw
            stopSelf()
        }
    }

    override fun onGetSession(controllerInfo: MediaSession.ControllerInfo): MediaSession? {
        return mediaSession
    }

    override fun onDestroy() {
        mediaSession?.run {
            player.release()
            release()
        }
        super.onDestroy()
    }
}
