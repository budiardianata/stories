/*
 * Copyright (C) 2022 Budi Ardianata.
 */
package com.exam.storyapp.domain.model

import com.exam.storyapp.common.util.Resource
import com.exam.storyapp.common.util.StringWrapper

sealed class UiState<T> {
    class Loading<T> : UiState<T>()
    data class Success<T>(val data: T) : UiState<T>()
    data class Error<T>(val exception: StringWrapper) : UiState<T>()

    companion object {

        /**
         * Returns [UiState.Loading] instance.
         */
        fun <T> loading() = Loading<T>()

        /**
         * Returns [UiState.Success] instance.
         * @param data Data to emit with status.
         */
        fun <T> success(data: T) =
            Success(data)

        /**
         * Returns [UiState.Error] instance.
         * @param message Description of failure.
         */
        fun <T> error(message: StringWrapper) = Error<T>(message)

        /**
         * Returns map [Resource] to [UiState]
         */
        fun <T> fromResource(resource: Resource<T>): UiState<T> = when (resource) {
            is Resource.Success -> success(resource.data)
            is Resource.Error -> error(resource.exception)
        }
    }
}
