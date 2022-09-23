/*
 * Copyright (C) 2022 Budi Ardianata.
 */
package com.exam.storyapp.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.cachedIn
import com.exam.storyapp.domain.model.User
import com.exam.storyapp.domain.repositories.StoryRepository
import com.exam.storyapp.domain.repositories.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val userRepository: UserRepository,
    storyRepository: StoryRepository,
) : ViewModel() {

    val stories = storyRepository.getPagedStories()
        .cachedIn(viewModelScope)

    val currentUser: StateFlow<User> = userRepository.getCurrentUser()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(), User())

    fun signOut() = viewModelScope.launch { userRepository.signOut() }
}
