package com.example.c001apk.view.ninegridimageview.indicator

import android.animation.ValueAnimator
import android.view.Gravity
import android.view.View
import android.widget.FrameLayout
import androidx.core.view.isVisible
import androidx.viewpager.widget.ViewPager
import net.mikaelzero.mojito.interfaces.IIndicator
import net.mikaelzero.mojito.tools.Utils

/**
 * @Author: MikaelZero
 * @CreateDate: 2020/6/13 5:39 PM
 * @Description:
 */
class CircleIndexIndicator : IIndicator {

    private var circleIndicator: CircleIndicator? = null
    private var originBottomMargin = 10
    private var currentBottomMargin = originBottomMargin

    override fun attach(parent: FrameLayout) {
        originBottomMargin = Utils.dip2px(parent.context, 16f)
        val indexLp = FrameLayout.LayoutParams(
            FrameLayout.LayoutParams.WRAP_CONTENT,
            Utils.dip2px(parent.context, 36f)
        )
        indexLp.gravity = Gravity.BOTTOM or Gravity.CENTER_HORIZONTAL
        indexLp.bottomMargin = originBottomMargin
        circleIndicator = CircleIndicator(parent.context)
        circleIndicator?.gravity = Gravity.CENTER_VERTICAL
        circleIndicator?.setLayoutParams(indexLp)
        parent.addView(circleIndicator)
    }

    override fun onShow(viewPager: ViewPager) {
        circleIndicator?.isVisible = true
        circleIndicator?.setViewPager(viewPager)
    }

    override fun move(moveX: Float, moveY: Float) {
        if (circleIndicator == null) {
            return
        }
        val indexLp = circleIndicator!!.layoutParams as FrameLayout.LayoutParams
        currentBottomMargin = Math.round(originBottomMargin - moveY / 6f)
        if (currentBottomMargin > originBottomMargin) {
            currentBottomMargin = originBottomMargin
        }
        indexLp.bottomMargin = currentBottomMargin
        circleIndicator?.setLayoutParams(indexLp)
    }

    override fun fingerRelease(isToMax: Boolean, isToMin: Boolean) {
        if (circleIndicator == null) {
            return
        }
        var begin = 0
        var end = 0
        if (isToMax) {
            begin = currentBottomMargin
            end = originBottomMargin
        }
        if (isToMin) {
            circleIndicator!!.visibility = View.GONE
            return
        }
        val indexLp = circleIndicator!!.layoutParams as FrameLayout.LayoutParams
        val valueAnimator = ValueAnimator.ofInt(begin, end)
        valueAnimator.addUpdateListener { animation: ValueAnimator ->
            indexLp.bottomMargin = animation.getAnimatedValue() as Int
            circleIndicator!!.setLayoutParams(indexLp)
        }
        valueAnimator.setDuration(300).start()
    }
}