/*
 * Copyright (C) 2022 Budi Ardianata.
 */
package com.exam.storyapp.ui.create

import android.net.Uri

sealed class CreateStoryEvent {
    data class AddDescription(val message: String) : CreateStoryEvent()
    data class AddImage(val image: Uri?) : CreateStoryEvent()
    object Save : CreateStoryEvent()
}
