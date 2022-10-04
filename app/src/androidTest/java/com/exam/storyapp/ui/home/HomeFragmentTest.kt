/*
 * Copyright (C) 2022 Budi Ardianata.
 */
package com.exam.storyapp.ui.home

import android.content.Context
import androidx.navigation.Navigation
import androidx.navigation.testing.TestNavHostController
import androidx.recyclerview.widget.RecyclerView
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.IdlingRegistry
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.contrib.RecyclerViewActions
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.filters.MediumTest
import androidx.test.internal.runner.junit4.statement.UiThreadStatement
import com.exam.storyapp.R
import com.exam.storyapp.common.extensions.launchFragmentInHiltContainer
import com.exam.storyapp.common.extensions.withResponse
import com.exam.storyapp.common.util.EspressoIdlingResource
import com.exam.storyapp.data.source.local.db.StoryDb
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import java.io.File
import javax.inject.Inject
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@MediumTest
@HiltAndroidTest
class HomeFragmentTest {

    @get:Rule
    var hiltRule = HiltAndroidRule(this)

    private val mockWebServer = MockWebServer()

    private val navController = TestNavHostController(ApplicationProvider.getApplicationContext())

    @Inject
    lateinit var storyDb: StoryDb

    @Before
    fun setUp() {
        hiltRule.inject()
        mockWebServer.start(8080)
        IdlingRegistry.getInstance().register(EspressoIdlingResource.countingIdlingResource)
        UiThreadStatement.runOnUiThread {
            navController.setGraph(R.navigation.nav_graph)
            navController.setCurrentDestination(R.id.homeFragment)
        }
        launchFragmentInHiltContainer<HomeFragment> {
            viewLifecycleOwnerLiveData.observeForever { viewLifecycleOwner ->
                if (viewLifecycleOwner != null) {
                    Navigation.setViewNavController(this.requireView(), navController)
                }
            }
        }
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
            .perform(
                RecyclerViewActions.actionOnItemAtPosition<RecyclerView.ViewHolder>(1, click()),
            )
        assertEquals(navController.currentDestination?.id, R.id.detailFragment)
    }

    @Test
    fun launchHomeFragment_thanGoToCreateFragment() {
        storyDb.clearAllTables()
        val mockResponse = MockResponse().withResponse("stories_empty.json", 401)
        mockWebServer.enqueue(mockResponse)
        onView(withId(R.id.home_list)).check(matches(isDisplayed()))
        onView(withId(R.id.retry_button)).check(matches(withEffectiveVisibility(Visibility.GONE)))
        onView(withId(R.id.fab)).perform(click())
        assertEquals(navController.currentDestination?.id, R.id.createStoryFragment)
    }

    @After
    fun tearDown() {
        mockWebServer.shutdown()
        IdlingRegistry.getInstance().unregister(EspressoIdlingResource.countingIdlingResource)

        File(
            ApplicationProvider.getApplicationContext<Context>().filesDir,
            "datastore",
        ).deleteRecursively()
    }
}
