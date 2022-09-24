/*
 * Copyright (C) 2022 Budi Ardianata.
 */
package com.exam.storyapp.ui.listMap

import android.content.Context
import com.exam.storyapp.R
import com.exam.storyapp.common.extensions.getBitmapDescriptor
import com.exam.storyapp.domain.model.Story
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.maps.android.clustering.ClusterManager
import com.google.maps.android.clustering.view.DefaultClusterRenderer

class StoryRenderer(
    private val context: Context,
    map: GoogleMap,
    clusterManager: ClusterManager<Story>,
) : DefaultClusterRenderer<Story>(context, map, clusterManager) {

    override fun onBeforeClusterItemRendered(item: Story, markerOptions: MarkerOptions) {
        markerOptions
            .title(item.description)
            .position(LatLng(item.lat, item.lon))
            .icon(
                context.getBitmapDescriptor(
                    R.drawable.ic_user,
                    androidx.appcompat.R.attr.colorPrimary,
                ),
            )
    }

    override fun onClusterItemRendered(clusterItem: Story, marker: Marker) {
        marker.tag = clusterItem
    }
}
