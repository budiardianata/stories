/*
 * Copyright (C) 2022 Budi Ardianata.
 */
package com.exam.storyapp.di

import androidx.datastore.core.DataStore
import com.exam.storyapp.common.util.Constant
import com.exam.storyapp.data.source.local.preference.UserPreference
import com.exam.storyapp.data.source.remote.StoryApi
import com.exam.storyapp.data.source.remote.adapter.NetworkCallAdapterFactory
import com.exam.storyapp.data.source.remote.interceptor.StoryHeaderInterceptor
import com.google.gson.GsonBuilder
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {
    @Singleton
    @Provides
    fun provideHeaderInterceptor(dataStore: DataStore<UserPreference>): StoryHeaderInterceptor {
        return StoryHeaderInterceptor(dataStore)
    }

    @Singleton
    @Provides
    fun provideOkHttpClient(headerInterceptor: StoryHeaderInterceptor): OkHttpClient {
        val builder = OkHttpClient.Builder()
        val logging = HttpLoggingInterceptor()
        logging.level = HttpLoggingInterceptor.Level.BODY
        return builder
            .addInterceptor(logging)
            .addInterceptor(headerInterceptor)
            .build()
    }

    @Singleton
    @Provides
    fun provideRetrofit(okHttpClient: OkHttpClient): Retrofit {
        return Retrofit.Builder()
            .baseUrl(Constant.API_URL)
            .addConverterFactory(
                GsonConverterFactory.create(
                    GsonBuilder().create(),
                ),
            )
            .addCallAdapterFactory(NetworkCallAdapterFactory.create())
            .client(okHttpClient)
            .build()
    }

    @Provides
    @Singleton
    fun provideStoryApi(retrofit: Retrofit): StoryApi {
        return retrofit.create(StoryApi::class.java)
    }
}
