/*
 * Copyright (C) 2022 Budi Ardianata.
 */
package com.exam.storyapp.data.source.remote.response

import com.exam.storyapp.data.model.UserData
import kotlinx.serialization.Serializable

@Serializable
data class LoginResponse(
    override val error: Boolean,
    override val message: String,
    val loginResult: UserData,
) : BaseResponse
