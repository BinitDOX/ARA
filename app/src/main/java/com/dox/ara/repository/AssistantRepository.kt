package com.dox.ara.repository

import android.content.Context
import android.media.AudioManager
import android.net.Uri
import android.os.Environment
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import com.dox.ara.api.AssistantAPI
import com.dox.ara.command.CommandHandler
import com.dox.ara.command.CommandHandlerFactory
import com.dox.ara.command.CommandResponse
import com.dox.ara.dao.AssistantDao
import com.dox.ara.dao.ChatDao
import com.dox.ara.dao.MessageDao
import com.dox.ara.listener.AppLifecycleListener
import com.dox.ara.manager.MediaControllerManager
import com.dox.ara.manager.NotificationChannelManager
import com.dox.ara.model.Assistant
import com.dox.ara.model.Message
import com.dox.ara.requestdto.AssistantRequest
import com.dox.ara.requestdto.MessageRequest
import com.dox.ara.responsedto.VoiceModelsResponse
import com.dox.ara.ui.data.MessageStatus
import com.dox.ara.ui.data.Role
import com.dox.ara.utility.AppTools
import com.dox.ara.utility.AppTools.Companion.decreaseRingVolumeTemporarily
import com.dox.ara.utility.AppTools.Companion.getAudioMode
import com.dox.ara.utility.Constants
import com.dox.ara.utility.Constants.AUDIO_DIR
import com.dox.ara.utility.Constants.BREAK
import com.truecrm.rat.utility.waitForCondition
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.time.delay
import kotlinx.coroutines.withContext
import okhttp3.ResponseBody
import timber.log.Timber
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.time.Duration
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.temporal.ChronoUnit
import javax.inject.Inject

