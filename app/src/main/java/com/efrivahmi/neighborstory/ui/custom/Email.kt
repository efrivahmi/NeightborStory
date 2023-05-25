package com.efrivahmi.neighborstory.ui.custom

import android.content.Context
import android.graphics.Rect
import android.graphics.drawable.Drawable
import android.text.Editable
import android.text.TextWatcher
import android.util.AttributeSet
import android.util.Patterns
import android.view.MotionEvent
import android.view.View
import androidx.appcompat.widget.AppCompatEditText
import androidx.core.content.ContextCompat
import com.efrivahmi.neighborstory.R

class Email: AppCompatEditText, View.OnTouchListener {
    private var isEmailValid: Boolean = false
    private lateinit var emailIcon: Drawable

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
        emailIcon = ContextCompat.getDrawable(context, R.drawable.ic_email) as Drawable
        onShowVisibilityIcon(emailIcon)
        addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                if (!Patterns.EMAIL_ADDRESS.matcher(s).matches()) {
                    setError(context.getString(R.string.wrong_email), null)
                }
            }

            override fun afterTextChanged(s: Editable) {}
        })
    }


    private fun checkEmail() {
        val email = text?.trim()
        if (email.isNullOrEmpty()) {
            isEmailValid = false
            error = resources.getString(R.string.email_input)
        }
        else {
            isEmailValid = true
        }
    }

    override fun onFocusChanged(focused: Boolean, direction: Int, previouslyFocusedRect: Rect?) {
        super.onFocusChanged(focused, direction, previouslyFocusedRect)
        if (!focused) checkEmail()
    }

    override fun onTouch(p0: View?, p1: MotionEvent?): Boolean {
        return false
    }
}