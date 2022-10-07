/*
 * Copyright (C) 2022 Budi Ardianata.
 */
package com.exam.storyapp.ui.listMap

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.exam.storyapp.domain.model.Story
import com.exam.storyapp.domain.model.UiState
import com.exam.storyapp.domain.repositories.StoryRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

@HiltViewModel
class StoriesMapViewModel @Inject constructor(
    private val repository: StoryRepository,
) : ViewModel() {
    private val _numLoadedStories = MutableStateFlow(25)
    private val _stories = MutableStateFlow<UiState<List<Story>>>(UiState.loading())
    private val isMapReady = MutableStateFlow(false)
    val numLoadedStories = _numLoadedStories.asStateFlow()
    val stories = _stories.asStateFlow()

    init {
        isMapReady.combine(numLoadedStories) { isReady, numLoaded ->
            if (isReady && numLoaded > 0) {
                repository.getStories(perPage = numLoaded, withLocation = true)
                    .map { resource -> UiState.fromResource(resource) }
                    .collect { state -> _stories.update { state } }
            }
        }.launchIn(viewModelScope)
    }

    fun dispatchEvent(event: StoriesMapEvent) {
        viewModelScope.launch {
            when (event) {
                is StoriesMapEvent.MapReady -> isMapReady.update { true }
                is StoriesMapEvent.StoriesLoaded -> _numLoadedStories.update { event.value }
            }
        }
    }
}
