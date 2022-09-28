/*
 * Copyright (C) 2022 Budi Ardianata.
 */
package com.exam.storyapp.widget

import android.content.Intent
import android.widget.RemoteViewsService
import com.exam.storyapp.domain.repositories.StoryRepository
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class StoryWidgetService : RemoteViewsService() {

    @Inject
    lateinit var storyRepository: StoryRepository

    override fun onGetViewFactory(intent: Intent): RemoteViewsFactory {
        return StoryRemoteViewsFactory(this.applicationContext, storyRepository)
    }
}
