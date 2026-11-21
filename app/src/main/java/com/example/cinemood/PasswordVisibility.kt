package com.example.cinemood

import android.text.method.PasswordTransformationMethod
import android.view.View
import android.widget.EditText
import android.widget.ImageButton
import androidx.annotation.StringRes
import androidx.core.widget.doAfterTextChanged

/** Keeps each field's visibility independent and leaves the cursor in place. */
internal fun bindPasswordVisibility(
    password: EditText,
    toggle: ImageButton,
    @StringRes showDescription: Int = R.string.show_password,
    @StringRes hideDescription: Int = R.string.hide_password
) {
    var visible = false

    fun updateToggle() {
        toggle.visibility = if (password.text.isNullOrEmpty()) View.INVISIBLE else View.VISIBLE
        toggle.setImageResource(if (visible) R.drawable.ic_password_hide else R.drawable.ic_password_show)
        toggle.contentDescription = toggle.context.getString(if (visible) hideDescription else showDescription)
    }

    fun setPasswordVisible(show: Boolean) {
        val start = password.selectionStart
        val end = password.selectionEnd
        visible = show
        password.transformationMethod = if (show) null else PasswordTransformationMethod.getInstance()
        if (start >= 0 && end >= 0) password.setSelection(start, end)
        updateToggle()
    }

    // Recreated forms start masked, including when Android restores their text.
    setPasswordVisible(false)
    password.doAfterTextChanged {
        if (it.isNullOrEmpty()) setPasswordVisible(false) else updateToggle()
    }
    toggle.setOnClickListener { setPasswordVisible(!visible) }
}
