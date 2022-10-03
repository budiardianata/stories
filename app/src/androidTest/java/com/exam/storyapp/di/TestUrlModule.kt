/*
 * Copyright (C) 2022 Budi Ardianata.
 */
package com.exam.storyapp.di

import com.exam.storyapp.common.annotations.BaseUrl
import dagger.Module
import dagger.Provides
import dagger.hilt.components.SingletonComponent
import dagger.hilt.testing.TestInstallIn

@Module
@TestInstallIn(
    components = [SingletonComponent::class],
    replaces = [UrlModule::class],
)
object TestUrlModule {
    @Provides
    @BaseUrl
    fun providesBaseUrl(): String = "http://127.0.0.1:8080/"
}
