/*
 * Copyright (C) 2022 Budi Ardianata.
 */
package com.exam.storyapp.data.source.remote.response

import kotlinx.serialization.Serializable

@Serializable
data class DefaultResponse(
    override val error: Boolean,
    override val message: String,
) : BaseResponse
