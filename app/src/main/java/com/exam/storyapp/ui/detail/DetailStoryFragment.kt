/*
 * Copyright (C) 2022 Budi Ardianata.
 */
package com.exam.storyapp.ui.detail

import android.graphics.Color
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.text.method.ScrollingMovementMethod
import android.view.View
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import by.kirich1409.viewbindingdelegate.viewBinding
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.exam.storyapp.R
import com.exam.storyapp.common.extensions.*
import com.exam.storyapp.common.util.Constant
import com.exam.storyapp.databinding.FragmentDetailStoryBinding
import com.exam.storyapp.domain.model.Story
import com.google.android.material.transition.MaterialContainerTransform
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class DetailStoryFragment : Fragment(R.layout.fragment_detail_story) {
    private val binding by viewBinding(FragmentDetailStoryBinding::bind)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        sharedElementEnterTransition = MaterialContainerTransform().apply {
            drawingViewId = R.id.nav_host_fragment
            duration = resources.getInteger(R.integer.anim_duration_long).toLong()
            scrimColor = Color.TRANSPARENT
            setAllContainerColors(requireContext().themeColor(com.google.android.material.R.attr.colorSurface))
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val story = arguments?.parcelable<Story>(Constant.KEY_STORY)
        requireActivity().window.fitSystemWindows()
        story?.let {
//            postponeEnterTransition()
            binding.run {
                Glide.with(requireContext())
                    .load(story.image)
                    .addListener(
                        object : RequestListener<Drawable> {
                            override fun onLoadFailed(
                                e: GlideException?,
                                model: Any?,
                                target: Target<Drawable>?,
                                isFirstResource: Boolean,
                            ): Boolean {
                                // Continue enter animation after image loaded
                                startPostponedEnterTransition()
                                return false
                            }

                            override fun onResourceReady(
                                resource: Drawable?,
                                model: Any?,
                                target: Target<Drawable>?,
                                dataSource: DataSource?,
                                isFirstResource: Boolean,
                            ): Boolean {
                                startPostponedEnterTransition()
                                return false
                            }
                        }
                    )
                    .into(this.ivDetailPhoto)
                ivDetailPhoto.contentDescription = story.description
                tvDetailName.text = story.createdBy
                tvDetailDescription.apply {
                    text = story.description
                    movementMethod = ScrollingMovementMethod()
                    background.alpha = 255 / 2
                }
                tvDetailDate.text = story.createAtFormatted
                closeDetail.apply {
                    setOnClickListener { findNavController().navigateUp() }
                    onWindowInsets { view, windowInsets ->
                        view.topMargin =
                            windowInsets.getInsets(WindowInsetsCompat.Type.systemBars()).top
                    }
                }
            }
        }
    }
}
