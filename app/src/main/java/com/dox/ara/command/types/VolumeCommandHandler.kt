package com.dox.ara.command.types

import android.content.Context
import android.media.AudioManager
import com.dox.ara.command.CommandHandler
import com.dox.ara.command.CommandResponse
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import dagger.hilt.android.qualifiers.ApplicationContext
import timber.log.Timber


class VolumeCommandHandler @AssistedInject constructor(
    @Assisted private val args : List<String>,
    @ApplicationContext private val context: Context,
) : CommandHandler(args) {
    override val numArgs = 1
    private var level: Int = 0


    override fun help(): String {
        return "Usage: volume(<0-100>)"
    }

    override fun parseArguments() {
        val level = args[0].replace("'", "")

        try {
            this.level = level.toInt()
            if (level.toInt() !in 0..100) {
                throw IllegalArgumentException("Volume level must be between 0 and 100")
            }
        } catch (e: NumberFormatException) {
            throw IllegalArgumentException("Cannot parse volume level: '$level', must be a number")
        }
    }

    override suspend fun execute(): CommandResponse {
        return try {
            val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
            val maxVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC)
            val desiredVolume = (maxVolume * (level / 100.0)).toInt()

            audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, desiredVolume, 0)
            CommandResponse(
                message = "Volume successfully set to $level",
                isSuccess = true,
                getResponse = false
            )
        } catch (e: Exception) {
            Timber.e("[${::execute.name}] Error: $e")
            CommandResponse(
                message = "Failed to set volume to $level",
                isSuccess = false,
                getResponse = true
            )
        }
    }
}