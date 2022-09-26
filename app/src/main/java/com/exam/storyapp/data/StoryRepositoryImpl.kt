/*
 * Copyright (C) 2022 Budi Ardianata.
 */
package com.exam.storyapp.data

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import androidx.exifinterface.media.ExifInterface
import androidx.paging.*
import com.exam.storyapp.R
import com.exam.storyapp.common.annotations.IODispatcher
import com.exam.storyapp.common.extensions.copyToCachedFile
import com.exam.storyapp.common.extensions.decodeSampledBitmap
import com.exam.storyapp.common.extensions.exifOrientationToMatrix
import com.exam.storyapp.common.extensions.extension
import com.exam.storyapp.common.util.Constant
import com.exam.storyapp.common.util.Resource
import com.exam.storyapp.common.util.StringWrapper
import com.exam.storyapp.data.source.StoryRemoteMediator
import com.exam.storyapp.data.source.local.db.StoryDb
import com.exam.storyapp.data.source.remote.StoryApi
import com.exam.storyapp.data.source.remote.adapter.NetworkResult
import com.exam.storyapp.domain.model.Story
import com.exam.storyapp.domain.repositories.StoryRepository
import com.google.android.gms.maps.model.LatLng
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.File
import java.io.IOException
import javax.inject.Inject
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody

class StoryRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context,
    @IODispatcher private val ioDispatcher: CoroutineDispatcher,
    private val remoteSource: StoryApi,
    private val localSource: StoryDb,
) : StoryRepository {
    @OptIn(ExperimentalPagingApi::class)
    override fun getPagedStories(): Flow<PagingData<Story>> = Pager(
        config = PagingConfig(
            pageSize = Constant.PAGING_PER_PAGE
        ),
        remoteMediator = StoryRemoteMediator(
            remoteSource,
            localSource,
        ),
        pagingSourceFactory = {
            localSource.storyDao().getStories()
        },
    ).flow.map { pagingData ->
        pagingData.map { it.toDomain() }
    }

    override fun getStories(
        perPage: Int,
        page: Int,
        withLocation: Boolean,
    ): Flow<Resource<List<Story>>> = flow {
        try {
            val response = remoteSource.getStories(page, perPage, if (withLocation) 1 else 0)
            if (response.listStory.isNotEmpty()) {
                emit(Resource.Success(response.listStory.map { it.toDomain() }))
            } else {
                emit(Resource.Error(StringWrapper.Resource(R.string.app_name)))
            }
        } catch (e: Exception) {
            emit(Resource.Error(StringWrapper.Dynamic(e.message ?: "Something went wrong")))
        }
    }.flowOn(ioDispatcher)

    override suspend fun createStory(
        description: String,
        image: Uri,
        location: LatLng?,
    ): Resource<String> = withContext(ioDispatcher) {
        try {
            compressImage(image).let {
                val requestFile = it.asRequestBody(it.toMediaType())
                val desc = description.toRequestBody("text/plain".toMediaType())
                val body = MultipartBody.Part.createFormData("photo", it.name, requestFile)
                var lat: RequestBody? = null
                var lon: RequestBody? = null
                if (location != null) {
                    lat = location.latitude.toString().toRequestBody("text/plain".toMediaType())
                    lon = location.longitude.toString().toRequestBody("text/plain".toMediaType())
                }

                return@withContext when (val result = remoteSource.uploadStory(desc, lat, lon, body)) {
                    is NetworkResult.Error -> Resource.Error(result.message!!)
                    is NetworkResult.Exception -> {
                        val message = when (result.e) {
                            is IOException -> StringWrapper.Resource(R.string.no_connection)
                            else -> StringWrapper.Resource(R.string.unknown_error)
                        }
                        Resource.Error(message)
                    }
                    is NetworkResult.Success -> {
                        if (!result.data.error) {
                            Resource.Success(result.data.message)
                        } else {
                            Resource.Error(StringWrapper.Resource(R.string.saved_error))
                        }
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            val message = e.localizedMessage ?: e.message
            return@withContext if (message.isNullOrEmpty()) {
                Resource.Error(StringWrapper.Resource(R.string.app_name))
            } else {
                Resource.Error(StringWrapper.Dynamic(message))
            }
        }
    }

    private fun compressImage(image: Uri, comparesQuality: Int = 80): File {
        val format = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            Bitmap.CompressFormat.WEBP_LOSSY
        } else {
            Bitmap.CompressFormat.JPEG
        }
        val tempFile = image.copyToCachedFile(context, format.extension())
        val exifInterface = ExifInterface(tempFile)
        val width = exifInterface.getAttributeInt(ExifInterface.TAG_IMAGE_WIDTH, 0)
        val height = exifInterface.getAttributeInt(ExifInterface.TAG_IMAGE_LENGTH, 0)
        val orientation = exifInterface.getAttributeInt(ExifInterface.TAG_ORIENTATION, 0)
        if (width != 0 || height != 0) {
            val bitmap: Bitmap = tempFile.decodeSampledBitmap(width / 2, height / 2)
            val matrix = exifOrientationToMatrix(orientation)
            val rotatedBitmap =
                Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
            rotatedBitmap.compress(format, comparesQuality, tempFile.outputStream())
        }
        return tempFile
    }

    private fun File.toMediaType() = when (extension) {
        "PNG" -> "image/png".toMediaType()
        "WEBP" -> "image/webp".toMediaType()
        else -> "image/jpeg".toMediaType()
    }
}
