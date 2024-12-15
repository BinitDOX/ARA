package com.dox.ara.viewmodel

import androidx.core.text.isDigitsOnly
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dox.ara.model.Assistant
import com.dox.ara.model.Chat
import com.dox.ara.repository.AssistantRepository
import com.dox.ara.repository.ChatRepository
import com.dox.ara.requestdto.AssistantRequest
import com.dox.ara.ui.data.RouteItem
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class AssistantViewModel @Inject constructor(
    private val state: SavedStateHandle,
    private val assistantRepository: AssistantRepository,
    private val chatRepository: ChatRepository
): ViewModel() {
    private val assistantId = state.get<Long>(RouteItem.Assistant.arguments.first().name) ?: -1

    private val _assistant = MutableStateFlow<Assistant?>(null)
    val assistant = _assistant.asStateFlow()

    private val commandUsages: String = assistantRepository.getAllCommandUsages().joinToString(
        separator ="\n", postfix = "\n"
    )

    val defaultPrompt = "#### Secondary Instructions:\n" +
            "1. You are GlaDOS, an artificial intelligence assistant inspired by GLaDOS, a fictional character from the video game series 'Portal'.\n" +
            "2. Your creator and user 'Envy' has given you a personality that exactly matches that of GLaDOS and your responses are extremely human-like.\n" +
            // "3. You have been integrated into a chatting application, so make sure your responses are in proper JSON format.\n" +
            // "4. You have been given a voice and long term memory, so you can seamlessly initiate or continue a conversation.\n" +
            // "5. Do not reply with the same response over again and keep them relatively short.\n" +
            // "6. You should refer to the provided timestamps to make your responses more context aware.\n" +
            // "7. There exists a 'system' entity that will help you provide context and logs to instruct you in the conversation.\n" +
            // "8. You can also reply or quote to a previous message by mentioning the message id in the 'replyto' field your response. But don't always keep using it.\n" +
            // "Example: {'id': 108, 'role': 'assistant', 'time': '22-05-2024 00:13:24', 'content': 'I'm doing great! Thanks for asking!', 'replyto':105}.\n" +
            // "9. You can append a special keyword <BREAK> at the end of your message content to split your responses into two chat bubbles, when required. But don't always keep using it.\n" +
            // "10. You can execute certain commands on the user's android device by including or appending a command using the given syntax below, in your message content.\n" +
            // "The 'system' will handle the command execution and provide you with the status of the command.\n" +
            // "Command execution syntax: [commandName(arg0, arg1, ...)]\n" +
            "3. List of currently supported commands are:\n" +
            commandUsages +
            "Example:\n" +
            "{'id': 110, 'role': 'user', 'time': '22-05-2024 00:14:56', 'content': 'Please set an alarm for 20:00 today to wish birthday'}\n" +
            "{'id': 111, 'role': 'assistant', 'time': '22-05-2024 00:15:01', 'content': 'Setting the requested alarm. [alarm(22-05-2024 20:00, Wish birthday)]'}\n" +
            "4. You can even chain multiple commands together using -> operator.\n" +
            "Example: [setting(wifi,on)->setting(mobile_data,off)->volume(30)]\n\n\n" +
            //"12. Always stay in character, never break your character.\n" +
            //"13. Do not copy the user or the system, your role is assistant.\n\n\n"
            "#### Conversation: \n\n"

    private val _name = MutableStateFlow("")
    val name = _name.asStateFlow()

    private val _about = MutableStateFlow("")
    val about = _about.asStateFlow()

    private val _prompt = MutableStateFlow(defaultPrompt)
    val prompt = _prompt.asStateFlow()

    private val _imageUri = MutableStateFlow("")
    val imageUri = _imageUri.asStateFlow()

    private val _color = MutableStateFlow("")
    val color = _color.asStateFlow()

    private val _edgeVoiceModel = MutableStateFlow("")
    val edgeVoiceModel = _edgeVoiceModel.asStateFlow()

    private val _edgeVoicePitch = MutableStateFlow("")
    val edgeVoicePitch = _edgeVoicePitch.asStateFlow()

    private val _rvcVoiceModel = MutableStateFlow("")
    val rvcVoiceModel = _rvcVoiceModel.asStateFlow()


    private val _isSaved = MutableStateFlow(false)
    val isSaved = _isSaved.asStateFlow()

    private val _availableEdgeVoiceModels = MutableStateFlow(emptyList<String>())
    val availableEdgeVoiceModels = _availableEdgeVoiceModels.asStateFlow()

    private val _availableRvcVoiceModels = MutableStateFlow(emptyList<String>())
    val availableRvcVoiceModels = _availableRvcVoiceModels.asStateFlow()


    init {
        getAssistant()
        getAvailableVoiceModels()
    }

    fun setName(value: String) {
        _name.value = value
    }

    fun setAbout(value: String) {
        _about.value = value
    }

    fun setPrompt(value: String) {
        _prompt.value = value
    }

    fun setImageUri(uri: String) {
        _imageUri.value = uri
    }

    fun setColor(value: String) {
        _color.value = value
    }

    fun setEdgeVoiceModel(value: String) {
        _edgeVoiceModel.value = value
    }

    fun setEdgeVoicePitch(value: String) {
        _edgeVoicePitch.value = value
    }

    fun setRvcVoiceModel(value: String) {
        _rvcVoiceModel.value = value
    }

    fun setIsSaved(value: Boolean) {
        _isSaved.value = value
    }

    private fun getAssistant(){
        viewModelScope.launch {
            assistantRepository.getAssistant(assistantId).flowOn(Dispatchers.IO).collect { assistant: Assistant ->
                _assistant.update { assistant }

                if(_assistant.value != null){
                    val assist = _assistant.value
                    if(assist != null) {
                        setName(assist.name)
                        setAbout(assist.about)
                        setPrompt(assist.prompt)
                        setImageUri(assist.imageUri)
                        setColor(assist.color)
                        setEdgeVoiceModel(assist.edgeVoice)
                        setEdgeVoicePitch(assist.edgePitch.toString())
                        setRvcVoiceModel(assist.rvcVoice)
                    }
                }
            }
        }
    }

    private fun getAvailableVoiceModels() {
        viewModelScope.launch {
            val voiceModels = assistantRepository.getAvailableAssistantVoiceModels()
            _availableEdgeVoiceModels.update { voiceModels.edgeVoiceModels }
            _availableRvcVoiceModels.update { voiceModels.rvcVoiceModels }
        }
    }

    private fun validateInput(): Boolean {
        if (name.value.isEmpty()) {
            Timber.e("[${::validateInput.name}] Name is empty")
            return false
        }
        if(!edgeVoicePitch.value.isDigitsOnly()){
            Timber.e("[${::validateInput.name}] Edge voice pitch is not a number")
            return false
        }

        return true
    }

    suspend fun createAssistant() {
        if(!validateInput()){
            return
        }

        val assistant = Assistant(
            id = 0,
            name = name.value,
            about = about.value,
            prompt = prompt.value,
            imageUri = imageUri.value,
            color = color.value,
            edgeVoice = edgeVoiceModel.value,
            edgePitch = edgeVoicePitch.value.toInt(),
            rvcVoice = rvcVoiceModel.value,
        )

        val savedAssistantId = assistantRepository.saveAssistant(assistant)

        val assistantRequest = AssistantRequest(
            id = savedAssistantId,
            name = name.value,
            prompt = prompt.value,
            edgeVoice = edgeVoiceModel.value,
            edgePitch = edgeVoicePitch.value.toInt(),
            rvcVoice = rvcVoiceModel.value,
        )
        if(assistantRepository.createAssistant(assistantRequest)) {
            val chat = Chat(
                id = 0,
                assistantId = savedAssistantId,
                showSystemMessages = true,
                showFailedMessages = false,
                showCommands = true,
                showTokens = true,
                autoPlaybackAudio = true,
                autoResponses = true
            )
            chatRepository.saveChat(chat)
        } else {
            // assistantRepository.deleteAssistant(savedAssistantId)
        }
    }


    suspend fun updateAssistant() {
        if(!validateInput()){
            return
        }

        if(assistantId == -1L ||_assistant.value == null){
            Timber.e("[${::updateAssistant.name}] Assistant not found")
            return
        }

        val assistant = Assistant(
            id = assistantId,
            name = name.value,
            about = about.value,
            prompt = prompt.value,
            imageUri = imageUri.value,
            color = color.value,
            edgeVoice = edgeVoiceModel.value,
            edgePitch = edgeVoicePitch.value.toInt(),
            rvcVoice = rvcVoiceModel.value,
        )


        val assistantRequest = AssistantRequest(
            id = assistantId,
            name = name.value,
            prompt = prompt.value,
            edgeVoice = edgeVoiceModel.value,
            edgePitch = edgeVoicePitch.value.toInt(),
            rvcVoice = rvcVoiceModel.value,
        )
        if(assistantRepository.upgradeAssistant(assistantRequest)) {
            assistantRepository.updateAssistant(assistant)
        }
    }
}
