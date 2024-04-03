package com.example.c001apk.view

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Rect
import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.absinthe.libraries.utils.extensions.dp
import com.google.android.material.color.MaterialColors

class QuestionItemDecorator(
    context: Context,
    private var itemCount: Int = 1,
) :
    RecyclerView.ItemDecoration() {

    private var mPaint: Paint = Paint()

    init {
        mPaint.isAntiAlias = true
        mPaint.color =
            MaterialColors.getColor(
                context,
                com.google.android.material.R.attr.colorSurfaceVariant,
                0
            )
    }

    override fun onDraw(c: Canvas, parent: RecyclerView, state: RecyclerView.State) {
        super.onDraw(c, parent, state)
        val childCount = parent.childCount
        for (i in 0 until childCount) {
            val view = parent.getChildAt(i)
            val index = parent.getChildAdapterPosition(view)
            if (index > itemCount) {
                val dividerTop = view.top.toFloat() - 1
                val dividerLeft = parent.paddingLeft
                val dividerBottom = view.top
                val dividerRight = parent.width - parent.paddingRight
                c.drawRect(
                    dividerLeft.toFloat(), dividerTop, dividerRight.toFloat(),
                    dividerBottom.toFloat(), mPaint
                )
            }
        }
    }

    override fun getItemOffsets(
        outRect: Rect,
        view: View,
        parent: RecyclerView,
        state: RecyclerView.State
    ) {
        val position = parent.getChildAdapterPosition(view)
        if (position <= itemCount) {
            outRect.bottom = 12.dp
            outRect.left = 15.dp
            outRect.right = 15.dp
        }
        if (position > itemCount)
            outRect.top = 1
    }

}