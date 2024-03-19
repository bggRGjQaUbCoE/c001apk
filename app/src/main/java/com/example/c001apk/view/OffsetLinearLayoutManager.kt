package com.example.c001apk.view

import android.content.Context
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class OffsetLinearLayoutManager(context: Context?) : LinearLayoutManager(context) {
    private val heightMap: MutableMap<Int, Int?> = HashMap()
    private var listHeight = 0
    override fun onLayoutCompleted(state: RecyclerView.State) {
        super.onLayoutCompleted(state)
        val count = childCount
        listHeight = 0
        for (i in 0 until count) {
            val view = getChildAt(i)
            heightMap[i] = view?.height ?: 0
            listHeight += view?.height ?: 0
        }
    }

    fun computeVerticalOffset(): Int {
        return if (childCount == 0) {
            0
        } else try {
            val firstVisiblePosition = findFirstVisibleItemPosition()
            val firstVisibleView = findViewByPosition(firstVisiblePosition)
            var offsetY = -(firstVisibleView?.y?.toInt() ?: 0)
            for (i in 0 until firstVisiblePosition) {
                offsetY += (if (heightMap[i] == null) 0 else heightMap[i]) ?: 0
            }
            offsetY
        } catch (e: Exception) {
            0
        }
    }
}
