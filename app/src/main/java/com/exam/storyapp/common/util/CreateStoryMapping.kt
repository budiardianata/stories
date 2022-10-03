/*
 * Copyright (C) 2022 Budi Ardianata.
 */
package com.exam.storyapp.common.util

import com.google.android.gms.maps.model.LatLng
import dagger.Reusable
import java.io.File
import javax.inject.Inject
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody

@Reusable
class CreateStoryMapping @Inject constructor() {
    fun toMap(description: String, latLng: LatLng? = null): Map<String, RequestBody> {
        val result = HashMap<String, RequestBody>()
        result["description"] = description.toRequestBody("text/plain".toMediaType())
        if (latLng != null) {
            result["lat"] = latLng.latitude.toString().toRequestBody("text/plain".toMediaType())
            result["lon"] = latLng.longitude.toString().toRequestBody("text/plain".toMediaType())
        }
        return result
    }

    fun toMultipartBody(file: File): MultipartBody.Part {
        val requestFile = file.asRequestBody(file.toMediaType())
        return MultipartBody.Part.createFormData("photo", file.name, requestFile)
    }

    private fun File.toMediaType() = when (extension) {
        "PNG" -> "image/png".toMediaType()
        "WEBP" -> "image/webp".toMediaType()
        else -> "image/jpeg".toMediaType()
    }
}
