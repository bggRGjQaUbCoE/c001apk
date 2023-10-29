package com.example.c001apk.view

import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.drawable.Drawable
import android.text.style.ImageSpan

public class CenteredImageSpan(drawableRes: Drawable, textSize: Int) :
    ImageSpan(drawableRes) {

    val size = textSize

    override fun getSize(
        paint: Paint,
        text: CharSequence?,
        start: Int,
        end: Int,
        fm: Paint.FontMetricsInt?
    ): Int {
        //return super.getSize(paint, text, start, end, fm)
        return size
    }

    override fun draw(
        canvas: Canvas, text: CharSequence,
        start: Int, end: Int, x: Float,
        top: Int, y: Int, bottom: Int, paint: Paint
    ) {
        // image to draw
        val b = drawable
        // font metrics of text to be replaced
        val fm = paint.fontMetricsInt
        val transY = ((y + fm.descent + y + fm.ascent) / 2
                - b.bounds.bottom / 2)
        canvas.save()
        canvas.translate(x, transY.toFloat())
        b.draw(canvas)
        canvas.restore()
    }
}