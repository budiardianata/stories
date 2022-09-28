/*
 * Copyright (C) 2022 Budi Ardianata.
 */
package com.exam.storyapp.ui.home.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.ViewCompat
import androidx.navigation.fragment.FragmentNavigator
import androidx.navigation.fragment.FragmentNavigatorExtras
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.exam.storyapp.R
import com.exam.storyapp.common.extensions.getProgressDrawable
import com.exam.storyapp.databinding.ItemStoryBinding
import com.exam.storyapp.domain.model.Story

class StoryAdapter(
    private val onItemClick: (FragmentNavigator.Extras, Story) -> Unit,
) : PagingDataAdapter<Story, StoryAdapter.StoryViewHolder>(DIFF_CALLBACK) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = StoryViewHolder(
        ItemStoryBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false,
        ),
    )

    override fun onBindViewHolder(holder: StoryViewHolder, position: Int) {
        getItem(position)?.let {
            holder.bind(it, onItemClick)
        }
    }

    inner class StoryViewHolder(private val binding: ItemStoryBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(story: Story, itemClick: (FragmentNavigator.Extras, Story) -> Unit) {
            with(binding) {
                val context = itemView.context
                storyDescription.text = story.description
                tvItemName.text = story.createdBy
                storyCreatedAt.text = story.createAtFormatted
                Glide.with(context)
                    .load(story.image)
                    .transition(DrawableTransitionOptions.withCrossFade())
                    .placeholder(
                        context.getProgressDrawable().apply {
                            start()
                        },
                    )
                    .into(ivItemPhoto)
                ViewCompat.setTransitionName(
                    ivItemPhoto,
                    context.getString(R.string.story_image_item_transition_name, story.id),
                )
                ViewCompat.setTransitionName(
                    tvItemName,
                    context.getString(R.string.story_name_item_transition_name, story.id),
                )
                ViewCompat.setTransitionName(
                    storyCreatedAt,
                    context.getString(R.string.story_date_item_transition_name, story.id),
                )
                ViewCompat.setTransitionName(
                    storyDescription,
                    context.getString(R.string.story_desc_item_transition_name, story.id),
                )
                ViewCompat.setTransitionName(
                    storyCard,
                    context.getString(R.string.story_card_item_transition_name, story.id),
                )
                val extras = FragmentNavigatorExtras(
                    storyCard to context.getString(R.string.story_card_detail_transition_name),
                    ivItemPhoto to context.getString(R.string.story_image_detail_transition_name),
                )
                storyCard.setOnClickListener {
                    itemClick(extras, story)
                }
            }
        }
    }

    companion object {
        private val DIFF_CALLBACK = object : DiffUtil.ItemCallback<Story>() {
            override fun areItemsTheSame(oldItem: Story, newItem: Story): Boolean {
                return oldItem == newItem
            }

            override fun areContentsTheSame(oldItem: Story, newItem: Story): Boolean {
                return oldItem.id == newItem.id
            }
        }
    }
}
