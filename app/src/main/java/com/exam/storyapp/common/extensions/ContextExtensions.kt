/*
 * Copyright (C) 2022 Budi Ardianata.
 */
package com.exam.storyapp.common.extensions

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Color
import android.text.Spanned
import androidx.annotation.AttrRes
import androidx.annotation.ColorInt
import androidx.core.content.res.use
import androidx.core.text.HtmlCompat
import androidx.swiperefreshlayout.widget.CircularProgressDrawable

@ColorInt
@SuppressLint("Recycle")
fun Context.themeColor(
    @AttrRes themeAttrId: Int,
): Int {
    return obtainStyledAttributes(
        intArrayOf(themeAttrId)
    ).use {
        it.getColor(0, Color.MAGENTA)
    }
}

@SuppressLint("AppName")
fun Context.getStyledAppName(): Spanned {
    val colorPrimary = themeColor(com.google.android.material.R.attr.colorPrimary)
    val hexColor = String.format("#%06X", (0xFFFFFF and colorPrimary))
    return HtmlCompat.fromHtml(
        "Stories<span  style='color:$hexColor';>App</span>",
        HtmlCompat.FROM_HTML_MODE_COMPACT
    )
}

@SuppressLint("progressDrawable")
fun Context.getProgressDrawable(@ColorInt color: Int? = null): CircularProgressDrawable {
    val schemaColors = color ?: themeColor(com.google.android.material.R.attr.colorPrimary)
    return CircularProgressDrawable(this).apply {
        setColorSchemeColors(schemaColors)
        strokeWidth = 5f
        centerRadius = 30f
    }
}
