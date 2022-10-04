/*
 * Copyright (C) 2022 Budi Ardianata.
 */
package com.exam.storyapp.common.utils

import com.exam.storyapp.data.model.StoryData
import com.exam.storyapp.data.model.UserData

object FakerProvider {
    const val FAKE_NAME = "budi"
    const val FAKE_EMAIL = "my@budi.web.id"
    const val FAKE_PASSWORD = "123456"

    fun getUserData() = UserData(
        userId = "user-or7gXNO-HTAEnZE-",
        name = FAKE_NAME,
        token = "eyJhbGciOiJIUzI1NiIsInR5cCI6Ikp9",
    )

    fun generateStoryData(total: Int): List<StoryData> = (1.rangeTo(total)).map { i ->
        StoryData(
            id = "story-$i",
            name = "budi $i",
            description = "lorem ipsum $i",
            createdAt = "2021-08-01T00:00:00.000Z",
            photoUrl = "https://story-api.dicoding.dev/images/stories/photos-1648719567500_ssbAAkGs.png",
            lat = -6.0019502,
            lon = 106.0662807,
        )
    }
}
