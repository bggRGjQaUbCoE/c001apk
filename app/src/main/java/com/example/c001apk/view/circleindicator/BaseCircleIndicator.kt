package com.example.c001apk.view.circleindicator

import android.animation.Animator
import android.animation.AnimatorInflater
import android.annotation.SuppressLint
import android.content.Context
import android.content.res.ColorStateList
import android.util.AttributeSet
import android.util.TypedValue
import android.view.Gravity
import android.view.View
import android.view.animation.Interpolator
import android.widget.LinearLayout
import androidx.annotation.ColorInt
import androidx.annotation.DrawableRes
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.DrawableCompat
import androidx.core.view.ViewCompat
import com.example.c001apk.R
import kotlin.math.abs

open class BaseCircleIndicator : LinearLayout {

    private var mIndicatorMargin = -1
    private var mIndicatorWidth = -1
    private var mIndicatorHeight = -1
    private var mIndicatorBackgroundResId = 0
    private var mIndicatorUnselectedBackgroundResId = 0
    private var mIndicatorTintColor: ColorStateList? = null
    private var mIndicatorTintUnselectedColor: ColorStateList? = null
    private var mAnimatorOut: Animator? = null
    private var mAnimatorIn: Animator? = null
    private var mImmediateAnimatorOut: Animator? = null
    private var mImmediateAnimatorIn: Animator? = null
    var mLastPosition = -1
    private var mIndicatorCreatedListener: IndicatorCreatedListener? = null

