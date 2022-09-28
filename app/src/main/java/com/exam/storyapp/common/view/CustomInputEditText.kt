/*
 * Copyright (C) 2022 Budi Ardianata.
 */
package com.exam.storyapp.common.view

import android.content.Context
import android.util.AttributeSet
import android.view.View
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout

internal abstract class CustomInputEditText constructor(
    context: Context,
    attrs: AttributeSet?,
    defStyleAttr: Int,
) : TextInputEditText(context, attrs, defStyleAttr) {

    final override fun setError(error: CharSequence?) {
        getInputLayout()?.let {
            it.error = error
        } ?: kotlin.run {
            super.setError(error)
        }
    }

    private fun getInputLayout(): TextInputLayout? {
        var parent = parent
        while (parent is View) {
            if (parent is TextInputLayout) {
                return parent
            }
            parent = parent.getParent()
        }
        return null
    }
}
