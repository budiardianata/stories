/*
 * Copyright (C) 2022 Budi Ardianata.
 */
package com.exam.storyapp.data.source.local.db

import androidx.room.Database
import androidx.room.RoomDatabase
import com.exam.storyapp.data.model.RemoteKeys
import com.exam.storyapp.data.model.StoryData

@Database(
    entities = [StoryData::class, RemoteKeys::class],
    version = 1,
    exportSchema = true,
)
abstract class StoryDb : RoomDatabase() {
    abstract fun storyDao(): StoryDao
    abstract fun remoteKeysDao(): RemoteKeysDao
}
