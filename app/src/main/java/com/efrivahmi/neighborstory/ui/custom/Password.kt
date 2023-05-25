package com.efrivahmi.neighborstory.ui.custom

import android.content.Context
import android.graphics.Rect
import android.graphics.drawable.Drawable
import android.text.method.PasswordTransformationMethod
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import androidx.appcompat.widget.AppCompatEditText
import androidx.core.content.ContextCompat
import com.efrivahmi.neighborstory.R

class Password : AppCompatEditText, View.OnTouchListener {
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

    private fun init() {
        keyIcon = ContextCompat.getDrawable(context, R.drawable.ic_key) as Drawable
        transformationMethod = PasswordTransformationMethod.getInstance()
        onShowVisibilityIcon(keyIcon)
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


    private fun checkCPass() {
        val pass = text?.trim()
        if (pass.isNullOrEmpty()) {
            isPasswordValid = false
            error = resources.getString(R.string.password_valided)
        } else {
            isPasswordValid = true
        }
    }


    override fun onFocusChanged(focused: Boolean, direction: Int, previouslyFocusedRect: Rect?) {
        super.onFocusChanged(focused, direction, previouslyFocusedRect)
        if (!focused) checkCPass()
    }

    override fun onTouch(v: View?, event: MotionEvent): Boolean {
        return false
    }
}