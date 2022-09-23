/*
 * Copyright (C) 2022 Budi Ardianata.
 */
package com.exam.storyapp.data.source.remote.response

import com.exam.storyapp.data.model.StoryData

data class StoriesResponse(
    override val error: Boolean,
    override val message: String,
    val listStory: List<StoryData>,
) : BaseResponse
