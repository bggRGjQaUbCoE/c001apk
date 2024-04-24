package com.example.c001apk.util

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context

object ClipboardUtil {
    fun copyText(context: Context, text: String, showToast: Boolean = true) {
        val clipboardManager =
            context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        ClipData.newPlainText("c001apk text", text)?.let { clipboardManager.setPrimaryClip(it) }
        if (showToast)
            context.makeToast("已复制: $text")
    }
}