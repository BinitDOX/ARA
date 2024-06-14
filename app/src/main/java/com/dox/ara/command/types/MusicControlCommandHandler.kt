package com.dox.ara.command.types

import com.dox.ara.command.CommandHandler
import com.dox.ara.command.CommandHandlerFactory.CommandType.MUSIC_CONTROL
import com.dox.ara.command.CommandResponse
import com.dox.ara.manager.MediaControllerManager
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import timber.log.Timber


class MusicControlCommandHandler @AssistedInject constructor(
    @Assisted private val args : List<String>,
    private val mediaControllerManager: MediaControllerManager
) : CommandHandler(args) {
    override val numArgs = 1
    private lateinit var musicCommand: MusicCommand

    enum class MusicCommand {
        RESUME,
        PAUSE,
        STOP,
    }

    override fun help(): String {
        val musicCommands = MusicCommand.entries.joinToString("|") { it.name.lowercase() }

        return "[${MUSIC_CONTROL.name.lowercase()}(<${musicCommands}>)]"    }

    override fun parseArguments() {
        val musicCommand = args[0].uppercase().replace("'", "")

        try {
            this.musicCommand = MusicCommand.valueOf(musicCommand)
        } catch (e: IllegalArgumentException) {
            throw IllegalArgumentException(
                "Invalid music command: $musicCommand, " +
                        "must be one of ${MusicCommand.entries.joinToString { it.name }}",
            )
        }
    }

    override suspend fun execute(): CommandResponse {
        return try {
            if(mediaControllerManager.isMusicStopped()){
                return CommandResponse(
                    message = "No music is currently playing, command not executed",
                    isSuccess = false,
                    getResponse = true
                )
            }

            when (musicCommand) {
                MusicCommand.RESUME -> {
                    return if(mediaControllerManager.isMusicPlaying()){
                        CommandResponse(
                            message = "Music is already playing",
                            isSuccess = true,
                            getResponse = true
                        )
                    } else {
                        mediaControllerManager.toggleMusicPlayPause()
                        CommandResponse(
                            message = "Music successfully resumed",
                            isSuccess = true,
                            getResponse = false
                        )
                    }
                }
                MusicCommand.PAUSE -> {
                    return if(!mediaControllerManager.isMusicPlaying()){
                        CommandResponse(
                            message = "Music is already paused",
                            isSuccess = true,
                            getResponse = true
                        )
                    } else {
                        mediaControllerManager.toggleMusicPlayPause()
                        CommandResponse(
                            message = "Music successfully paused",
                            isSuccess = true,
                            getResponse = false
                        )
                    }
                }
                MusicCommand.STOP -> {
                    mediaControllerManager.stopMusic()
                    CommandResponse(
                        message = "Music successfully stopped",
                        isSuccess = true,
                        getResponse = false
                    )
                }
            }
        } catch (e: Exception) {
            Timber.e("[${::execute.name}] Error: $e")
            CommandResponse(
                message = "Failed to execute music command: $musicCommand",
                isSuccess = false,
                getResponse = true
            )
        }
    }
}