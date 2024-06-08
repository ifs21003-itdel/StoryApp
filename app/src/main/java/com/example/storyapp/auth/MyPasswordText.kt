package com.example.storyapp.auth

import android.content.Context
import android.graphics.Canvas
import android.graphics.drawable.Drawable
import android.text.Editable
import android.text.InputType
import android.text.TextWatcher
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import android.widget.TextView
import androidx.appcompat.widget.AppCompatEditText
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.DrawableCompat
import com.example.storyapp.R

class MyPasswordText @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null
) : AppCompatEditText(context, attrs), View.OnTouchListener {

    private var showPasswordImage: Drawable
    private var hidePasswordImage: Drawable
    private var isPasswordVisible: Boolean = false
    private var helperTextView: TextView? = null

    init {
        showPasswordImage = ContextCompat.getDrawable(context, R.drawable.baseline_remove_red_eye_24) as Drawable
        hidePasswordImage = ContextCompat.getDrawable(context, R.drawable.baseline_hide_source_24) as Drawable
        setOnTouchListener(this)
        updateInputType()  // Set the initial input type to password
        updateDrawable()

        // Add TextWatcher to monitor text changes
        addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                updateHelperText()
            }

            override fun afterTextChanged(s: Editable?) {}
        })
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        textAlignment = View.TEXT_ALIGNMENT_VIEW_START
        updateDrawable()
    }

    private fun updateDrawable() {
        val drawable = if (isPasswordVisible) hidePasswordImage else showPasswordImage
        drawable.setBounds(0, 0, drawable.intrinsicWidth, drawable.intrinsicHeight)
        DrawableCompat.setTint(drawable, currentHintTextColor)
        setCompoundDrawables(null, null, drawable, null)
    }

    override fun onTouch(v: View?, event: MotionEvent): Boolean {
        if (event.action == MotionEvent.ACTION_UP) {
            val drawableRight = compoundDrawables[2]
            if (drawableRight != null && event.rawX >= (right - drawableRight.bounds.width())) {
                isPasswordVisible = !isPasswordVisible
                updateInputType()
                updateDrawable()
                return true
            }
        }
        return false
    }

    private fun updateInputType() {
        if (isPasswordVisible) {
            inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
        } else {
            inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
        }
        setSelection(text?.length ?: 0)
    }

    fun validatePassword(): String? {
        val password = text.toString()
        return when {
            password == "" -> "Required"
            password.length < 8 -> "Minimum 8 Character Password"
            !password.matches(".*[A-Z].*".toRegex()) -> "Password must contain at least 1 Uppercase Character"
            !password.matches(".*[a-z].*".toRegex()) -> "Password must contain at least 1 Lowercase Character"
            !password.matches(".*[0-9].*".toRegex()) -> "Password must contain at least 1 Number Character"
            else -> null
        }
    }

    fun updateHelperText() {
        val validationMessage = validatePassword()
        helperTextView?.text = validationMessage ?: "Required"
        helperTextView?.visibility = if (validationMessage != null) View.VISIBLE else View.GONE
    }

    fun setHelperTextView(helperTextView: TextView) {
        this.helperTextView = helperTextView
    }
}

