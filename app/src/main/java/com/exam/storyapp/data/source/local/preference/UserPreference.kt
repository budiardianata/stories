/*
 * Copyright (C) 2022 Budi Ardianata.
 */
package com.exam.storyapp.data.source.local.preference

import androidx.appcompat.app.AppCompatDelegate
import com.exam.storyapp.data.model.UserData

@kotlinx.serialization.Serializable
data class UserPreference(
    val theme: Int = AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM,
    val user: UserData = UserData(),
)
