package com.dox.ara.repository

import com.dox.ara.dao.ChatDao
import com.dox.ara.model.Chat
import com.dox.ara.ui.data.ChatItem
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class ChatRepository @Inject constructor(
    private val chatDao: ChatDao
){
    suspend fun getAssistantId(chatId: Long): Long? {
        return chatDao.getAssistantId(chatId)
    }

    suspend fun saveChat(chat: Chat): Long{
        return chatDao.insert(chat)
    }

    fun getChats(): Flow<List<ChatItem>> {
        return chatDao.getChatItems()
    }

    suspend fun updateChat(chat: Chat) {
        return chatDao.update(chat)
    }

    fun getChat(chatId: Long): Flow<Chat> {
        return chatDao.getChatFlow(chatId)
    }
}