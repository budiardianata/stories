/*
 * Copyright (C) 2022 Budi Ardianata.
 */
package com.exam.storyapp.ui.home

import android.content.Context
import androidx.recyclerview.widget.RecyclerView
import androidx.room.withTransaction
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.IdlingRegistry
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.contrib.RecyclerViewActions.scrollToPosition
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.filters.MediumTest
import com.exam.storyapp.R
import com.exam.storyapp.common.extensions.atPosition
import com.exam.storyapp.common.extensions.launchFragmentInHiltContainer
import com.exam.storyapp.common.extensions.notNullRecyclerItem
import com.exam.storyapp.common.extensions.withResponse
import com.exam.storyapp.common.util.EspressoIdlingResource
import com.exam.storyapp.common.utils.FakerProvider
import com.exam.storyapp.data.model.RemoteKeys
import com.exam.storyapp.data.source.local.db.StoryDb
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import java.io.File
import javax.inject.Inject
import kotlinx.coroutines.runBlocking
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@MediumTest
@HiltAndroidTest
class HomeFragmentTest {

    @get:Rule
    var hiltRule = HiltAndroidRule(this)

    private val mockWebServer = MockWebServer()

    @Inject
    lateinit var storyDb: StoryDb

    @Before
    fun setUp() {
        hiltRule.inject()
        mockWebServer.start(8080)
        IdlingRegistry.getInstance().register(EspressoIdlingResource.countingIdlingResource)
        launchFragmentInHiltContainer<HomeFragment>()
    }

    @Test
    fun launchHomeFragment_Failed_showError_when_no_cache() {
        val mockResponse = MockResponse().withResponse("error.json", 400)
        mockWebServer.enqueue(mockResponse)
        onView(withId(R.id.home_list)).check(matches(withEffectiveVisibility(Visibility.GONE)))
        onView(withId(R.id.retry_button)).check(matches(isDisplayed()))
        onView(withId(R.id.error_msg)).check(matches(isDisplayed()))
    }

    @Test
    fun launchHomeFragment_Error_only_retrieve_from_cache(): Unit = runBlocking {
        storyDb.withTransaction {
            val list = FakerProvider.generateStoryData(5)
            storyDb.remoteKeysDao().insert(list.map { RemoteKeys(it.id, null, 2) })
            storyDb.storyDao().insert(list)
        }
        val mockResponse = MockResponse().withResponse("error.json", 400)
        mockWebServer.enqueue(mockResponse)

        onView(withId(R.id.home_list)).check(matches(isDisplayed()))
            .check(matches(isDisplayed()))
            .perform(scrollToPosition<RecyclerView.ViewHolder>(0))
            .check(matches(atPosition(0, hasDescendant(withId(R.id.retry_button_item)))))

        onView(withId(R.id.retry_button)).check(matches(withEffectiveVisibility(Visibility.GONE)))
        onView(withId(R.id.error_msg)).check(matches(withEffectiveVisibility(Visibility.GONE)))
    }

    @Test
    fun launchHomeFragment_Success_goToDetailStory() {
        val mockResponse = MockResponse().withResponse("stories_success.json", 200)
        mockWebServer.enqueue(mockResponse)

        onView(withId(R.id.retry_button))
            .check(matches(withEffectiveVisibility(Visibility.GONE)))
        onView(withId(R.id.error_msg))
            .check(matches(withEffectiveVisibility(Visibility.GONE)))
        onView(withId(R.id.home_list))
            .check(matches(isDisplayed()))
            .check(matches(notNullRecyclerItem()))
    }

    @After
    fun tearDown() {
        mockWebServer.shutdown()
        storyDb.apply {
            clearAllTables()
            close()
        }
        IdlingRegistry.getInstance().unregister(EspressoIdlingResource.countingIdlingResource)
        File(
            ApplicationProvider.getApplicationContext<Context>().filesDir,
            "datastore",
        ).deleteRecursively()
    }
}
