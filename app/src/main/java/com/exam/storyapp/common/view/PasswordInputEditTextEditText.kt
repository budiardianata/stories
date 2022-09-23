/*
 * Copyright (C) 2022 Budi Ardianata.
 */
package com.exam.storyapp.common.view

import android.content.Context
import android.util.AttributeSet
import androidx.core.widget.doAfterTextChanged
import com.exam.storyapp.R
import com.exam.storyapp.common.extensions.isPasswordValid
import com.exam.storyapp.common.util.Constant

internal class PasswordInputEditTextEditText @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = com.google.android.material.R.attr.editTextStyle,
) : CustomInputEditText(context, attrs, defStyleAttr) {

    init {
        this.doAfterTextChanged { text ->
            error = if (!text.isNullOrEmpty() && text.toString().isPasswordValid().not()) {
                resources.getQuantityString(R.plurals.password_min_length,  Constant.PASSWORD_MIN, Constant.PASSWORD_MIN)
            } else {
                onInputValidListener?.invoke(text.toString())
                null
            }
        }
    }
}
