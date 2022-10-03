/*
 * Copyright (C) 2022 Budi Ardianata.
 */
package com.exam.storyapp.data

import androidx.paging.*
import com.exam.storyapp.R
import com.exam.storyapp.common.annotations.IODispatcher
import com.exam.storyapp.common.util.Constant
import com.exam.storyapp.common.util.CreateStoryMapping
import com.exam.storyapp.common.util.Resource
import com.exam.storyapp.common.util.StringWrapper
import com.exam.storyapp.data.source.StoryRemoteMediator
import com.exam.storyapp.data.source.local.db.StoryDb
import com.exam.storyapp.data.source.remote.StoryApi
import com.exam.storyapp.data.source.remote.adapter.NetworkResult
import com.exam.storyapp.domain.model.Story
import com.exam.storyapp.domain.repositories.StoryRepository
import com.google.android.gms.maps.model.LatLng
import java.io.File
import java.io.IOException
import javax.inject.Inject
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

class StoryRepositoryImpl @Inject constructor(
    @IODispatcher private val ioDispatcher: CoroutineDispatcher,
    private val createStoryMapper: CreateStoryMapping,
    private val remoteSource: StoryApi,
    private val localSource: StoryDb,
) : StoryRepository {
    @OptIn(ExperimentalPagingApi::class)
    override fun getPagedStories(): Flow<PagingData<Story>> = Pager(
        config = PagingConfig(
            pageSize = Constant.PAGING_PER_PAGE,
        ),
        remoteMediator = StoryRemoteMediator(
            remoteSource,
            localSource,
        ),
        pagingSourceFactory = {
            localSource.storyDao().getStories()
        },
    ).flow.map { pagingData ->
        pagingData.map { it.toDomain() }
    }

    override fun getStories(
        perPage: Int,
        page: Int,
        withLocation: Boolean,
    ): Flow<Resource<List<Story>>> = flow {
        try {
            val response = remoteSource.getStories(page, perPage, if (withLocation) 1 else 0)
            if (response.listStory.isNotEmpty()) {
                emit(Resource.Success(response.listStory.map { it.toDomain() }))
            } else {
                emit(Resource.Error(StringWrapper.Resource(R.string.app_name)))
            }
        } catch (e: Exception) {
            e.printStackTrace()
            emit(
                Resource.Error(
                    StringWrapper.Dynamic(
                        e.localizedMessage
                            ?: "Something went wrong",
                    ),
                ),
            )
        }
    }.flowOn(ioDispatcher)

    override suspend fun createStory(
        description: String,
        image: File,
        location: LatLng?,
    ): Resource<String> = withContext(ioDispatcher) {
        try {
            val map = createStoryMapper.toMap(description, location)
            val body = createStoryMapper.toMultipartBody(image)
            return@withContext when (
                val result =
                    remoteSource.uploadStory(map, body)
            ) {
                is NetworkResult.Error -> Resource.Error(result.message!!)
                is NetworkResult.Exception -> {
                    val message = when (result.e) {
                        is IOException -> StringWrapper.Resource(R.string.no_connection)
                        else -> StringWrapper.Resource(R.string.unknown_error)
                    }
                    Resource.Error(message)
                }
                is NetworkResult.Success -> {
                    Resource.Success(result.data.message)
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            val message = e.localizedMessage ?: e.message
            return@withContext if (message.isNullOrEmpty()) {
                Resource.Error(StringWrapper.Resource(R.string.app_name))
            } else {
                Resource.Error(StringWrapper.Dynamic(message))
            }
        }
    }
}
