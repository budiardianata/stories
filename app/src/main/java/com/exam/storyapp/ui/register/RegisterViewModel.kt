/*
 * Copyright (C) 2022 Budi Ardianata.
 */
package com.exam.storyapp.ui.register

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.exam.storyapp.common.extensions.isEmailValid
import com.exam.storyapp.common.extensions.isPasswordValid
import com.exam.storyapp.common.util.Constant
import com.exam.storyapp.common.util.Resource
import com.exam.storyapp.domain.model.FormState
import com.exam.storyapp.domain.model.UiState
import com.exam.storyapp.domain.repositories.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

@HiltViewModel
class RegisterViewModel @Inject constructor(
    private val savedStateHandle: SavedStateHandle,
    private val userRepository: UserRepository,
) : ViewModel() {
    private val name = savedStateHandle.getStateFlow(Constant.KEY_NAME, "")
    private val email = savedStateHandle.getStateFlow(Constant.KEY_EMAIL, "")
    private val password = savedStateHandle.getStateFlow(Constant.KEY_PASSWORD, "")

    private val _formState = MutableStateFlow<FormState>(FormState.Validating(false))
    val formState = _formState.asStateFlow()

    init {
        combine(name, email, password) { name, email, password ->
            val isValid = name.isNotBlank() && email.isEmailValid() && password.isPasswordValid()
            _formState.update { FormState.Validating(isValid) }
        }.launchIn(viewModelScope)
    }

    fun dispatchEvent(event: RegisterEvent) {
        viewModelScope.launch {
            when (event) {
                is RegisterEvent.EmailChange -> savedStateHandle[Constant.KEY_EMAIL] = event.email
                is RegisterEvent.NameChange -> savedStateHandle[Constant.KEY_NAME] = event.name
                is RegisterEvent.PasswordChange -> savedStateHandle[Constant.KEY_PASSWORD] =
                    event.password
                RegisterEvent.Register -> performSignUp()
            }
        }
    }

    private fun performSignUp() {
        viewModelScope.launch {
            _formState.update { FormState.Submit(UiState.Loading()) }
            when (
                val status = userRepository.signUp(
                    email = email.value,
                    password = password.value,
                    name = name.value,
                )
            ) {
                is Resource.Error -> _formState.update { FormState.Submit(UiState.Error(status.exception)) }
                is Resource.Success -> _formState.update { FormState.Submit(UiState.Success(status.data)) }
            }
        }
    }
}