class AssistantRepository @Inject constructor(
    @ApplicationContext private val context: Context,
    private val assistantDao: AssistantDao,
    private val messageDao: MessageDao,
    private val chatDao: ChatDao,
    private val assistantAPI: AssistantAPI,
    private val commandHandlerFactory: CommandHandlerFactory,
    private val mediaControllerManager: MediaControllerManager,
    private val eventRepository: EventRepository,
    private val notificationChannelManager: NotificationChannelManager
){
    private val MAX_BREAKS = 2
    private var breakCount = 0  // Shared

    suspend fun saveAssistant(assistant: Assistant) : Long {
        return assistantDao.insert(assistant)
    }

    suspend fun updateAssistant(assistant: Assistant) {
        assistantDao.update(assistant)
    }

    suspend fun getAvailableAssistantVoiceModels(): VoiceModelsResponse {
        var payload = VoiceModelsResponse(
            edgeVoiceModels = emptyList(),
            rvcVoiceModels = emptyList()
        )
        try {
            val response = assistantAPI.voiceModels()
            if (response.isSuccessful && response.body()?.isSuccess == true) {
                Timber.d("[${::getAvailableAssistantVoiceModels.name}] Response: " +
                        response.body()?.payload)
                response.body()?.payload?.let { payload = it }
            } else {
                Timber.e("[${::getAvailableAssistantVoiceModels.name}] Response: " +
                        if (!response.isSuccessful) response.errorBody().toString() else response.body()
                )
            }
        } catch (e: Exception) {
            Timber.e("[${::getAvailableAssistantVoiceModels.name}] Error: $e")
        }
        return payload
    }

    suspend fun createAssistant(assistantRequest: AssistantRequest): Boolean {
        return try {
            val response = assistantAPI.create(assistantRequest)
            return if (response.isSuccessful && response.body()?.isSuccess == true) {
                Timber.d("[${::createAssistant.name}] Response: " + response.body()?.payload)
                true
            } else {
                Timber.e("[${::createAssistant.name}] Response: " +
                    if (!response.isSuccessful) response.errorBody().toString() else response.body())
                assistantDao.deleteById(assistantRequest.id)
                false
            }
        } catch (e: Exception) {
            Timber.e("[${::createAssistant.name}] [${Constants.Entity.MESSAGE}] Error: $e")
            assistantDao.deleteById(assistantRequest.id)
            false
        }
    }

    suspend fun upgradeAssistant(assistantRequest: AssistantRequest): Boolean {
        return true
    }

    suspend fun parseAndExecuteCommands(content: String, chatId: Long): List<CommandResponse> {
        val commandResponses = mutableListOf<CommandResponse>()

        val commands = try {
            commandHandlerFactory.getCommandHandlers(content)
        } catch (ex: IllegalArgumentException){
            return listOf(CommandHandler.makeResponseFromException(ex))
        }

        for (command in commands) {
            val commandResponse = command.validateAndExecute(chatId)
            if(!commandResponse.isSuccess && commandResponse.getResponse) {
                commandResponses.add(commandResponse.copy(
                    message = commandResponse.message + "\nInform and ask the user before trying to execute the command again"
                ))
            } else {
                commandResponses.add(commandResponse)
            }

            Timber.d("[${::parseAndExecuteCommands.name}] Command response: $commandResponse")

        }
        return commandResponses
    }

    fun getAllCommandUsages() : List<String> {
        return commandHandlerFactory.getAllCommandUsages()
    }

    private suspend fun saveAndUploadSystemMessages(commandResponses: List<CommandResponse>,
                                                   chatId: Long, quotedMessageId: Long?) {
        for ((index, commandResponse) in commandResponses.withIndex()) {
            val messageId = buildAndSaveSystemMessage(
                content = commandResponse.message,
                getResponse = index != commandResponses.lastIndex || !commandResponse.getResponse,
                chatId = chatId,
                quotedMessageId = quotedMessageId
            )

            if(commandResponse.sendResponse) {
                eventRepository.uploadSystemMessage(messageId, chatId)
            }
        }
    }


    suspend fun buildAndSaveSystemMessage(content: String, chatId: Long, getResponse: Boolean,
                                          quotedMessageId: Long?, isTest: Boolean = false): Long {
        val message = Message(
            id = 0,
            chatId = chatId,
            content = content + if (getResponse) BREAK else "",
            timestamp = Instant.now().toEpochMilli(),
            quotedId = quotedMessageId,
            from = Role.SYSTEM,
            status = if(isTest) MessageStatus.BLOCKED else MessageStatus.PENDING
        )

        return messageDao.insert(message)
    }

    suspend fun getAssistantResponse(assistantId: Long, chatId: Long, autoPromptUser: Boolean = false) {
        val message = Message (
            id = 0,
            quotedId = null,
            content = "Typing...",
            timestamp = Instant.now().toEpochMilli(),
            from = Role.ASSISTANT,
            chatId = chatId,
            status = MessageStatus.PENDING
        )

        val messageId = messageDao.insert(message)

        try {
            val messageRequest = MessageRequest(
                id = messageId,
                content = message.content,
                timestamp = message.timestamp,
                from = message.from,
                chatId = message.chatId,
                quotedId = message.quotedId,
                assistantId = assistantId
            )

            val response = assistantAPI.response(messageRequest)
            if (response.isSuccessful && response.body()?.isSuccess == true) {
                Timber.d("[${::getAssistantResponse.name}] Response: " + response.body()?.payload)
                val content = response.body()?.payload?.content ?: ""
                val quotedId = response.body()?.payload?.quotedId
                val updatedMessage = message.copy(
                    id = messageId,
                    content = content,
                    quotedId = quotedId,
                    status = MessageStatus.DELIVERED
                )
                messageDao.update(updatedMessage)

                if(!AppLifecycleListener.isAppInForeground()){
                    val assistant = assistantDao.getById(assistantId)
                    notificationChannelManager.showAssistantResponseNotification(chatId, assistant.name, content)
                }

                messageDao.markAllDeliveredAndFromUserOrSystemAsRead(chatId)

                val currentAudioMode = getAudioMode(context)
                getAssistantResponseAudio(assistantId, updatedMessage, currentAudioMode)

                if(autoPromptUser && currentAudioMode!= AudioManager.MODE_IN_CALL
                    && currentAudioMode!= AudioManager.MODE_IN_COMMUNICATION) {
                    CoroutineScope(Dispatchers.IO).launch {
                        delay(Duration.of(3, ChronoUnit.SECONDS))

                        withContext(Dispatchers.Main) {
                            waitForCondition {
                                mediaControllerManager.isSpeechPlaying()
                            }
                        }

                        eventRepository.startListeningForUserInput(chatId)
                    }
                }

                if(updatedMessage.content.contains(BREAK)){
                    breakCount++
                    if(breakCount < MAX_BREAKS) {
                        getAssistantResponse(assistantId, chatId)
                    } else {
                        val commandResponse = CommandResponse(
                            isSuccess = false,
                            message = "Consecutive usage of $BREAK token exceeded the max limit of $MAX_BREAKS." +
                                    " Stop using it.",
                            getResponse = false
                        )
                        saveAndUploadSystemMessages(listOf(commandResponse), chatId, null)
                    }
                } else {
                    breakCount = 0
                }

                val commandResponses = parseAndExecuteCommands(content, chatId)
                saveAndUploadSystemMessages(commandResponses, chatId, quotedMessageId = messageId)

            } else {
                Timber.e("[${::getAssistantResponse.name}] Response: " +
                    if (!response.isSuccessful) response.errorBody().toString() else response.body())
                messageDao.deleteById(messageId)
            }
        } catch (e: Exception) {
            Timber.e("[${::getAssistantResponse.name}] Error: $e")
            messageDao.deleteById(messageId)
        }
    }

    private fun saveAudioFile(body: ResponseBody, fileName: String) {
        val audioFile = File(AppTools.getPublicDirectory(AUDIO_DIR, Environment.DIRECTORY_MUSIC), fileName)

        try {
            FileOutputStream(audioFile).use { fos ->
                fos.write(body.bytes())
                Timber.d("[${::saveAudioFile.name}] Saved: ${audioFile.absolutePath}")
            }
        } catch (e: IOException) {
            Timber.d("[${::saveAudioFile.name}] Error: $e")
        }
    }

    fun getAudioFile(messageId: Long): String? {
        val audioFile = File(AppTools.getPublicDirectory(AUDIO_DIR, Environment.DIRECTORY_MUSIC), "$messageId.wav")
        return if (audioFile.exists()) audioFile.absolutePath else null
    }

    private suspend fun getAssistantResponseAudio(assistantId: Long, message: Message, audioMode: Int){
        try {
            val response = assistantAPI.responseAudio(assistantId, message.id)
            if (response.isSuccessful && response.body() != null) {
                Timber.d("[${::getAssistantResponseAudio.name}] Response: " + response.body())
                saveAudioFile(response.body()!!, "${message.id}.wav")

                if(audioMode == AudioManager.MODE_IN_CALL || audioMode == AudioManager.MODE_IN_COMMUNICATION){
                    Timber.d("[${::getAssistantResponseAudio.name}] User in call, skipping playback")
                    return
                }

                val chat = chatDao.getChat(message.chatId)
                if (chat.autoPlaybackAudio) {
                    val source = getAudioFile(message.id)
                    if (!source.isNullOrBlank()) {
                        val dateTime = LocalDateTime.ofInstant(
                            Instant.ofEpochMilli(message.timestamp),
                            ZoneId.systemDefault()
                        )
                        val day = dateTime.dayOfMonth
                        val month = dateTime.monthValue
                        val year = dateTime.year

                        val assistant = assistantDao.getById(assistantId)
                        val mediaItem = MediaItem.Builder()
                            .setMediaId("Speech-${message.chatId}")
                            .setUri(source)
                            .setMediaMetadata(
                                MediaMetadata.Builder()
                                    .setArtist(assistant.name)
                                    .setDescription(message.content)
                                    .setRecordingDay(day)
                                    .setRecordingYear(month)
                                    .setRecordingMonth(year)
                                    .setTitle(Constants.APP_NAME)
                                    .setArtworkUri(Uri.parse(assistant.imageUri))
                                    .build()
                            )
                            .build()


                        if(audioMode == AudioManager.MODE_RINGTONE){
                            decreaseRingVolumeTemporarily(context, 10, 25)
                        }

                        // This should run on application thread
                        CoroutineScope(Dispatchers.Main).launch {
                            mediaControllerManager.playSpeech(mediaItem)
                        }
                    } else {
                        Timber.e("[${::getAssistantResponseAudio.name}] Audio file not found: $source")
                    }
                }
            } else {
                Timber.e("[${::getAssistantResponseAudio.name}] Response: " + response.errorBody().toString())
            }
        } catch (e: Exception) {
            Timber.e("[${::getAssistantResponseAudio.name}] Error: $e")
        }
    }


    fun getAssistant(id: Long) = assistantDao.getFlowById(id)

    suspend fun deleteAssistant(id: Long) {
        try {
            val response = assistantAPI.delete(assistantId = id)
            return if (response.isSuccessful) {
                if (response.body()?.isSuccess == false) {
                    Timber.e("[${::deleteAssistant.name}] Response: " + response.body()?.payload)
                    Timber.w("[${::deleteAssistant.name}] Still deleting assistant locally")
                } else {
                    Timber.d("[${::deleteAssistant.name}] Response: " + response.body()?.payload)
                }
                assistantDao.deleteById(id)
            } else {
                Timber.e(
                    "[${::deleteAssistant.name}] Response: " +
                            if (!response.isSuccessful) response.errorBody()
                                .toString() else response.body()
                )
            }
        } catch (e: Exception){
            Timber.e("[${::deleteAssistant.name}] Error: $e")
        }
    }
}