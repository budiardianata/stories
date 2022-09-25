/*
 * Copyright (C) 2022 Budi Ardianata.
 */
package com.exam.storyapp.ui.create

import android.net.Uri
import com.google.android.gms.maps.model.LatLng

sealed class CreateStoryEvent {
    data class AddDescription(val message: String) : CreateStoryEvent()
    data class AddImage(val image: Uri?) : CreateStoryEvent()
    data class AddLocation(val location: LatLng?) : CreateStoryEvent()
    object Save : CreateStoryEvent()
}