    constructor(context: Context) : super(context) {
        init(context, null)
    }

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        init(context, attrs)
    }

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    ) {
        init(context, attrs)
    }

    constructor(
        context: Context, attrs: AttributeSet?, defStyleAttr: Int,
        defStyleRes: Int
    ) : super(context, attrs, defStyleAttr, defStyleRes) {
        init(context, attrs)
    }

    private fun init(context: Context, attrs: AttributeSet?) {
        val config = handleTypedArray(context, attrs)
        initialize(config)
        if (isInEditMode) {
            createIndicators(3, 1)
        }
    }

    @SuppressLint("ResourceType")
    private fun handleTypedArray(context: Context, attrs: AttributeSet?): Config {
        val config = Config()
        if (attrs == null) {
            return config
        }
        val typedArray = context.obtainStyledAttributes(attrs, R.styleable.BaseCircleIndicator)
        config.width =
            typedArray.getDimensionPixelSize(R.styleable.BaseCircleIndicator_ci_width, -1)
        config.height =
            typedArray.getDimensionPixelSize(R.styleable.BaseCircleIndicator_ci_height, -1)
        config.margin =
            typedArray.getDimensionPixelSize(R.styleable.BaseCircleIndicator_ci_margin, -1)
        config.animatorResId = typedArray.getResourceId(
            R.styleable.BaseCircleIndicator_ci_animator,
            R.anim.scale_with_alpha
        )
        config.animatorReverseResId =
            typedArray.getResourceId(R.styleable.BaseCircleIndicator_ci_animator_reverse, 0)
        config.backgroundResId = typedArray.getResourceId(
            R.styleable.BaseCircleIndicator_ci_drawable,
            R.drawable.white_radius
        )
        config.unselectedBackgroundId = config.backgroundResId
        //  typedArray.getResourceId(R.styleable.BaseCircleIndicator_ci_drawable_unselected,
        // config.backgroundResId );
        config.orientation =
            -1 //typedArray.getInt(R.styleable.BaseCircleIndicator_ci_orientation, -1);
        config.gravity = -1 //typedArray.getInt(R.styleable.BaseCircleIndicator_ci_gravity, -1);
        typedArray.recycle()
        return config
    }

    private fun initialize(config: Config) {
        val miniSize = (TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            5f, resources.displayMetrics
        ) + 0.5f).toInt()
        mIndicatorWidth = if (config.width < 0) miniSize else config.width
        mIndicatorHeight = if (config.height < 0) miniSize else config.height
        mIndicatorMargin = if (config.margin < 0) miniSize else config.margin
        mAnimatorOut = createAnimatorOut(config)
        mImmediateAnimatorOut = createAnimatorOut(config)
        mImmediateAnimatorOut?.setDuration(0)
        mAnimatorIn = createAnimatorIn(config)
        mImmediateAnimatorIn = createAnimatorIn(config)
        mImmediateAnimatorIn?.setDuration(0)
        mIndicatorBackgroundResId =
            if (config.backgroundResId == 0) R.drawable.white_radius else config.backgroundResId
        mIndicatorUnselectedBackgroundResId =
            if (config.unselectedBackgroundId == 0) config.backgroundResId else config.unselectedBackgroundId
        orientation =
            if (config.orientation == VERTICAL) VERTICAL else HORIZONTAL
        gravity = if (config.gravity >= 0) config.gravity else Gravity.CENTER
    }

    @JvmOverloads
    fun tintIndicator(
        @ColorInt indicatorColor: Int,
        @ColorInt unselectedIndicatorColor: Int = indicatorColor
    ) {
        mIndicatorTintColor = ColorStateList.valueOf(indicatorColor)
        mIndicatorTintUnselectedColor = ColorStateList.valueOf(unselectedIndicatorColor)
        changeIndicatorBackground()
    }

    @JvmOverloads
    fun changeIndicatorResource(
        @DrawableRes indicatorResId: Int,
        @DrawableRes indicatorUnselectedResId: Int = indicatorResId
    ) {
        mIndicatorBackgroundResId = indicatorResId
        mIndicatorUnselectedBackgroundResId = indicatorUnselectedResId
        changeIndicatorBackground()
    }

    interface IndicatorCreatedListener {
        /**
         * IndicatorCreatedListener
         *
         * @param view     internal indicator view
         * @param position position
         */
        fun onIndicatorCreated(view: View?, position: Int)
    }

    fun setIndicatorCreatedListener(
        indicatorCreatedListener: IndicatorCreatedListener?
    ) {
        mIndicatorCreatedListener = indicatorCreatedListener
    }

    private fun createAnimatorOut(config: Config): Animator {
        return AnimatorInflater.loadAnimator(context, config.animatorResId)
    }

    private fun createAnimatorIn(config: Config): Animator {
        val animatorIn: Animator
        if (config.animatorReverseResId == 0) {
            animatorIn = AnimatorInflater.loadAnimator(context, config.animatorResId)
            animatorIn.interpolator =
                ReverseInterpolator()
        } else {
            animatorIn = AnimatorInflater.loadAnimator(context, config.animatorReverseResId)
        }
        return animatorIn
    }

    fun createIndicators(count: Int, currentPosition: Int) {
        if (mImmediateAnimatorOut?.isRunning == true) {
            mImmediateAnimatorOut?.end()
            mImmediateAnimatorOut?.cancel()
        }
        if (mImmediateAnimatorIn?.isRunning == true) {
            mImmediateAnimatorIn?.end()
            mImmediateAnimatorIn?.cancel()
        }

        // Diff View
        val childViewCount = childCount
        if (count < childViewCount) {
            removeViews(count, childViewCount - count)
        } else if (count > childViewCount) {
            val addCount = count - childViewCount
            val orientation = orientation
            for (i in 0 until addCount) {
                addIndicator(orientation)
            }
        }

        // Bind Style
        var indicator: View
        for (i in 0 until count) {
            indicator = getChildAt(i)
            if (currentPosition == i) {
                bindIndicatorBackground(indicator, mIndicatorBackgroundResId, mIndicatorTintColor)
                mImmediateAnimatorOut?.setTarget(indicator)
                mImmediateAnimatorOut?.start()
                mImmediateAnimatorOut?.end()
            } else {
                bindIndicatorBackground(
                    indicator, mIndicatorUnselectedBackgroundResId,
                    mIndicatorTintUnselectedColor
                )
                mImmediateAnimatorIn?.setTarget(indicator)
                mImmediateAnimatorIn?.start()
                mImmediateAnimatorIn?.end()
            }
            if (mIndicatorCreatedListener != null) {
                mIndicatorCreatedListener?.onIndicatorCreated(indicator, i)
            }
        }
        mLastPosition = currentPosition
    }

    private fun addIndicator(orientation: Int) {
        val indicator = View(context)
        val params = generateDefaultLayoutParams()
        params.width = mIndicatorWidth
        params.height = mIndicatorHeight
        if (orientation == HORIZONTAL) {
            params.leftMargin = mIndicatorMargin
            params.rightMargin = mIndicatorMargin
        } else {
            params.topMargin = mIndicatorMargin
            params.bottomMargin = mIndicatorMargin
        }
        addView(indicator, params)
    }

    fun animatePageSelected(position: Int) {
        if (mLastPosition == position) {
            return
        }
        if (mAnimatorIn?.isRunning == true) {
            mAnimatorIn?.end()
            mAnimatorIn?.cancel()
        }
        if (mAnimatorOut?.isRunning == true) {
            mAnimatorOut?.end()
            mAnimatorOut?.cancel()
        }
        val currentIndicator: View? = getChildAt(mLastPosition)
        if (mLastPosition >= 0 && currentIndicator != null) {
            bindIndicatorBackground(
                currentIndicator, mIndicatorUnselectedBackgroundResId,
                mIndicatorTintUnselectedColor
            )
            mAnimatorIn?.setTarget(currentIndicator)
            mAnimatorIn?.start()
        }
        val selectedIndicator = getChildAt(position)
        if (selectedIndicator != null) {
            bindIndicatorBackground(
                selectedIndicator, mIndicatorBackgroundResId,
                mIndicatorTintColor
            )
            mAnimatorOut?.setTarget(selectedIndicator)
            mAnimatorOut?.start()
        }
        mLastPosition = position
    }

    private fun changeIndicatorBackground() {
        val count = childCount
        if (count <= 0) {
            return
        }
        var currentIndicator: View
        for (i in 0 until count) {
            currentIndicator = getChildAt(i)
            if (i == mLastPosition) {
                bindIndicatorBackground(
                    currentIndicator, mIndicatorBackgroundResId,
                    mIndicatorTintColor
                )
            } else {
                bindIndicatorBackground(
                    currentIndicator, mIndicatorUnselectedBackgroundResId,
                    mIndicatorTintUnselectedColor
                )
            }
        }
    }

    private fun bindIndicatorBackground(
        view: View, @DrawableRes drawableRes: Int,
        tintColor: ColorStateList?
    ) {
        if (tintColor != null) {
            val indicatorDrawable = ContextCompat.getDrawable(context, drawableRes)?.mutate()?.let {
                DrawableCompat.wrap(it)
            }
            indicatorDrawable?.let {
                DrawableCompat.setTintList(it, tintColor)
            }
            ViewCompat.setBackground(view, indicatorDrawable)
        } else {
            view.setBackgroundResource(drawableRes)
        }
    }

    protected class ReverseInterpolator : Interpolator {
        override fun getInterpolation(value: Float): Float {
            return abs((1.0f - value).toDouble()).toFloat()
        }
    }

}
