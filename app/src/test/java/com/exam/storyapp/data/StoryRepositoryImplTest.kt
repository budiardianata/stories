/*
 * Copyright (C) 2022 Budi Ardianata.
 */
package com.exam.storyapp.data

import app.cash.turbine.testIn
import com.exam.storyapp.CoroutineDispatcherRule
import com.exam.storyapp.R
import com.exam.storyapp.common.util.CreateStoryMapping
import com.exam.storyapp.common.util.Resource
import com.exam.storyapp.common.util.StringWrapper
import com.exam.storyapp.common.utils.FakerProvider
import com.exam.storyapp.data.source.local.db.StoryDb
import com.exam.storyapp.data.source.remote.StoryApi
import com.exam.storyapp.data.source.remote.adapter.NetworkResult
import com.exam.storyapp.data.source.remote.response.DefaultResponse
import com.exam.storyapp.data.source.remote.response.StoriesResponse
import io.mockk.*
import io.mockk.impl.annotations.MockK
import java.io.IOException
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@ExperimentalCoroutinesApi
class StoryRepositoryImplTest {
    @get:Rule
    var dispatcherRule = CoroutineDispatcherRule()

    @MockK(relaxed = true)
    lateinit var api: StoryApi

    @MockK(relaxed = true)
    lateinit var database: StoryDb

    @MockK(relaxed = true)
    lateinit var mapper: CreateStoryMapping

    // SUT
    private lateinit var storyRepositoryImpl: StoryRepositoryImpl

    @Before
    fun setUp() {
        MockKAnnotations.init(this)
        storyRepositoryImpl =
            StoryRepositoryImpl(dispatcherRule.testDispatcher, mapper, api, database)
    }

    @Test
    fun `create story - result Error`() = runTest {
        val expectedResult = StringWrapper.Resource(R.string.unknown_error)
        every { mapper.toMap(any(), any()) } returns FakerProvider.inputPart
        every { mapper.toMultipartBody(any()) } returns FakerProvider.multipartBody
        coEvery {
            api.uploadStory(any(), any())
        } returns NetworkResult.Error(402, expectedResult)

        val actualResult = storyRepositoryImpl.createStory(
            FakerProvider.FAKE_DESCRIPTION,
            FakerProvider.FAKE_IMAGE,
            FakerProvider.FAKE_LOCATION,
        )

        assertTrue(actualResult is Resource.Error)
        assertEquals(expectedResult, (actualResult as Resource.Error).exception)
        coVerifyAll {
            api.uploadStory(FakerProvider.inputPart, FakerProvider.multipartBody)
            database wasNot Called
            mapper.toMap(FakerProvider.FAKE_DESCRIPTION, FakerProvider.FAKE_LOCATION)
            mapper.toMultipartBody(FakerProvider.FAKE_IMAGE)
        }
    }

    @Test
    fun `create story - result Exception`() = runTest {
        every { mapper.toMap(any(), any()) } returns FakerProvider.inputPart
        every { mapper.toMultipartBody(any()) } returns FakerProvider.multipartBody
        coEvery {
            api.uploadStory(any(), any())
        } returns NetworkResult.Exception(IOException())

        val actualResult = storyRepositoryImpl.createStory(
            FakerProvider.FAKE_DESCRIPTION,
            FakerProvider.FAKE_IMAGE,
            FakerProvider.FAKE_LOCATION,
        )

        assertEquals(
            Resource.Error(StringWrapper.Resource(R.string.no_connection)),
            actualResult,
        )
        coVerifyAll {
            api.uploadStory(FakerProvider.inputPart, FakerProvider.multipartBody)
            mapper.toMap(FakerProvider.FAKE_DESCRIPTION, FakerProvider.FAKE_LOCATION)
            mapper.toMultipartBody(FakerProvider.FAKE_IMAGE)
        }
    }

    @Test
    fun `create story - result success`() = runTest {
        val expectedResult = "Story created successfully"
        every { mapper.toMap(any(), any()) } returns FakerProvider.inputPart
        every { mapper.toMultipartBody(any()) } returns FakerProvider.multipartBody
        coEvery {
            api.uploadStory(any(), any())
        } returns NetworkResult.Success(DefaultResponse(false, expectedResult))

        val actualResult = storyRepositoryImpl.createStory(
            FakerProvider.FAKE_DESCRIPTION,
            FakerProvider.FAKE_IMAGE,
            FakerProvider.FAKE_LOCATION,
        )
        assertTrue(actualResult is Resource.Success)
        assertEquals(expectedResult, (actualResult as Resource.Success).data)
        coVerifyAll {
            api.uploadStory(FakerProvider.inputPart, FakerProvider.multipartBody)
            mapper.toMap(FakerProvider.FAKE_DESCRIPTION, FakerProvider.FAKE_LOCATION)
            mapper.toMultipartBody(FakerProvider.FAKE_IMAGE)
        }
    }

    @Test
    fun `getStories - result success`() = runTest {
        val givenPerPage = 5
        val expectedResult = FakerProvider.generateStoryData(givenPerPage)
        coEvery { api.getStories(any(), any(), any()) } returns StoriesResponse(
            true,
            "Success",
            expectedResult,
        )

        val actualReceiver = storyRepositoryImpl.getStories(givenPerPage).testIn(this)
        val actualResult = actualReceiver.awaitItem()

        assertTrue(actualResult is Resource.Success)
        assertEquals(
            expectedResult.map { it.toDomain() },
            (actualResult as Resource.Success).data,
        )
        coVerifyAll {
            api.getStories(1, size = givenPerPage)
        }
        actualReceiver.cancelAndConsumeRemainingEvents()
    }

    @Test
    fun `getStories - result Error Exception`() = runTest {
        val expectedResult = "this is a test exception"
        coEvery { api.getStories(any(), any(), any()) } throws Exception(expectedResult)
        val givenPerPage = 5

        val actualReceiver = storyRepositoryImpl.getStories(givenPerPage).testIn(this)
        val actualResult = actualReceiver.awaitItem()

        assertTrue(actualResult is Resource.Error)
        assertEquals(StringWrapper.Dynamic(expectedResult), (actualResult as Resource.Error).exception)
        coVerifyAll {
            api.getStories(1, size = givenPerPage)
        }
        actualReceiver.cancelAndConsumeRemainingEvents()
    }

    @After
    fun tearDown() {
        confirmVerified(api, mapper, database)
        unmockkAll()
    }
}
