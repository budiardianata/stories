/*
 * Copyright (C) 2022 Budi Ardianata.
 */
package com.exam.storyapp.common.extensions

import androidx.core.util.PatternsCompat
import com.exam.storyapp.common.util.Constant

fun String.isEmailValid(): Boolean {
    return this.isNotEmpty() && PatternsCompat.EMAIL_ADDRESS.matcher(this).matches()
}

fun String.isPasswordValid(min: Int = Constant.PASSWORD_MIN): Boolean {
    return this.isNotEmpty() && this.length >= min
}
