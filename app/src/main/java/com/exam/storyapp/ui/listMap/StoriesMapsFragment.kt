/*
 * Copyright (C) 2022 Budi Ardianata.
 */
package com.exam.storyapp.ui.listMap

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.core.os.bundleOf
import androidx.core.view.MenuProvider
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.onNavDestinationSelected
import androidx.navigation.ui.setupWithNavController
import by.kirich1409.viewbindingdelegate.viewBinding
import com.exam.storyapp.R
import com.exam.storyapp.common.extensions.action
import com.exam.storyapp.common.extensions.showSnackbar
import com.exam.storyapp.common.util.Constant
import com.exam.storyapp.databinding.FragmentStoriesMapsBinding
import com.exam.storyapp.domain.model.Story
import com.exam.storyapp.domain.model.UiState
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.gms.maps.model.MapStyleOptions
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import com.google.maps.android.clustering.ClusterManager
import com.google.maps.android.ktx.awaitMap
import com.google.maps.android.ktx.awaitMapLoad
import dagger.hilt.android.AndroidEntryPoint
import kotlin.properties.Delegates
import kotlinx.coroutines.launch
import net.samystudio.permissionlauncher.RationalePermissionLauncher
import net.samystudio.permissionlauncher.anyOf
import net.samystudio.permissionlauncher.createMultiplePermissionsLauncher

@AndroidEntryPoint
class StoriesMapsFragment : Fragment(R.layout.fragment_stories_maps) {
    private val binding by viewBinding(FragmentStoriesMapsBinding::bind)
    private val viewmodel by viewModels<StoriesMapViewModel>()
    private var currentLocation by Delegates.observable(LatLng(0.0, 0.0)) { _, _, new ->
        googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new, 5f))
    }

    private var perPageList = 10

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
        binding.setUpToolbar()
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity())
        val mapFragment = binding.mapFragment.getFragment<SupportMapFragment>()
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    googleMap = mapFragment.awaitMap()
                    googleMap.setupStyle()
                    viewmodel.dispatchEvent(StoriesMapEvent.MapReady)
                    setupCluster()
                    googleMap.awaitMapLoad()
                }
                launch {
                    viewmodel.stories.collect {
                        when (it) {
                            is UiState.Error -> Toast.makeText(
                                requireContext(),
                                it.exception.toString(),
                                Toast.LENGTH_SHORT,
                            ).show()
                            is UiState.Loading -> {
                            }
                            is UiState.Success -> addStoriesToCluster(it.data)
                        }
                    }
                }
                launch {
                    viewmodel.numLoadedStories.collect {
                        perPageList = it
                    }
                }
            }
        }
    }

    private fun FragmentStoriesMapsBinding.setUpToolbar() {
        val navController = findNavController()
        val appBarConfiguration = AppBarConfiguration(navController.graph)
        topAppBar.setupWithNavController(navController, appBarConfiguration)
        topAppBar.addMenuProvider(
            object : MenuProvider {
                override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                    menuInflater.inflate(R.menu.menu_map, menu)
                }

                override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
                    return when (menuItem.itemId) {
                        R.id.action_filter -> {
                            showPerPageOption()
                            true
                        }
                        else -> menuItem.onNavDestinationSelected(findNavController())
                    }
                }
            },
        )
    }

    private fun GoogleMap.setupStyle() {
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
    }

    private fun setupCluster() {
        // Create the ClusterManager class and set the custom renderer.
        if (clusterManager == null) {
            clusterManager = ClusterManager<Story>(requireContext(), googleMap)
            clusterManager?.let { cluster ->
                cluster.renderer = StoryRenderer(requireContext(), googleMap, cluster)
                cluster.markerCollection.setInfoWindowAdapter(StoryInfoWindowAdapter(requireContext()))
                cluster.setOnClusterClickListener {
                    val builder = LatLngBounds.builder()
                    for (item in it.items) {
                        builder.include(item.position)
                    }
                    val bounds = builder.build()
                    googleMap.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds, 100))
                    true
                }
                cluster.setOnClusterItemInfoWindowClickListener {
                    findNavController().navigate(
                        resId = R.id.action_storiesMaps_to_detailFragment,
                        args = bundleOf(Constant.KEY_STORY to it),
                    )
                }

                // When the camera starts moving, change the alpha value of the marker to translucent
                googleMap.setOnCameraMoveStartedListener {
                    cluster.markerCollection.markers.forEach { it.alpha = 0.3f }
                    cluster.clusterMarkerCollection.markers.forEach { it.alpha = 0.3f }
                }
                googleMap.setOnCameraIdleListener {
                    // When the camera stops moving, change the alpha value back to opaque
                    cluster.markerCollection.markers.forEach { it.alpha = 1.0f }
                    cluster.clusterMarkerCollection.markers.forEach { it.alpha = 1.0f }
                    cluster.onCameraIdle()
                }
            }
            getCurrentLocation()
        }
    }

    private fun addStoriesToCluster(stories: List<Story>) {
        if (clusterManager != null) {
            clusterManager?.clearItems()
            clusterManager?.addItems(stories)
            clusterManager?.cluster()
        }
    }

    @SuppressLint("MissingPermission")
    private fun getCurrentLocation() {
        locationPermissionLauncher.launch(::handleRationalePermission, ::handleDeniedPermission) {
            googleMap.isMyLocationEnabled = true
            fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                if (location != null) {
                    currentLocation = LatLng(location.latitude, location.longitude)
                }
            }.addOnFailureListener {
                currentLocation = LatLng(-6.175221730207895, 106.82718498421391)
            }
        }
    }

    private fun handleDeniedPermission(permission: Set<String>, neverAskAgain: Boolean) {
        if (neverAskAgain) {
            requireActivity().showSnackbar(
                getString(
                    R.string.permission_rationale_message,
                    permission.joinToString(", ") { it.split(".").last() },
                ),
                Snackbar.LENGTH_INDEFINITE,
            ) {
                action(R.string.permission_grant) {
                    Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                        data = Uri.fromParts("package", requireActivity().packageName, null)
                    }.also(::startActivity)
                }
            }
        }
    }

    private fun showPerPageOption() {
        val list = arrayOf(10, 25, 50, 100)
        var selectedIndex = list.indexOfFirst { it == perPageList }
        MaterialAlertDialogBuilder(requireContext()).apply {
            setTitle(R.string.filter_map)
            setSingleChoiceItems(
                list.map { it.toString() }.toTypedArray(),
                list.indexOfFirst { it == perPageList },
            ) { _, which ->
                selectedIndex = which
            }
            setPositiveButton(android.R.string.ok) { dialog, _ ->
                viewmodel.dispatchEvent(StoriesMapEvent.StoriesLoaded(list[selectedIndex]))
                dialog.dismiss()
            }
            setNegativeButton(android.R.string.cancel) { dialog, _ ->
                dialog.dismiss()
            }
        }.show()
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
