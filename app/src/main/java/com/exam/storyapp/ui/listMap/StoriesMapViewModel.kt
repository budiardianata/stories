/*
 * Copyright (C) 2022 Budi Ardianata.
 */
package com.exam.storyapp.ui.listMap

import androidx.lifecycle.SavedStateHandle
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
    private val savedStateHandle: SavedStateHandle,
    private val repository: StoryRepository,
) : ViewModel() {
    private val isMapReady = savedStateHandle.getStateFlow(MAP_READY, false)
    val numLoadedStories = savedStateHandle.getStateFlow(PER_PAGE, 25)
    private val _stories = MutableStateFlow<UiState<List<Story>>>(UiState.loading())
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
                is StoriesMapEvent.MapReady -> savedStateHandle[MAP_READY] = true
                is StoriesMapEvent.StoriesLoaded -> savedStateHandle[PER_PAGE] = event.value
            }
        }
    }

    companion object {
        const val MAP_READY = "isMapReady"
        const val PER_PAGE = "numLoadedStories"
    }
}
