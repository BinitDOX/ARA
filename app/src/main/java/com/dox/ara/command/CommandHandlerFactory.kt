package com.dox.ara.command

import timber.log.Timber
import java.util.regex.Pattern
import javax.inject.Inject


class CommandHandlerFactory @Inject constructor(
    private val payUpiCommandHandler: PayUpiCommandHandlerFactory,
    private val payQrCommandHandler: PayQrCommandHandlerFactory,
    private val settingCommandHandler: SettingCommandHandlerFactory,
    private val volumeCommandHandler: VolumeCommandHandlerFactory,
    private val incomingCallCommandHandler: IncomingCallCommandHandlerFactory,
    private val callCommandHandler: CallCommandHandlerFactory,
    private val alarmCommandHandler: AlarmCommandHandlerFactory,
    private val playMusicCommandHandler: PlayMusicCommandHandlerFactory,
    private val musicControlCommandHandler: MusicControlCommandHandlerFactory,
    private val bookCabCommandHandler: BookCabCommandHandlerFactory
) {
    private val BRACKET_PATTERN: Pattern = Pattern.compile("\\[(.*?)]")
    private val COMMAND_PATTERN: Pattern = Pattern.compile("(\\w+)\\((.*?)\\)")
    private val COMMAND_DELIMITER = "->"
    private val ARGUMENT_DELIMITER = ","

    enum class CommandType {
        SETTING,
        PAY_UPI,
        PAY_QR,
        VOLUME,
        INCOMING_CALL,
        ALARM,
        CALL,
        PLAY_MUSIC,
        MUSIC_CONTROL,
        BOOK_CAB
    }


    @Throws(IllegalArgumentException::class)
    fun getCommandHandlers(content: String): List<CommandHandler> {
        val commandHandlers: MutableList<CommandHandler> = ArrayList()

        val bracketMatcher = BRACKET_PATTERN.matcher(content)
        if (bracketMatcher.find()) {
            val commandSequence = bracketMatcher.group(1)
            Timber.d("[${::getCommandHandlers.name}] Command sequence: $commandSequence")

            val commandParts = commandSequence?.split(COMMAND_DELIMITER)?.map { it.trim() }
            if (commandParts != null) {
                for (part in commandParts) {
                    val commandMatcher = COMMAND_PATTERN.matcher(part)

                    if (commandMatcher.find()) {
                        val commandType = commandMatcher.group(1)
                        val args = commandMatcher.group(2)?.split(ARGUMENT_DELIMITER)?.map { it.trim() }

                        if(args == null){
                            Timber.e("[${::getCommandHandlers.name}] Invalid arguments format: '$part'")
                            throw IllegalArgumentException("Invalid arguments format: $part")
                        }

                        when (commandType?.uppercase()) {
                            CommandType.SETTING.name -> commandHandlers.add(settingCommandHandler.create(args))
                            CommandType.PAY_UPI.name -> commandHandlers.add(payUpiCommandHandler.create(args))
                            CommandType.PAY_QR.name -> commandHandlers.add(payQrCommandHandler.create(args))
                            CommandType.VOLUME.name -> commandHandlers.add(volumeCommandHandler.create(args))
                            CommandType.INCOMING_CALL.name -> commandHandlers.add(incomingCallCommandHandler.create(args))
                            CommandType.CALL.name -> commandHandlers.add(callCommandHandler.create(args))
                            CommandType.ALARM.name -> commandHandlers.add(alarmCommandHandler.create(args))
                            CommandType.PLAY_MUSIC.name -> commandHandlers.add(playMusicCommandHandler.create(args))
                            CommandType.MUSIC_CONTROL.name -> commandHandlers.add(musicControlCommandHandler.create(args))
                            CommandType.BOOK_CAB.name -> commandHandlers.add(bookCabCommandHandler.create(args))
                            else -> {
                                Timber.e("[${::getCommandHandlers.name}] Unknown command type: $commandType")
                                throw IllegalArgumentException("Command not found: '$commandType'")
                            }
                        }
                    } else {
                        Timber.e("[${::getCommandHandlers.name}] Invalid command format: $part")
                        throw IllegalArgumentException("Invalid command format: '$part'")
                    }
                }
            } else {
                Timber.e("[${::getCommandHandlers.name}] Invalid command format: $commandSequence")
                throw IllegalArgumentException("Invalid command format: '$commandSequence'")
            }
        }

        return commandHandlers
    }

    fun getAllCommandUsages(): List<String>{
        val commandHelps = ArrayList<String>()
        commandHelps.add(settingCommandHandler.create(emptyList()).getUsage())
        commandHelps.add(payUpiCommandHandler.create(emptyList()).getUsage())
        commandHelps.add(payQrCommandHandler.create(emptyList()).getUsage())
        commandHelps.add(volumeCommandHandler.create(emptyList()).getUsage())
        commandHelps.add(incomingCallCommandHandler.create(emptyList()).getUsage())
        commandHelps.add(callCommandHandler.create(emptyList()).getUsage())
        commandHelps.add(alarmCommandHandler.create(emptyList()).getUsage())
        commandHelps.add(playMusicCommandHandler.create(emptyList()).getUsage())
        commandHelps.add(musicControlCommandHandler.create(emptyList()).getUsage())
        commandHelps.add(bookCabCommandHandler.create(emptyList()).getUsage())
        return commandHelps
    }
}
