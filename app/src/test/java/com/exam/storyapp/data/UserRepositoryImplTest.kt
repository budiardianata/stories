/*
 * Copyright (C) 2022 Budi Ardianata.
 */
package com.exam.storyapp.data

import androidx.datastore.core.DataStore
import app.cash.turbine.testIn
import com.exam.storyapp.CoroutineDispatcherRule
import com.exam.storyapp.R
import com.exam.storyapp.common.util.Resource
import com.exam.storyapp.common.util.StringWrapper
import com.exam.storyapp.common.utils.FakerProvider
import com.exam.storyapp.data.model.UserData
import com.exam.storyapp.data.source.local.preference.UserPreference
import com.exam.storyapp.data.source.remote.StoryApi
import com.exam.storyapp.data.source.remote.adapter.NetworkResult
import com.exam.storyapp.data.source.remote.response.DefaultResponse
import com.exam.storyapp.data.source.remote.response.LoginResponse
import io.mockk.*
import io.mockk.impl.annotations.MockK
import java.io.IOException
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class UserRepositoryImplTest {
    @get:Rule
    var dispatcherRule = CoroutineDispatcherRule()

    @MockK(relaxed = true)
    lateinit var dataStore: DataStore<UserPreference>

    @MockK(relaxed = true)
    lateinit var storyApi: StoryApi

    // SUT
    private lateinit var repository: UserRepositoryImpl

    @Before
    fun setUp() {
        MockKAnnotations.init(this)
        repository = UserRepositoryImpl(dispatcherRule.testDispatcher, dataStore, storyApi)
    }

    @Test
    fun `when perform login - result error response`() = runTest {
        val expectedException = StringWrapper.Resource(R.string.unknown_error)
        coEvery {
            storyApi.login(any(), any())
        } returns NetworkResult.Error(
            401,
            StringWrapper.Resource(R.string.unknown_error),
        )

        val actualResult = repository.signIn(FakerProvider.FAKE_EMAIL, FakerProvider.FAKE_PASSWORD)
        assertTrue(actualResult is Resource.Error)
        assertEquals(expectedException, (actualResult as Resource.Error).exception)
        coVerifyAll {
            storyApi.login(FakerProvider.FAKE_EMAIL, FakerProvider.FAKE_PASSWORD)
            dataStore wasNot Called
        }
    }

    @Test
    fun `when perform login - result error (Exception)`() = runTest {
        val expectedException = "This is Test Exception"
        coEvery {
            storyApi.login(any(), any())
        } returns NetworkResult.Exception(Exception(expectedException))

        val actualResult = repository.signIn(FakerProvider.FAKE_EMAIL, FakerProvider.FAKE_PASSWORD)

        assertTrue(actualResult is Resource.Error)
        assertEquals(StringWrapper.Dynamic(expectedException), (actualResult as Resource.Error).exception)
        coVerifyAll {
            storyApi.login(FakerProvider.FAKE_EMAIL, FakerProvider.FAKE_PASSWORD)
            dataStore wasNot Called
        }
    }

    @Test
    fun `when perform login - result success`() = runTest {
        val expectedResult = FakerProvider.getUserData()
        coEvery {
            storyApi.login(any(), any())
        } returns NetworkResult.Success(
            LoginResponse(
                error = false,
                message = "Success",
                loginResult = expectedResult,
            ),
        )

        val actualResult = repository.signIn(FakerProvider.FAKE_EMAIL, FakerProvider.FAKE_PASSWORD)

        assertTrue(actualResult is Resource.Success)
        assertEquals(expectedResult.mapToDomain(), (actualResult as Resource.Success).data)
        coVerifyAll {
            storyApi.login(FakerProvider.FAKE_EMAIL, FakerProvider.FAKE_PASSWORD)
            dataStore wasNot Called
        }
    }

    @Test
    fun `when perform signUp - result error`() = runTest {
        val expectedResult = StringWrapper.Resource(R.string.unknown_error)
        coEvery {
            storyApi.register(any(), any(), any())
        } returns NetworkResult.Error(401, expectedResult)

        val actualResult = repository.signUp(
            email = FakerProvider.FAKE_EMAIL,
            password = FakerProvider.FAKE_PASSWORD,
            name = FakerProvider.FAKE_NAME,
        )

        assertEquals(
            Resource.Error(expectedResult),
            actualResult,
        )
        coVerifyAll {
            storyApi.register(
                FakerProvider.FAKE_NAME,
                FakerProvider.FAKE_EMAIL,
                FakerProvider.FAKE_PASSWORD,
            )
            dataStore wasNot Called
        }
    }

    @Test
    fun `when perform signUp - result exception`() = runTest {
        coEvery {
            storyApi.register(any(), any(), any())
        } returns NetworkResult.Exception(IOException())

        val actualResult = repository.signUp(
            email = FakerProvider.FAKE_EMAIL,
            password = FakerProvider.FAKE_PASSWORD,
            name = FakerProvider.FAKE_NAME,
        )

        assertEquals(
            Resource.Error(StringWrapper.Resource(R.string.no_connection)),
            actualResult,
        )
        coVerifyAll {
            storyApi.register(
                FakerProvider.FAKE_NAME,
                FakerProvider.FAKE_EMAIL,
                FakerProvider.FAKE_PASSWORD,
            )
            dataStore wasNot Called
        }
    }

    @Test
    fun `when perform signUp - result success`() = runTest {
        coEvery {
            storyApi.register(any(), any(), any())
        } returns NetworkResult.Success(
            DefaultResponse(
                error = false,
                message = "Success",
            ),
        )

        val actualResult = repository.signUp(
            email = FakerProvider.FAKE_EMAIL,
            password = FakerProvider.FAKE_PASSWORD,
            name = FakerProvider.FAKE_NAME,
        )

        assertEquals(
            Resource.Success("Success"),
            actualResult,
        )
        coVerifyAll {
            storyApi.register(
                FakerProvider.FAKE_NAME,
                FakerProvider.FAKE_EMAIL,
                FakerProvider.FAKE_PASSWORD,
            )
            dataStore wasNot Called
        }
    }

    @Test
    fun `when getCurrentUser - result token is not empty`() = runTest {
        val expectedResult = UserPreference(theme = -1, user = FakerProvider.getUserData())
        coEvery { dataStore.data } returns flowOf(expectedResult)

        val actualReceiver = repository.getCurrentUser().testIn(this)

        val actualResult = actualReceiver.awaitItem()
        assertTrue(actualResult.token.isNotEmpty())
        assertEquals(expectedResult.user.mapToDomain(), actualResult)
        coVerifyAll {
            dataStore.data
            storyApi wasNot Called
        }
        actualReceiver.cancelAndConsumeRemainingEvents()
    }

    @Test
    fun `when getCurrentUser - result token is empty`() = runTest {
        val expectedResult = UserPreference(theme = -1, user = UserData())
        coEvery { dataStore.data } returns flowOf(expectedResult)

        val actualReceiver = repository.getCurrentUser().testIn(this)

        val actualResult = actualReceiver.awaitItem()
        assertTrue(actualResult.token.isEmpty())
        assertEquals(expectedResult.user.mapToDomain(), actualResult)
        coVerifyAll {
            dataStore.data
            storyApi wasNot Called
        }
        actualReceiver.cancelAndConsumeRemainingEvents()
    }

    @After
    fun tearDown() {
        confirmVerified(dataStore, storyApi)
        unmockkAll()
    }
}
