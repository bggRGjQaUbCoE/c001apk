/*
 * Copyright (C) 2015 AlexMofer
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.example.c001apk.view

import android.content.Context
import android.content.SharedPreferences
import android.util.AttributeSet
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.LinearLayout
import com.example.c001apk.R
import com.example.c001apk.util.dp

/**
 * 顺滑的输入面板
 * Created by Alex on 2016/12/4.
 */
@Suppress("unused")
class SmoothInputLayout : LinearLayout {
    private var mMaxKeyboardHeight = Int.MIN_VALUE
    private var mDefaultKeyboardHeight = 0
    private var mMinKeyboardHeight = 0
    private var mKeyboardHeight = 0
        get() = if (field < 276.dp) 276.dp
        else field
    private var mInputViewId = 0
    private var mInputView: View? = null
    private val imm by lazy {
        context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
    }


    /**
     * 是否输入法已打开
     *
     * @return 是否输入法已打开
     */
    var isKeyBoardOpen = false
    private var mEmojiPanelId = 0
    private var mEmojiPanel: View? = null
    private var mListener: OnVisibilityChangeListener? = null
    private var keyboardChangeListener: OnKeyboardChangeListener? = null
    private var mAutoSaveKeyboardHeight = false
    private var mKeyboardProcessor: KeyboardProcessor? = null
    private var isShowEmojiPanel = false

