package com.example.c001apk.util

import android.app.ActivityOptions
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

    inline fun <reified T> startActivity(context: Context, block: Intent.() -> Unit) {
        val intent = Intent(context, T::class.java)
        intent.block()
        val animationBundle = ActivityOptions.makeCustomAnimation(
            context,
            R.anim.right_in,
            R.anim.left_out
        ).toBundle()
        context.startActivity(intent, animationBundle)
    }

}