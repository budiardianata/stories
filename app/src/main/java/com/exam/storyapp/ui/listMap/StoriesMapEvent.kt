/*
 * Copyright (C) 2022 Budi Ardianata.
 */
package com.exam.storyapp.ui.listMap

sealed class StoriesMapEvent {
    object MapReady : StoriesMapEvent()
    data class StoriesLoaded(val value: Int) : StoriesMapEvent()
}
