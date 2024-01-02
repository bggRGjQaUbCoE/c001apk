package com.example.c001apk.view

import android.content.Context
import android.util.AttributeSet
import com.google.android.material.appbar.CollapsingToolbarLayout

class NestCollapsingToolbarLayout : CollapsingToolbarLayout {

    private var mIsScrimsShown: Boolean = false
    private var scrimsShowListener: OnScrimsShowListener? = null

    constructor(context: Context) : super(context)

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    )

    override fun setScrimsShown(shown: Boolean, animate: Boolean) {
        super.setScrimsShown(shown, animate)
        if (mIsScrimsShown != shown) {
            mIsScrimsShown = shown
            if (scrimsShowListener != null) {
                scrimsShowListener?.onScrimsShowChange(this, mIsScrimsShown)
            }
        }
    }

    fun setOnScrimesShowListener(listener: OnScrimsShowListener) {
        scrimsShowListener = listener
    }

    interface OnScrimsShowListener {
        fun onScrimsShowChange(
            nestCollapsingToolbarLayout: NestCollapsingToolbarLayout,
            isScrimesShow: Boolean
        )
    }
}