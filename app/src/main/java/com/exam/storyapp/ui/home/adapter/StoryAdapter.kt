/*
 * Copyright (C) 2022 Budi Ardianata.
 */
package com.exam.storyapp.ui.home.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.exam.storyapp.R
import com.exam.storyapp.databinding.ItemStoryBinding
import com.exam.storyapp.domain.model.Story

class StoryAdapter(
    private val onItemClick: (View, Story) -> Unit,
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
        fun bind(story: Story, itemClick: (View, Story) -> Unit) {
            with(binding) {
                storyDescription.text = story.description
                tvItemName.text = story.createdBy
                storyCreatedAt.text = story.createAtFormatted
                Glide.with(itemView.context)
                    .applyDefaultRequestOptions(
                        RequestOptions().placeholder(R.drawable.ic_image_placeholder),
                    )
                    .load(story.image)
                    .into(ivItemPhoto)
                storyCard.transitionName = storyCard.context.getString(
                    R.string.story_card_item_transition_name,
                    story.id,
                )
                storyCard.setOnClickListener {
                    itemClick(it, story)
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
