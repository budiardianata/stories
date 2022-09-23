/*
 * Copyright (C) 2022 Budi Ardianata.
 */
package com.exam.storyapp.domain.repositories

import com.exam.storyapp.common.util.Resource
import com.exam.storyapp.domain.model.User
import kotlinx.coroutines.flow.Flow

interface UserRepository {
    fun getCurrentUser(): Flow<User>
    suspend fun saveUserSession(user: User)
    suspend fun signIn(
        email: String,
        password: String,
    ): Resource<User>

    suspend fun signUp(name: String, email: String, password: String): Resource<String>

    suspend fun signOut()
}
