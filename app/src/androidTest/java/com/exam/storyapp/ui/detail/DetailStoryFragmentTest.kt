/*
 * Copyright (C) 2022 Budi Ardianata.
 */
package com.exam.storyapp.ui.detail

import androidx.core.os.bundleOf
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.filters.MediumTest
import com.exam.storyapp.R
import com.exam.storyapp.common.extensions.launchFragmentInHiltContainer
import com.exam.storyapp.common.util.Constant
import com.exam.storyapp.common.utils.FakerProvider
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import org.junit.Rule
import org.junit.Test

@MediumTest
@HiltAndroidTest
class DetailStoryFragmentTest {
    @get:Rule(order = 0)
    var hiltRule = HiltAndroidRule(this)

    @Test
    fun launchDetailStory_then_check_view_equals_with_given_bundle() {
        val given = FakerProvider.generateStoryData(1).first().toDomain()

        launchFragmentInHiltContainer<DetailStoryFragment>(
            fragmentArgs = bundleOf(Constant.KEY_STORY to given)
        )

        onView(withId(R.id.tv_detail_name))
            .check(matches(isDisplayed()))
            .check(matches(withText(given.createdBy)))

        onView(withId(R.id.tv_detail_description))
            .check(matches(isDisplayed()))
            .check(matches(withText(given.description)))

        onView(withId(R.id.tv_detail_date))
            .check(matches(isDisplayed()))
            .check(matches(withText(given.createAtFormatted)))

        onView(withId(R.id.iv_detail_photo))
            .check(matches(isDisplayed()))
            .check(matches(withContentDescription(given.description)))
    }
}
