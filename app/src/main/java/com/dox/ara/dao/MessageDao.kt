package com.dox.ara.dao

import androidx.paging.PagingSource
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.dox.ara.model.Message

@Dao
interface MessageDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(message: Message): Long

    @Update
    suspend fun update(message: Message)

    @Query("UPDATE MESSAGE SET status = 'READ'" +
            "WHERE chat_id = :chatId AND status = 'DELIVERED' AND `from` = 'ASSISTANT'")
    suspend fun markAllDeliveredAndFromAIAsRead(chatId: Long)

    @Query("UPDATE MESSAGE SET status = 'READ'" +
            "WHERE chat_id = :chatId AND status = 'DELIVERED' AND (`from` = 'SYSTEM' OR `from` = 'USER')")
    suspend fun markAllDeliveredAndFromUserOrSystemAsRead(chatId: Long)

    @Query(
        "SELECT Message.* " +
            "FROM Message " +
            "INNER JOIN Chat ON Message.chat_id = Chat.id " +
            "WHERE Message.chat_id = :chatId " +
            "ORDER BY Message.timestamp DESC"
    )
    fun getMessages(chatId: Long): PagingSource<Int, Message>

    @Query("SELECT * FROM MESSAGE WHERE id = :id")
    suspend fun getById(id: Long): Message?

    @Query("DELETE FROM Message WHERE id = :id")
    suspend fun deleteById(id: Long)

}