/*
 * Copyright (C) 2022 Budi Ardianata.
 */
package com.exam.storyapp.ui.listMap

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import com.bumptech.glide.Glide
import com.exam.storyapp.databinding.ItemStoryBinding
import com.exam.storyapp.domain.model.Story
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.Marker

class StoryInfoWindowAdapter(private val context: Context) : GoogleMap.InfoWindowAdapter {
    override fun getInfoContents(marker: Marker): View? {
        val place = marker.tag as? Story ?: return null
        ItemStoryBinding.inflate(LayoutInflater.from(context)).apply {
            Glide.with(context).load(place.image).into(ivItemPhoto)
            storyDescription.text = place.description
            return root
        }
    }

    override fun getInfoWindow(marker: Marker): View? {
        return null
    }
}
