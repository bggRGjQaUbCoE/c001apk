package com.example.c001apk.view

import com.google.android.material.appbar.AppBarLayout
import com.google.android.material.appbar.AppBarLayout.OnOffsetChangedListener

abstract class AppBarLayoutStateChangeListener : OnOffsetChangedListener {
    enum class State {
        EXPANDED,
        COLLAPSED,
        INTERMEDIATE
    }

    private var mCurrentState = State.INTERMEDIATE
    override fun onOffsetChanged(appBarLayout: AppBarLayout, verticalOffset: Int) {
        mCurrentState = if (verticalOffset == 0) {
            if (mCurrentState != State.EXPANDED) {
                onStateChanged(
                    appBarLayout,
                    State.EXPANDED
                )
            }
            State.EXPANDED
        } else if (Math.abs(verticalOffset) >= appBarLayout.totalScrollRange) {
            if (mCurrentState != State.COLLAPSED) {
                onStateChanged(
                    appBarLayout,
                    State.COLLAPSED
                )
            }
            State.COLLAPSED
        } else {
            if (mCurrentState != State.INTERMEDIATE) {
                onStateChanged(
                    appBarLayout,
                    State.INTERMEDIATE
                )
            }
            State.INTERMEDIATE
        }
    }

    abstract fun onStateChanged(appBarLayout: AppBarLayout?, state: State?)
}
