/*
 * Copyright (C) 2022 Budi Ardianata.
 */
package com.exam.storyapp.data

import androidx.datastore.core.DataStore
import com.exam.storyapp.R
import com.exam.storyapp.common.annotations.IODispatcher
import com.exam.storyapp.common.util.Resource
import com.exam.storyapp.common.util.StringWrapper
import com.exam.storyapp.common.util.wrapEspressoIdlingResource
import com.exam.storyapp.data.model.UserData
import com.exam.storyapp.data.source.local.preference.UserPreference
import com.exam.storyapp.data.source.remote.StoryApi
import com.exam.storyapp.data.source.remote.adapter.NetworkResult
import com.exam.storyapp.domain.model.User
import com.exam.storyapp.domain.repositories.UserRepository
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

@Singleton
class UserRepositoryImpl @Inject constructor(
    @IODispatcher private val ioDispatcher: CoroutineDispatcher,
    private val dataStore: DataStore<UserPreference>,
    private val remoteSource: StoryApi,
) : UserRepository {
    override fun getCurrentUser(): Flow<User> =
        dataStore.data.map { it.user.mapToDomain() }.flowOn(ioDispatcher)

    override suspend fun saveUserSession(user: User) {
        dataStore.updateData {
            it.copy(
                user = UserData(
                    userId = user.id,
                    name = user.name,
                    token = user.token
                )
            )
        }
    }

    override suspend fun signIn(email: String, password: String): Resource<User> =
        withContext(ioDispatcher) {
            wrapEspressoIdlingResource {
                return@withContext when (
                    val response =
                        remoteSource.login(email = email, password = password)
                ) {
                    is NetworkResult.Error -> Resource.Error(response.message!!)
                    is NetworkResult.Exception -> {
                        Resource.Error(response.e.toWrapper())
                    }
                    is NetworkResult.Success -> {
                        Resource.Success(response.data.loginResult.mapToDomain())
                    }
                }
            }
        }

    private fun Throwable.toWrapper(): StringWrapper {
        return when (this) {
            is IOException -> StringWrapper.Resource(R.string.no_connection)
            else -> {
                (this.localizedMessage ?: this.message)?.let {
                    StringWrapper.Dynamic(it)
                } ?: kotlin.run {
                    StringWrapper.Resource(R.string.unknown_error)
                }
            }
        }
    }

    override suspend fun signUp(name: String, email: String, password: String): Resource<String> =
        withContext(ioDispatcher) {
            return@withContext when (
                val result =
                    remoteSource.register(name = name, email = email, password = password)
            ) {
                is NetworkResult.Error -> Resource.Error(result.message!!)
                is NetworkResult.Exception -> {
                    Resource.Error(result.e.toWrapper())
                }
                is NetworkResult.Success -> {
                    if (result.data.error.not()) {
                        Resource.Success(result.data.message)
                    } else {
                        Resource.Error(StringWrapper.Resource(R.string.saved_error))
                    }
                }
            }
        }

    override suspend fun signOut() {
        withContext(ioDispatcher) {
            dataStore.updateData {
                it.copy(user = UserData())
            }
        }
    }
}
