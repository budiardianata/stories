/*
 * Copyright (C) 2022 Budi Ardianata.
 */
package com.exam.storyapp.di

import com.exam.storyapp.BuildConfig
import com.exam.storyapp.common.annotations.BaseUrl
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
object UrlModule {
    @Provides
    @BaseUrl
    fun providesBaseUrl(): String = BuildConfig.BASE_URL
}
