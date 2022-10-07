/*
 * Copyright (C) 2022 Budi Ardianata.
 */
package com.exam.storyapp.common.view

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import com.exam.storyapp.common.extensions.getProgressDrawable
import com.google.android.material.button.MaterialButton
import kotlin.properties.Delegates

@SuppressLint("CustomViewStyleable", "PrivateResource")
class LoadingButton @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = com.google.android.material.R.attr.materialButtonStyle,
) : MaterialButton(context, attrs, defStyleAttr) {
    private var defaultIcon: Drawable? = icon
    private val progressDrawable by lazy {
        context.getProgressDrawable()
    }

    var isLoading by Delegates.observable(false) { _, _, newValue ->
        if (newValue) {
            showLoading()
        } else {
            hideLoading()
        }
    }

    init {
        val attributes = context.obtainStyledAttributes(
            attrs,
            com.google.android.material.R.styleable.MaterialButton,
            defStyleAttr,
            com.google.android.material.R.style.Widget_MaterialComponents_Button
        )
        try {
            progressDrawable.setColorSchemeColors(
                attributes.getColor(
                    com.google.android.material.R.styleable.MaterialButton_iconTint,
                    com.google.android.material.R.attr.colorPrimary
                )
            )
        } finally {
            attributes.recycle()
        }
    }

    private fun showLoading() {
        this.icon = progressDrawable
        progressDrawable.start()
        isEnabled = false
    }

    private fun hideLoading() {
        progressDrawable.stop()
        this.icon = defaultIcon
        isEnabled = true
    }
}
