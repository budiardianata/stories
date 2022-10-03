/*
 * Copyright (C) 2022 Budi Ardianata.
 */
package com.exam.storyapp.data.source.local.db

import androidx.paging.PagingSource
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.exam.storyapp.data.model.StoryData

@Dao
interface StoryDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(stories: List<StoryData>): LongArray

    @Query("SELECT * FROM story")
    fun getStories(): PagingSource<Int, StoryData>

    @Query("DELETE FROM story")
    fun clearAll()
}
