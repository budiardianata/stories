/*
 * Copyright (C) 2022 Budi Ardianata.
 */
package com.exam.storyapp.data.source.local.preference

import androidx.datastore.core.Serializer
import java.io.InputStream
import java.io.OutputStream
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json

@Suppress("BlockingMethodInNonBlockingContext")
object UserPreferenceSerializer : Serializer<UserPreference> {
    override val defaultValue: UserPreference get() = UserPreference()

    override suspend fun readFrom(input: InputStream): UserPreference {
        return try {
            Json.decodeFromString(
                deserializer = UserPreference.serializer(),
                string = input.readBytes().decodeToString(),
            )
        } catch (e: SerializationException) {
            e.printStackTrace()
            defaultValue
        }
    }

    override suspend fun writeTo(t: UserPreference, output: OutputStream) {
        output.write(
            Json.encodeToString(
                serializer = UserPreference.serializer(),
                value = t,
            ).encodeToByteArray(),
        )
    }
}
