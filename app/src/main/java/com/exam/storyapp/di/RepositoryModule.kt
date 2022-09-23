/*
 * Copyright (C) 2022 Budi Ardianata.
 */
package com.exam.storyapp.di

import com.exam.storyapp.data.StoryRepositoryImpl
import com.exam.storyapp.data.UserRepositoryImpl
import com.exam.storyapp.domain.repositories.StoryRepository
import com.exam.storyapp.domain.repositories.UserRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
interface RepositoryModule {
    @Binds
    @Singleton
    fun bindUserRepository(repository: UserRepositoryImpl): UserRepository

    @Binds
    @Singleton
    fun bindStoryRepository(repository: StoryRepositoryImpl): StoryRepository
}
