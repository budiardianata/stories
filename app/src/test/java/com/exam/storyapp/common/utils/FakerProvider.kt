/*
 * Copyright (C) 2022 Budi Ardianata.
 */
package com.exam.storyapp.common.utils

import com.exam.storyapp.data.model.StoryData
import com.exam.storyapp.data.model.UserData
import com.google.android.gms.maps.model.LatLng
import java.io.File
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody

object FakerProvider {
    const val FAKE_NAME = "budi"
    const val FAKE_EMAIL = "my@budi.web.id"
    const val FAKE_PASSWORD = "123456"
    const val FAKE_DESCRIPTION = "description"
    val FAKE_IMAGE = File("path.webp")
    val FAKE_LOCATION = LatLng(-6.0019502, 106.0662807)

    val inputPart = mapOf(
        "description" to FAKE_DESCRIPTION.toRequestBody("text/plain".toMediaType()),
        "lat" to FAKE_LOCATION.latitude.toString().toRequestBody("text/plain".toMediaType()),
        "lon" to FAKE_LOCATION.longitude.toString().toRequestBody("text/plain".toMediaType()),
    )

    val multipartBody = MultipartBody.Part.createFormData(
        "photo",
        FAKE_IMAGE.name,
        FAKE_IMAGE.asRequestBody("image/webp".toMediaType()),
    )

    fun getUser() = getUserData().mapToDomain()

    fun getUserData() = UserData(
        userId = "user-or7gXNO-HTAEnZE-",
        name = FAKE_NAME,
        token = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9",
    )

    fun generateStoryData(total: Int = 10): List<StoryData> = (1.rangeTo(total)).map { i ->
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
