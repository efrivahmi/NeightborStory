package com.efrivahmi.neighborstory.ui.custom

import android.content.Context
import android.graphics.Rect
import android.graphics.drawable.Drawable
import android.text.Editable
import android.text.TextWatcher
import android.text.method.PasswordTransformationMethod
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import androidx.appcompat.widget.AppCompatEditText
import androidx.core.content.ContextCompat
import com.efrivahmi.neighborstory.R

class InputPassword : AppCompatEditText, View.OnTouchListener {
    private var isPasswordValid: Boolean = false
    private lateinit var keyIcon: Drawable

    constructor(context: Context) : super(context) {
        init()
    }

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        init()
    }

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    ) {
        init()
    }

    private fun onShowVisibilityIcon(icon: Drawable) {
        setButtonDrawables(startOfTheText = icon)
    }

    private fun setButtonDrawables(
        startOfTheText: Drawable? = null,
        topOfTheText: Drawable? = null,
        endOfTheText: Drawable? = null,
        bottomOfTheText: Drawable? = null
    ) {
        setCompoundDrawablesWithIntrinsicBounds(
            startOfTheText,
            topOfTheText,
            endOfTheText,
            bottomOfTheText
        )

    }

    private fun init() {
        keyIcon = ContextCompat.getDrawable(context, R.drawable.ic_key) as Drawable
        transformationMethod = PasswordTransformationMethod.getInstance()
        onShowVisibilityIcon(keyIcon)
        addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                if (s.length < 8) {
                    setError(context.getString(R.string.password_length), null)
                }
            }

            override fun afterTextChanged(s: Editable) {}
        })
    }

    private fun checkPass() {
        val pass = text?.trim()
        when {
            pass.isNullOrEmpty() -> {
                isPasswordValid = false
                error = resources.getString(R.string.input_password)
            }
            else -> {
                isPasswordValid = true
            }
        }
    }

    override fun onTouch(p0: View?, p1: MotionEvent?): Boolean {
        return false
    }

    override fun onFocusChanged(focused: Boolean, direction: Int, previouslyFocusedRect: Rect?) {
        super.onFocusChanged(focused, direction, previouslyFocusedRect)
        if (!focused) checkPass()
        init()
    }
}