package com.example.c001apk.view

import android.graphics.Rect
import android.view.View
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ItemDecoration
import androidx.recyclerview.widget.StaggeredGridLayoutManager

class StaggerItemDecoration(
    private val space: Int,
    private var itemCount: Int = 1
) : ItemDecoration() {

    override fun getItemOffsets(
        outRect: Rect,
        view: View,
        parent: RecyclerView,
        state: RecyclerView.State
    ) {
        val position = parent.getChildAdapterPosition(view)
        val params = view.layoutParams as StaggeredGridLayoutManager.LayoutParams
        val spanIndex = params.spanIndex
        if (position == 0 || position == itemCount) {
            outRect.top = space
        }
        if (spanIndex % 2 == 1) {
            outRect.right = space
        }
        outRect.left = space
        outRect.bottom = space
    }

}