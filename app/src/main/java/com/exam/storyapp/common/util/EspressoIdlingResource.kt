/*
 * Copyright (C) 2022 Budi Ardianata.
 */
package com.exam.storyapp.common.util

import androidx.test.espresso.idling.CountingIdlingResource
import com.exam.storyapp.BuildConfig

object EspressoIdlingResource {
    private const val RESOURCE = "GLOBAL"

    @JvmField
    val countingIdlingResource = CountingIdlingResource(RESOURCE)

    fun increment() {
        countingIdlingResource.increment()
    }

    fun decrement() {
        if (!countingIdlingResource.isIdleNow) {
            countingIdlingResource.decrement()
        }
    }
}

inline fun <T> wrapEspressoIdlingResource(fn: () -> T): T {
    if (!BuildConfig.DEBUG) return fn()
    EspressoIdlingResource.increment() // Set app as busy.
    return try {
        fn()
    } finally {
        EspressoIdlingResource.decrement() // Set app as idle.
    }
}
