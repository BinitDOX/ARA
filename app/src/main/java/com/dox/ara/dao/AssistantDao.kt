package com.dox.ara.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.dox.ara.model.Assistant
import kotlinx.coroutines.flow.Flow

@Dao
interface AssistantDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(assistant: Assistant): Long

    @Update
    suspend fun update(assistant: Assistant)

    @Query("SELECT * FROM Assistant")
    suspend fun getAll(): List<Assistant>

    @Query("SELECT * FROM Assistant WHERE id = :id")
    fun getFlowById(id: Long): Flow<Assistant>

    @Query("SELECT * FROM Assistant WHERE id = :id")
    fun getById(id: Long): Assistant

    @Query("DELETE FROM Assistant WHERE id = :id")
    suspend fun deleteById(id: Long)
}