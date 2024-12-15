package com.dox.ara.module

import android.content.Context
import androidx.room.Room
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.dox.ara.dao.AlarmDao
import com.dox.ara.dao.AssistantDao
import com.dox.ara.dao.ChatDao
import com.dox.ara.dao.MessageDao
import com.dox.ara.database.AppDatabase
import com.dox.ara.utility.Constants.APP_ID
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@InstallIn(SingletonComponent::class)
@Module
class DatabaseModule {

    @Singleton
    @Provides
    fun provideAppDatabase(@ApplicationContext context: Context): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "${APP_ID}_database"
        ).addMigrations(MIGRATION_1_2).build()
    }

    // Destructive migration for alarms
    // Can be non-destructive but needs a temp-table and copying of data
    private val MIGRATION_1_2 = object : Migration(1, 2) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL("DROP TABLE IF EXISTS alarm")

            db.execSQL("""
            CREATE TABLE alarm (
                id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                time INTEGER NOT NULL,
                description TEXT NOT NULL,
                isActive INTEGER NOT NULL,
                volume INTEGER NOT NULL DEFAULT 50,
                chat_id INTEGER,
                FOREIGN KEY (chat_id) REFERENCES chat(id) ON DELETE CASCADE
            )
        """)
        }
    }


    @Singleton
    @Provides
    fun provideAssistantDao(appDatabase: AppDatabase): AssistantDao {
        return appDatabase.assistantDao()
    }

    @Singleton
    @Provides
    fun provideChatDao(appDatabase: AppDatabase): ChatDao {
        return appDatabase.chatDao()
    }

    @Singleton
    @Provides
    fun provideMessageDao(appDatabase: AppDatabase): MessageDao {
        return appDatabase.messageDao()
    }

    @Singleton
    @Provides
    fun provideAlarmDao(appDatabase: AppDatabase): AlarmDao {
        return appDatabase.alarmDao()
    }
}