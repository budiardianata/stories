/*
 * Copyright (C) 2022 Budi Ardianata.
 */
package com.exam.storyapp.data.source.remote.response

interface BaseResponse {
    val error: Boolean
    val message: String
}
