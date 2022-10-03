/*
 * Copyright (C) 2022 Budi Ardianata.
 */
package com.exam.storyapp.ui.create

import com.google.android.gms.maps.model.LatLng
import java.io.File

sealed class CreateStoryEvent {
    data class AddDescription(val message: String) : CreateStoryEvent()
    data class AddImage(val image: File?) : CreateStoryEvent()
    data class AddLocation(val location: LatLng?) : CreateStoryEvent()
    object CreateStory : CreateStoryEvent()
}
