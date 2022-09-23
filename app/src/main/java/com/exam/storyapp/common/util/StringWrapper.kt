/*
 * Copyright (C) 2022 Budi Ardianata.
 */
package com.exam.storyapp.common.util

import android.content.Context
import androidx.annotation.StringRes

sealed class StringWrapper {
    data class Dynamic(val value: String) : StringWrapper()
    class Resource(@StringRes val resId: Int, vararg val args: Any) : StringWrapper()

    fun toString(context: Context): String {
        return when (this) {
            is Dynamic -> value
            is Resource -> context.getString(resId, *args)
        }
    }
}
