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
import com.exam.storyapp.data.source.local.db.StoryDb
import com.exam.storyapp.data.source.local.preference.UserPreference
import com.exam.storyapp.data.source.local.preference.UserPreferenceSerializer
import dagger.Module
import dagger.Provides
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import dagger.hilt.testing.TestInstallIn
import javax.inject.Singleton
import kotlin.random.Random
import kotlinx.coroutines.CoroutineScope

@Module
@TestInstallIn(
    components = [SingletonComponent::class],
    replaces = [LocalModule::class]
)
object TestLocalModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): StoryDb {
        return Room.inMemoryDatabaseBuilder(
            context.applicationContext,
            StoryDb::class.java
        ).allowMainThreadQueries().build()
    }

    @Singleton
    @Provides
    fun provideUserPreference(
        @ApplicationContext appContext: Context,
        @IOCoroutineScope coroutineScope: CoroutineScope,
    ): DataStore<UserPreference> {
        val random = Random.nextInt()
        return DataStoreFactory.create(
            serializer = UserPreferenceSerializer,
            produceFile = { appContext.dataStoreFile("TEST_PREFERENCE_$random") },
            scope = coroutineScope,
            corruptionHandler = null
        )
    }
}
