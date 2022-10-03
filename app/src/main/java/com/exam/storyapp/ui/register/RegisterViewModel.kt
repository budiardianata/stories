/*
 * Copyright (C) 2022 Budi Ardianata.
 */
package com.exam.storyapp.ui.register

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.exam.storyapp.R
import com.exam.storyapp.common.extensions.isEmailValid
import com.exam.storyapp.common.extensions.isPasswordValid
import com.exam.storyapp.common.util.Resource
import com.exam.storyapp.common.util.StringWrapper
import com.exam.storyapp.domain.model.FormState
import com.exam.storyapp.domain.model.UiState
import com.exam.storyapp.domain.repositories.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

@HiltViewModel
class RegisterViewModel @Inject constructor(
    private val userRepository: UserRepository,
) : ViewModel() {
    private val name = MutableStateFlow("")
    private val email = MutableStateFlow("")
    private val password = MutableStateFlow("")

    private val _formState = MutableStateFlow<FormState>(FormState.Validating(false))
    val formState = _formState.asStateFlow()

    init {
        combine(name, email, password) { name, email, password ->
            val isValid = name.isNotEmpty() && email.isEmailValid() && password.isPasswordValid()
            _formState.update { FormState.Validating(isValid) }
        }.launchIn(viewModelScope)
    }

    fun dispatchEvent(event: RegisterEvent) {
        viewModelScope.launch {
            when (event) {
                is RegisterEvent.EmailChange -> email.update { event.email }
                is RegisterEvent.NameChange -> name.update { event.name }
                is RegisterEvent.PasswordChange -> password.update { event.password }
                RegisterEvent.Register -> performSignUp()
            }
        }
    }

    private fun performSignUp() {
        _formState.update { FormState.Submit(UiState.Loading()) }
        viewModelScope.launch {
            if (name.value.isNotEmpty() && email.value.isEmailValid() && password.value.isPasswordValid()) {
                when (val status = userRepository.signUp(name.value, email.value, password.value)) {
                    is Resource.Error -> _formState.update { FormState.Submit(UiState.Error(status.exception)) }
                    is Resource.Success -> _formState.update {
                        FormState.Submit(UiState.Success(status.data))
                    }
                }
            } else {
                _formState.update {
                    FormState.Submit(UiState.Error(StringWrapper.Resource(R.string.input_invalid)))
                }
            }
        }
    }
}
