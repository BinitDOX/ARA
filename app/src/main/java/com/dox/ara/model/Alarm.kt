package com.dox.ara.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "alarm")
data class Alarm (
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val time: Long,
    val description: String,
    val isActive: Boolean
)
