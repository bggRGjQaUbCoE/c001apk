package com.example.c001apk.view


import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Color
import android.text.Selection
import android.text.Spannable
import android.text.method.LinkMovementMethod
import android.text.method.Touch
import android.text.style.ClickableSpan
import android.util.AttributeSet
import android.view.MotionEvent
import android.widget.TextView
import com.example.c001apk.util.SpannableStringBuilderUtil
import rikka.material.widget.FakeFontWeightMaterialTextView

//https://stackoverflow.com/questions/8558732
class LinkTextView : FakeFontWeightMaterialTextView {

    override fun getHighlightColor(): Int {
        return Color.TRANSPARENT
    }

    private var dontConsumeNonUrlClicks = true
    var linkHit = false

    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) :
            super(context, attrs, defStyleAttr)

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent): Boolean {
        linkHit = false
        val res = super.onTouchEvent(event)
        return if (dontConsumeNonUrlClicks) linkHit else res
    }

    class LocalLinkMovementMethod : LinkMovementMethod() {
        override fun onTouchEvent(
            widget: TextView,
            buffer: Spannable, event: MotionEvent
        ): Boolean {
            val action = event.action
            if (action == MotionEvent.ACTION_UP ||
                action == MotionEvent.ACTION_DOWN
            ) {
                var x = event.x.toInt()
                var y = event.y.toInt()
                x -= widget.totalPaddingLeft
                y -= widget.totalPaddingTop
                x += widget.scrollX
                y += widget.scrollY
                val layout = widget.layout
                val line = layout.getLineForVertical(y)
                val off = layout.getOffsetForHorizontal(line, x.toFloat())
                val link = buffer.getSpans(
                    off, off, ClickableSpan::class.java
                )
                return if (link.isNotEmpty()) {
                    if (action == MotionEvent.ACTION_UP) {
                        link[0].onClick(widget)
                    }/* else if (action == MotionEvent.ACTION_DOWN) {
                        Selection.setSelection(
                            buffer,
                            buffer.getSpanStart(link[0]),
                            buffer.getSpanEnd(link[0])
                        )
                    }*/
                    val linkText =
                        buffer.substring(buffer.getSpanStart(link[0]), buffer.getSpanEnd(link[0]))
                    if (widget is LinkTextView) {
                        widget.linkHit = linkText != "查看更多"
                    }
                    true
                } else {
                    Selection.removeSelection(buffer)
                    Touch.onTouchEvent(widget, buffer, event)
                    false
                }
            }
            return Touch.onTouchEvent(widget, buffer, event)
        }

        companion object {
            private var sInstance: LocalLinkMovementMethod? = null
            val instance: LocalLinkMovementMethod?
                get() {
                    if (sInstance == null) sInstance = LocalLinkMovementMethod()
                    return sInstance
                }
        }
    }

    override fun setText(text: CharSequence?, type: BufferType?) {
        this.movementMethod = LocalLinkMovementMethod.instance
        val spText =
            SpannableStringBuilderUtil.setText(
                context,
                text.toString(),
                this.textSize,
                null
            )
        super.setText(spText, type)
    }

}