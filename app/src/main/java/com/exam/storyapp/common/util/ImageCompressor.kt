/*
 * Copyright (C) 2022 Budi Ardianata.
 */
package com.exam.storyapp.common.util

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import androidx.exifinterface.media.ExifInterface
import com.exam.storyapp.common.extensions.decodeSampledBitmap
import com.exam.storyapp.common.extensions.exifOrientationToMatrix
import com.exam.storyapp.common.extensions.extension
import java.io.File
import kotlin.coroutines.CoroutineContext
import kotlinx.coroutines.*

object ImageCompressor {
    suspend fun compress(
        context: Context,
        image: Uri,
        comparesQuality: Int = 80,
        coroutineContext: CoroutineContext = Dispatchers.IO,
    ): File = withContext(coroutineContext) {
        val format = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            Bitmap.CompressFormat.WEBP_LOSSY
        } else {
            Bitmap.CompressFormat.JPEG
        }
        val tempFile = File(context.cacheDir, "${System.currentTimeMillis()}.${format.extension()}").apply {
            createNewFile()
        }
        try {
            image.copyToCachedFile(context, tempFile)
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
            return@withContext tempFile
        } catch (e: Exception) {
            return@withContext tempFile
        }
    }

    private fun Uri.copyToCachedFile(context: Context, file: File): File {
        context.contentResolver.openInputStream(this).use { input ->
            input?.copyTo(file.outputStream())
        }
        return file
    }
}
