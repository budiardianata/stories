/*
 * Copyright (C) 2022 Budi Ardianata.
 */
package com.exam.storyapp.data.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.exam.storyapp.domain.model.Story
import java.time.Instant
import kotlinx.serialization.Serializable

@Entity(tableName = "story")
@Serializable
data class StoryData(
    @PrimaryKey val id: String,
    val name: String,
    val description: String,
    @ColumnInfo(name = "photo_url") val photoUrl: String,
    @ColumnInfo(name = "created_at") val createdAt: String,
    val lat: Double = 0.0,
    val lon: Double = 0.0,
) {
    fun toDomain(): Story {
        return Story(
            id = id,
            description = description,
            image = photoUrl,
            createdBy = name,
            createdAt = Instant.parse(createdAt).toEpochMilli(),
            lat = lat,
            lon = lon,
        )
    }
}
