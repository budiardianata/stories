/*
 * Copyright (C) 2022 Budi Ardianata.
 */
package com.exam.storyapp.ui.register

import android.content.Context
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.closeSoftKeyboard
import androidx.test.espresso.action.ViewActions.typeText
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.isEnabled
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.filters.SmallTest
import androidx.test.platform.app.InstrumentationRegistry
import com.exam.storyapp.R
import com.exam.storyapp.common.extensions.hasTextInputLayoutErrorText
import com.exam.storyapp.common.extensions.launchFragmentInHiltContainer
import com.exam.storyapp.common.util.Constant
import com.exam.storyapp.common.utils.FakerProvider
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import org.hamcrest.CoreMatchers.not
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@SmallTest
@HiltAndroidTest
class RegisterFragmentTest {
    @get:Rule(order = 0)
    var hiltRule = HiltAndroidRule(this)

    private val context: Context = InstrumentationRegistry.getInstrumentation().targetContext

    @Before
    fun setUp() {
        hiltRule.inject()
        launchFragmentInHiltContainer<RegisterFragment>()
    }

    @Test
    fun launchRegisterFragment_give_incorrect_input_then_error_appear() {
        onView(withId(R.id.register_button)).check(matches(not(isEnabled())))

        onView(withId(R.id.ed_register_email))
            .perform(typeText(FakerProvider.FAKE_NAME), closeSoftKeyboard())

        onView(withId(R.id.lay_email))
            .check(matches(hasTextInputLayoutErrorText(context.getString(R.string.email_invalid))))

        onView(withId(R.id.ed_register_password))
            .perform(typeText("1234"), closeSoftKeyboard())

        onView(withId(R.id.lay_password))
            .check(
                matches(
                    hasTextInputLayoutErrorText(
                        context.resources.getQuantityString(
                            R.plurals.password_min_length,
                            Constant.PASSWORD_MIN,
                            Constant.PASSWORD_MIN,
                        ),
                    ),
                ),
            )

        onView(withId(R.id.register_button)).check(matches(not(isEnabled())))
    }

    @Test
    fun launchRegisterFragment_give_correct_input_then_register_button_enable() {
        onView(withId(R.id.register_button)).check(matches(not(isEnabled())))

        onView(withId(R.id.ed_register_name)).perform(
            typeText(FakerProvider.FAKE_NAME),
            closeSoftKeyboard(),
        )

        onView(withId(R.id.ed_register_email)).perform(
            typeText(FakerProvider.FAKE_EMAIL),
            closeSoftKeyboard(),
        )

        onView(withId(R.id.ed_register_password)).perform(
            typeText(FakerProvider.FAKE_PASSWORD),
            closeSoftKeyboard(),
        )

        onView(withId(R.id.register_button)).check(matches(isEnabled()))
    }
}
