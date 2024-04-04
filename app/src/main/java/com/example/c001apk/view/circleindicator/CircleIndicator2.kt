package com.example.c001apk.view.circleindicator

import android.content.Context
import android.util.AttributeSet
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.SnapHelper

/**
 * CircleIndicator2 work with RecyclerView and SnapHelper
 */
class CircleIndicator2 : BaseCircleIndicator {
    private var mRecyclerView: RecyclerView? = null
    private var mSnapHelper: SnapHelper? = null

    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

    constructor(
        context: Context, attrs: AttributeSet?, defStyleAttr: Int,
        defStyleRes: Int
    ) : super(context, attrs, defStyleAttr, defStyleRes)

    fun attachToRecyclerView(
        recyclerView: RecyclerView,
        snapHelper: SnapHelper
    ) {
        mRecyclerView = recyclerView
        mSnapHelper = snapHelper
        mLastPosition = -1
        createIndicators()
        recyclerView.removeOnScrollListener(mInternalOnScrollListener)
        recyclerView.addOnScrollListener(mInternalOnScrollListener)
    }

    private fun createIndicators() {
        val adapter = mRecyclerView?.adapter
        val count: Int = adapter?.itemCount ?: 0
        createIndicators(count, getSnapPosition(mRecyclerView?.layoutManager))
    }

    fun getSnapPosition(layoutManager: RecyclerView.LayoutManager?): Int {
        if (layoutManager == null) {
            return RecyclerView.NO_POSITION
        }
        val snapView = mSnapHelper?.findSnapView(layoutManager)
            ?: return RecyclerView.NO_POSITION
        return layoutManager.getPosition(snapView)
    }

    private val mInternalOnScrollListener: RecyclerView.OnScrollListener =
        object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                val position = getSnapPosition(recyclerView.layoutManager)
                if (position == RecyclerView.NO_POSITION) {
                    return
                }
                animatePageSelected(position)
            }
        }
    val adapterDataObserver: RecyclerView.AdapterDataObserver =
        object : RecyclerView.AdapterDataObserver() {
            override fun onChanged() {
                super.onChanged()
                if (mRecyclerView == null) {
                    return
                }
                val adapter = mRecyclerView?.adapter
                val newCount = adapter?.itemCount ?: 0
                val currentCount = childCount
                mLastPosition = if (newCount == currentCount) {
                    // No change
                    return
                } else if (mLastPosition < newCount) {
                    getSnapPosition(mRecyclerView?.layoutManager)
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
