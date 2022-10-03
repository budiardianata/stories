/*
 * Copyright (C) 2022 Budi Ardianata.
 */
package com.exam.storyapp.ui.create

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.exam.storyapp.R
import com.exam.storyapp.common.util.StringWrapper
import com.exam.storyapp.domain.model.FormState
import com.exam.storyapp.domain.model.UiState
import com.exam.storyapp.domain.repositories.StoryRepository
import com.google.android.gms.maps.model.LatLng
import dagger.hilt.android.lifecycle.HiltViewModel
import java.io.File
import javax.inject.Inject
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

@HiltViewModel
class CreateStoryViewModel @Inject constructor(
    private val storyRepository: StoryRepository,
) : ViewModel() {

    private val description = MutableStateFlow("")
    private val location = MutableStateFlow<LatLng?>(null)
    private val image = MutableStateFlow<File?>(null)
    private val _formState = MutableStateFlow<FormState>(FormState.Validating())

    val formState = _formState.asStateFlow()

    init {
        description.combine(image) { description, image ->
            _formState.value = FormState.Validating(description.isNotBlank() && image != null)
        }.launchIn(viewModelScope)
    }

    fun dispatchEvent(event: CreateStoryEvent) {
        when (event) {
            is CreateStoryEvent.AddDescription -> description.update { event.message }
            is CreateStoryEvent.AddImage -> image.update { event.image }
            is CreateStoryEvent.AddLocation -> location.update { event.location }
            CreateStoryEvent.CreateStory -> submitStory()
        }
    }

    private fun submitStory() {
        viewModelScope.launch {
            if (description.value.isNotBlank() && image.value != null) {
                _formState.update { FormState.Submit(UiState.Loading()) }
                val result = storyRepository.createStory(
                    description.value,
                    image.value!!,
                    location.value,
                )
                _formState.update { FormState.Submit(UiState.fromResource(result)) }
            } else {
                _formState.update {
                    FormState.Submit(
                        UiState.Error(StringWrapper.Resource(R.string.input_invalid)),
                    )
                }
            }
        }
    }
}
