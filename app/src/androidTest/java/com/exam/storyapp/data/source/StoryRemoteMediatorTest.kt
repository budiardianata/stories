/*
 * Copyright (C) 2022 Budi Ardianata.
 */
package com.exam.storyapp.data.source

import androidx.paging.*
import com.exam.storyapp.data.model.StoryData
import com.exam.storyapp.data.source.local.db.StoryDb
import com.exam.storyapp.data.source.local.remote.StoryApiFake
import com.exam.storyapp.data.source.local.remote.TestMode
import com.exam.storyapp.data.source.remote.StoryApi
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import java.io.IOException
import javax.inject.Inject
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test

/**
 * testing the paging3 library with remote mediator
 * test for [StoryRemoteMediator] using [StoryApiFake] as a fake of [StoryApi]
 * local database injected With Hilt using 'TestLocalModule'
 */
@ExperimentalCoroutinesApi
@ExperimentalPagingApi
@HiltAndroidTest
class StoryRemoteMediatorTest {
    @get:Rule(order = 0)
    var hiltRule = HiltAndroidRule(this)

    @Inject
    lateinit var database: StoryDb

    private val pagingState = PagingState<Int, StoryData>(
        listOf(),
        null,
        PagingConfig(10),
        10
    )

    @Before
    fun setUp() { hiltRule.inject() }

    @Test
    fun refreshLoad_then_returnsErrorResult_IOException() = runBlocking {
        val fakeApi: StoryApi = StoryApiFake(TestMode.ERROR)
        val storyRemoteMediator = StoryRemoteMediator(fakeApi, database)

        val result = storyRemoteMediator.load(LoadType.REFRESH, pagingState)

        assertTrue(result is RemoteMediator.MediatorResult.Error)
        assertTrue((result as RemoteMediator.MediatorResult.Error).throwable is IOException)
    }

    @Test
    fun refreshLoad_then_returnSuccessResult_noMoreData() = runBlocking {
        val fakeApi: StoryApi = StoryApiFake(TestMode.EMPTY)
        val storyRemoteMediator = StoryRemoteMediator(fakeApi, database)

        val result = storyRemoteMediator.load(LoadType.REFRESH, pagingState)

        assertTrue(result is RemoteMediator.MediatorResult.Success)
        assertTrue((result as RemoteMediator.MediatorResult.Success).endOfPaginationReached)
    }

    @Test
    fun refreshLoad_then_returnSuccessResult_moreDataIsPresent() = runBlocking {
        val fakeApi: StoryApi = StoryApiFake(TestMode.SUCCESS)
        val storyRemoteMediator = StoryRemoteMediator(fakeApi, database)

        val result = storyRemoteMediator.load(LoadType.REFRESH, pagingState)

        assertTrue(result is RemoteMediator.MediatorResult.Success)
        assertFalse((result as RemoteMediator.MediatorResult.Success).endOfPaginationReached)
    }

    @After
    fun tearDown() {
        database.apply {
            clearAllTables()
            close()
        }
    }
}
