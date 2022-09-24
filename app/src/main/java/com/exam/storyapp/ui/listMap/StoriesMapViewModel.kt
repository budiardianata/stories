/*
 * Copyright (C) 2022 Budi Ardianata.
 */
package com.exam.storyapp.ui.listMap

import androidx.lifecycle.ViewModel
import com.exam.storyapp.domain.repositories.StoryRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class StoriesMapViewModel @Inject constructor(
    repository: StoryRepository,
) : ViewModel()
