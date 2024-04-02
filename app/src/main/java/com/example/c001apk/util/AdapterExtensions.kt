package com.example.c001apk.util

import androidx.recyclerview.widget.ConcatAdapter
import androidx.recyclerview.widget.RecyclerView
import com.absinthe.libraries.utils.extensions.dp
import com.example.c001apk.R
import com.example.c001apk.adapter.PlaceHolderAdapter

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

fun RecyclerView.setSpaceFooterView(placeHolderAdapter: PlaceHolderAdapter) {
    val adapter = adapter as ConcatAdapter
    val hasFooter = adapter.adapters.contains(placeHolderAdapter)
    val a = childCount
    val b = adapter.itemCount
    val should = if (hasFooter) {
        a >= b - 1
    } else {
        a >= b
    }
    if (should) {
        if (!hasFooter) {
            adapter.addAdapter(placeHolderAdapter)
        }
    } else {
        if (hasFooter) {
            adapter.removeAdapter(placeHolderAdapter)
        }
    }
}