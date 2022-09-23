/*
 * Copyright (C) 2022 Budi Ardianata.
 */
package com.exam.storyapp.data.source.remote.adapter

import com.exam.storyapp.R
import com.exam.storyapp.common.util.StringWrapper
import com.exam.storyapp.data.source.remote.response.DefaultResponse
import com.google.gson.Gson
import okhttp3.Request
import okio.Timeout
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class NetworkResultCall<T : Any>(
    private val proxy: Call<T>,
) : Call<NetworkResult<T>> {
    override fun enqueue(callback: Callback<NetworkResult<T>>) {
        proxy.enqueue(
            object : Callback<T> {
                override fun onResponse(call: Call<T>, response: Response<T>) {
                    val networkResult = handleResponse(response)
                    callback.onResponse(this@NetworkResultCall, Response.success(networkResult))
                }

                override fun onFailure(call: Call<T>, t: Throwable) {
                    val result = NetworkResult.Exception<T>(t)
                    callback.onResponse(this@NetworkResultCall, Response.success(result))
                }
            },
        )
    }

    private fun handleResponse(response: Response<T>): NetworkResult<T> {
        val body = response.body()
        val code = response.code()
        return if (response.isSuccessful && body != null) {
            NetworkResult.Success(body)
        } else {
            val s = try {
                val error =
                    Gson().fromJson(response.errorBody()?.charStream(), DefaultResponse::class.java)
                StringWrapper.Dynamic(error.message)
            } catch (e: Exception) {
                StringWrapper.Resource(R.string.email)
            }
            NetworkResult.Error(code, s)
        }
    }

    override fun execute(): Response<NetworkResult<T>> = throw NotImplementedError()

    override fun clone(): Call<NetworkResult<T>> = NetworkResultCall(proxy.clone())

    override fun request(): Request = proxy.request()

    override fun timeout(): Timeout = proxy.timeout()

    override fun isExecuted(): Boolean = proxy.isExecuted

    override fun isCanceled(): Boolean = proxy.isCanceled

    override fun cancel() {
        proxy.cancel()
    }
}
