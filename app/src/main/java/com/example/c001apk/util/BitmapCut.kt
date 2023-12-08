package com.example.c001apk.util

import android.graphics.Bitmap

object BitmapCut {
    fun cutBitmap(bm: Bitmap?): Bitmap? {
        var bitmap: Bitmap? = null
        if (bm != null) {
            bitmap =
                if (bm.height < bm.width * 1320 / 540) {
                    Bitmap.createBitmap(bm, 0, 0, bm.width, bm.width)
                    throw IllegalArgumentException("BitmapCut: height: ${bm.height}, width: ${bm.width}")
                } else
                    Bitmap.createBitmap(bm, 0, 0, bm.width, bm.width * 1320 / 540)
        }
        return bitmap
    }
}