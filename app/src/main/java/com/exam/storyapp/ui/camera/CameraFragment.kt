/*
 * Copyright (C) 2022 Budi Ardianata.
 */
package com.exam.storyapp.ui.camera

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.view.OrientationEventListener
import android.view.Surface
import android.view.View
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import androidx.core.os.bundleOf
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.isVisible
import androidx.exifinterface.media.ExifInterface
import androidx.fragment.app.Fragment
import androidx.fragment.app.setFragmentResult
import androidx.navigation.fragment.findNavController
import by.kirich1409.viewbindingdelegate.viewBinding
import com.exam.storyapp.R
import com.exam.storyapp.common.annotations.IODispatcher
import com.exam.storyapp.common.annotations.MainDispatcher
import com.exam.storyapp.common.extensions.*
import com.exam.storyapp.common.util.Constant
import com.exam.storyapp.databinding.FragmentCameraBinding
import dagger.hilt.android.AndroidEntryPoint
import java.io.File
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import javax.inject.Inject
import kotlinx.coroutines.*

@AndroidEntryPoint
class CameraFragment : Fragment(R.layout.fragment_camera) {

    @Inject
    @IODispatcher
    lateinit var ioDispatcher: CoroutineDispatcher

    @Inject
    @MainDispatcher
    lateinit var mainDispatcher: CoroutineDispatcher

    private val binding by viewBinding(FragmentCameraBinding::bind)

    private var cameraProvider: ProcessCameraProvider? = null
    private lateinit var cameraExecutor: ExecutorService
    private var lensFacing = CameraSelector.LENS_FACING_BACK
    private var preview: Preview? = null
    private var imageCapture: ImageCapture? = null
    private var camera: Camera? = null
    private val hasBackCamera
        get() = cameraProvider?.hasCamera(CameraSelector.DEFAULT_BACK_CAMERA) ?: false
    private val hasFrontCamera
        get() = cameraProvider?.hasCamera(CameraSelector.DEFAULT_BACK_CAMERA) ?: false

