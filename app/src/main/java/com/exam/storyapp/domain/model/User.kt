/*
 * Copyright (C) 2022 Budi Ardianata.
 */
package com.exam.storyapp.domain.model

data class User(
    val id: String = "",
    val name: String = "",
    val token: String = "",
) {
    val isLogin = token.isNotEmpty()
}
