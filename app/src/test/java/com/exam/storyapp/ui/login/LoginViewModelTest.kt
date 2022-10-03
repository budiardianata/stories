/*
 * Copyright (C) 2022 Budi Ardianata.
 */
package com.exam.storyapp.ui.login

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import app.cash.turbine.testIn
import com.exam.storyapp.CoroutineDispatcherRule
import com.exam.storyapp.R
import com.exam.storyapp.common.util.Resource
import com.exam.storyapp.common.util.StringWrapper
import com.exam.storyapp.common.utils.FakerProvider
import com.exam.storyapp.domain.model.FormState
import com.exam.storyapp.domain.model.UiState
import com.exam.storyapp.domain.repositories.UserRepository
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
class LoginViewModelTest {
    @get:Rule
    var instantExecutorRule = InstantTaskExecutorRule()

    @get:Rule
    var dispatcherRule = CoroutineDispatcherRule()

    @MockK(relaxed = true)
    private lateinit var userRepository: UserRepository

    // SUT
    private lateinit var loginViewModel: LoginViewModel

    @Before
    fun setUp() {
        MockKAnnotations.init(this)
        loginViewModel = LoginViewModel(userRepository)
    }

    @Test
    fun `when performLogin with empty Input - result uiState Error`() = runTest {
        val expectedResult = UiState.Error<String>(StringWrapper.Resource(R.string.input_invalid))

        // act
        loginViewModel.dispatchEvent(LoginEvent.PerformLogin)

        // assert
        val actualReceiver = loginViewModel.loginState.testIn(this)
        val actualResult = actualReceiver.awaitItem()
        assertTrue(actualResult is FormState.Submit)
        val submitState = (actualResult as FormState.Submit).submitState
        assertTrue(submitState is UiState.Error)
        assertEquals(expectedResult, submitState)
        verify { userRepository wasNot Called }
        actualReceiver.cancelAndConsumeRemainingEvents()
    }

    @Test
    fun `when performLogin with valid Input - result uiState Error`() = runTest {
        val expectedResponse = Resource.Error(StringWrapper.Dynamic("User not found"))
        coEvery {
            userRepository.signIn(any(), any())
        } returns expectedResponse

        // act
        loginViewModel.dispatchEvent(LoginEvent.EmailChange(FakerProvider.FAKE_EMAIL))
        loginViewModel.dispatchEvent(LoginEvent.PasswordChange(FakerProvider.FAKE_PASSWORD))
        loginViewModel.dispatchEvent(LoginEvent.PerformLogin)

        // assert
        val actualReceiver = loginViewModel.loginState.testIn(this)
        val actualResult = actualReceiver.awaitItem()
        assertTrue(actualResult is FormState.Submit)
        val submitState = (actualResult as FormState.Submit).submitState
        assertTrue(submitState is UiState.Error)
        assertEquals(UiState.fromResource(expectedResponse), submitState)
        coVerify {
            userRepository.signIn(FakerProvider.FAKE_EMAIL, FakerProvider.FAKE_PASSWORD)
        }
        actualReceiver.cancelAndConsumeRemainingEvents()
    }

    @Test
    fun `performLogin with valid Input - result uiState Success`() = runTest {
        val user = FakerProvider.getUser()
        val expectedResponse = Resource.Success(user)
        coEvery { userRepository.signIn(any(), any()) } returns expectedResponse

        // act
        loginViewModel.dispatchEvent(LoginEvent.EmailChange(FakerProvider.FAKE_EMAIL))
        loginViewModel.dispatchEvent(LoginEvent.PasswordChange(FakerProvider.FAKE_PASSWORD))
        loginViewModel.dispatchEvent(LoginEvent.PerformLogin)

        // assert
        val actualReceiver = loginViewModel.loginState.testIn(this)
        val actualResult = actualReceiver.awaitItem()
        val submitState = (actualResult as FormState.Submit).submitState
        assertTrue(submitState is UiState.Success)
        assertEquals(UiState.Success("Login Success"), submitState)
        coVerifyAll {
            userRepository.signIn(FakerProvider.FAKE_EMAIL, FakerProvider.FAKE_PASSWORD)
            userRepository.saveUserSession(user)
        }
        actualReceiver.cancelAndConsumeRemainingEvents()
    }

    @After
    fun tearDown() {
        confirmVerified(userRepository)
        unmockkAll()
    }
}
