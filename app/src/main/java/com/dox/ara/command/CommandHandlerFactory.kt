package com.dox.ara.command

import timber.log.Timber
import java.util.regex.Pattern
import javax.inject.Inject


class CommandHandlerFactory @Inject constructor(
    private val payCommandHandler: PayCommandHandlerFactory,
    private val settingCommandHandler: SettingCommandHandlerFactory,
    private val volumeCommandHandler: VolumeCommandHandlerFactory,
    private val incomingCallCommandHandler: IncomingCallCommandHandlerFactory,
    private val alarmCommandHandler: AlarmCommandHandlerFactory
) {
    private val BRACKET_PATTERN: Pattern = Pattern.compile("\\[(.*?)]")
    private val COMMAND_PATTERN: Pattern = Pattern.compile("(\\w+)\\((.*?)\\)")
    private val COMMAND_DELIMITER = "->"
    private val ARGUMENT_DELIMITER = ","

    enum class CommandType {
        SETTING,
        PAY,
        VOLUME,
        INCOMING_CALL,
        ALARM
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
                            CommandType.PAY.name -> commandHandlers.add(payCommandHandler.create(args))
                            CommandType.VOLUME.name -> commandHandlers.add(volumeCommandHandler.create(args))
                            CommandType.INCOMING_CALL.name -> commandHandlers.add(incomingCallCommandHandler.create(args))
                            CommandType.ALARM.name -> commandHandlers.add(alarmCommandHandler.create(args))
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
}
