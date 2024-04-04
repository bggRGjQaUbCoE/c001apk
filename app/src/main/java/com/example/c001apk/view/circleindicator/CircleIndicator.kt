package com.example.c001apk.view.circleindicator

import android.content.Context
import android.database.DataSetObserver
import android.util.AttributeSet
import androidx.viewpager.widget.ViewPager
import androidx.viewpager.widget.ViewPager.OnPageChangeListener

/**
 * CircleIndicator work with ViewPager
 */
class CircleIndicator : BaseCircleIndicator {
    private var mViewpager: ViewPager? = null

    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    )

    constructor(
        context: Context,
        attrs: AttributeSet?,
        defStyleAttr: Int,
        defStyleRes: Int
    ) : super(context, attrs, defStyleAttr, defStyleRes)

    fun setViewPager(viewPager: ViewPager?) {
        mViewpager = viewPager
        if (mViewpager != null && mViewpager?.adapter != null) {
            mLastPosition = -1
            createIndicators()
            mViewpager?.removeOnPageChangeListener(mInternalPageChangeListener)
            mViewpager?.addOnPageChangeListener(mInternalPageChangeListener)
            mInternalPageChangeListener.onPageSelected(mViewpager?.currentItem ?: 0)
        }
    }

    private fun createIndicators() {
        val adapter = mViewpager?.adapter
        val count: Int = adapter?.count ?: 0
        createIndicators(count, mViewpager?.currentItem ?: 0)
    }

    private val mInternalPageChangeListener: OnPageChangeListener = object : OnPageChangeListener {
        override fun onPageScrolled(
            position: Int, positionOffset: Float,
            positionOffsetPixels: Int
        ) {
        }

        override fun onPageSelected(position: Int) {
            if (mViewpager?.adapter == null
                || (mViewpager?.adapter?.count ?: 0) <= 0
            ) {
                return
            }
            animatePageSelected(position)
        }

        override fun onPageScrollStateChanged(state: Int) {}
    }
    val dataSetObserver: DataSetObserver = object : DataSetObserver() {
        override fun onChanged() {
            super.onChanged()
            if (mViewpager == null) {
                return
            }
            val adapter = mViewpager?.adapter
            val newCount = adapter?.count ?: 0
            val currentCount = childCount
            mLastPosition = if (newCount == currentCount) {
                // No change
                return
            } else if (mLastPosition < newCount) {
                mViewpager?.currentItem ?: 0
            } else {
                -1
            }
            createIndicators()
        }
    }

    @Deprecated("User ViewPager addOnPageChangeListener")
    fun setOnPageChangeListener(
        onPageChangeListener: OnPageChangeListener?
    ) {
        if (mViewpager == null) {
            throw NullPointerException("can not find Viewpager , setViewPager first")
        }
        onPageChangeListener?.let {
            mViewpager?.removeOnPageChangeListener(it)
            mViewpager?.addOnPageChangeListener(it)
        }
    }
}
