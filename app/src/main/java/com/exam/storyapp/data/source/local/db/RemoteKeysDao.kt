/*
 * Copyright (C) 2022 Budi Ardianata.
 */
package com.exam.storyapp.data.source.local.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.exam.storyapp.data.model.RemoteKeys

@Dao
interface RemoteKeysDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(remoteKey: List<RemoteKeys>): LongArray

    @Query("SELECT * FROM remote_keys WHERE id = :id")
    suspend fun getById(id: String): RemoteKeys?

    @Query("DELETE FROM remote_keys")
    suspend fun clearAll()
}
