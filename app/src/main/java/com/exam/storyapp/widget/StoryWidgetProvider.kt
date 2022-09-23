/*
 * Copyright (C) 2022 Budi Ardianata.
 */
package com.exam.storyapp.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.widget.RemoteViews
import androidx.core.os.bundleOf
import androidx.navigation.NavDeepLinkBuilder
import com.exam.storyapp.R
import com.exam.storyapp.common.extensions.parcelable
import com.exam.storyapp.common.util.Constant
import com.exam.storyapp.common.util.Constant.WIDGET_ITEM_CLICK_ACTION
import com.exam.storyapp.common.util.Constant.WIDGET_REFRESH_ACTION
import com.exam.storyapp.domain.model.Story

/**
 * Implementation of App Widget functionality.
 */
class StoryWidgetProvider : AppWidgetProvider() {
    private val flags: Int
        get() {
            return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE
            } else 0
        }

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray,
    ) {
        appWidgetIds.forEach { appWidgetId ->
            val serviceIntent = Intent(context, StoryWidgetService::class.java).apply {
                putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
                data = Uri.parse(toUri(Intent.URI_INTENT_SCHEME))
            }

            val intentCreate = NavDeepLinkBuilder(context)
                .setGraph(R.navigation.nav_graph)
                .setDestination(R.id.createStoryFragment)
                .createPendingIntent()

            val views = RemoteViews(context.packageName, R.layout.widget_layout).apply {
                setRemoteAdapter(R.id.list_grid_stories, serviceIntent)
                setEmptyView(R.id.list_grid_stories, R.id.empty_grid)
                setPendingIntentTemplate(
                    R.id.list_grid_stories,
                    getSelfIntent(context, appWidgetId, WIDGET_ITEM_CLICK_ACTION),
                )
                setOnClickPendingIntent(
                    R.id.refresh_story_button,
                    getSelfIntent(context, appWidgetId, WIDGET_REFRESH_ACTION),
                )
                setOnClickPendingIntent(R.id.create_story_button, intentCreate)
            }

            appWidgetManager.updateAppWidget(appWidgetId, views)
        }
        super.onUpdate(context, appWidgetManager, appWidgetIds)
    }

    override fun onReceive(context: Context, intent: Intent?) {
        when (intent?.action) {
            WIDGET_ITEM_CLICK_ACTION -> {
                val story = intent.parcelable<Story>(Constant.KEY_STORY)
                val detailStoryIntent = NavDeepLinkBuilder(context)
                    .setGraph(R.navigation.nav_graph)
                    .setDestination(R.id.detailFragment)
                    .setArguments(
                        bundleOf(
                            Constant.KEY_STORY to story,
                        ),
                    )
                    .createPendingIntent()
                detailStoryIntent.send()
            }
            WIDGET_REFRESH_ACTION -> {
                val widgetManager = AppWidgetManager.getInstance(context)
                val componentName = ComponentName(context, StoryWidgetProvider::class.java)
                val widgetId = widgetManager.getAppWidgetIds(componentName)
                widgetManager.notifyAppWidgetViewDataChanged(widgetId, R.id.list_grid_stories)
            }
        }
        super.onReceive(context, intent)
    }

    private fun getSelfIntent(context: Context, appWidgetId: Int, action: String): PendingIntent {
        val toastIntent = Intent(context, StoryWidgetProvider::class.java)
        toastIntent.action = action
        return PendingIntent.getBroadcast(context, appWidgetId, toastIntent, flags)
    }
}
