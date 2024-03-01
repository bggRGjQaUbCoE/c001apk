package com.example.c001apk.util

import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView

object ViewUtils {
    fun getChildRecyclerView(view: View): View? {
        val unvisited = ArrayList<View>()
        unvisited.add(view)
        while (unvisited.isNotEmpty()) {
            val child = unvisited.removeAt(0)
            if (child is RecyclerView) {
                return child
            }
            if (child !is ViewGroup) {
                continue
            }
            for (i in 0 until child.childCount) {
                unvisited.add(child.getChildAt(i))
            }
        }
        return null
    }
}
