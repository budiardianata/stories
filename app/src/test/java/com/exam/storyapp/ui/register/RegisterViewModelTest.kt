/*
 * Copyright (C) 2022 Budi Ardianata.
 */
package com.exam.storyapp.ui.register

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
import org.junit.*
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue

@ExperimentalCoroutinesApi
class RegisterViewModelTest {
    @get:Rule
    var instantExecutorRule = InstantTaskExecutorRule()

    @get:Rule
    var dispatcherRule = CoroutineDispatcherRule()

    @MockK(relaxed = true)
    private lateinit var userRepository: UserRepository

    // SUT
    private lateinit var registerViewModel: RegisterViewModel

    @Before
    fun setUp() {
        MockKAnnotations.init(this)
        registerViewModel = RegisterViewModel(userRepository)
    }

    @Test
    fun `when performRegister with invalid Input - result Error`() = runTest {
        val expectedResult = UiState.Error<String>(StringWrapper.Resource(R.string.input_invalid))

        // act
        registerViewModel.dispatchEvent(RegisterEvent.Register)

        // assert
        val actualReceiver = registerViewModel.formState.testIn(this)
        val actualResult = actualReceiver.awaitItem()
        assertTrue(actualResult is FormState.Submit)
        val submitState = (actualResult as FormState.Submit).submitState
        assertTrue(submitState is UiState.Error)
        assertEquals(expectedResult, submitState)
        coVerify { userRepository wasNot Called }
        actualReceiver.cancelAndConsumeRemainingEvents()
    }

    @Test
    fun `when performRegister with valid Input - result Error (user already register)`() = runTest {
        val expectedResult = UiState.Error<String>(StringWrapper.Dynamic("already register"))
        coEvery {
            userRepository.signUp(any(), any(), any())
        } returns Resource.Error(StringWrapper.Dynamic("already register"))

        // act
        registerViewModel.dispatchEvent(RegisterEvent.NameChange(FakerProvider.FAKE_NAME))
        registerViewModel.dispatchEvent(RegisterEvent.EmailChange(FakerProvider.FAKE_EMAIL))
        registerViewModel.dispatchEvent(RegisterEvent.PasswordChange(FakerProvider.FAKE_PASSWORD))
        registerViewModel.dispatchEvent(RegisterEvent.Register)

        // assert
        val actualReceiver = registerViewModel.formState.testIn(this)
        val actualResult = actualReceiver.awaitItem()
        assertTrue(actualResult is FormState.Submit)
        val submitState = (actualResult as FormState.Submit).submitState
        assertTrue(submitState is UiState.Error)
        assertEquals(expectedResult, submitState)
        coVerify {
            userRepository.signUp(
                FakerProvider.FAKE_NAME,
                FakerProvider.FAKE_EMAIL,
                FakerProvider.FAKE_PASSWORD
            )
        }
        actualReceiver.cancelAndConsumeRemainingEvents()
    }

    @Test
    fun `when performRegister with valid Input - result Success`() = runTest {
        val expectedResult = UiState.Success("register Success")
        coEvery {
            userRepository.signUp(any(), any(), any())
        } returns Resource.Success("register Success")

        // act
        registerViewModel.dispatchEvent(RegisterEvent.NameChange(FakerProvider.FAKE_NAME))
        registerViewModel.dispatchEvent(RegisterEvent.EmailChange(FakerProvider.FAKE_EMAIL))
        registerViewModel.dispatchEvent(RegisterEvent.PasswordChange(FakerProvider.FAKE_PASSWORD))
        registerViewModel.dispatchEvent(RegisterEvent.Register)

        // assert
        val actualReceiver = registerViewModel.formState.testIn(this)
        val actualResult = actualReceiver.awaitItem()
        val submitState = (actualResult as FormState.Submit).submitState
        assertTrue(submitState is UiState.Success)
        assertEquals(expectedResult, submitState)
        coVerify {
            userRepository.signUp(
                FakerProvider.FAKE_NAME,
                FakerProvider.FAKE_EMAIL,
                FakerProvider.FAKE_PASSWORD
            )
        }
        actualReceiver.cancelAndConsumeRemainingEvents()
    }

    @After
    fun tearDown() {
        confirmVerified(userRepository)
        unmockkAll()
    }
}
