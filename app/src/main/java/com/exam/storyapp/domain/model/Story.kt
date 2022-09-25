/*
 * Copyright (C) 2022 Budi Ardianata.
 */
package com.exam.storyapp.domain.model

import android.os.Parcelable
import android.text.format.DateUtils
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.clustering.ClusterItem
import kotlinx.parcelize.Parcelize

@Parcelize
data class Story(
    val id: String,
    val description: String,
    val image: String,
    val createdBy: String,
    val createdAt: Long,
    val lat: Double = 0.0,
    val lon: Double = 0.0,
) : Parcelable, ClusterItem {

    val createAtFormatted: String
        get() {
            return try {
                DateUtils.getRelativeTimeSpanString(
                    createdAt,
                    System.currentTimeMillis(),
                    DateUtils.MINUTE_IN_MILLIS,
                ).toString()
            } catch (e: Exception) {
                e.printStackTrace()
                ""
            }
        }

    override fun getPosition(): LatLng {
        return LatLng(lat, lon)
    }

    override fun getTitle(): String = createdBy

    override fun getSnippet(): String = description
}
