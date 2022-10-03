/*
 * Copyright (C) 2022 Budi Ardianata.
 */
package com.exam.storyapp.ui.create

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.MenuRes
import androidx.appcompat.widget.PopupMenu
import androidx.core.os.bundleOf
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.Fragment
import androidx.fragment.app.setFragmentResult
import androidx.fragment.app.setFragmentResultListener
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupWithNavController
import by.kirich1409.viewbindingdelegate.viewBinding
import com.exam.storyapp.R
import com.exam.storyapp.common.extensions.*
import com.exam.storyapp.common.util.Constant
import com.exam.storyapp.common.util.ImageCompressor
import com.exam.storyapp.databinding.FragmentCreateStoryBinding
import com.exam.storyapp.domain.model.FormState
import com.exam.storyapp.domain.model.UiState
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.model.LatLng
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.transition.MaterialContainerTransform
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import net.samystudio.permissionlauncher.RationalePermissionLauncher
import net.samystudio.permissionlauncher.createPermissionLauncher

@AndroidEntryPoint
class CreateStoryFragment : Fragment(R.layout.fragment_create_story) {
    private val binding by viewBinding(FragmentCreateStoryBinding::bind)
    private val cameraPermissionLauncher = createPermissionLauncher(Manifest.permission.CAMERA)
    private val locationPermissionLauncher =
        createPermissionLauncher(Manifest.permission.ACCESS_COARSE_LOCATION)
    private val storagePermissionLauncher =
        createPermissionLauncher(Manifest.permission.READ_EXTERNAL_STORAGE)
    private val viewmodel by viewModels<CreateStoryViewModel>()
    private val pickImage = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        processImage(uri)
    }
    private lateinit var fusedLocationClient: FusedLocationProviderClient

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        sharedElementEnterTransition = MaterialContainerTransform().apply {
            drawingViewId = R.id.nav_host_fragment
            startDelay = resources.getInteger(R.integer.anim_duration_short).toLong()
            duration = resources.getInteger(R.integer.anim_duration_long).toLong()
            scrimColor = Color.TRANSPARENT
            setAllContainerColors(requireContext().themeColor(com.google.android.material.R.attr.colorSurface))
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setFragmentResultListener(Constant.REQ_IMAGE) { _, bundle ->
            bundle.parcelable<Uri>(Constant.KEY_IMAGE)?.let {
                processImage(it)
            }
        }
        val navController = findNavController()
        val appBarConfiguration = AppBarConfiguration(navController.graph)
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity())
        binding.run {
            toolbarDetail.setupWithNavController(navController, appBarConfiguration)
            buttonAdd.setOnClickListener { viewmodel.dispatchEvent(CreateStoryEvent.CreateStory) }
            addImageChooser.setOnClickListener { showMenu(it, R.menu.menu_add_image) }
            edAddDescription.doAfterTextChanged {
                viewmodel.dispatchEvent(CreateStoryEvent.AddDescription(it.toString()))
            }
            switchShareLocation.setOnCheckedChangeListener { buttonView, isChecked ->
                if (buttonView.isPressed) {
                    if (isChecked) {
                        getCurrentLocation {
                            viewmodel.dispatchEvent(CreateStoryEvent.AddLocation(it))
                        }
                    } else {
                        viewmodel.dispatchEvent(CreateStoryEvent.AddLocation(null))
                    }
                }
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.RESUMED) {
                launch {
                    viewmodel.formState.collect {
                        when (it) {
                            is FormState.Submit -> handleUiState(it.submitState)
                            is FormState.Validating -> binding.buttonAdd.isEnabled = it.isValid
                        }
                    }
                }
            }
        }
    }

    private fun processImage(uri: Uri?) {
        binding.previewImage.setImageURI(uri)
        if (uri != null) {
            viewLifecycleOwner.lifecycleScope.launch {
                val imageFile = ImageCompressor.compress(requireContext(), uri)
                viewmodel.dispatchEvent(CreateStoryEvent.AddImage(imageFile))
            }
        } else {
            viewmodel.dispatchEvent(CreateStoryEvent.AddImage(null))
        }
    }

    @SuppressLint("MissingPermission")
    private fun getCurrentLocation(callback: (location: LatLng) -> Unit) {
        locationPermissionLauncher.launch(::handleRationale, ::handleDenied) {
            fusedLocationClient.locationAvailability.addOnSuccessListener { locationAvailability ->
                if (locationAvailability.isLocationAvailable) {
                    fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                        location?.let {
                            callback(LatLng(it.latitude, it.longitude))
                        } ?: run {
                            showErrorLocation()
                        }
                    }
                } else {
                    showErrorLocation()
                }
            }
        }
    }

    private fun showErrorLocation() {
        Toast.makeText(
            requireContext(),
            requireContext().getString(R.string.error_location),
            Toast.LENGTH_SHORT,
        ).show()
        binding.switchShareLocation.isChecked = false
    }

    private fun FragmentCreateStoryBinding.formEnable(isEnable: Boolean) {
        buttonAdd.isLoading = isEnable.not()
        addImageChooser.isEnabled = isEnable
        edAddDescription.isEnabled = isEnable
    }

    private fun handleUiState(state: UiState<String>) {
        when (state) {
            is UiState.Loading -> {
                binding.formEnable(false)
                requireActivity().hideKeyboard()
            }
            is UiState.Error -> {
                binding.formEnable(true)
                requireActivity().showSnackbar(
                    state.exception.toString(requireContext()),
                )
            }
            is UiState.Success -> {
                binding.formEnable(true)
                Toast.makeText(requireContext(), state.data, Toast.LENGTH_SHORT).show()
                setFragmentResult(Constant.REQ_REFRESH, bundleOf(Constant.KEY_REFRESH to true))
                findNavController().navigateUp()
            }
        }
    }

    private fun showMenu(v: View, @MenuRes menuRes: Int) {
        val popup = PopupMenu(requireContext(), v)
        popup.menuInflater.inflate(menuRes, popup.menu)
        popup.setOnMenuItemClickListener {
            when (it.itemId) {
                R.id.action_gallery -> {
                    launchPickGallery()
                    true
                }
                R.id.action_camera -> {
                    launchPickCamera()
                    true
                }
                else -> false
            }
        }
        popup.show()
    }

    private fun launchPickGallery() {
        storagePermissionLauncher.launch(::handleRationale, ::handleDenied) {
            pickImage.launch("image/*")
        }
    }

    private fun launchPickCamera() {
        cameraPermissionLauncher.launch(::handleRationale, ::handleDenied) {
            findNavController().navigate(R.id.action_addStory_to_cameraFragment)
        }
    }

    private fun handleDenied(permission: String, neverAskAgain: Boolean) {
        if (neverAskAgain) {
            with(requireActivity()) {
                showSnackbar(
                    getString(R.string.permission_denied, permission.split(".").last()),
                    Snackbar.LENGTH_INDEFINITE,
                ) {
                    action(R.string.permission_grant) {
                        Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                            data = Uri.fromParts("package", packageName, null)
                        }.also(::startActivity)
                    }
                }
            }
        }
    }

    private fun handleRationale(permission: String, rationale: RationalePermissionLauncher) {
        MaterialAlertDialogBuilder(requireContext()).apply {
            setTitle(R.string.permission_rationale)
            setMessage(
                getString(
                    R.string.permission_rationale_message,
                    permission.split(".").last(),
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

    override fun onDestroy() {
        super.onDestroy()
        pickImage.unregister()
    }
}
