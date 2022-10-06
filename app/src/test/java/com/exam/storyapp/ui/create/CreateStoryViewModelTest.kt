/*
 * Copyright (C) 2022 Budi Ardianata.
 */
package com.exam.storyapp.ui.create

import app.cash.turbine.testIn
import com.exam.storyapp.CoroutineDispatcherRule
import com.exam.storyapp.R
import com.exam.storyapp.common.util.Resource
import com.exam.storyapp.common.util.StringWrapper
import com.exam.storyapp.common.utils.FakerProvider
import com.exam.storyapp.domain.model.FormState
import com.exam.storyapp.domain.model.UiState
import com.exam.storyapp.domain.repositories.StoryRepository
import io.mockk.*
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@ExperimentalCoroutinesApi
class CreateStoryViewModelTest {
    @get:Rule
    var dispatcherRule = CoroutineDispatcherRule()

    @MockK(relaxed = true)
    lateinit var storyRepository: StoryRepository

    private lateinit var createStoryViewModel: CreateStoryViewModel

    @Before
    fun setUp() {
        MockKAnnotations.init(this)
        createStoryViewModel = CreateStoryViewModel(storyRepository)
    }

    @Test
    fun `when createStory with invalid Input - result Error Local Validation`() = runTest {
        val expectedResult = UiState.Error<StringWrapper>(StringWrapper.Resource(R.string.input_invalid))

        createStoryViewModel.dispatchEvent(CreateStoryEvent.CreateStory)

        val actualReceiver = createStoryViewModel.formState.testIn(this)
        val actualResult = actualReceiver.awaitItem()
        assertTrue(actualResult is FormState.Submit)
        val submitState = (actualResult as FormState.Submit).submitState
        assertTrue(submitState is UiState.Error)
        assertEquals(expectedResult, submitState)
        coVerify { storyRepository wasNot Called }
        actualReceiver.cancelAndConsumeRemainingEvents()
    }

    @Test
    fun `when createStory with valid Input - result Error Server validation`() = runTest {
        val expectedResult = Resource.Error(StringWrapper.Dynamic("Error"))
        coEvery { storyRepository.createStory(any(), any(), any()) } returns expectedResult

        createStoryViewModel.dispatchEvent(CreateStoryEvent.AddImage(FakerProvider.FAKE_IMAGE))
        createStoryViewModel.dispatchEvent(CreateStoryEvent.AddLocation(FakerProvider.FAKE_LOCATION))
        createStoryViewModel.dispatchEvent(CreateStoryEvent.AddDescription(FakerProvider.FAKE_DESCRIPTION))
        createStoryViewModel.dispatchEvent(CreateStoryEvent.CreateStory)

        val actualReceiver = createStoryViewModel.formState.testIn(this)
        val actualResult = actualReceiver.awaitItem()
        assertTrue(actualResult is FormState.Submit)
        val submitState = (actualResult as FormState.Submit).submitState
        assertTrue(submitState is UiState.Error)
        assertEquals(UiState.fromResource(expectedResult), submitState)
        coVerify {
            storyRepository.createStory(
                FakerProvider.FAKE_DESCRIPTION,
                FakerProvider.FAKE_IMAGE,
                FakerProvider.FAKE_LOCATION
            )
        }
        actualReceiver.cancelAndConsumeRemainingEvents()
    }

    @Test
    fun `when createStory with valid Input - result Success`() = runTest {
        val expectedResult = Resource.Success("Success")
        coEvery { storyRepository.createStory(any(), any(), any()) } returns expectedResult

        createStoryViewModel.dispatchEvent(CreateStoryEvent.AddImage(FakerProvider.FAKE_IMAGE))
        createStoryViewModel.dispatchEvent(CreateStoryEvent.AddLocation(FakerProvider.FAKE_LOCATION))
        createStoryViewModel.dispatchEvent(CreateStoryEvent.AddDescription(FakerProvider.FAKE_DESCRIPTION))
        createStoryViewModel.dispatchEvent(CreateStoryEvent.CreateStory)

        val actualReceiver = createStoryViewModel.formState.testIn(this)
        val actualResult = actualReceiver.awaitItem()
        assertTrue(actualResult is FormState.Submit)
        val submitState = (actualResult as FormState.Submit).submitState
        assertTrue(submitState is UiState.Success)
        assertEquals(UiState.fromResource(expectedResult), submitState)
        coVerify {
            storyRepository.createStory(
                FakerProvider.FAKE_DESCRIPTION,
                FakerProvider.FAKE_IMAGE,
                FakerProvider.FAKE_LOCATION
            )
        }
        actualReceiver.cancelAndConsumeRemainingEvents()
    }

    @After
    fun tearDown() {
        confirmVerified(storyRepository)
        unmockkAll()
    }
}
