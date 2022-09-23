/*
 * Copyright (C) 2022 Budi Ardianata.
 */
package com.exam.storyapp.data.source

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.exam.storyapp.common.util.Constant
import com.exam.storyapp.data.source.remote.StoryApi
import com.exam.storyapp.domain.model.Story

class StoryDataSource(
    private val storyApi: StoryApi,
) : PagingSource<Int, Story>() {

    override fun getRefreshKey(state: PagingState<Int, Story>): Int? = state.anchorPosition?.let {
        val anchorPage = state.closestPageToPosition(it)
        anchorPage?.prevKey?.plus(1) ?: anchorPage?.nextKey?.minus(1)
    }

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, Story> {
        return try {
            val position = params.key ?: Constant.PAGING_INITIAL_PAGE
            val response = storyApi.getStories(page = position, size = params.loadSize)
            val stories = response.listStory.map { it.toDomain() }
            LoadResult.Page(
                data = stories,
                prevKey = if (position == Constant.PAGING_INITIAL_PAGE) null else position - 1,
                nextKey = if (stories.isEmpty()) null else position + 1,
            )
        } catch (e: Exception) {
            LoadResult.Error(e)
        }
    }
}
