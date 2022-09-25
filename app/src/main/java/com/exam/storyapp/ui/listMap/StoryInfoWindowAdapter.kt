/*
 * Copyright (C) 2022 Budi Ardianata.
 */
package com.exam.storyapp.ui.listMap

import android.content.Context
import android.graphics.drawable.Drawable
import android.view.LayoutInflater
import android.view.View
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.exam.storyapp.R
import com.exam.storyapp.databinding.ItemStoryMapBinding
import com.exam.storyapp.domain.model.Story
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.Marker
import com.google.android.material.shape.CornerFamily
import com.google.android.material.shape.ShapeAppearanceModel
import com.google.android.material.shape.TriangleEdgeTreatment

class StoryInfoWindowAdapter(private val context: Context) : GoogleMap.InfoWindowAdapter {
    override fun getInfoWindow(marker: Marker): View? {
        val place = marker.tag as? Story ?: return null
        val binding = ItemStoryMapBinding.inflate(LayoutInflater.from(context))
        binding.run {
            Glide.with(context)
                .load(place.image)
                .listener(MarkerCallback(marker))
                .placeholder(R.drawable.ic_image_placeholder)
                .into(storyImage)
            storyDescription.text = place.description
            storyBy.text = place.createdBy
            val size = context.resources.getDimension(R.dimen.dim_8) // 16dp
            val shapePathModel = ShapeAppearanceModel.builder()
                .setAllCorners(CornerFamily.ROUNDED, 40.toFloat())
                .setBottomEdge(TriangleEdgeTreatment(size, false))
                .build()
            storyCard.shapeAppearanceModel = shapePathModel
        }
        return binding.root
    }

    override fun getInfoContents(marker: Marker): View? = null

    inner class MarkerCallback constructor(private val marker: Marker) :
        RequestListener<Drawable> {

        private fun onSuccess() {
            if (marker.isInfoWindowShown) {
                marker.hideInfoWindow()
                marker.showInfoWindow()
            }
        }

        override fun onLoadFailed(
            e: GlideException?,
            model: Any?,
            target: Target<Drawable>?,
            isFirstResource: Boolean,
        ): Boolean {
            return false
        }

        override fun onResourceReady(
            resource: Drawable?,
            model: Any?,
            target: Target<Drawable>?,
            dataSource: DataSource?,
            isFirstResource: Boolean,
        ): Boolean {
            onSuccess()
            return false
        }
    }
}
