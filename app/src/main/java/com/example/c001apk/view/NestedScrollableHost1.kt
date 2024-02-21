package com.example.c001apk.view

import android.annotation.SuppressLint
import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import android.view.ViewConfiguration
import android.widget.FrameLayout
import androidx.viewpager2.widget.ViewPager2
import com.example.c001apk.R
import kotlin.math.absoluteValue
import kotlin.math.sign

class NestedScrollableHost1 : FrameLayout {
    private var isChildHasSameDirection = true
    private var touchSlop = 0
    private var initialX = 0f
    private var initialY = 0f

    private val parentViewPager: ViewPager2?
        get() = generateSequence(parent) { it.parent }
            .filterIsInstance<ViewPager2>()
            .firstOrNull()

    private val child: View? get() = ViewUtils.getChildRecyclerView(this)

    constructor(context: Context) : super(context) {
        init(context)
    }

    @SuppressLint("CustomViewStyleable")
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        context.obtainStyledAttributes(attrs, R.styleable.NestedScrollableHost).apply {
            isChildHasSameDirection =
                getBoolean(R.styleable.NestedScrollableHost_sameDirectionWithParent, false)
            recycle()
        }
        init(context)
    }

    private fun init(context: Context) {
        touchSlop = ViewConfiguration.get(context).scaledTouchSlop
    }

    private fun canChildScroll(orientation: Int, delta: Float): Boolean {
        val direction = -delta.sign.toInt()
        return child?.let { 
            when (orientation) {
                ViewPager2.ORIENTATION_HORIZONTAL -> it.canScrollHorizontally(direction)
                ViewPager2.ORIENTATION_VERTICAL -> it.canScrollVertically(direction)
                else -> false
            } 
        } ?: false
    }

    override fun onInterceptTouchEvent(e: MotionEvent): Boolean {
        handleInterceptTouchEvent(e)
        return super.onInterceptTouchEvent(e)
    }

    private fun handleInterceptTouchEvent(e: MotionEvent) {
        val orientation = parentViewPager?.orientation ?: return
        val childOrientation = if (isChildHasSameDirection) orientation else orientation xor 1

        if (!canChildScroll(childOrientation, -1f) && !canChildScroll(childOrientation, 1f)) {
            return
        }

        when (e.action) {
            MotionEvent.ACTION_DOWN -> {
                initialX = e.x
                initialY = e.y
                parent.requestDisallowInterceptTouchEvent(true)
            }
            MotionEvent.ACTION_MOVE -> {
                val dx = e.x - initialX
                val dy = e.y - initialY
                val isVpHorizontal = orientation == ViewPager2.ORIENTATION_HORIZONTAL

                val scaledDx = dx.absoluteValue * if (isVpHorizontal) .5f else 1f
                val scaledDy = dy.absoluteValue * if (isVpHorizontal) 1f else .5f

                val disallowIntercept = if (scaledDx > touchSlop || scaledDy > touchSlop) {
                    isVpHorizontal == (scaledDx > scaledDy) && canChildScroll(orientation, if (isVpHorizontal) dx else dy)
                } else {
                    false
                }
                parent.requestDisallowInterceptTouchEvent(disallowIntercept)
            }
        }
    }
}
