/*
 * Copyright (C) 2022 Budi Ardianata.
 */
package com.exam.storyapp.domain.repositories

import androidx.paging.PagingData
import com.exam.storyapp.common.util.Constant
import com.exam.storyapp.common.util.Resource
import com.exam.storyapp.domain.model.Story
import com.google.android.gms.maps.model.LatLng
import java.io.File
import kotlinx.coroutines.flow.Flow

interface StoryRepository {
    fun getPagedStories(): Flow<PagingData<Story>>
    fun getStories(
        perPage: Int = Constant.PAGING_PER_PAGE,
        page: Int = 1,
        withLocation: Boolean = false,
    ): Flow<Resource<List<Story>>>

    suspend fun createStory(description: String, image: File, location: LatLng?): Resource<String>
}
