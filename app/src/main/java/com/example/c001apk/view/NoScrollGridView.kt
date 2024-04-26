package com.example.c001apk.view

import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import android.widget.GridView

class NoScrollGridView(context: Context, attributeSet: AttributeSet? = null) :
    GridView(context, attributeSet) {

    override fun onInterceptTouchEvent(ev: MotionEvent): Boolean {
        return false
    }

    override fun onTouchEvent(ev: MotionEvent): Boolean {
        return false
    }

}