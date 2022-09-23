/*
 * Copyright (C) 2022 Budi Ardianata.
 */
package com.exam.storyapp.ui.home.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.paging.LoadState
import androidx.paging.LoadStateAdapter
import androidx.recyclerview.widget.RecyclerView
import com.exam.storyapp.databinding.ItemLoadingBinding

class StoryLoadStateAdapter(
    private val retryCallback: () -> Unit,
) : LoadStateAdapter<StoryLoadStateAdapter.LoadingStateViewHolder>() {

    override fun onBindViewHolder(holder: LoadingStateViewHolder, loadState: LoadState) {
        holder.bind(loadState, retryCallback)
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        loadState: LoadState,
    ): LoadingStateViewHolder = LoadingStateViewHolder(
        ItemLoadingBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false,
        ),
    )

    inner class LoadingStateViewHolder(private val binding: ItemLoadingBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(loadState: LoadState, retryCallback: () -> Unit) {
            with(binding) {
                if (loadState is LoadState.Error) {
                    errorMsg.text = loadState.error.localizedMessage ?: loadState.error.message
                }
                retryButton.isVisible = loadState is LoadState.Error
                errorMsg.isVisible = loadState is LoadState.Error
                progressBar.isVisible = loadState is LoadState.Loading

                retryButton.setOnClickListener { retryCallback() }
            }
        }
    }
}
