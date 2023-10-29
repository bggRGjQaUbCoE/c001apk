package com.example.c001apk.view

//noinspection SuspiciousImport
import android.R
import android.content.Context
import android.util.AttributeSet
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputConnection
import android.view.inputmethod.InputConnectionWrapper
import com.google.android.material.textfield.TextInputEditText

class ExtendEditText : TextInputEditText {

    private var mOnPasteCallback: OnPasteCallback? = null

    interface OnPasteCallback {
        fun onPaste(text: String?, isPaste: Boolean)
    }

    fun setOnPasteCallback(onPasteCallback: OnPasteCallback?) {
        mOnPasteCallback = onPasteCallback
    }

    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    )

    override fun onCreateInputConnection(outAttrs: EditorInfo): InputConnection {
        return MyInputConnection(super.onCreateInputConnection(outAttrs), false)
    }

    internal inner class MyInputConnection
        (target: InputConnection?, mutable: Boolean) : InputConnectionWrapper(target, mutable),
        InputConnection {
        override fun commitText(text: CharSequence, newCursorPosition: Int): Boolean {
            if (mOnPasteCallback != null) {
                mOnPasteCallback!!.onPaste(text.toString(), false)
            }
            //return super<InputConnectionWrapper>.commitText(text, newCursorPosition)
            return false
        }
    }

    override fun onTextContextMenuItem(id: Int): Boolean {
        if (id == R.id.copy) {
            //Log.i("shawn", "onTextContextMenuItem 复制")
        } else if (id == R.id.paste) {
            if (mOnPasteCallback != null) {
                mOnPasteCallback!!.onPaste(null, true)
            }
            //Log.i("shawn", "onTextContextMenuItem 粘贴")
        } else if (id == R.id.selectAll) {
            //Log.i("shawn", "onTextContextMenuItem 全选")
        } else {
            //Log.i("shawn", "onTextContextMenuItem id$id")
        }
        return super.onTextContextMenuItem(id)
    }
}