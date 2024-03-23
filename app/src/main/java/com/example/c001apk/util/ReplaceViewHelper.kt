package com.example.c001apk.util

import android.content.Context
import android.view.View
import android.view.ViewGroup
import android.widget.RelativeLayout

class ReplaceViewHelper(val context: Context) {

    private var mTargetView: View? = null

    /**
     * @return 返回你替换进来的View
     */
    var view: View? = null
        private set

    /**
     * 用来替换某个View，比如你可以用一个空页面去替换某个View
     *
     * @param targetView       被替换的那个View
     * @param replaceViewResId 要替换进去的布局LayoutId
     * @return
     */
    fun toReplaceView(targetView: View?, replaceViewResId: Int): ReplaceViewHelper {
        toReplaceView(targetView, View.inflate(context, replaceViewResId, null))
        return this
    }

    /**
     * 用来替换某个View，比如你可以用一个空页面去替换某个View
     *
     * @param targetView  被替换的那个View
     * @param replaceView 要替换进去的那个View
     * @return
     */
    fun toReplaceView(targetView: View?, replaceView: View?): ReplaceViewHelper {
        mTargetView = targetView
        if (mTargetView == null) {
            return this
        } else if (mTargetView?.parent !is ViewGroup) {
            return this
        }
        val parentViewGroup = mTargetView?.parent as ViewGroup
        val index = parentViewGroup.indexOfChild(mTargetView)
        if (view != null) {
            parentViewGroup.removeView(view)
        }
        view = replaceView
        view?.setLayoutParams(mTargetView?.layoutParams)
        parentViewGroup.addView(view, index)

        //RelativeLayout时别的View可能会依赖这个View的位置，所以不能GONE
        if (parentViewGroup is RelativeLayout) {
            mTargetView?.visibility = View.INVISIBLE
        } else {
            mTargetView?.visibility = View.GONE
        }
        return this
    }

    /**
     * 移除你替换进来的View
     */
    fun removeView(): ReplaceViewHelper {
        if (view != null && mTargetView != null) {
            if (mTargetView?.parent is ViewGroup) {
                val parentViewGroup = mTargetView?.parent as ViewGroup
                parentViewGroup.removeView(view)
                view = null
                mTargetView?.visibility = View.VISIBLE
            }
        }
        return this
    }
}
