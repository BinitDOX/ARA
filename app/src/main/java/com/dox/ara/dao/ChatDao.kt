package com.dox.ara.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.dox.ara.model.Chat
import com.dox.ara.ui.data.ChatItem
import kotlinx.coroutines.flow.Flow

@Dao
interface ChatDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(chat: Chat): Long

    @Update
    suspend fun update(chat: Chat)

    @Query("SELECT * FROM Chat WHERE id = :chatId")
    fun getChatFlow(chatId: Long): Flow<Chat>

    @Query("SELECT * FROM Chat WHERE id = :chatId")
    suspend fun getChat(chatId: Long): Chat

    @Query(
        "SELECT Chat.id, Assistant.name, Assistant.imageUri, Message.content, " +
                "strftime('%m-%d %H:%M', MAX(Message.timestamp) / 1000, 'unixepoch') AS date, " +
                "Message.`from`, Message.status, COUNT(CASE WHEN Message.`from` = 'ASSISTANT' AND Message.status = 'DELIVERED' THEN 1 END) AS unreadCount " +
            "FROM Assistant " +
            "INNER JOIN Chat ON Assistant.id = Chat.assistant_id " +
            "LEFT JOIN Message ON Chat.id = Message.chat_id " +
            "GROUP BY Assistant.id " +
            "ORDER BY Message.timestamp DESC ")
    fun getChatItems(): Flow<List<ChatItem>>

    @Query("SELECT assistant_id FROM Chat WHERE id = :chatId")
    suspend fun getAssistantId(chatId: Long): Long?
}