package com.example.c001apk.util

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context

object ClipboardUtil {
    fun copyText(ctx: Context, text: String) {
        val clipboardManager =
            ctx.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        ClipData.newPlainText("c001apk text", text)?.let { clipboardManager.setPrimaryClip(it) }
        ToastUtil.toast("已复制: $text")
    }
}