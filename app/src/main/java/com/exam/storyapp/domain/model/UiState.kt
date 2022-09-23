/*
 * Copyright (C) 2022 Budi Ardianata.
 */
package com.exam.storyapp.domain.model

import com.exam.storyapp.common.util.StringWrapper

sealed class UiState<out R> {
    object Loading : UiState<Nothing>()
    data class Success<out T>(val data: T) : UiState<T>()
    data class Error(val exception: StringWrapper) : UiState<Nothing>()
}
