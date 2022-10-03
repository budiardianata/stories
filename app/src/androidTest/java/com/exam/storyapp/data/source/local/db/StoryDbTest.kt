/*
 * Copyright (C) 2022 Budi Ardianata.
 */
package com.exam.storyapp.data.source.local.db

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.exam.storyapp.common.utils.FakerProvider
import com.exam.storyapp.data.model.RemoteKeys
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import javax.inject.Inject
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@ExperimentalCoroutinesApi
@HiltAndroidTest
class StoryDbTest {
    @get:Rule(order = 0)
    var hiltRule = HiltAndroidRule(this)

    @get:Rule(order = 1)
    val instantExecutorRule = InstantTaskExecutorRule()

    @Inject
    lateinit var database: StoryDb

    @Before
    fun setUp() {
        hiltRule.inject()
    }

    @Test
    fun insertData_Success() = runBlocking {
        val given = FakerProvider.generateStoryData(5)
        val keys = given.map {
            RemoteKeys(id = it.id, prevKey = 0, nextKey = 1)
        }

        val insertedKey = database.remoteKeysDao().insert(keys)
        val insertedStory = database.storyDao().insert(given)

        assertEquals(given.size, insertedKey.size)
        assertEquals(given.size, insertedStory.size)
    }

    @Test
    fun insertKey_then_VerifyData() = runBlocking {
        val expectedResult = RemoteKeys(id = "4", prevKey = 3, nextKey = 4)
        val given = listOf(
            expectedResult,
            RemoteKeys(id = "5", prevKey = 4, nextKey = 5),
        )

        database.remoteKeysDao().insert(given)

        val actualResult = database.remoteKeysDao().getById("4")
        assertEquals(expectedResult, actualResult)
    }

    @After
    fun tearDown() {
        database.run {
            clearAllTables()
            close()
        }
    }
}
