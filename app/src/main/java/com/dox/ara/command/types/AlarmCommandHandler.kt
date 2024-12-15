package com.dox.ara.command.types

import com.dox.ara.command.CommandHandler
import com.dox.ara.command.CommandHandlerFactory.CommandType.ALARM
import com.dox.ara.command.CommandResponse
import com.dox.ara.model.Alarm
import com.dox.ara.repository.AlarmRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import timber.log.Timber
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.Locale


class AlarmCommandHandler @AssistedInject constructor(
    @Assisted private val args : List<String>,
    private val alarmRepository: AlarmRepository
) : CommandHandler(args) {
    override val numArgs = 3
    private var time: Long = 0L
    private lateinit var timeString: String
    private lateinit var description: String
    private var volume: Int = 0

    override fun help(): String {
        return "[${ALARM.name.lowercase()}('dd-mm-yyyy hh:mm', 'description', volume(0-100))]"
    }

    override fun parseArguments() {
        val timeString = args[0].replace("'","").replace("\"", "")
        val description = args[1].replace("'","").replace("\"", "")
        val volume = args[2].replace("'","").replace("\"", "")

        try {
            val sdf = SimpleDateFormat("dd-MM-yyyy HH:mm", Locale.getDefault())
            val date = sdf.parse(timeString)
            this.time = date?.time ?: throw ParseException("Invalid date-time", 0)
            this.timeString = timeString
        } catch (e: ParseException) {
            throw IllegalArgumentException("Cannot parse date-time: '$timeString', must be a in format: dd-mm-yyyy hh:mm")
        }

        try {
            this.volume = volume.toInt()
            if (volume.toInt() !in 0..100) {
                throw IllegalArgumentException("Volume level must be between 0 and 100")
            }
        } catch (e: NumberFormatException) {
            throw IllegalArgumentException("Cannot parse volume level: '$volume', must be a number")
        }

        this.description = description
    }

    override suspend fun execute(chatId: Long): CommandResponse {
        return try {
            val alarm = Alarm(
                id = 0L,
                time = time,
                description = description,
                isActive = true,
                volume = volume,
                chatId = chatId
            )
            val alarmId = alarmRepository.save(alarm)
            val savedAlarm = alarm.copy(id = alarmId)
            if(!alarmRepository.scheduleAlarm(savedAlarm)){
                alarmRepository.delete(savedAlarm)
                throw RuntimeException("Cannot schedule alarm")
            }
            CommandResponse(
                message = "Alarm set successfully:\n" +
                        "Time: $timeString\n" +
                        "Description: $description\n" +
                        "Volume: $volume",
                isSuccess = true,
                getResponse = false
            )
        } catch (e: Exception) {
            Timber.e("[${::execute.name}] Error: $e")
            CommandResponse(
                message = "Failed to set alarm",
                isSuccess = false,
                getResponse = true
            )
        }
    }
}