/*
 * Copyright (C) 2022 Budi Ardianata.
 */
package com.exam.storyapp.common.util

object Constant {
    private const val packageName = "com.exam.storyapp"

    const val API_URL = "https://story-api.dicoding.dev/v1/"
    const val KEY_EMAIL = "email"
    const val KEY_DESCRIPTIONS = "descriptions"
    const val KEY_IMAGE = "image"
    const val KEY_NAME = "name"
    const val KEY_PASSWORD = "password"
    const val KEY_REFRESH = "refresh"
    const val KEY_STORY = "story"
    const val PAGING_INITIAL_PAGE = 1
    const val PAGING_PER_PAGE = 5
    const val PASSWORD_MIN = 6
    const val PREFERENCE_NAME = "story_app"
    const val REQ_IMAGE = "IMAGE_REQUEST"
    const val REQ_REFRESH = "REFRESH_REQUEST"
    const val WIDGET_ITEM_CLICK_ACTION = "$packageName.action.WIDGET_ITEM_CLICK"
    const val WIDGET_REFRESH_ACTION = "$packageName.action.WIDGET_REFRESH"
}
