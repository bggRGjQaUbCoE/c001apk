package com.example.c001apk.vertical.util

import android.content.Context

/**
 * Created by chqiu on 2017/1/20.
 */
object DisplayUtil {
    @JvmStatic
    fun dp2px(context: Context, dp: Float): Int {
        val scale = context.resources.displayMetrics.density
        return (dp * scale + 0.5f).toInt()
    }

    fun px2dp(context: Context, pxValue: Float): Int {
        val scale = context.resources.displayMetrics.density
        return (pxValue / scale + 0.5f).toInt()
    }
}