/*
 * Copyright (C) 2022 Budi Ardianata.
 */
package com.exam.storyapp.data.model

import com.exam.storyapp.domain.model.User
import kotlinx.serialization.Serializable

@Serializable
data class UserData(
    val userId: String = "",
    val name: String = "",
    val token: String = "",
) {
    fun mapToDomain(): User {
        return User(
            id = userId,
            name = name,
            token = token
        )
    }
}
