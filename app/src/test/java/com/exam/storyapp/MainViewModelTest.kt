/*
 * Copyright (C) 2022 Budi Ardianata.
 */
package com.exam.storyapp

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import app.cash.turbine.testIn
import com.exam.storyapp.common.utils.FakerProvider
import com.exam.storyapp.domain.model.User
import com.exam.storyapp.domain.repositories.UserRepository
import io.mockk.*
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@ExperimentalCoroutinesApi
class MainViewModelTest {
    @get:Rule
    var instantExecutorRule = InstantTaskExecutorRule()

    @get:Rule
    var dispatcherRule = CoroutineDispatcherRule()

    @MockK(relaxed = true)
    private lateinit var userRepository: UserRepository

    @Before
    fun setUp() {
        MockKAnnotations.init(this)
    }

    @Test
    fun `when token is not null - result isLogin true`() = runTest {
        every { userRepository.getCurrentUser() } returns flowOf(FakerProvider.getUser())
        val viewModel = MainViewModel(userRepository)

        // act
        val actualReceiver = viewModel.isLogin.testIn(this)

        // assert
        assertTrue(actualReceiver.awaitItem())
        verify { userRepository.getCurrentUser() }
        actualReceiver.cancelAndConsumeRemainingEvents()
    }

    @Test
    fun `when token is empty - result isLogin false`() = runTest {
        every { userRepository.getCurrentUser() } returns flowOf(User())
        val viewModel = MainViewModel(userRepository)

        // act
        val actualReceiver = viewModel.isLogin.testIn(this)

        // assert
        assertFalse(actualReceiver.awaitItem())
        verify { userRepository.getCurrentUser() }
        actualReceiver.cancelAndConsumeRemainingEvents()
    }

    @Test
    fun `when logOut - verify repository called`() = runTest {
        val viewModel = MainViewModel(userRepository)

        // act
        viewModel.logOut()

        // assert
        coVerify { userRepository.signOut() }
    }

    @After
    fun tearDown() {
        unmockkAll()
    }
}
