/*
 * Copyright (C) 2022 Budi Ardianata.
 */
package com.exam.storyapp.common.utils

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import java.io.InputStreamReader

object JsonFileReader {
    fun getJson(filename: String): String {
        try {
            val applicationContext = ApplicationProvider.getApplicationContext<Context>()
            val inputStream = applicationContext.assets.open(filename)
            val builder = StringBuilder()
            val reader = InputStreamReader(inputStream, "UTF-8")
            reader.readLines().forEach {
                builder.append(it)
            }
            return builder.toString()
        } catch (e: Exception) {
            throw e
        }
    }
}
