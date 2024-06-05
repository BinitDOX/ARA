package com.truecrm.rat.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.dox.ara.dao.AlarmDao
import com.dox.ara.dao.AssistantDao
import com.dox.ara.dao.ChatDao
import com.dox.ara.dao.MessageDao
import com.dox.ara.model.Alarm
import com.dox.ara.model.Assistant
import com.dox.ara.model.Chat
import com.dox.ara.model.Message

@Database(entities = [Assistant::class, Chat::class,
    Message::class, Alarm::class],
    version = 1,
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun assistantDao(): AssistantDao
    abstract fun chatDao(): ChatDao
    abstract fun messageDao(): MessageDao
    abstract fun alarmDao(): AlarmDao
}