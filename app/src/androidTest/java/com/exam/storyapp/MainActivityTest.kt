/*
 * Copyright (C) 2022 Budi Ardianata.
 */
package com.exam.storyapp

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.lifecycle.Lifecycle
import androidx.navigation.NavController
import androidx.navigation.findNavController
import androidx.recyclerview.widget.RecyclerView
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.Espresso.pressBack
import androidx.test.espresso.IdlingRegistry
import androidx.test.espresso.action.ViewActions.*
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.contrib.RecyclerViewActions.actionOnItemAtPosition
import androidx.test.espresso.contrib.RecyclerViewActions.scrollToPosition
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.filters.LargeTest
import com.exam.storyapp.common.extensions.notNullRecyclerItem
import com.exam.storyapp.common.util.EspressoIdlingResource
import com.exam.storyapp.common.utils.FakerProvider
import com.exam.storyapp.common.utils.JsonFileReader
import com.exam.storyapp.data.model.UserData
import com.exam.storyapp.data.source.local.db.StoryDb
import com.exam.storyapp.data.source.local.preference.UserPreference
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import java.io.File
import javax.inject.Inject
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import okhttp3.mockwebserver.Dispatcher
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import okhttp3.mockwebserver.RecordedRequest
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@LargeTest
@HiltAndroidTest
class MainActivityTest {
    @get:Rule
    var hiltRule = HiltAndroidRule(this)

    @Inject
    lateinit var dataStore: DataStore<UserPreference>

    @Inject
    lateinit var database: StoryDb

    private val loginDispatcher = object : Dispatcher() {
        override fun dispatch(request: RecordedRequest): MockResponse {
            request.path?.let {
                val body = when {
                    it.contains("/login") -> JsonFileReader.getJson("login_success.json")
                    it.contains("/stories?page=1") -> JsonFileReader.getJson("stories_success.json")
                    else -> JsonFileReader.getJson("stories_empty.json")
                }
                return MockResponse()
                    .setResponseCode(200)
                    .setBody(body)
            }
            return MockResponse()
                .setResponseCode(400)
                .setBody(JsonFileReader.getJson("error.json"))
        }
    }

    private val mockWebServer = MockWebServer()

    @Before
    fun setUp(): Unit = runBlocking {
        hiltRule.inject()
        mockWebServer.start(8080)
        mockWebServer.dispatcher = loginDispatcher
        IdlingRegistry.getInstance().register(EspressoIdlingResource.countingIdlingResource)
    }

    @Test
    fun endToEnd_authentication_flow(): Unit = runBlocking {
        dataStore.updateData { it.copy(user = UserData()) }

        var navController: NavController? = null
        val scenario = ActivityScenario.launch(MainActivity::class.java)
        scenario.moveToState(Lifecycle.State.RESUMED)
        scenario.onActivity {
            navController = it.findNavController(R.id.nav_host_fragment)
        }

        delay(500)
        assert(navController?.currentDestination?.id == R.id.loginFragment)
        onView(withId(R.id.ed_login_email))
            .perform(typeText(FakerProvider.FAKE_EMAIL), closeSoftKeyboard())

        onView(withId(R.id.ed_login_password))
            .perform(typeText(FakerProvider.FAKE_PASSWORD), closeSoftKeyboard())

        onView(withId(R.id.login_button)).check(matches(isEnabled())).perform(click())

        delay(500)
        assert(navController?.currentDestination?.id == R.id.homeFragment)
        onView(withId(R.id.home_list))
            .check(matches(isDisplayed()))
            .check(matches(notNullRecyclerItem()))
        onView(withId(R.id.fab))
            .check(matches(isDisplayed()))
        val context = ApplicationProvider.getApplicationContext<Context>()
        Espresso.openActionBarOverflowOrOptionsMenu(context)
        onView(withText(context.getString(R.string.logout))).perform(click())
        delay(500)
        assert(navController?.currentDestination?.id == R.id.loginFragment)
        scenario.close()
    }

    @Test
    fun endToEnd_home_flow() = runBlocking {
        dataStore.updateData { it.copy(user = FakerProvider.getUserData()) }
        var navController: NavController? = null
        val scenario = ActivityScenario.launch(MainActivity::class.java)
        scenario.moveToState(Lifecycle.State.RESUMED)
        scenario.onActivity {
            navController = it.findNavController(R.id.nav_host_fragment)
        }

        delay(500)
        assert(navController?.currentDestination?.id == R.id.homeFragment)
        onView(withId(R.id.home_list))
            .check(matches(isDisplayed()))
            .check(matches(notNullRecyclerItem()))
            .perform(scrollToPosition<RecyclerView.ViewHolder>(2))
            .perform(actionOnItemAtPosition<RecyclerView.ViewHolder>(2, click()))

        delay(1000)
        assert(navController?.currentDestination?.id == R.id.detailFragment)
        onView(withId(R.id.detail_root)).check(matches(isDisplayed()))
        onView(withId(R.id.tv_detail_name)).check(matches(isDisplayed()))
        pressBack()

        assert(navController?.currentDestination?.id == R.id.homeFragment)
        onView(withId(R.id.fab)).check(matches(isDisplayed()))
            .check(matches(isDisplayed()))
            .perform(click())

        assert(navController?.currentDestination?.id == R.id.createStoryFragment)
        onView(withId(R.id.add_root)).check(matches(isDisplayed()))
        onView(withId(R.id.button_add)).check(matches(isDisplayed()))
        onView(withId(R.id.ed_add_description)).check(matches(isDisplayed()))

        scenario.close()
    }

    @After
    fun tearDown(): Unit = runBlocking {
        mockWebServer.shutdown()
        database.clearAllTables()
        IdlingRegistry.getInstance().unregister(EspressoIdlingResource.countingIdlingResource)
        File(
            ApplicationProvider.getApplicationContext<Context>().filesDir,
            "datastore",
        ).deleteRecursively()
    }
}
