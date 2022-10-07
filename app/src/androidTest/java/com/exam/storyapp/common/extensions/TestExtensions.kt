/*
 * Copyright (C) 2022 Budi Ardianata.
 */
package com.exam.storyapp.common.extensions

import android.content.ComponentName
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.annotation.StyleRes
import androidx.core.util.Preconditions
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentFactory
import androidx.navigation.NavHostController
import androidx.navigation.Navigation
import androidx.recyclerview.widget.RecyclerView
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.matcher.BoundedMatcher
import com.exam.storyapp.HiltTestActivity
import com.exam.storyapp.R
import com.exam.storyapp.common.utils.JsonFileReader
import com.google.android.material.textfield.TextInputLayout
import okhttp3.mockwebserver.MockResponse
import org.hamcrest.Description
import org.hamcrest.Matcher
import org.hamcrest.TypeSafeMatcher

const val FRAGMENT_THEME =
    "androidx.fragment.app.testing.FragmentScenario.EmptyFragmentActivity.THEME_EXTRAS_BUNDLE_KEY"

inline fun <reified T : Fragment> launchFragmentInHiltContainer(
    fragmentArgs: Bundle? = null,
    @StyleRes themeResId: Int = R.style.Theme_StoryApp,
    fragmentFactory: FragmentFactory? = null,
    navHostController: NavHostController? = null,
    crossinline action: T.() -> Unit = {},
) {
    val mainActivityIntent = Intent.makeMainActivity(
        ComponentName(
            ApplicationProvider.getApplicationContext(),
            HiltTestActivity::class.java
        )
    ).putExtra(
        FRAGMENT_THEME,
        themeResId
    )

    ActivityScenario.launch<HiltTestActivity>(mainActivityIntent).onActivity { activity ->

        fragmentFactory?.let {
            activity.supportFragmentManager.fragmentFactory = it
        }
        val fragment: Fragment = activity.supportFragmentManager.fragmentFactory.instantiate(
            Preconditions.checkNotNull(T::class.java.classLoader),
            T::class.java.name
        )
        fragment.arguments = fragmentArgs
        fragment.viewLifecycleOwnerLiveData.observeForever { viewLifecycleOwner ->
            if (viewLifecycleOwner != null) {
                navHostController?.let {
                    it.setGraph(R.navigation.nav_graph)
                    Navigation.setViewNavController(fragment.requireView(), it)
                }
            }
        }
        activity.supportFragmentManager
            .beginTransaction()
            .add(android.R.id.content, fragment, "")
            .commit()

        (fragment as T).action()
    }
}

fun hasTextInputLayoutErrorText(expectedErrorText: String): Matcher<View> =
    object : TypeSafeMatcher<View>() {

        override fun describeTo(description: Description) {
            description.appendText("Has Error Text: $expectedErrorText")
        }

        override fun matchesSafely(item: View?): Boolean {
            if (item !is TextInputLayout) return false
            val error = item.error ?: return false
            return expectedErrorText == error
        }
    }

fun atPosition(position: Int, itemMatcher: Matcher<View>): Matcher<View> {
    return object : BoundedMatcher<View, RecyclerView>(RecyclerView::class.java) {
        override fun describeTo(description: Description) {
            description.appendText("has item at position $position: ")
            itemMatcher.describeTo(description)
        }

        override fun matchesSafely(view: RecyclerView): Boolean {
            val viewHolder = view.findViewHolderForAdapterPosition(position) ?: return false
            return itemMatcher.matches(viewHolder.itemView)
        }
    }
}

fun notNullRecyclerItem(): Matcher<View> {
    return object : BoundedMatcher<View, RecyclerView>(RecyclerView::class.java) {
        override fun describeTo(description: Description?) {
            description?.appendText("RecyclerView item is not null")
        }

        override fun matchesSafely(item: RecyclerView?): Boolean {
            return item?.adapter?.itemCount != 0
        }
    }
}

fun MockResponse.withResponse(fileName: String, responseCode: Int = 200): MockResponse {
    return this.setResponseCode(responseCode)
        .setBody(JsonFileReader.getJson(fileName))
}
