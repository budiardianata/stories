/*
 * Copyright (C) 2022 Budi Ardianata.
 */
package com.exam.storyapp.ui.create

import android.net.Uri
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.exam.storyapp.common.util.Constant
import com.exam.storyapp.domain.model.FormState
import com.exam.storyapp.domain.model.UiState
import com.exam.storyapp.domain.repositories.StoryRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

@HiltViewModel
class CreateStoryViewModel @Inject constructor(
    private val savedStateHandle: SavedStateHandle,
    private val storyRepository: dagger.Lazy<StoryRepository>,
) : ViewModel() {

    private val description = savedStateHandle.getStateFlow(Constant.KEY_DESCRIPTIONS, "")
    val image = savedStateHandle.getStateFlow<Uri?>(Constant.KEY_IMAGE, null)

    private val _formState = MutableStateFlow<FormState>(FormState.Validating())

    val formState = _formState.asStateFlow()

    init {
        description.combine(image) { description, image ->
            _formState.value = FormState.Validating(description.isNotBlank() && image != null)
        }.launchIn(viewModelScope)
    }

    fun dispatchEvent(event: CreateStoryEvent) {
        when (event) {
            is CreateStoryEvent.AddDescription -> savedStateHandle[Constant.KEY_DESCRIPTIONS] =
                event.message
            is CreateStoryEvent.AddImage -> savedStateHandle[Constant.KEY_IMAGE] = event.image
            CreateStoryEvent.Save -> submitStory()
        }
    }

    private fun submitStory() {
        viewModelScope.launch {
            if (description.value.isNotBlank() && image.value != null) {
                _formState.update { FormState.Submit(UiState.Loading()) }
                val result = storyRepository.get().createStory(description.value, image.value!!)
                _formState.update { FormState.Submit(UiState.fromResource(result)) }
            }
        }
    }
}
