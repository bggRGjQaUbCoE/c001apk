package com.example.c001apk.view

import com.google.android.material.appbar.AppBarLayout
import com.google.android.material.appbar.AppBarLayout.OnOffsetChangedListener
import kotlin.math.abs

abstract class AppBarLayoutStateChangeListener : OnOffsetChangedListener {

    override fun onOffsetChanged(appBarLayout: AppBarLayout, verticalOffset: Int) {
        onScroll(1 - abs(verticalOffset).toFloat() / appBarLayout.totalScrollRange.toFloat())
    }

    abstract fun onScroll(percent: Float)

}
