/*
 * Copyright (C) 2022 Budi Ardianata.
 */
package com.exam.storyapp.ui.login

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
class LoginViewModel @Inject constructor(
    private val userRepository: UserRepository,
) : ViewModel() {
    private val _loginState = MutableStateFlow<FormState>(FormState.Validating(false))
    private val email = MutableStateFlow("")
    private val password = MutableStateFlow("")
    val loginState: StateFlow<FormState> = _loginState.asStateFlow()

    init {
        combine(email, password) { email, password ->
            val isValid = email.isEmailValid() && password.isPasswordValid()
            _loginState.update { FormState.Validating(isValid) }
        }.launchIn(viewModelScope)
    }

    fun dispatchEvent(event: LoginEvent) {
        viewModelScope.launch {
            when (event) {
                is LoginEvent.EmailChange -> email.update { event.email }
                is LoginEvent.PasswordChange -> password.update { event.password }
                is LoginEvent.PerformLogin -> performLogin()
            }
        }
    }

    private fun performLogin() {
        _loginState.update { FormState.Submit(UiState.Loading()) }
        viewModelScope.launch {
            if (email.value.isEmailValid() && password.value.isPasswordValid()) {
                when (val result = userRepository.signIn(email.value, password.value)) {
                    is Resource.Error -> {
                        _loginState.update { FormState.Submit(UiState.Error(result.exception)) }
                    }
                    is Resource.Success -> {
                        _loginState.update {
                            FormState.Submit(UiState.Success("Login Success"))
                        }
                        userRepository.saveUserSession(result.data)
                    }
                }
            } else {
                _loginState.update {
                    FormState.Submit(UiState.Error(StringWrapper.Resource(R.string.input_invalid)))
                }
            }
        }
    }
}
