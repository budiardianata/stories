/*
 * Copyright (C) 2022 Budi Ardianata.
 */
package com.exam.storyapp.data.source.remote.adapter

import java.lang.reflect.ParameterizedType
import java.lang.reflect.Type
import retrofit2.Call
import retrofit2.CallAdapter
import retrofit2.Retrofit

class NetworkCallAdapterFactory private constructor() : CallAdapter.Factory() {

    override fun get(
        returnType: Type,
        annotations: Array<out Annotation>,
        retrofit: Retrofit,
    ): CallAdapter<*, *>? {
        if (getRawType(returnType) != Call::class.java) {
            return null
        }
        val callType = getParameterUpperBound(0, returnType as ParameterizedType)
        if (getRawType(callType) != NetworkResult::class.java) {
            return null
        }

        val resultType = getParameterUpperBound(0, callType as ParameterizedType)

        return NetworkResultCallAdapter(resultType)
    }

    inner class NetworkResultCallAdapter(
        private val resultType: Type,
    ) : CallAdapter<Type, Call<NetworkResult<Type>>> {
        override fun responseType(): Type = resultType

        override fun adapt(call: Call<Type>): Call<NetworkResult<Type>> {
            return NetworkResultCall(call)
        }
    }

    companion object {
        fun create(): NetworkCallAdapterFactory = NetworkCallAdapterFactory()
    }
}
