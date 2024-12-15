package com.dox.ara.viewmodel

import android.Manifest
import android.content.ClipboardManager
import android.content.Context
import android.net.Uri
import androidx.compose.ui.graphics.Color
import androidx.core.graphics.toColorInt
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.dox.ara.listener.SpeechToTextListener
import com.dox.ara.manager.MediaControllerManager
import com.dox.ara.manager.NotificationChannelManager
import com.dox.ara.manager.PermissionManager
import com.dox.ara.manager.SharedPreferencesManager
import com.dox.ara.model.Assistant
import com.dox.ara.model.Chat
import com.dox.ara.model.Message
import com.dox.ara.repository.AssistantRepository
import com.dox.ara.repository.ChatRepository
import com.dox.ara.repository.MessageRepository
import com.dox.ara.ui.data.MessageStatus
import com.dox.ara.ui.data.Role
import com.dox.ara.ui.data.RouteItem
import com.dox.ara.utility.Constants.APP_NAME
import com.dox.ara.utility.Constants.DEFAULT_ASSISTANT_ID_KEY
import com.dox.ara.utility.Constants.DEFAULT_CHAT_ID_KEY
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber
import java.text.SimpleDateFormat
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.util.Locale
import javax.inject.Inject
import kotlin.random.Random

@HiltViewModel
class ChatViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val state: SavedStateHandle,
    private val messageRepository: MessageRepository,
    private val assistantRepository: AssistantRepository,
    private val chatRepository: ChatRepository,
    private val mediaControllerManager: MediaControllerManager,
    private val speechToTextListener: SpeechToTextListener,
    private val permissionManager: PermissionManager,
    private val sharedPreferencesManager: SharedPreferencesManager,
    private val notificationChannelManager: NotificationChannelManager
): ViewModel() {
    private val chatId = state.get<Long>(RouteItem.Chat.arguments.first().name) ?: -1

    private var mediaPlayerMessageId = 0L
    
    private val _assistant = MutableStateFlow<Assistant?>(Assistant.getEmptyAssistant())
    val assistant = _assistant.asStateFlow()

    private val _assistantColor = MutableStateFlow(getRandomColor())
    val assistantColor = _assistantColor.asStateFlow()

    private val _chat = MutableStateFlow<Chat?>(null)
    val chat = _chat.asStateFlow()

    private val _isDefaultAssistant = MutableStateFlow(checkDefaultChat())
    val isDefaultAssistant = _isDefaultAssistant.asStateFlow()

    val speechToTextState = speechToTextListener.state

    val messages: Flow<PagingData<Message>> = messageRepository.getMessages(chatId = chatId)
        .cachedIn(viewModelScope)

    val simpleDateFormat = SimpleDateFormat("hh:mm", Locale.getDefault())

    val colorUser = getRandomColor()
    val colorSystem = getRandomColor()

    init {
        speechToTextListener.initialize()
        getAssistant()
        getChat()
        markAsRead()
        cancelNotifications()
    }

    private fun getAssistant(){
        viewModelScope.launch {
            val assistantId = chatRepository.getAssistantId(chatId) ?: -1
            assistantRepository.getAssistant(assistantId).flowOn(Dispatchers.IO).collect { assistant: Assistant ->
                _assistant.update { assistant }
                try {
                _assistantColor.update {
                    assistant.color.toColorInt().let { Color(it) }
                } } catch (_: Exception) {}
            }
        }
    }

    private fun getChat(){
        viewModelScope.launch {
            chatRepository.getChatFlow(chatId).flowOn(Dispatchers.IO).collect { chat: Chat ->
                _chat.update { chat }
            }
        }
    }

    fun toggleShowSystemMessages() {
        viewModelScope.launch {
            _chat.value?.let { chat ->
                val updatedChat = chat.copy(showSystemMessages = !chat.showSystemMessages)
                _chat.value = updatedChat
                chatRepository.updateChat(updatedChat)
            }
        }
    }

    fun toggleShowFailedMessages() {
        viewModelScope.launch {
            _chat.value?.let { chat ->
                val updatedChat = chat.copy(showFailedMessages = !chat.showFailedMessages)
                _chat.value = updatedChat
                chatRepository.updateChat(updatedChat)
            }
        }
    }

    fun toggleShowCommands() {
        viewModelScope.launch {
            _chat.value?.let { chat ->
                val updatedChat = chat.copy(showCommands = !chat.showCommands)
                _chat.value = updatedChat
                chatRepository.updateChat(updatedChat)
            }
        }
    }

    fun toggleShowTokens() {
        viewModelScope.launch {
            _chat.value?.let { chat ->
                val updatedChat = chat.copy(showTokens = !chat.showTokens)
                _chat.value = updatedChat
                chatRepository.updateChat(updatedChat)
            }
        }
    }

    fun toggleAutoPlaybackAudio() {
        viewModelScope.launch {
            _chat.value?.let { chat ->
                val updatedChat = chat.copy(autoPlaybackAudio = !chat.autoPlaybackAudio)
                _chat.value = updatedChat
                chatRepository.updateChat(updatedChat)
            }
        }
    }

    fun toggleAutoResponses() {
        viewModelScope.launch {
            _chat.value?.let { chat ->
                val updatedChat = chat.copy(autoResponses = !chat.autoResponses)
                _chat.value = updatedChat
                chatRepository.updateChat(updatedChat)
            }
        }
    }


    fun toggleDefaultChat(){
        if(checkDefaultChat()) {
            _isDefaultAssistant.value = false
            sharedPreferencesManager.save(DEFAULT_CHAT_ID_KEY, null)
            sharedPreferencesManager.save(DEFAULT_ASSISTANT_ID_KEY, null)
        } else {
            _isDefaultAssistant.value = true
            sharedPreferencesManager.save(DEFAULT_CHAT_ID_KEY, chatId.toString())
            sharedPreferencesManager.save(DEFAULT_ASSISTANT_ID_KEY, _assistant.value?.id.toString())
        }
    }

    private fun checkDefaultChat(): Boolean {
        return sharedPreferencesManager.get(DEFAULT_CHAT_ID_KEY) ==  chatId.toString()
    }

    fun copyToClipboard(content: String) {
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = android.content.ClipData.newPlainText(APP_NAME, content)
        clipboard.setPrimaryClip(clip)

    }

    fun markAsRead(){
        CoroutineScope(Dispatchers.IO).launch {
            messageRepository.markAllAsRead(chatId)
        }
    }

    private fun cancelNotifications(){
        notificationChannelManager.cancelNotification(chatId.toInt())
    }

    suspend fun sendMessage(messageContent: String, quotedMessageId: Long?) {
        val message = Message(
            id = 0,
            chatId = chatId,
            content = messageContent,
            timestamp = Instant.now().toEpochMilli(),
            quotedId = quotedMessageId,
            from = Role.USER,
            status = MessageStatus.PENDING
        )

        val messageId = messageRepository.saveMessage(message)
        messageRepository.uploadMessage(messageId, chatId)
    }

    suspend fun editMessage(messageId: Long, content: String) {
        CoroutineScope(Dispatchers.IO).launch {
            messageRepository.editMessage(messageId, content)
        }
    }

    fun deleteChat(assistantId: Long) {
        // Deleting assistant will cascade to chat and messages
        CoroutineScope(Dispatchers.IO).launch {
            assistantRepository.deleteAssistant(id = assistantId)
        }
    }

    fun getAssistantResponse(assistantId: Long){
        CoroutineScope(Dispatchers.IO).launch {
            assistantRepository.getAssistantResponse(assistantId, chatId)
        }
    }

    suspend fun deleteMessage(messageId: Long) {
        assistant.value?.id?.let { messageRepository.deleteMessage(it, messageId) }
    }

    suspend fun sendTestMessage(messageContent: String, quotedMessageId: Long?) {
        val message = Message(
            id = 0,
            chatId = chatId,
            content = messageContent,
            timestamp = Instant.now().toEpochMilli(),
            quotedId = quotedMessageId,
            from = Role.USER,
            status = MessageStatus.BLOCKED
        )

        messageRepository.saveMessage(message)
        val commandResponses = assistantRepository.parseAndExecuteCommands(
            messageContent,
            chatId
        )
        for (commandResponse in commandResponses) {
            assistantRepository.buildAndSaveSystemMessage(
                commandResponse.message,  chatId, false, quotedMessageId, true)
        }
    }

    fun getAllTestCommandUsages(): List<String> {
        return assistantRepository.getAllCommandUsages()
            .map {"<TEST> $it"}
    }

    fun playMessageAudio(messageId: Long, content: String, timestamp: Long) {
        try {
            Timber.d("[${::playMessageAudio.name}] Called with messageId: $messageId")
            if (messageId == mediaPlayerMessageId) {
                toggleMessageAudio()
                return
            }

            val source = assistantRepository.getAudioFile(messageId)
            if (!source.isNullOrBlank()) {
                val dateTime = LocalDateTime.ofInstant(Instant.ofEpochMilli(timestamp), ZoneId.systemDefault())
                val day = dateTime.dayOfMonth
                val month = dateTime.monthValue
                val year = dateTime.year

                val mediaItem = MediaItem.Builder()
                    .setMediaId("Speech-${chatId}")
                    .setUri(source)
                    .setMediaMetadata(
                        MediaMetadata.Builder()
                            .setArtist(assistant.value?.name)
                            .setDescription(content)
                            .setRecordingDay(day)
                            .setRecordingYear(month)
                            .setRecordingMonth(year)
                            .setTitle(APP_NAME)
                            .setArtworkUri(Uri.parse(assistant.value?.imageUri))
                            .build()
                        )
                    .build()

                mediaControllerManager.playSpeech(mediaItem)
                mediaPlayerMessageId = messageId
            } else {
                Timber.e("[${::playMessageAudio.name}] Audio file not found for messageId: $messageId")
            }
        } catch (e: Exception) {
            Timber.e("[${::playMessageAudio.name}] Error: $e")
        }
    }

    private fun toggleMessageAudio() {
        mediaControllerManager.toggleSpeechPlayPause()
    }

    fun startListening() {
        if (canRecord()) {
            speechToTextListener.startListening()
        }
    }

    private fun canRecord(): Boolean{
        if(!permissionManager.checkCaptureMic()){
            var permissionDenied = true
            permissionManager.requestSinglePermission(
                permission = Manifest.permission.RECORD_AUDIO,
                onPermissionGranted = { permissionDenied = false },
                onPermissionDenied = { permissionDenied = false }
            )
            return permissionDenied
        }
        return true
    }

    fun stopListening() {
        speechToTextListener.stopListening()
    }

    override fun onCleared() {
        super.onCleared()
        markAsRead()
    }
}


fun getRandomColor() = Color(
    red = Random.nextInt(256),
    green = Random.nextInt(256),
    blue = Random.nextInt(256),
    alpha = 255
)
