package com.example.c001apk.view

import android.content.Context
import android.view.View
import androidx.recyclerview.widget.LinearSmoothScroller
import androidx.recyclerview.widget.OrientationHelper
import androidx.recyclerview.widget.RecyclerView


//https://liyuyu.cn/post/RecyclerViewScrollToCenter
class MyLinearSmoothScroller(context: Context) : LinearSmoothScroller(context) {
    override fun onTargetFound(targetView: View, state: RecyclerView.State, action: Action) {
        // 计算距离
        val distance = distanceToCenter(
            layoutManager!!,
            targetView,
            getOrientationHelper(layoutManager!!)!!
        )
        // 计算动画时间
        val time = calculateTimeForDeceleration(distance)
        if (time > 0) {
            // 这里仅实现了水平或者垂直一种方向上的矫正，两者同时的情况暂不考虑
            if (layoutManager!!.canScrollVertically())
                action.update(0, distance, time * 3, mDecelerateInterpolator)
            else
                action.update(distance, 0, time * 3, mDecelerateInterpolator)
        }

    }

    /**
     * 计算 targetView 中心点到 RecyclerView 中心点的距离
     */
    private fun distanceToCenter(
        layoutManager: RecyclerView.LayoutManager,
        targetView: View, helper: OrientationHelper
    ): Int {
        val childCenter =
            helper.getDecoratedStart(targetView) + helper.getDecoratedMeasurement(targetView) / 2
        val containerCenter = if (layoutManager.clipToPadding) {
            helper.startAfterPadding + helper.totalSpace / 2
        } else {
            helper.end / 2
        }
        return childCenter - containerCenter
    }

    /**
     * 不同方向上的距离使用不同的 OrientationHelper
     */
    private fun getOrientationHelper(layoutManager: RecyclerView.LayoutManager): OrientationHelper? {
        if (layoutManager.canScrollVertically()) {
            return OrientationHelper.createVerticalHelper(layoutManager)
        } else if (layoutManager.canScrollHorizontally()) {
            return OrientationHelper.createHorizontalHelper(layoutManager)
        }
        return null
    }
}