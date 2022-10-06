/*
 * Copyright (C) 2022 Budi Ardianata.
 */
package com.exam.storyapp.data.source.local.remote

import com.exam.storyapp.common.utils.FakerProvider
import com.exam.storyapp.data.source.remote.StoryApi
import com.exam.storyapp.data.source.remote.adapter.NetworkResult
import com.exam.storyapp.data.source.remote.response.DefaultResponse
import com.exam.storyapp.data.source.remote.response.LoginResponse
import com.exam.storyapp.data.source.remote.response.StoriesResponse
import java.io.IOException
import okhttp3.MultipartBody
import okhttp3.RequestBody

enum class TestMode {
    SUCCESS,
    ERROR,
    EMPTY
}

/**
 * Fake StoryApi for testing remote mediator only
 * some of the functions are not implemented and not used in this test
 */
class StoryApiFake(private val testMode: TestMode) : StoryApi {
    override suspend fun register(
        name: String,
        email: String,
        password: String,
    ): NetworkResult<DefaultResponse> { TODO("Not yet implemented on remote mediator") }

    override suspend fun login(email: String, password: String): NetworkResult<LoginResponse> {
        TODO("Not yet implemented on remote mediator")
    }

    override suspend fun uploadStory(
        requestBodies: Map<String, RequestBody>,
        body: MultipartBody.Part,
    ): NetworkResult<DefaultResponse> {
        TODO("Not yet implemented on remote mediator")
    }

    override suspend fun getStories(page: Int, size: Int, location: Int): StoriesResponse {
        return when (testMode) {
            TestMode.SUCCESS -> {
                val stories = FakerProvider.generateStoryData(100)
                StoriesResponse(
                    false,
                    "Success",
                    stories.subList((page - 1) * size, (page - 1) * size + size)
                )
            }
            TestMode.ERROR -> throw IOException()
            TestMode.EMPTY -> StoriesResponse(false, "empty", emptyList())
        }
    }
}