    constructor(context: Context) : super(context) {
        initView(null)
    }

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        initView(attrs)
    }

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    ) {
        initView(attrs)
    }

    constructor(
        context: Context, attrs: AttributeSet?, defStyleAttr: Int,
        defStyleRes: Int
    ) : super(context, attrs, defStyleAttr, defStyleRes) {
        initView(attrs)
    }

    private fun initView(attrs: AttributeSet?) {
        var defaultInputHeight =
            (DEFAULT_KEYBOARD_HEIGHT * resources.displayMetrics.density).toInt()
        var minInputHeight = (MIN_KEYBOARD_HEIGHT * resources.displayMetrics.density).toInt()
        mInputViewId = NO_ID
        mEmojiPanelId = NO_ID
        val autoSave: Boolean
        val custom = context.obtainStyledAttributes(attrs, R.styleable.SmoothInputLayout)
        defaultInputHeight = custom.getDimensionPixelOffset(
            R.styleable.SmoothInputLayout_silDefaultKeyboardHeight, defaultInputHeight
        )
        minInputHeight = custom.getDimensionPixelOffset(
            R.styleable.SmoothInputLayout_silMinKeyboardHeight,
            minInputHeight
        )
        mInputViewId =
            custom.getResourceId(R.styleable.SmoothInputLayout_silInputView, mInputViewId)
        mEmojiPanelId =
            custom.getResourceId(R.styleable.SmoothInputLayout_silEmojiPanel, mEmojiPanelId)
        autoSave =
            custom.getBoolean(R.styleable.SmoothInputLayout_silAutoSaveKeyboardHeight, true)
        custom.recycle()
        setDefaultKeyboardHeight(defaultInputHeight)
        setMinKeyboardHeight(minInputHeight)
        setAutoSaveKeyboardHeight(autoSave)
    }

    override fun onFinishInflate() {
        super.onFinishInflate()
        if (mInputViewId != NO_ID) {
            setInputView(findViewById(mInputViewId))
        }
        if (mEmojiPanelId != NO_ID) {
            setEmojiPanel(findViewById(mEmojiPanelId))
        }
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val heightSize = MeasureSpec.getSize(heightMeasureSpec)
        if (heightSize > mMaxKeyboardHeight) {
            mMaxKeyboardHeight = heightSize
        }
        val heightChange = mMaxKeyboardHeight - heightSize
        if (heightChange > mMinKeyboardHeight) {
            if (mKeyboardHeight != heightChange) {
                mKeyboardHeight = heightChange
                saveKeyboardHeight()
            }
            isKeyBoardOpen = true
            // 输入法弹出，隐藏功能面板
            if (mEmojiPanel != null && mEmojiPanel?.visibility == VISIBLE) {
                mEmojiPanel?.visibility = GONE
                mListener?.onVisibilityChange(GONE)
            }
        } else {
            isKeyBoardOpen = false
            if (isShowEmojiPanel) {
                isShowEmojiPanel = false
                if (mEmojiPanel != null && mEmojiPanel?.visibility == GONE) {
                    updateLayout()
                    mEmojiPanel?.visibility = VISIBLE
                    mListener?.onVisibilityChange(VISIBLE)
                    forceLayout()
                }
            }
        }
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        keyboardChangeListener?.onKeyboardChanged(isKeyBoardOpen)
    }

    private val keyboardSharedPreferences: SharedPreferences
        /**
         * 获取键盘SP
         *
         * @return 键盘SP
         */
        get() = context.getSharedPreferences(SP_KEYBOARD, Context.MODE_PRIVATE)

    /**
     * 存储键盘高度
     */
    private fun saveKeyboardHeight() {
        if (mAutoSaveKeyboardHeight)
            keyboardSharedPreferences.edit().putInt(KEY_HEIGHT, mKeyboardHeight).apply()
        else
            mKeyboardProcessor?.onSaveKeyboardHeight(mKeyboardHeight)
    }

    /**
     * 更新子项高度
     */
    private fun updateLayout() {
        if (mEmojiPanel == null) return
        if (mKeyboardHeight == 0) mKeyboardHeight = getKeyboardHeight(mDefaultKeyboardHeight)
        val layoutParams = mEmojiPanel?.layoutParams
        if (layoutParams != null) {
            layoutParams.height = mKeyboardHeight
            mEmojiPanel?.setLayoutParams(layoutParams)
        }
    }

    private fun getKeyboardHeight(defaultHeight: Int): Int {
        return if (mAutoSaveKeyboardHeight)
            keyboardSharedPreferences.getInt(KEY_HEIGHT, defaultHeight)
        else if (mKeyboardProcessor != null)
            mKeyboardProcessor?.getSavedKeyboardHeight(defaultHeight) ?: defaultHeight
        else defaultHeight
    }

    /**
     * 设置默认系统输入面板高度
     *
     * @param height 输入面板高度
     */
    private fun setDefaultKeyboardHeight(height: Int) {
        if (mDefaultKeyboardHeight != height)
            mDefaultKeyboardHeight = height
    }

    /**
     * 设置最小系统输入面板高度
     *
     * @param height 输入面板高度
     */
    private fun setMinKeyboardHeight(height: Int) {
        if (mMinKeyboardHeight != height)
            mMinKeyboardHeight = height
    }

    /**
     * 设置输入框
     *
     * @param edit 输入框
     */
    private fun setInputView(edit: View) {
        if (mInputView !== edit)
            mInputView = edit
    }

    /**
     * 设置特殊输入面板
     *
     * @param pane 面板
     */
    private fun setEmojiPanel(pane: View) {
        if (mEmojiPanel !== pane)
            mEmojiPanel = pane
    }

    /**
     * 设置面板可见改变监听
     *
     * @param listener 面板可见改变监听
     */
    fun setOnVisibilityChangeListener(listener: OnVisibilityChangeListener?) {
        mListener = listener
    }

    /**
     * 设置键盘改变监听
     *
     * @param listener 键盘改变监听
     */
    fun setOnKeyboardChangeListener(listener: OnKeyboardChangeListener?) {
        keyboardChangeListener = listener
    }

    /**
     * 设置自动保存键盘高度
     *
     * @param auto 是否自动
     */
    private fun setAutoSaveKeyboardHeight(auto: Boolean) {
        mAutoSaveKeyboardHeight = auto
    }

    /**
     * 设置键盘处理器
     * 仅在关闭自动保存键盘高度时设置的处理器才有效[.setAutoSaveKeyboardHeight]
     *
     * @param processor 处理器
     */
    fun setKeyboardProcessor(processor: KeyboardProcessor?) {
        mKeyboardProcessor = processor
    }

    private val isEmojiPanelOpen: Boolean
        /**
         * 是否特殊输入面板已打开
         *
         * @return 特殊输入面板已打开
         */
        get() = mEmojiPanel != null && mEmojiPanel?.visibility == VISIBLE

    /**
     * 关闭特殊输入面板
     */
    fun closeEmojiPanel() {
        if (isEmojiPanelOpen) {
            mEmojiPanel?.visibility = GONE
            mListener?.onVisibilityChange(GONE)
        }
    }

    /**
     * 显示特殊输入面板
     *
     * @param focus 是否让输入框拥有焦点
     */
    fun showEmojiPanel(focus: Boolean) {
        if (isKeyBoardOpen) {
            isShowEmojiPanel = true
            imm.hideSoftInputFromWindow(windowToken, InputMethodManager.HIDE_NOT_ALWAYS)
        } else {
            if (mEmojiPanel != null && mEmojiPanel?.visibility == GONE) {
                updateLayout()
                mEmojiPanel?.visibility = VISIBLE
                mListener?.onVisibilityChange(VISIBLE)
            }
        }
        if (focus) {
            mInputView?.requestFocus()
            mInputView?.requestFocusFromTouch()
        } else {
            if (mInputView != null) {
                isFocusable = true
                setFocusableInTouchMode(true)
                mInputView?.clearFocus()
            }
        }
    }

    /**
     * 关闭键盘
     *
     * @param clearFocus 是否清除输入框焦点
     */
    fun closeKeyboard(clearFocus: Boolean) {
        imm.hideSoftInputFromWindow(windowToken, InputMethodManager.HIDE_NOT_ALWAYS)
        if (clearFocus && mInputView != null) {
            isFocusable = true
            setFocusableInTouchMode(true)
            mInputView?.clearFocus()
        }
    }

    /**
     * 打开键盘
     */
    fun showKeyboard() {
        mInputView?.let {
            it.requestFocus()
            it.requestFocusFromTouch()
            imm.showSoftInput(it, InputMethodManager.SHOW_IMPLICIT)
        }
    }

    /**
     * 面板可见改变监听
     */
    interface OnVisibilityChangeListener {
        fun onVisibilityChange(visibility: Int)
    }

    /**
     * 键盘改变监听
     */
    interface OnKeyboardChangeListener {
        fun onKeyboardChanged(open: Boolean)
    }

    /**
     * 键盘处理器
     */
    interface KeyboardProcessor {
        /**
         * 存储键盘高度
         *
         * @param height 高度
         */
        fun onSaveKeyboardHeight(height: Int)

        /**
         * 获取存储的键盘高度
         *
         * @param defaultHeight 默认高度
         * @return 键盘高度
         */
        fun getSavedKeyboardHeight(defaultHeight: Int): Int
    }

    companion object {
        const val DEFAULT_KEYBOARD_HEIGHT = 387
        const val MIN_KEYBOARD_HEIGHT = 20
        private const val SP_KEYBOARD = "keyboard"
        private const val KEY_HEIGHT = "height"
    }
}