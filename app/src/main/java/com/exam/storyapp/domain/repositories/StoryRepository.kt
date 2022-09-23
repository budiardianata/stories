/*
 * Copyright (C) 2022 Budi Ardianata.
 */
package com.exam.storyapp.domain.repositories

import android.net.Uri
import androidx.paging.PagingData
import com.exam.storyapp.common.util.Constant
import com.exam.storyapp.common.util.Resource
import com.exam.storyapp.domain.model.Story
import kotlinx.coroutines.flow.Flow

interface StoryRepository {
    fun getPagedStories(): Flow<PagingData<Story>>
    suspend fun getWidgetStories(
        perPage: Int = Constant.PAGING_PER_PAGE,
        page: Int = 1,
    ): Resource<List<Story>>

    suspend fun createStory(description: String, image: Uri): Resource<String>
}
