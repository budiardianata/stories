/*
 * Copyright (C) 2022 Budi Ardianata.
 */
package com.exam.storyapp.widget

import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.widget.AdapterView
import android.widget.RemoteViews
import android.widget.RemoteViewsService
import androidx.core.os.bundleOf
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.exam.storyapp.R
import com.exam.storyapp.common.util.Constant
import com.exam.storyapp.common.util.Resource
import com.exam.storyapp.domain.model.Story
import com.exam.storyapp.domain.repositories.StoryRepository
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking

class StoryRemoteViewsFactory(
    private val context: Context,
    private val storyRepository: StoryRepository,
) : RemoteViewsService.RemoteViewsFactory {
    private var storyItems = listOf<Story>()

    override fun onCreate() {}

    override fun onDestroy() {}

    /**
     * TODO (if possible) :Called from Coroutine Scope + SupervisorJob
     */
    override fun onDataSetChanged() = runBlocking {
        storyItems = when (val result = storyRepository.getStories(10).first()) {
            is Resource.Error -> emptyList()
            is Resource.Success -> {
                result.data
            }
        }
    }

    override fun getCount(): Int {
        return storyItems.size
    }

    override fun getViewAt(position: Int): RemoteViews {
        if (position == AdapterView.INVALID_POSITION || storyItems.isEmpty()) {
            return RemoteViews(context.packageName, R.layout.widget_item_loading)
        }
        val story = storyItems[position]
        val photo = try {
            Glide.with(context)
                .applyDefaultRequestOptions(
                    RequestOptions()
                        .override(100, 100)
                        .centerCrop()
                        .error(R.drawable.ic_image_placeholder)
                )
                .asBitmap()
                .load(story.image)
                .submit()
                .get()
        } catch (e: Exception) {
            BitmapFactory.decodeResource(context.resources, R.drawable.ic_image_placeholder)
        }
        val fillInIntent = Intent().apply {
            putExtras(bundleOf(Constant.KEY_STORY to story))
        }
        return RemoteViews(context.packageName, R.layout.widget_item).apply {
            setTextViewText(R.id.widget_item_description, story.description)
            setTextViewText(R.id.widget_item_name, story.createdBy)
            setImageViewBitmap(R.id.widget_item_image, photo)
            setOnClickFillInIntent(R.id.widget_item_root, fillInIntent)
        }
    }

    override fun getLoadingView(): RemoteViews =
        RemoteViews(context.packageName, R.layout.widget_item_loading)

    override fun getViewTypeCount(): Int = 1

    override fun getItemId(position: Int): Long {
        if (position == AdapterView.INVALID_POSITION || storyItems.isEmpty()) {
            return 0
        }
        return storyItems[position].hashCode().toLong()
    }

    override fun hasStableIds(): Boolean = true
}
