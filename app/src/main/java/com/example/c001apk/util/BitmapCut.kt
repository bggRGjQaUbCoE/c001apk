package com.example.c001apk.util

import android.graphics.Bitmap

object BitmapCut {
    fun cutBitmap(bm: Bitmap?): Bitmap? {
        var bitmap: Bitmap? = null
        if (bm != null) {
            bitmap = Bitmap.createBitmap(bm, 0, 0, bm.width, bm.width * 1320 / 540)
        }
        return bitmap
    }
}