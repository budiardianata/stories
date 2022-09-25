/*
 * Copyright (C) 2022 Budi Ardianata.
 */
package com.exam.storyapp.di

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.core.DataStoreFactory
import androidx.datastore.dataStoreFile
import androidx.room.Room
import com.exam.storyapp.common.annotations.IOCoroutineScope
import com.exam.storyapp.common.util.Constant
import com.exam.storyapp.data.source.local.db.RemoteKeysDao
import com.exam.storyapp.data.source.local.db.StoryDao
import com.exam.storyapp.data.source.local.db.StoryDb
import com.exam.storyapp.data.source.local.preference.UserPreference
import com.exam.storyapp.data.source.local.preference.UserPreferenceSerializer
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton
import kotlinx.coroutines.CoroutineScope

@Module
@InstallIn(SingletonComponent::class)
object LocalModule {

    @Singleton
    @Provides
    fun provideUserPreference(
        @ApplicationContext appContext: Context,
        @IOCoroutineScope coroutineScope: CoroutineScope,
    ): DataStore<UserPreference> {
        return DataStoreFactory.create(
            serializer = UserPreferenceSerializer,
            produceFile = { appContext.dataStoreFile(Constant.PREFERENCE_NAME) },
            scope = coroutineScope,
            corruptionHandler = null,
        )
    }

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): StoryDb {
        return Room.databaseBuilder(
            context.applicationContext,
            StoryDb::class.java,
            "story_db",
        ).build()
    }

    @Provides
    fun provideStoryDao(database: StoryDb): StoryDao = database.storyDao()

    @Provides
    fun provideRemoteKeysDao(database: StoryDb): RemoteKeysDao = database.remoteKeysDao()
}
