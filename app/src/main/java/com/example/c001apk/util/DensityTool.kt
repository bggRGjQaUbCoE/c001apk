package com.example.c001apk.util

import android.annotation.SuppressLint
import android.content.Context


object DensityTool {

    @SuppressLint("DiscouragedApi", "InternalInsetResource")
    fun getNavigationBarHeight(context: Context): Int {
        val rid: Int =
            context.resources.getIdentifier("config_showNavigationBar", "bool", "android")
        return if (rid != 0) {
            val resourceId: Int =
                context.resources.getIdentifier("navigation_bar_height", "dimen", "android")
            context.resources.getDimensionPixelSize(resourceId)
        } else {
            0
        }
    }

    @SuppressLint("InternalInsetResource", "DiscouragedApi")
    fun getStatusBarHeight(context: Context): Int {
        val resourceId: Int = context.resources
            .getIdentifier("status_bar_height", "dimen", "android")
        if (resourceId > 0)
            return context.resources.getDimensionPixelSize(resourceId)
        return 0
    }

    fun getScreenWidth(context: Context): Int {
        return context.resources.displayMetrics.widthPixels
    }

    fun getScreenHeight(context: Context): Int {
        return context.resources.displayMetrics.heightPixels
    }

}