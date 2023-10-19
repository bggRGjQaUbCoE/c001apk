package com.example.c001apk.util

import android.content.Context
import android.content.res.Resources
import android.util.TypedValue


object DensityTool {

    fun getScreenWidth(context: Context): Int {
        return context.resources.displayMetrics.widthPixels
    }

    fun dp2px(context: Context, dp: Float): Float {
        return TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            dp,
            context.resources.displayMetrics
        )
    }

    fun px2dp(pxValue: Float): Float {
        return pxValue / Resources.getSystem().displayMetrics.density
    }

}