/*
 * Copyright (C) 2022 Budi Ardianata.
 */
package com.exam.storyapp

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.exam.storyapp.domain.repositories.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@HiltViewModel
class MainViewModel @Inject constructor(
    private val userRepository: UserRepository,
) : ViewModel() {

    val isLogin: StateFlow<Boolean> = userRepository.getCurrentUser()
        .map { it.isLogin }
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            true,
        )

    fun logOut() = viewModelScope.launch { userRepository.signOut() }
}
