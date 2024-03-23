package com.example.c001apk.view

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.ColorFilter
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.Typeface
import android.graphics.drawable.Drawable
import android.text.TextPaint
import com.absinthe.libraries.utils.extensions.dp
import com.google.android.material.color.MaterialColors

/*
    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.
 */

// mod from https://github.com/klinker24/Android-BadgedImageView
class BadgeDrawable(context: Context, private val text: String) : Drawable() {
    private val width: Int
    private val height: Int
    private val paint: Paint

    init {
        val padding = PADDING
        val cornerRadius = CORNER_RADIUS
        val textBounds = Rect()
        val textPaint = TextPaint(Paint.ANTI_ALIAS_FLAG or Paint.SUBPIXEL_TEXT_FLAG).apply {
            setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.NORMAL))
            textSize = TEXT_SIZE
            getTextBounds(text, 0, text.length, textBounds)
            setColor(
                MaterialColors.getColor(
                    context,
                    com.google.android.material.R.attr.colorOnPrimaryContainer,
                    0
                )
            )
        }
        height = (padding + textBounds.height() + padding).toInt()
        width = (padding + textBounds.width() + padding).toInt()
        if (bitmaps[text] == null) {
            val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
            bitmap.setHasAlpha(true)
            val canvas = Canvas(bitmap)
            val backgroundPaint = Paint(Paint.ANTI_ALIAS_FLAG)
            backgroundPaint.setColor(
                MaterialColors.getColor(
                    context,
                    com.google.android.material.R.attr.colorPrimaryContainer,
                    0
                )
            )
            canvas.drawRoundRect(
                0f, 0f, width.toFloat(), height.toFloat(),
                cornerRadius, cornerRadius, backgroundPaint
            )
            val fix = if (text == "GIF") 1 else 2
            canvas.drawText(text, padding - fix, height - padding - fix, textPaint)
            bitmaps[text] = bitmap
        }
        paint = Paint()
    }

    override fun getIntrinsicWidth(): Int {
        return width
    }

    override fun getIntrinsicHeight(): Int {
        return height
    }

    override fun draw(canvas: Canvas) {
        bitmaps[text]?.let {
            canvas.drawBitmap(
                it,
                getBounds().left.toFloat(),
                getBounds().top.toFloat(),
                paint
            )
        }

    }

    override fun setAlpha(alpha: Int) {}

    override fun setColorFilter(cf: ColorFilter?) {
        paint.setColorFilter(cf)
    }

    override fun getOpacity(): Int {
        return 0.dp
    }

    companion object {
        private val bitmaps: MutableMap<String, Bitmap?> = HashMap()
        private val TEXT_SIZE = 12.dp.toFloat()
        private val PADDING = 4.dp.toFloat()
        private val CORNER_RADIUS = 4.dp.toFloat()
    }
}