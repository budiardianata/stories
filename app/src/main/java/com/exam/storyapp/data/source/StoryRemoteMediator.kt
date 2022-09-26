/*
 * Copyright (C) 2022 Budi Ardianata.
 */
package com.exam.storyapp.data.source

import androidx.paging.ExperimentalPagingApi
import androidx.paging.LoadType
import androidx.paging.PagingState
import androidx.paging.RemoteMediator
import androidx.room.withTransaction
import com.exam.storyapp.common.util.Constant
import com.exam.storyapp.data.model.RemoteKeys
import com.exam.storyapp.data.model.StoryData
import com.exam.storyapp.data.source.local.db.StoryDb
import com.exam.storyapp.data.source.remote.StoryApi
import java.io.IOException
import retrofit2.HttpException

@OptIn(ExperimentalPagingApi::class)
class StoryRemoteMediator constructor(
    private val storyApi: StoryApi,
    private val storyDb: StoryDb,
) : RemoteMediator<Int, StoryData>() {

    override suspend fun initialize(): InitializeAction = InitializeAction.LAUNCH_INITIAL_REFRESH

    override suspend fun load(
        loadType: LoadType,
        state: PagingState<Int, StoryData>,
    ): MediatorResult {
        val page = when (loadType) {
            LoadType.REFRESH -> {
                val remoteKeys = getRemoteKeyClosestToCurrentPosition(state)
                remoteKeys?.nextKey?.minus(1) ?: 1
            }
            LoadType.PREPEND -> {
                val remoteKeys = getRemoteKeyForFirstItem(state)
                val prevKey = remoteKeys?.prevKey ?: return MediatorResult.Success(
                    endOfPaginationReached = remoteKeys != null
                )
                prevKey
            }
            LoadType.APPEND -> {
                val remoteKeys = getRemoteKeysForLastItem(state)
                val nextKey = remoteKeys?.nextKey ?: return MediatorResult.Success(
                    endOfPaginationReached = remoteKeys != null
                )
                nextKey
            }
        }
        try {
            val apiResponse = storyApi.getStories(page = page, size = state.config.pageSize)

            val dataList = apiResponse.listStory
            val endOfPaginationReached = dataList.isEmpty()
            storyDb.withTransaction {
                // clear all tables in the database
                if (loadType == LoadType.REFRESH) {
                    storyDb.remoteKeysDao().clearAll()
                    storyDb.storyDao().clearAll()
                }
                val prevKey = if (page == Constant.PAGING_INITIAL_PAGE) null else page - 1
                val nextKey = if (endOfPaginationReached) null else page + 1
                val keys = dataList.map {
                    RemoteKeys(id = it.id, prevKey = prevKey, nextKey = nextKey)
                }
                storyDb.remoteKeysDao().insert(keys)
                storyDb.storyDao().insert(dataList)
            }
            return MediatorResult.Success(endOfPaginationReached = endOfPaginationReached)
        } catch (exception: IOException) {
            return MediatorResult.Error(exception)
        } catch (exception: HttpException) {
            return MediatorResult.Error(exception)
        }
    }

    /**
     * Get RemoteKeys for last item from local database
     *
     * @param state PagingState
     */
    private suspend fun getRemoteKeysForLastItem(state: PagingState<Int, StoryData>): RemoteKeys? {
        return state.pages.lastOrNull { it.data.isNotEmpty() }?.data?.lastOrNull()?.let { data ->
            storyDb.remoteKeysDao().getById(data.id)
        }
    }

    /**
     * Get RemoteKeys for first item from local database
     *
     * @param state PagingState
     */
    private suspend fun getRemoteKeyForFirstItem(state: PagingState<Int, StoryData>): RemoteKeys? {
        return state.pages.firstOrNull { it.data.isNotEmpty() }?.data?.firstOrNull()?.let { data ->
            storyDb.remoteKeysDao().getById(data.id)
        }
    }

    /**
     * Get RemoteKeys for closest to current position from local database
     *
     * @param state PagingState
     */
    private suspend fun getRemoteKeyClosestToCurrentPosition(state: PagingState<Int, StoryData>): RemoteKeys? {
        return state.anchorPosition?.let { position ->
            state.closestItemToPosition(position)?.id?.let { id ->
                storyDb.remoteKeysDao().getById(id)
            }
        }
    }
}
