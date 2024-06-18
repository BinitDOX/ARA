package com.dox.ara.repository

import android.content.Context
import androidx.work.Constraints
import androidx.work.Data
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.dox.ara.api.AuthenticationAPI
import com.dox.ara.listener.SpeechToTextListener
import com.dox.ara.listener.SpeechToTextState
import com.dox.ara.manager.SharedPreferencesManager
import com.dox.ara.model.Message
import com.dox.ara.ui.data.MessageStatus
import com.dox.ara.ui.data.Role
import com.dox.ara.utility.Constants
import com.dox.ara.utility.Constants.DEFAULT_CHAT_ID_KEY
import com.dox.ara.worker.MessageWorker
import com.truecrm.rat.utility.getWorkerName
import com.truecrm.rat.utility.waitForCondition
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.time.Instant
import javax.inject.Inject

class EventRepository @Inject constructor(
    @ApplicationContext private val context: Context,
    private val authenticationAPI: AuthenticationAPI,
    private val sharedPreferencesManager: SharedPreferencesManager,
    private val messageRepository: MessageRepository,
    private val speechToTextListener: SpeechToTextListener
) {
    data class EventResponse(
        val message: String,
        val getResponse: Boolean,  // Get response from assistant
        val getUserInput: Boolean  // Get user voice input after assistant response
    )

    init {
        CoroutineScope(Dispatchers.Main).launch {
            speechToTextListener.initialize()
        }
        CoroutineScope(Dispatchers.IO).launch {
            getSpeechToTextState()
        }
    }

    private lateinit var speechToTextState: SpeechToTextState

    private suspend fun getSpeechToTextState(): SpeechToTextState {
        speechToTextListener.state.collect {
            speechToTextState = it
        }
    }


    suspend fun handleEvent(eventResponse: EventResponse, chatId: String? = null) {
        val cId = chatId ?: sharedPreferencesManager.get(DEFAULT_CHAT_ID_KEY)

        if (cId == null) {
            Timber.w("[${::handleEvent.name}] Default assistant is not set")
            return
        }

        val message = Message(
            id = 0,
            chatId = cId.toLong(),
            content = eventResponse.message + if (!eventResponse.getResponse)
                Constants.BREAK else "",
            timestamp = Instant.now().toEpochMilli(),
            quotedId = null,
            from = Role.SYSTEM,
            status = MessageStatus.PENDING
        )

        val response = authenticationAPI.getStatus()
        if(response.isSuccessful && response.body()?.isSuccess == true){
            val messageId = messageRepository.saveMessage(message)
            uploadSystemMessage(messageId, cId.toLong(), eventResponse.getUserInput)
        } else {
            messageRepository.saveMessage(message.copy(status = MessageStatus.FAILED))
            Timber.e("[${::handleEvent.name}] No connection to backend, event message not uploaded")
        }
    }

    fun uploadSystemMessage(messageId: Long, chatId: Long, autoPromptUser: Boolean = false) {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        val workerName = getWorkerName("${Role.SYSTEM}_${Constants.Entity.MESSAGE}_$chatId")

        val workRequest = OneTimeWorkRequestBuilder<MessageWorker>()
            .addTag(workerName)
            .setConstraints(constraints)
            .setInputData(
                Data.Builder()
                    .putBoolean(MessageWorker.PROMPT_KEY, autoPromptUser)
                    .putLong(MessageWorker.ID_KEY, messageId)
                    .build())
            .build()

        WorkManager.getInstance(context).enqueueUniqueWork(
            workerName, ExistingWorkPolicy.APPEND_OR_REPLACE, workRequest)

        Timber.i("[${::uploadSystemMessage.name}] $workerName enqueued")
    }

    suspend fun startListeningForUserInput(chatId: Long? = null){
        Timber.d("[${::startListeningForUserInput.name}] Called")
        var defaultChatId = chatId
        if(chatId == null) {
            defaultChatId = sharedPreferencesManager.get(DEFAULT_CHAT_ID_KEY)?.toLong()
        }
        if(defaultChatId == null) {
            Timber.w("[${::startListeningForUserInput.name}] Default assistant is not set")
            return
        }

        withContext(Dispatchers.Main) {
            speechToTextListener.startListening()
        }

        delay(2000L)
        waitForCondition {
            speechToTextState.isListening
        }
        delay(1000L)

        if(speechToTextState.isListening){
            withContext(Dispatchers.Main) {
                speechToTextListener.stopListening()
            }
        }

        val content = speechToTextState.spokenText
        Timber.d("[${::startListeningForUserInput.name}] Spoken content: $content")
        if(content.isNotBlank()) {
            val message = Message(
                id = 0,
                chatId = defaultChatId,
                content = content,
                timestamp = Instant.now().toEpochMilli(),
                quotedId = null,
                from = Role.USER,
                status = MessageStatus.PENDING
            )
            val messageId = messageRepository.saveMessage(message)
            messageRepository.uploadMessage(messageId, defaultChatId.toLong())
        }
    }
}