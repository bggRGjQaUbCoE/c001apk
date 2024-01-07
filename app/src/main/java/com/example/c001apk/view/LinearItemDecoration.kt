package com.example.c001apk.view

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Rect
import android.view.View
import androidx.appcompat.widget.ThemeUtils
import androidx.recyclerview.widget.RecyclerView

class LinearItemDecoration(private val space: Int) :
    RecyclerView.ItemDecoration() {

    override fun getItemOffsets(
        outRect: Rect,
        view: View,
        parent: RecyclerView,
        state: RecyclerView.State
    ) {
        val position: Int = parent.getChildAdapterPosition(view)
        if (position == 0)
            outRect.top = space
        outRect.left = space
        outRect.right = space
        outRect.bottom = space
    }

}

class LinearItemDecoration1(private val space: Int) :
    RecyclerView.ItemDecoration() {

    override fun getItemOffsets(
        outRect: Rect,
        view: View,
        parent: RecyclerView,
        state: RecyclerView.State
    ) {
        val position: Int = parent.getChildAdapterPosition(view)
        if (position == 0)
            outRect.left = space
        outRect.top = space
        outRect.right = space
        outRect.bottom = space
    }

}

@SuppressLint("RestrictedApi")
class ReplyItemDecoration(
    context: Context,
    private val space: Int
) :
    RecyclerView.ItemDecoration() {

    private var mPaint: Paint = Paint()

    init {
        mPaint.isAntiAlias = true
        mPaint.color =
            ThemeUtils.getThemeAttrColor(
                context,
                com.google.android.material.R.attr.colorSurfaceVariant
            )
    }

    override fun onDraw(c: Canvas, parent: RecyclerView, state: RecyclerView.State) {
        super.onDraw(c, parent, state)
        val childCount = parent.childCount
        for (i in 0 until childCount) {
            val view = parent.getChildAt(i)
            val dividerTop = view.top.toFloat() - space
            val dividerLeft = parent.paddingLeft
            val dividerBottom = view.top
            val dividerRight = parent.width - parent.paddingRight
            c.drawRect(
                dividerLeft.toFloat(), dividerTop, dividerRight.toFloat(),
                dividerBottom.toFloat(), mPaint
            )
        }
    }

    override fun getItemOffsets(
        outRect: Rect,
        view: View,
        parent: RecyclerView,
        state: RecyclerView.State
    ) {
        outRect.top = space
    }

}