/*
 * Copyright (C) 2022 Budi Ardianata.
 */
package com.exam.storyapp.data.model

import com.exam.storyapp.domain.model.Story
import java.time.Instant

data class StoryData(
    val id: String,
    val name: String,
    val description: String,
    val photoUrl: String,
    val createdAt: String,
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
