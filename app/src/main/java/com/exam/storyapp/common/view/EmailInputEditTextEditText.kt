/*
 * Copyright (C) 2022 Budi Ardianata.
 */
package com.exam.storyapp.common.view

import android.content.Context
import android.text.InputType
import android.util.AttributeSet
import androidx.core.widget.doAfterTextChanged
import com.exam.storyapp.R
import com.exam.storyapp.common.extensions.isEmailValid

internal class EmailInputEditTextEditText @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = com.google.android.material.R.attr.editTextStyle,
) : CustomInputEditText(context, attrs, defStyleAttr) {

    init {
        this.doAfterTextChanged { text ->
            if (text.isNullOrEmpty().not() && text.toString().isEmailValid().not()) {
                this.error = resources.getString(R.string.email_invalid)
            } else {
                onInputValidListener?.invoke(text.toString())
                this.error = null
            }
        }
        inputType = InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS
    }
}
