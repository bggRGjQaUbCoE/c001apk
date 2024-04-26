package com.example.c001apk.view

import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import android.widget.HorizontalScrollView


/*
* Copyright (C) 2015 SpiritCroc
* Email: spiritcroc@gmail.com
*
* This program is free software: you can redistribute it and/or modify
* it under the terms of the GNU General Public License as published by
* the Free Software Foundation, either version 3 of the License, or
* (at your option) any later version.
*
* This program is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
* GNU General Public License for more details.
*
* You should have received a copy of the GNU General Public License
* along with this program.  If not, see <http://www.gnu.org/licenses/>.
*/

/**
 * Enables reasonable support for HorizontalContainerFragments within HorizontalContainerFragments
 */
class CustomHorizontalScrollView(context: Context, attrs: AttributeSet?) :
    HorizontalScrollView(context, attrs) {

    private var pointerX = 0f

    override fun onInterceptTouchEvent(ev: MotionEvent): Boolean {
        val result: Boolean = when (ev.action and MotionEvent.ACTION_MASK) {
            MotionEvent.ACTION_MOVE ->
                if (!canScroll) {
                    // If it can't scroll, don't intercept touch events
                    false
                } else if (ev.x > pointerX && !canScrollLeft || ev.x < pointerX && !canScrollRight) {
                    false
                } else {
                    super.onInterceptTouchEvent(ev)
                }

            else -> super.onInterceptTouchEvent(ev)
        }
        pointerX = ev.x
        return result
    }

    private val canScrollLeft: Boolean
        get() = scrollX != 0


    private val canScrollRight: Boolean
        get() = measuredWidth + scrollX != getChildAt(0).measuredWidth


    private val canScroll: Boolean
        get() = measuredWidth < getChildAt(0).measuredWidth

}
