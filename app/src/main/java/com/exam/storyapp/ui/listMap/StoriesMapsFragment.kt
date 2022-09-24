/*
 * Copyright (C) 2022 Budi Ardianata.
 */
package com.exam.storyapp.ui.listMap

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.view.View
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import by.kirich1409.viewbindingdelegate.viewBinding
import com.exam.storyapp.R
import com.exam.storyapp.databinding.FragmentStoriesMapsBinding
import com.exam.storyapp.domain.model.Story
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.gms.maps.model.MapStyleOptions
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.maps.android.clustering.ClusterManager
import com.google.maps.android.ktx.addMarker
import com.google.maps.android.ktx.awaitMap
import com.google.maps.android.ktx.awaitMapLoad
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import net.samystudio.permissionlauncher.RationalePermissionLauncher
import net.samystudio.permissionlauncher.anyOf
import net.samystudio.permissionlauncher.createMultiplePermissionsLauncher

@AndroidEntryPoint
class StoriesMapsFragment : Fragment(R.layout.fragment_stories_maps) {
    private val binding by viewBinding(FragmentStoriesMapsBinding::bind)
    private val viewmodel by viewModels<StoriesMapViewModel>()
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var googleMap: GoogleMap
    private var clusterManager: ClusterManager<Story>? = null
    private val permissions = listOf(
        Manifest.permission.ACCESS_COARSE_LOCATION,
        Manifest.permission.ACCESS_FINE_LOCATION,
    )

    private val locationPermissionLauncher = createMultiplePermissionsLauncher(
        anyOf(*permissions.toTypedArray()),
    )

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity())
        val mapFragment =
            childFragmentManager.findFragmentById(R.id.mapFragment) as SupportMapFragment
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    googleMap = mapFragment.awaitMap()
                    googleMap.run {
                        setMapStyle(
                            MapStyleOptions.loadRawResourceStyle(
                                requireContext(),
                                R.raw.map_style,
                            ),
                        )
                        uiSettings.apply {
                            isZoomControlsEnabled = true
                            isMapToolbarEnabled = true
                        }
                        googleMap.moveCamera(
                            CameraUpdateFactory.newLatLng(
                                LatLng(-6.175221730207895, 106.82718498421391),
                            ),
                        )
                    }
                    googleMap.awaitMapLoad()
                    setupCluster()
                    getCurrentLocation()
                }
                launch {
                    // TODO FLOW LIST FROM VIEWMODEL
                }
            }
        }
    }

    private fun setupCluster() {
        // Create the ClusterManager class and set the custom renderer.
        clusterManager = ClusterManager<Story>(requireContext(), googleMap).apply {
            renderer = StoryRenderer(requireContext(), googleMap, this)

            // Set custom info window adapter
            markerCollection.setInfoWindowAdapter(StoryInfoWindowAdapter(requireContext()))

            // When the camera starts moving, change the alpha value of the marker to translucent
            googleMap.setOnCameraMoveStartedListener {
                markerCollection.markers.forEach { it.alpha = 0.3f }
                clusterMarkerCollection.markers.forEach { it.alpha = 0.3f }
            }

            googleMap.setOnCameraIdleListener {
                // When the camera stops moving, change the alpha value back to opaque
                markerCollection.markers.forEach { it.alpha = 1.0f }
                clusterMarkerCollection.markers.forEach { it.alpha = 1.0f }
                onCameraIdle()
            }
        }
    }

    private fun addStoriesToCluster(stories: List<Story>) {
        clusterManager?.addItems(stories)
        clusterManager?.cluster()
        val bounds = LatLngBounds.builder()
        stories.forEach { bounds.include(LatLng(it.lat, it.lon)) }
        googleMap.moveCamera(CameraUpdateFactory.newLatLngBounds(bounds.build(), 20))
    }

    private fun getCurrentLocation() {
        locationPermissionLauncher.launch(::handleRationalePermission, ::handleDeniedPermission) {
            binding.showErrorMessage(false)
            getMyLastLocation()
        }
    }

    private fun FragmentStoriesMapsBinding.showErrorMessage(isShowing: Boolean) {
        mapFragment.isVisible = !isShowing
        mapError.isVisible = isShowing
        mapErrorButton.isVisible = isShowing
    }

    private fun handleDeniedPermission(permission: Set<String>, neverAskAgain: Boolean) {
        if (neverAskAgain) {
            binding.run {
                mapError.text = getString(
                    R.string.permission_denied,
                    permission.joinToString(", ") { it.split(".").last() },
                )
                mapErrorButton.setOnClickListener {
                    Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                        data = Uri.fromParts("package", requireActivity().packageName, null)
                    }.also(::startActivity)
                }
                showErrorMessage(true)
            }
        }
    }

    private fun checkPermission(): Boolean {
        return permissions.all {
            ContextCompat.checkSelfPermission(
                requireContext(),
                it,
            ) == PackageManager.PERMISSION_GRANTED
        }
    }

    @SuppressLint("MissingPermission")
    private fun getMyLastLocation() {
        if (checkPermission()) {
            fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                if (location != null) {
                    googleMap.isMyLocationEnabled = true
                    val currentLocation = LatLng(location.latitude, location.longitude)
                    googleMap.addMarker { position(currentLocation) }
                    googleMap.animateCamera(CameraUpdateFactory.newLatLng(currentLocation))
                }
            }.addOnFailureListener {
            }
        } else {
            locationPermissionLauncher.launch {
                getMyLastLocation()
            }
        }
    }

    private fun handleRationalePermission(
        permission: Set<String>,
        rationale: RationalePermissionLauncher,
    ) {
        MaterialAlertDialogBuilder(requireContext()).apply {
            setTitle(R.string.permission_rationale)
            setMessage(
                getString(
                    R.string.permission_rationale_message,
                    permission.joinToString(", ") { it.split(".").last() },
                ),
            )
            setPositiveButton(android.R.string.ok) { dialog, _ ->
                rationale.accept()
                dialog.dismiss()
            }
            setNegativeButton(android.R.string.cancel) { dialog, _ ->
                rationale.cancel()
                dialog.dismiss()
            }
        }.show()
    }
}
