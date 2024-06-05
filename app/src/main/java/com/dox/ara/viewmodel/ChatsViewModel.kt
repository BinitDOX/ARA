package com.dox.ara.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dox.ara.repository.ChatRepository
import com.dox.ara.ui.data.ChatItem
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ChatsViewModel @Inject constructor(
    private val chatRepository: ChatRepository
): ViewModel() {

    private val _chatItems = MutableStateFlow(emptyList<ChatItem>())
    val chatItems = _chatItems.asStateFlow()

    init {
        getChats()
    }

    private fun getChats() {
        viewModelScope.launch {
            chatRepository.getChats().flowOn(Dispatchers.IO).collect { chatItem: List<ChatItem> ->
                _chatItems.update { chatItem }
            }
        }
    }
}
