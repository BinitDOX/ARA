package com.dox.ara.repository

import android.content.Context
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.work.Constraints
import androidx.work.Data
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.dox.ara.api.MessageAPI
import com.dox.ara.dao.ChatDao
import com.dox.ara.dao.MessageDao
import com.dox.ara.model.Message
import com.dox.ara.requestdto.MessageRequest
import com.dox.ara.ui.data.MessageStatus
import com.dox.ara.ui.data.Role
import com.dox.ara.utility.Constants
import com.dox.ara.worker.MessageWorker
import com.truecrm.rat.utility.getWorkerName
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import timber.log.Timber
import javax.inject.Inject

class MessageRepository @Inject constructor(
    @ApplicationContext private val context: Context,
    private val messageDao: MessageDao,
    private val chatDao: ChatDao,
    private val messageAPI: MessageAPI
){

    suspend fun saveMessage(message: Message): Long{
        return messageDao.insert(message)
    }

    suspend fun updateMessage(message: Message){
        messageDao.update(message)
    }

    suspend fun markAllAsRead(chatId: Long){
        messageDao.markAllDeliveredAndFromAIAsRead(chatId)
    }

    suspend fun getMessageById(id: Long): Message? = messageDao.getById(id)

    suspend fun getAssistantLastMessageFromEveryChat(): List<Message> = messageDao.getAssistantLastMessageFromEveryChat()

    fun getMessages(chatId: Long): Flow<PagingData<Message>> {
        val pagingConfig = PagingConfig(
            pageSize = 20,
            prefetchDistance = 20,
            enablePlaceholders = false
        )

        val messagePager = Pager(
            config = pagingConfig,
            pagingSourceFactory = { messageDao.getMessages(chatId) }
        )
        return messagePager.flow
    }

    fun uploadMessage(messageId: Long, chatId: Long) {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        val workerName = getWorkerName("${Role.USER}_${Constants.Entity.MESSAGE}_$chatId")

        val workRequest = OneTimeWorkRequestBuilder<MessageWorker>()
            .addTag(workerName)
            .setConstraints(constraints)
            .setInputData(
                Data.Builder()
                .putLong(MessageWorker.ID_KEY, messageId)
                .build())
            .build()

        WorkManager.getInstance(context).enqueueUniqueWork(
            workerName, ExistingWorkPolicy.APPEND_OR_REPLACE, workRequest)

        Timber.i("[${::uploadMessage.name}] $workerName enqueued")
    }

    suspend fun editMessage(messageId: Long, content: String){
        val message = getMessageById(messageId)
        if(message == null) {
            Timber.e("[${::editMessage.name}] Message not found")
            return
        }

        val assistantId = chatDao.getAssistantId(message.chatId)
        if(assistantId == null) {
            Timber.e("[${::editMessage.name}] Assistant not found")
            return
        }

        val updatedMessage = message.copy(content = content)
        if(message.status == MessageStatus.PENDING ||
            message.status == MessageStatus.FAILED ||
            message.status == MessageStatus.BLOCKED){
            messageDao.update(updatedMessage)
            return
        }

        val messageRequest = MessageRequest(
            id = messageId,
            assistantId = assistantId,
            chatId = message.chatId,
            content = content,
            from = message.from,
            timestamp = message.timestamp,
            quotedId = message.quotedId,
        )

        try {
            val response = messageAPI.edit(messageRequest)
            if (response.isSuccessful && response.body()?.isSuccess == true) {
                Timber.d("[${::editMessage.name}] Response: " + response.body()?.payload)
                messageDao.update(updatedMessage)
            } else {
                Timber.e("[${::editMessage.name}] Response: " +
                    if (!response.isSuccessful) response.errorBody().toString() else response.body())
            }
        } catch (e: Exception) {
            Timber.e("[${::editMessage.name}] Error: $e")
        }
    }

    suspend fun deleteMessage(assistantId: Long, messageId: Long){
        val message = getMessageById(messageId)
        if(message?.status == MessageStatus.PENDING ||
            message?.status == MessageStatus.FAILED ||
            message?.status == MessageStatus.BLOCKED){
            messageDao.deleteById(messageId)
            return
        }

        try {
            val response = messageAPI.delete(assistantId = assistantId, messageId = messageId)
            return if (response.isSuccessful && response.body()?.isSuccess == true) {
                Timber.d("[${::deleteMessage.name}] Response: " + response.body()?.payload)
                messageDao.deleteById(messageId)
            } else {
                Timber.e("[${::deleteMessage.name}] Response: " +
                    if (!response.isSuccessful) response.errorBody().toString() else response.body())
            }
        } catch (e: Exception) {
            Timber.e("[${::deleteMessage.name}] Error: $e")
        }
    }
}