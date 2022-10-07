/*
 * Copyright (C) 2022 Budi Ardianata.
 */
package com.exam.storyapp.ui.listMap

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.Drawable
import android.view.LayoutInflater
import android.view.View
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.exam.storyapp.R
import com.exam.storyapp.databinding.MapMarkerBinding
import com.exam.storyapp.domain.model.Story
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.*
import com.google.android.material.shape.CornerFamily
import com.google.android.material.shape.ShapeAppearanceModel
import com.google.maps.android.clustering.ClusterManager
import com.google.maps.android.clustering.view.DefaultClusterRenderer

class StoryRenderer(
    private val context: Context,
    map: GoogleMap,
    clusterManager: ClusterManager<Story>,
) : DefaultClusterRenderer<Story>(context, map, clusterManager) {
    private val layout by lazy { MapMarkerBinding.inflate(LayoutInflater.from(context)) }
    override fun onBeforeClusterItemRendered(item: Story, markerOptions: MarkerOptions) {
        markerOptions
            .title(item.description)
            .position(LatLng(item.lat, item.lon))
            .icon(layout.toBitmapDescriptor())
        super.onBeforeClusterItemRendered(item, markerOptions)
    }

    private fun MapMarkerBinding.toBitmapDescriptor(): BitmapDescriptor {
        root.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED)
        val bitmap =
            Bitmap.createBitmap(root.measuredWidth, root.measuredHeight, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        root.layout(0, 0, root.measuredWidth, root.measuredHeight)
        root.draw(canvas)
        return BitmapDescriptorFactory.fromBitmap(bitmap)
    }

    private fun getBitmap(context: Context, url: String, listener: (Drawable) -> Unit) {
        Glide.with(context)
            .load(url)
            .dontTransform()
            .dontAnimate()
            .override(40, 80)
            .error(R.drawable.ic_image_placeholder)
            .diskCacheStrategy(DiskCacheStrategy.ALL)
            .into(
                object : CustomTarget<Drawable>() {
                    override fun onLoadCleared(placeholder: Drawable?) {}

                    override fun onResourceReady(
                        resource: Drawable,
                        transition: Transition<in Drawable>?,
                    ) {
                        listener.invoke(resource)
                    }
                }
            )
    }

    override fun onClusterItemRendered(clusterItem: Story, marker: Marker) {
        getBitmap(context, clusterItem.image) {
            layout.markerImage.setImageDrawable(it)
            val shapePathModel = ShapeAppearanceModel.builder()
                .setAllCorners(CornerFamily.ROUNDED, 30.toFloat())
                .build()
            layout.markerImage.shapeAppearanceModel = shapePathModel
            marker.setIcon(layout.toBitmapDescriptor())
        }
        marker.tag = clusterItem
        super.onClusterItemRendered(clusterItem, marker)
    }
}
