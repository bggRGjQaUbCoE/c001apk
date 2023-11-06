package com.example.c001apk.view

import android.graphics.Canvas
import android.graphics.Rect
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class StickyItemDecorator(private val space: Int, private val listener: SortShowListener) :
    RecyclerView.ItemDecoration() {

    private var mLayoutManager: LinearLayoutManager? = null
    private var index = 0
    override fun onDrawOver(c: Canvas, parent: RecyclerView, state: RecyclerView.State) {
        super.onDrawOver(c, parent, state)
        if (parent.adapter!!.itemCount <= 0) return
        mLayoutManager = parent.layoutManager as LinearLayoutManager?
        index = (parent.layoutManager as LinearLayoutManager?)!!.findFirstVisibleItemPosition()
        //Log.d(TAG, "ondrawover firstVisible:$index")
        if (index >= 1) {
            listener.showSort(true)
        } else {
            listener.showSort(false)
        }
    }

    interface SortShowListener {
        fun showSort(show: Boolean)
    }

    /*companion object {
        private const val TAG = "StickyItemDecorator"
    }*/

    override fun getItemOffsets(
        outRect: Rect,
        view: View,
        parent: RecyclerView,
        state: RecyclerView.State
    ) {
        val position: Int = parent.getChildAdapterPosition(view)
        if (position == 0 || position == 2)
            outRect.top = space
        if (position != 1) {
            outRect.left = space
            outRect.right = space
            outRect.bottom = space
        }
    }

}