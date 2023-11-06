package com.example.c001apk.view

import android.graphics.Rect
import android.view.View
import androidx.recyclerview.widget.RecyclerView

class SpacesItemDecoration(
    private val spanCount: Int,
    private val spaceValue: HashMap<String, Int>,
    private val includeEdge: Boolean
) : RecyclerView.ItemDecoration() {

    override fun getItemOffsets(
        outRect: Rect,
        view: View,
        parent: RecyclerView,
        state: RecyclerView.State
    ) {
        val position: Int = parent.getChildAdapterPosition(view)
        val column = position % spanCount
        if (includeEdge) {
            outRect.left = spaceValue[LEFT_SPACE]!! - column * spaceValue[LEFT_SPACE]!! / spanCount
            outRect.right = (column + 1) * spaceValue[RIGHT_SPACE]!! / spanCount
            if (position < spanCount) {
                outRect.top = spaceValue[TOP_SPACE]!!
            }
            outRect.bottom = spaceValue[BOTTOM_SPACE]!!
        } else {
            outRect.left = column * spaceValue[LEFT_SPACE]!! / spanCount
            outRect.right =
                spaceValue[RIGHT_SPACE]!! - (column + 1) * spaceValue[RIGHT_SPACE]!! / spanCount
            if (position >= spanCount) {
                outRect.top = spaceValue[TOP_SPACE]!!
            }
        }
    }

    companion object {
        const val TOP_SPACE = "top_space"
        const val BOTTOM_SPACE = "bottom_space"
        const val LEFT_SPACE = "left_space"
        const val RIGHT_SPACE = "right_space"
    }
}