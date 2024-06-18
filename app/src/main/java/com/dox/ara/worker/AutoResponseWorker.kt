package com.dox.ara.worker

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.dox.ara.manager.SharedPreferencesManager
import com.dox.ara.repository.ChatRepository
import com.dox.ara.repository.EventRepository
import com.dox.ara.repository.MessageRepository
import com.dox.ara.utility.Constants.ASSISTANT_IDLE_AUTO_RESPONSE_TIME_KEY
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import timber.log.Timber
import java.time.Instant
import java.time.temporal.ChronoUnit

@HiltWorker
class AutoResponseWorker @AssistedInject constructor (
    @Assisted private val appContext: Context,
    @Assisted workerParams: WorkerParameters,
    private val messageRepository: MessageRepository,
    private val chatRepository: ChatRepository,
    private val sharedPreferencesManager: SharedPreferencesManager,
    private val eventRepository: EventRepository
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        Timber.d("[${::doWork.name}] [autoWake] Work started")
        return try {
            val messages = messageRepository.getAssistantLastMessageFromEveryChat()
            val idleResponseTime = sharedPreferencesManager.get(ASSISTANT_IDLE_AUTO_RESPONSE_TIME_KEY)

            if(idleResponseTime.isNullOrBlank()){
                Timber.d("[${::doWork.name}] [autoWake] Idle response time is not set, skipping")
                return Result.success()
            }

            val idleResponseTimeInHours = idleResponseTime.toLong()

            for (message in messages) {
                val messageTime = Instant.ofEpochMilli(message.timestamp)
                val currentTime = Instant.now()
                val hoursDifference = ChronoUnit.HOURS.between(messageTime, currentTime)

                if (hoursDifference > idleResponseTimeInHours) {
                    val eventResponse = EventRepository.EventResponse(
                        message = "It has been $hoursDifference hours since your last message. " +
                                "Interact or start a conversation with the user, " +
                                "according to your configured personality and character.",
                        getResponse = true,
                        getUserInput = false
                    )
                    val chat = chatRepository.getChat(message.chatId)
                    if(chat.autoResponses) {
                        eventRepository.handleEvent(eventResponse, chat.id.toString())
                    }
                }
            }

            Result.success()
        } catch (e: Exception) {
            Timber.e("[${::doWork.name}] [autoWake] Exception: $e")
            Result.failure()
        }
    }
}