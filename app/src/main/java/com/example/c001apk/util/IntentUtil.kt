package com.example.c001apk.util

import android.content.Context
import android.content.Intent
import com.example.c001apk.R

object IntentUtil {

    fun shareText(ctx: Context, text: String) {
        val intent = Intent(Intent.ACTION_SEND)
        intent.type = "text/plain"
        intent.putExtra(Intent.EXTRA_SUBJECT, ctx.getString(R.string.share))
        intent.putExtra(
            Intent.EXTRA_TEXT, text
        )
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        ctx.startActivity(Intent.createChooser(intent, ctx.getString(R.string.share)))
    }

}