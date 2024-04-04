package com.example.c001apk.view.circleindicator

import android.content.Context
import android.util.AttributeSet
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2

/**
 * CircleIndicator work with ViewPager2
 */
class CircleIndicator3 : BaseCircleIndicator {
    private var mViewpager: ViewPager2? = null

    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)

    constructor(
        context: Context, attrs: AttributeSet?, defStyleAttr: Int
    ) : super(context, attrs, defStyleAttr)

    constructor(
        context: Context, attrs: AttributeSet?, defStyleAttr: Int, defStyleRes: Int
    ) : super(context, attrs, defStyleAttr, defStyleRes)

    fun setViewPager(viewPager: ViewPager2?) {
        mViewpager = viewPager
        if (mViewpager != null && mViewpager?.adapter != null) {
            mLastPosition = -1
            createIndicators()
            mViewpager?.unregisterOnPageChangeCallback(mInternalPageChangeCallback)
            mViewpager?.registerOnPageChangeCallback(mInternalPageChangeCallback)
            mInternalPageChangeCallback.onPageSelected(mViewpager?.currentItem ?: 0)
        }
    }

    private fun createIndicators() {
        val adapter = mViewpager?.adapter
        val count: Int = adapter?.itemCount ?: 0
        createIndicators(count, mViewpager?.currentItem ?: 0)
    }

    private val mInternalPageChangeCallback: ViewPager2.OnPageChangeCallback =
        object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                if (position == mLastPosition
                    || mViewpager?.adapter == null
                    || (mViewpager?.adapter?.itemCount ?: 0) <= 0
                ) {
                    return
                }
                animatePageSelected(position)
            }
        }
    val adapterDataObserver: RecyclerView.AdapterDataObserver =
        object : RecyclerView.AdapterDataObserver() {
            override fun onChanged() {
                super.onChanged()
                if (mViewpager == null) {
                    return
                }
                val adapter = mViewpager?.adapter
                val newCount = adapter?.itemCount ?: 0
                val currentCount = childCount
                mLastPosition = if (newCount == currentCount) {
                    // No change
                    return
                } else if (mLastPosition < newCount) {
                    mViewpager?.currentItem ?: 0
                } else {
                    RecyclerView.NO_POSITION
                }
                createIndicators()
            }

            override fun onItemRangeChanged(positionStart: Int, itemCount: Int) {
                super.onItemRangeChanged(positionStart, itemCount)
                onChanged()
            }

            override fun onItemRangeChanged(
                positionStart: Int, itemCount: Int,
                payload: Any?
            ) {
                super.onItemRangeChanged(positionStart, itemCount, payload)
                onChanged()
            }

            override fun onItemRangeInserted(positionStart: Int, itemCount: Int) {
                super.onItemRangeInserted(positionStart, itemCount)
                onChanged()
            }

            override fun onItemRangeRemoved(positionStart: Int, itemCount: Int) {
                super.onItemRangeRemoved(positionStart, itemCount)
                onChanged()
            }

            override fun onItemRangeMoved(fromPosition: Int, toPosition: Int, itemCount: Int) {
                super.onItemRangeMoved(fromPosition, toPosition, itemCount)
                onChanged()
            }
        }
}
