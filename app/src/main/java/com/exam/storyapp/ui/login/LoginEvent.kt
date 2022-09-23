/*
 * Copyright (C) 2022 Budi Ardianata.
 */
package com.exam.storyapp.ui.login

sealed class LoginEvent {
    object PerformLogin : LoginEvent()
    data class EmailChange(val email: String) : LoginEvent()
    data class PasswordChange(val password: String) : LoginEvent()
}
