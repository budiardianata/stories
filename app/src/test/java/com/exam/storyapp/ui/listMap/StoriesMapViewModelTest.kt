/*
 * Copyright (C) 2022 Budi Ardianata.
 */
package com.exam.storyapp.ui.listMap

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import app.cash.turbine.testIn
import com.exam.storyapp.CoroutineDispatcherRule
import com.exam.storyapp.common.util.Resource
import com.exam.storyapp.common.util.StringWrapper
import com.exam.storyapp.common.utils.FakerProvider
import com.exam.storyapp.domain.model.UiState
import com.exam.storyapp.domain.repositories.StoryRepository
import io.mockk.*
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@ExperimentalCoroutinesApi
class StoriesMapViewModelTest {
    @get:Rule
    var instantExecutorRule = InstantTaskExecutorRule()

    @get:Rule
    var dispatcherRule = CoroutineDispatcherRule()

    @MockK(relaxUnitFun = true)
    lateinit var storyRepository: StoryRepository

    // SUT
    private lateinit var storiesMapViewModel: StoriesMapViewModel

    @Before
    fun setUp() {
        MockKAnnotations.init(this, relaxUnitFun = true)
        storiesMapViewModel = StoriesMapViewModel(storyRepository)
    }

    @Test
    fun `when map not Loaded - result stories is not null`() = runTest {
        verify {
            storyRepository wasNot Called
        }
    }

    @Test
    fun `when map is loaded - result stories is not null`() = runTest {
        val expectedResult = FakerProvider.generateStoryData(25).map { it.toDomain() }
        every {
            storyRepository.getStories(any(), any(), any())
        } returns flowOf(Resource.Success(expectedResult))

        storiesMapViewModel.dispatchEvent(StoriesMapEvent.MapReady)

        val receiver = storiesMapViewModel.stories.testIn(this)
        val actualResult = receiver.awaitItem()
        assertTrue(actualResult is UiState.Success)
        assertEquals(expectedResult, (actualResult as UiState.Success).data)
        verify { storyRepository.getStories(perPage = 25, withLocation = true) }
        receiver.cancelAndConsumeRemainingEvents()
    }

    @Test
    fun `when map is loaded - result stories error`() = runTest {
        val expectedResult = StringWrapper.Dynamic("error")
        every { storyRepository.getStories(any(), any(), any()) } returns flowOf(
            Resource.Error(StringWrapper.Dynamic("error"))
        )

        storiesMapViewModel.dispatchEvent(StoriesMapEvent.MapReady)

        val receiver = storiesMapViewModel.stories.testIn(this)
        val result = receiver.awaitItem()
        assertTrue(result is UiState.Error)
        assertEquals(expectedResult, (result as UiState.Error).exception)
        verify { storyRepository.getStories(perPage = 25, withLocation = true) }
        receiver.cancelAndConsumeRemainingEvents()
    }

    @Test
    fun `when numLoadedStories change - than verify stories is change`() = runTest {
        val expectedNumLoaded = 10
        val expectedStories = FakerProvider.generateStoryData(expectedNumLoaded).map { it.toDomain() }
        every { storyRepository.getStories(any(), any(), any()) } returns flowOf(
            Resource.Success(expectedStories)
        )

        storiesMapViewModel.dispatchEvent(StoriesMapEvent.StoriesLoaded(expectedNumLoaded)) // default 25
        storiesMapViewModel.dispatchEvent(StoriesMapEvent.MapReady)

        val numLoadedReceiver = storiesMapViewModel.numLoadedStories.testIn(this)
        val storyReceiver = storiesMapViewModel.stories.testIn(this)
        val actualNumLoaded = numLoadedReceiver.awaitItem()
        val actualStories = storyReceiver.awaitItem()
        assertEquals(expectedNumLoaded, actualNumLoaded)
        assertTrue(actualStories is UiState.Success)
        val storiesData = (actualStories as UiState.Success).data
        assertTrue(expectedNumLoaded >= storiesData.size)
        assertEquals(expectedStories, storiesData)
        verify {
            storyRepository.getStories(perPage = actualNumLoaded, withLocation = true)
        }
        numLoadedReceiver.cancelAndConsumeRemainingEvents()
        storyReceiver.cancelAndConsumeRemainingEvents()
    }

    @After
    fun tearDown() {
        confirmVerified(storyRepository)
        unmockkAll()
    }
}
