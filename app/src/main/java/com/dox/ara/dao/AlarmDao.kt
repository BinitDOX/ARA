package com.dox.ara.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.dox.ara.model.Alarm
import com.dox.ara.model.Assistant
import kotlinx.coroutines.flow.Flow

@Dao
interface AlarmDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(alarm: Alarm): Long

    @Update
    suspend fun update(alarm: Alarm)

    @Delete
    suspend fun delete(alarm: Alarm)

    @Query("SELECT * FROM Alarm WHERE id = :alarmId")
    fun getAlarmFlow(alarmId: Long): Flow<Alarm>

    @Query("SELECT * FROM Alarm WHERE id = :alarmId")
    suspend fun getAlarm(alarmId: Long): Alarm?

    @Query("SELECT * FROM Alarm")
    fun getAllAlarms(): Flow<List<Alarm>>

    @Query("SELECT assistant.* " +
            "FROM assistant " +
            "INNER JOIN chat ON chat.assistant_id = assistant.id " +
            "INNER JOIN alarm ON alarm.chat_id = chat.id " +
            "WHERE alarm.id = :alarmId")
    suspend fun getAssistantByAlarmId(alarmId: Long): Assistant?
}