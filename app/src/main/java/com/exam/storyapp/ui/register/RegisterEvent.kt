/*
 * Copyright (C) 2022 Budi Ardianata.
 */
package com.exam.storyapp.ui.register

sealed class RegisterEvent {
    data class NameChange(val name: String) : RegisterEvent()
    data class EmailChange(val email: String) : RegisterEvent()
    data class PasswordChange(val password: String) : RegisterEvent()
    object Register : RegisterEvent()
}
