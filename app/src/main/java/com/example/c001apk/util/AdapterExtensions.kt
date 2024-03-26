package com.example.c001apk.util

import androidx.recyclerview.widget.RecyclerView
import com.absinthe.libraries.utils.extensions.dp
import com.example.c001apk.R

// from LibChecker
fun RecyclerView.setBottomPaddingSpace() {
    val addedPadding = getTag(R.id.adapter_bottom_padding_id)?.toString().orEmpty().isNotBlank()
    fun should(): Boolean {
        val a = childCount
        val b = adapter?.itemCount ?: 0
        return if (!addedPadding) {
            a >= b
        } else {
            a >= b - 1
        }
    }
    if (should()) {
        if (addedPadding) return
        setPadding(paddingLeft, paddingTop, paddingRight, paddingBottom + 96.dp)
        setTag(R.id.adapter_bottom_padding_id, true)
    } else {
        if (!addedPadding) return
        setPadding(paddingLeft, paddingTop, paddingRight, paddingBottom - 96.dp)
        setTag(R.id.adapter_bottom_padding_id, false)
    }
}