    private val orientationEventListener by lazy {
        object : OrientationEventListener(requireContext()) {
            override fun onOrientationChanged(orientation: Int) {
                if (orientation == -1) return
                val rotation = when (orientation) {
                    in 45 until 135 -> Surface.ROTATION_270
                    in 135 until 225 -> Surface.ROTATION_180
                    in 225 until 315 -> Surface.ROTATION_90
                    else -> Surface.ROTATION_0
                }
                imageCapture?.targetRotation = rotation
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        cameraExecutor = Executors.newSingleThreadExecutor()
        orientationEventListener.enable()
        requireActivity().window.fitSystemWindows()
        binding.run {
            closeCamera.apply {
                setOnClickListener { findNavController().popBackStack() }
                onWindowInsets { view, windowInsets ->
                    view.topMargin =
                        windowInsets.getInsets(WindowInsetsCompat.Type.systemBars()).top
                }
            }
            capture.setOnClickListener { takePicture() }
            cameraSwitchButton.setOnClickListener {
                lensFacing = if (lensFacing == CameraSelector.LENS_FACING_BACK) {
                    CameraSelector.LENS_FACING_FRONT
                } else {
                    CameraSelector.LENS_FACING_BACK
                }
                it.animate().apply {
                    rotation(180f)
                    duration = 300L
                    setListener(
                        object : AnimatorListenerAdapter() {
                            override fun onAnimationEnd(animation: Animator) {
                                it.rotation = 0f
                            }
                        },
                    )
                    start()
                }
                bindCameraUseCases()
            }
            viewFinder.post {
                startCamera()
            }
        }
    }

    private fun startCamera() {
        binding.capture.isEnabled = false
        val cameraProviderFuture = ProcessCameraProvider.getInstance(requireContext())
        cameraProviderFuture.addListener({
            cameraProvider = cameraProviderFuture.get()
            lensFacing = when {
                hasBackCamera -> CameraSelector.LENS_FACING_BACK
                hasFrontCamera -> CameraSelector.LENS_FACING_FRONT
                else -> throw IllegalStateException("Back and front camera are unavailable")
            }
            updateCameraSwitchButton()
            bindCameraUseCases()
        }, ContextCompat.getMainExecutor(requireContext()),)
    }

    private fun bindCameraUseCases() {
        val rotation = binding.viewFinder.display.rotation
        val cameraProvider =
            cameraProvider ?: throw IllegalStateException("Camera initialization failed.")

        // CameraSelector
        val cameraSelector = cameraLensToSelector(lensFacing)

        // Preview
        preview = Preview.Builder()
            .setTargetAspectRatio(AspectRatio.RATIO_16_9)
            .setTargetRotation(rotation)
            .build()

        // ImageCapture
        imageCapture = ImageCapture.Builder()
            .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
            .setTargetAspectRatio(AspectRatio.RATIO_16_9)
            .setTargetRotation(rotation)
            .build()

        cameraProvider.unbindAll()

        try {
            camera = cameraProvider.bindToLifecycle(
                this,
                cameraSelector,
                preview,
                imageCapture,
            )

            cameraProvider.bindToLifecycle(
                this,
                cameraSelector,
                preview,
                imageCapture,
            )
            preview?.setSurfaceProvider(binding.viewFinder.surfaceProvider)
            binding.capture.isEnabled = true
        } catch (exc: Exception) {
            binding.capture.isEnabled = false
        }
    }

    private fun FragmentCameraBinding.onCaptureEvent(visible: Boolean) {
        capture.isEnabled = !visible
        closeCamera.isEnabled = !visible
        cameraSwitchButton.isEnabled = !visible
        cameraProgress.apply {
            isIndeterminate = visible
            isVisible = visible
        }
    }

    private fun takePicture() {
        binding.onCaptureEvent(true)
        imageCapture?.let { imageCapture ->
            val filename = "${System.currentTimeMillis()}.jpg"
            val folder = File(requireContext().filesDir, "Images").also {
                if (!it.exists()) it.mkdir()
            }
            val outputFile = File(folder, filename)

            val metadata = ImageCapture.Metadata().apply {
                isReversedHorizontal = lensFacing == CameraSelector.LENS_FACING_FRONT
            }

            val outputOptions = ImageCapture.OutputFileOptions.Builder(outputFile)
                .setMetadata(metadata)
                .build()

            imageCapture.takePicture(
                outputOptions,
                ContextCompat.getMainExecutor(requireContext()),
                object : ImageCapture.OnImageSavedCallback {
                    override fun onError(exc: ImageCaptureException) {
                        requireActivity().showSnackbar(getString(R.string.capture_failed))
                    }

                    override fun onImageSaved(output: ImageCapture.OutputFileResults) {
                        fixRotation(outputFile)
                    }
                },
            )
        }
    }

    private fun submitResult(uri: Uri) {
        setFragmentResult(Constant.REQ_IMAGE, bundleOf(Constant.KEY_IMAGE to uri))
        findNavController().navigateUp()
    }

    private fun cameraLensToSelector(@CameraSelector.LensFacing lensFacing: Int): CameraSelector =
        when (lensFacing) {
            CameraSelector.LENS_FACING_FRONT -> CameraSelector.DEFAULT_FRONT_CAMERA
            CameraSelector.LENS_FACING_BACK -> CameraSelector.DEFAULT_BACK_CAMERA
            else -> throw IllegalArgumentException("Invalid lens facing type: $lensFacing")
        }

    private fun updateCameraSwitchButton() {
        try {
            binding.cameraSwitchButton.isEnabled = hasBackCamera && hasFrontCamera
        } catch (e: CameraInfoUnavailableException) {
            binding.cameraSwitchButton.isEnabled = false
        }
    }

    private fun fixRotation(file: File) {
        preview?.setSurfaceProvider(null)
        CoroutineScope(SupervisorJob() + ioDispatcher).launch {
            val orientation = ExifInterface(file).getAttributeInt(
                ExifInterface.TAG_ORIENTATION,
                ExifInterface.ORIENTATION_UNDEFINED,
            )
            val options = BitmapFactory.Options().apply {
                inPreferredConfig = Bitmap.Config.ARGB_8888
            }
            val matrix = exifOrientationToMatrix(orientation)
            val bitmap = BitmapFactory.decodeFile(file.path, options)
            val rotatedBitmap =
                Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
            file.outputStream().use {
                rotatedBitmap.compress(Bitmap.CompressFormat.JPEG, 100, it)
            }
            withContext(mainDispatcher) {
                val uri = requireActivity().getUriProvider(file)
                submitResult(uri)
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        orientationEventListener.disable()
        cameraExecutor.shutdown()
    }
}
