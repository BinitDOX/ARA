package com.dox.ara.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity(
    tableName = "alarm",
    foreignKeys = [
        ForeignKey(
            entity = Chat::class,
            parentColumns = ["id"],
            childColumns = ["chat_id"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class Alarm(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val time: Long,
    val description: String,
    val isActive: Boolean,
    val volume: Int,

    @ColumnInfo("chat_id")
    val chatId: Long?
)
