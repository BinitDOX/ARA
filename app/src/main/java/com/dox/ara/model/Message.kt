package com.dox.ara.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import com.dox.ara.ui.data.MessageStatus
import com.dox.ara.ui.data.Role

@Entity(tableName = "message",
    foreignKeys = [
        ForeignKey(
            entity = Chat::class,
            parentColumns = ["id"],
            childColumns = ["chat_id"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class Message(
    @PrimaryKey(autoGenerate = true)
    var id: Long,
    var quotedId: Long?,
    var content: String,
    val timestamp: Long,
    val from: Role,
    var status: MessageStatus,

    @ColumnInfo(name = "chat_id")
    val chatId: Long
)

