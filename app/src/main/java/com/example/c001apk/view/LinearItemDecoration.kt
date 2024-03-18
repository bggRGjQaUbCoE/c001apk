package com.example.c001apk.view

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Rect
import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.absinthe.libraries.utils.extensions.getColorByAttr

class LinearItemDecoration(private val space: Int) :
    RecyclerView.ItemDecoration() {

    override fun getItemOffsets(
        outRect: Rect,
        view: View,
        parent: RecyclerView,
        state: RecyclerView.State
    ) {
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

class ReplyItemDecoration(
    context: Context,
    private val space: Int
) :
    RecyclerView.ItemDecoration() {

    private var mPaint: Paint = Paint()

    init {
        mPaint.isAntiAlias = true
        mPaint.color =
            context.getColorByAttr(
                com.google.android.material.R.attr.colorSurfaceVariant
            )
    }

    override fun onDraw(c: Canvas, parent: RecyclerView, state: RecyclerView.State) {
        super.onDraw(c, parent, state)
        val childCount = parent.childCount
        for (i in 1 until childCount) {
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
        if (parent.getChildAdapterPosition(view) != 0)
            outRect.top = space
    }

}

class LinearItemDecoration2(private val space: Int) :
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