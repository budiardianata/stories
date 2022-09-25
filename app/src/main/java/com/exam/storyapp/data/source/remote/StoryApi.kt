/*
 * Copyright (C) 2022 Budi Ardianata.
 */
package com.exam.storyapp.data.source.remote

import com.exam.storyapp.data.source.remote.adapter.NetworkResult
import com.exam.storyapp.data.source.remote.response.DefaultResponse
import com.exam.storyapp.data.source.remote.response.LoginResponse
import com.exam.storyapp.data.source.remote.response.StoriesResponse
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.http.*

interface StoryApi {

    @FormUrlEncoded
    @POST("register")
    suspend fun register(
        @Field("name") name: String,
        @Field("email") email: String,
        @Field("password") password: String,
    ): NetworkResult<DefaultResponse>

    @FormUrlEncoded
    @POST("login")
    suspend fun login(
        @Field("email") email: String,
        @Field("password") password: String,
    ): NetworkResult<LoginResponse>

    @Multipart
    @POST("stories")
    suspend fun uploadStory(
        @Part("description") description: RequestBody,
        @Part("lat") lat: RequestBody?,
        @Part("lon") lon: RequestBody?,
        @Part body: MultipartBody.Part,
    ): NetworkResult<DefaultResponse>

    @GET("stories")
    suspend fun getStories(
        @Query("page") page: Int,
        @Query("size") size: Int = 10,
        @Query("location") location: Int = 0,
    ): StoriesResponse
}
