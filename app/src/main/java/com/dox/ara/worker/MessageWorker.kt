package com.dox.ara.worker

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.dox.ara.api.MessageAPI
import com.dox.ara.repository.AssistantRepository
import com.dox.ara.repository.ChatRepository
import com.dox.ara.repository.MessageRepository
import com.dox.ara.requestdto.MessageRequest
import com.dox.ara.ui.data.MessageStatus
import com.dox.ara.ui.data.Role
import com.dox.ara.utility.Constants
import com.dox.ara.utility.Constants.BREAK
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import timber.log.Timber

@HiltWorker
class MessageWorker @AssistedInject constructor(
    @Assisted private val appContext: Context,
    @Assisted workerParams: WorkerParameters,
    private val messageRepository: MessageRepository,
    private val chatRepository: ChatRepository,
    private val assistantRepository: AssistantRepository,
    private val messageAPI: MessageAPI
) : CoroutineWorker(appContext, workerParams) {
    companion object {
        const val ID_KEY = "id"
        const val PROMPT_KEY = "prompt"
    }

    override suspend fun doWork(): Result {
        Timber.d("[${::doWork.name}] [${Constants.Entity.MESSAGE}] Upload work started")

        val id = inputData.getLong(ID_KEY, -1)
        val autoPromptUser = inputData.getBoolean(PROMPT_KEY, false)
        val message = messageRepository.getMessageById(id)
        if(message == null) {
            Timber.e("[${::doWork.name}] [${Constants.Entity.MESSAGE}] Entity not found with id: $id")
            return Result.success()
        }
        val assistantId = chatRepository.getAssistantId(chatId = message.chatId)
        if(assistantId == null) {
            Timber.e("[${::doWork.name}] [${Constants.Entity.CHAT}] Entity not found with id: $id")
            messageRepository.updateMessage(message.copy(status = MessageStatus.FAILED))
            return Result.success()
        }

        if(message.status != MessageStatus.PENDING){
            Timber.e("[${::doWork.name}] [${Constants.Entity.MESSAGE}] " +
                    "Entity status is not ${MessageStatus.PENDING}: ${message.status}")
            return Result.success()
        }

        val getAssistantResponse = !message.content.endsWith(BREAK)
        if(message.from == Role.SYSTEM && !getAssistantResponse){
            message.content.replace(BREAK, "")
            messageRepository.updateMessage(message)
        }

        val messageRequest = MessageRequest(
            id = message.id,
            quotedId = message.quotedId,
            from = message.from,
            content = message.content,
            timestamp = message.timestamp,

            chatId = message.chatId,
            assistantId = assistantId,
        )

        return try {
            val response = messageAPI.upload(messageRequest)
            if (response.isSuccessful && response.body()?.isSuccess == true) {
                Timber.d("[${::doWork.name}] [${Constants.Entity.MESSAGE}] Response: " + response.body()?.payload)
                messageRepository.updateMessage(message.copy(status = MessageStatus.DELIVERED))

                if(getAssistantResponse) {
                    CoroutineScope(Dispatchers.IO).launch {
                        assistantRepository.getAssistantResponse(assistantId, message.chatId, autoPromptUser)
                    }
                }

                Result.success()
            } else {
                Timber.e("[${::doWork.name}] [${Constants.Entity.MESSAGE}] Response: " +
                        if (!response.isSuccessful) response.errorBody().toString() else response.body().toString())
                messageRepository.updateMessage(message.copy(status = MessageStatus.FAILED))
                Result.success()
            }
        } catch (e: Exception) {
            Timber.e("[${::doWork.name}] [${Constants.Entity.MESSAGE}] Exception: $e")
            messageRepository.updateMessage(message.copy(status = MessageStatus.FAILED))
            Result.success()
        }
    }
}