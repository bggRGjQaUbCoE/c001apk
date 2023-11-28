package com.example.c001apk.view

import android.graphics.Rect
import android.view.View
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ItemDecoration
import androidx.recyclerview.widget.StaggeredGridLayoutManager

class VoteItemDecoration(private val space: Int) : ItemDecoration() {

    override fun getItemOffsets(
        outRect: Rect,
        view: View,
        parent: RecyclerView,
        state: RecyclerView.State
    ) {
        val params = view.layoutParams as StaggeredGridLayoutManager.LayoutParams

        val spanIndex = params.spanIndex

        if (parent.getChildAdapterPosition(view) == 0) {
            outRect.top = space
            outRect.left = space
            outRect.right = space
            outRect.bottom = space
        } else if (parent.getChildAdapterPosition(view) == 1) {
            outRect.bottom = space
            outRect.left = space
            outRect.right = space
        } else {
            if (spanIndex % 2 == 1) {
                outRect.right = space
            }
            outRect.left = space
            outRect.bottom = space
        }


    }
}