/*
 * Copyright (C) 2022 Budi Ardianata.
 */
package com.exam.storyapp.common.extensions

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.net.Uri
import androidx.exifinterface.media.ExifInterface
import java.io.File

@JvmName("copyToCachedFile")
fun Uri.copyToCachedFile(context: Context, extension: String): File {
    val tempFile = File(context.cacheDir, "${System.currentTimeMillis()}.$extension")
    tempFile.createNewFile()
    context.contentResolver.openInputStream(this).use { input ->
        input?.copyTo(tempFile.outputStream())
    }
    return tempFile
}

@JvmName("decodeSampledBitmap")
fun File.decodeSampledBitmap(width: Int, height: Int): Bitmap {
    return BitmapFactory.Options().run {
        inJustDecodeBounds = true
        BitmapFactory.decodeFile(absolutePath, this)

        inSampleSize = calculateInSampleSize(width, height)

        inJustDecodeBounds = false
        BitmapFactory.decodeFile(absolutePath, this)
    }
}

@JvmName("exifOrientationToMatrix")
fun exifOrientationToMatrix(exifOrientation: Int): Matrix {
    val matrix = Matrix()
    when (exifOrientation) {
        ExifInterface.ORIENTATION_NORMAL, ExifInterface.ORIENTATION_UNDEFINED -> Unit
        ExifInterface.ORIENTATION_ROTATE_90 -> matrix.postRotate(90F)
        ExifInterface.ORIENTATION_ROTATE_180 -> matrix.postRotate(180F)
        ExifInterface.ORIENTATION_ROTATE_270 -> matrix.postRotate(270F)
        ExifInterface.ORIENTATION_FLIP_HORIZONTAL -> matrix.postScale(-1F, 1F)
        ExifInterface.ORIENTATION_FLIP_VERTICAL -> matrix.postScale(1F, -1F)
        ExifInterface.ORIENTATION_TRANSPOSE -> {
            matrix.postScale(-1F, 1F)
            matrix.postRotate(270F)
        }
        ExifInterface.ORIENTATION_TRANSVERSE -> {
            matrix.postScale(-1F, 1F)
            matrix.postRotate(90F)
        }
        else -> Unit
    }

    return matrix
}

@JvmName("calculateInSampleSize")
private fun BitmapFactory.Options.calculateInSampleSize(reqWidth: Int, reqHeight: Int): Int {
    val (height: Int, width: Int) = this.run { outHeight to outWidth }
    var inSampleSize = 1

    if (height > reqHeight || width > reqWidth) {
        val halfHeight: Int = height / 2
        val halfWidth: Int = width / 2
        while (halfHeight / inSampleSize >= reqHeight && halfWidth / inSampleSize >= reqWidth) {
            inSampleSize *= 2
        }
    }
    return inSampleSize
}

@JvmName("extension")
fun Bitmap.CompressFormat.extension() = when (this) {
    Bitmap.CompressFormat.PNG -> "png"
    Bitmap.CompressFormat.JPEG -> "jpg"
    else -> "webp"
}
