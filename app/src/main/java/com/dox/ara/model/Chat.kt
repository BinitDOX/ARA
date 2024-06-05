package com.dox.ara.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity(tableName = "chat",
    foreignKeys = [ForeignKey(entity = Assistant::class,
        parentColumns = ["id"],
        childColumns = ["assistant_id"],
        onDelete = ForeignKey.CASCADE)])
data class Chat (
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val showSystemMessages: Boolean,
    val autoPlaybackAudio: Boolean,

    @ColumnInfo(name = "assistant_id")
    val assistantId: Long
)