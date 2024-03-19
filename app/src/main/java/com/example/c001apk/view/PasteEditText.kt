package com.example.c001apk.view

//noinspection SuspiciousImport
import android.R
import android.content.Context
import android.util.AttributeSet
import com.google.android.material.textfield.TextInputEditText


class PasteEditText : TextInputEditText {
    private var mOnPasteCallback: OnPasteCallback? = null

    interface OnPasteCallback {
        fun onPaste()
    }

    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)

    override fun onTextContextMenuItem(id: Int): Boolean {
        when (id) {
            R.id.cut -> {}
            R.id.copy -> {}
            R.id.paste ->
                mOnPasteCallback?.onPaste()
        }
        return super.onTextContextMenuItem(id)
    }

    fun setOnPasteCallback(onPasteCallback: OnPasteCallback?) {
        mOnPasteCallback = onPasteCallback
    }
}