package com.example.c001apk.view

import android.graphics.Rect
import android.view.View
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ItemDecoration
import androidx.recyclerview.widget.StaggeredGridLayoutManager

class StaggerMessItemDecoration(private val space: Int) : ItemDecoration() {

    override fun getItemOffsets(
        outRect: Rect,
        view: View,
        parent: RecyclerView,
        state: RecyclerView.State
    ) {
        val params = view.layoutParams as StaggeredGridLayoutManager.LayoutParams
        val spanIndex = params.spanIndex
        if (spanIndex % 2 == 1) {
            outRect.right = space
        }
        outRect.left = space
        outRect.bottom = space
    }

}