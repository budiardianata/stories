/*
 * Copyright (C) 2022 Budi Ardianata.
 */
package com.exam.storyapp.ui.home

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.paging.PagingData
import app.cash.turbine.testIn
import com.exam.storyapp.CoroutineDispatcherRule
import com.exam.storyapp.common.extensions.collectData
import com.exam.storyapp.common.utils.FakerProvider
import com.exam.storyapp.domain.repositories.StoryRepository
import com.exam.storyapp.ui.home.adapter.StoryAdapter
import io.mockk.*
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@ExperimentalCoroutinesApi
class HomeViewModelTest {

    @get:Rule
    var dispatcherRule = CoroutineDispatcherRule()

    @get:Rule
    var instantExecutorRule = InstantTaskExecutorRule()

    @MockK(relaxed = true)
    private lateinit var storyRepository: StoryRepository

    @Before
    fun setUp() {
        MockKAnnotations.init(this)
    }

    @Test
    fun `when collect stories - result data is Empty (data does not exit)`() = runTest {
        every { storyRepository.getPagedStories() } returns flowOf(PagingData.empty())
        val viewModel = HomeViewModel(storyRepository)

        val actualReceiver = viewModel.stories.testIn(this)

        val actualResult = actualReceiver.awaitItem().collectData(StoryAdapter.DIFF_CALLBACK)
        assertTrue(actualResult.isEmpty())
        verify {
            storyRepository.getPagedStories()
        }
        actualReceiver.cancelAndConsumeRemainingEvents()
    }

    @Test
    fun `when collect stories - result data is not Null and equal as expected`() = runTest {
        val expectedResult = FakerProvider.generateStoryData(10).map { it.toDomain() }
        every { storyRepository.getPagedStories() } returns flowOf(PagingData.from(expectedResult))
        val viewModel = HomeViewModel(storyRepository)

        val actualReceiver = viewModel.stories.testIn(this)

        val actualResult = actualReceiver.awaitItem().collectData(StoryAdapter.DIFF_CALLBACK)
        assertNotNull(actualResult)
        assertEquals(actualResult.size, expectedResult.size)
        assertEquals(actualResult, expectedResult)
        verifyAll {
            storyRepository.getPagedStories()
        }
        actualReceiver.cancelAndConsumeRemainingEvents()
    }

    @After
    fun tearDown() {
        confirmVerified(storyRepository)
        unmockkAll()
    }
}
