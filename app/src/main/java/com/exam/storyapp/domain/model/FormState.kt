/*
 * Copyright (C) 2022 Budi Ardianata.
 */
package com.exam.storyapp.domain.model

sealed class FormState {
    data class Validating(val isValid: Boolean = false) : FormState()
    data class Submit(val submitState: UiState<String>) : FormState()
}
