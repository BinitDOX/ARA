package com.dox.ara.module

import android.content.Context
import androidx.room.Room
import com.dox.ara.dao.AlarmDao
import com.dox.ara.dao.AssistantDao
import com.dox.ara.dao.ChatDao
import com.dox.ara.dao.MessageDao
import com.dox.ara.utility.Constants.APP_ID
import com.truecrm.rat.database.AppDatabase
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
        ).build()
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