package com.example.c001apk.view

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.util.AttributeSet
import android.view.Gravity
import android.view.View
import androidx.appcompat.widget.AppCompatTextView
import com.example.c001apk.R
import com.example.c001apk.util.dp
import kotlin.math.min


// https://github.com/SmartToolFactory/BadgeTextView
class BadgeTextView : AppCompatTextView {

    private var badgeCount = -1

    var maxNumber = Int.MAX_VALUE

    private var isCircleShape = true

    var circleShapeThreshold = 1

    var paddingVertical = 0
        get() {
            return if (field > borderWidth + badgeShadowYOffset) field
            else (field + borderWidth + badgeShadowYOffset).toInt()
        }

    var paddingHorizontal = 0
        get() {
            return if (field > borderWidth + badgeShadowXOffset) field
            else (field + borderWidth + badgeShadowXOffset).toInt()
        }

    var radiusRatio = .5f
        set(value) {
            if (value < 1) field = value
        }

    private var badgeRoundedRectRadius = radiusRatio

    var badgeBackgroundColor = Color.RED

    var borderWidth = 0f
    var borderColor = Color.rgb(211, 47, 47)

    private val drawBorder: Boolean
        get() {
            return (borderWidth > 0)
        }

    var badgeShadowColor = 0x55000000
    var badgeShadowRadius = 0f

    var badgeShadowXOffset = 0f
        get() {
            return if (badgeShadowRadius == 0f) 0f else field + 0.5f
        }

    var badgeShadowYOffset = 1.5f
        get() {
            return if (badgeShadowRadius == 0f) 0f else field + 0.5f
        }

    private val drawShadow: Boolean
        get() {
            return (badgeShadowRadius > 0)
        }

    private val paintBackground by lazy {
        Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.FILL
            color = badgeBackgroundColor
        }
    }

    private val paintBorder by lazy {
        Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.STROKE
            color = borderColor
        }
    }

    private val paintDebug by lazy {
        Paint().apply {
            style = Paint.Style.STROKE
            strokeWidth = 5f
            color = Color.LTGRAY
        }
    }

    private val rect = Rect()

    var pinMode = false
        set(value) {
            if (value) setBadgeCount(0)
            text = ""
            field = value
        }

    private var currentWidth = 0

    private var textLength = 0

    private fun init() {
        gravity = Gravity.CENTER
        maxLines = 1

        background = null

        if (drawShadow) {
            setLayerType(View.LAYER_TYPE_SOFTWARE, paintBackground)
            paintBackground.setShadowLayer(
                badgeShadowRadius,
                badgeShadowXOffset,
                badgeShadowYOffset,
                badgeShadowColor
            )
        }

        textLength = text.length

    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {

        val length = text.length

        isCircleShape = (length <= circleShapeThreshold)

        val textHeight = textSize

        val textWidth = (paint.measureText(text.toString())).toInt()

        paint.getTextBounds(text.toString(), 0, length, rect)

        val verticalSpaceAroundText = (textHeight * .12f + 6 + paddingVertical).toInt()

        val horizontalSpaceAroundText = ((textHeight * .24f) + 8 + paddingHorizontal).toInt()

        val widthMode = MeasureSpec.getMode(widthMeasureSpec)
        val widthSize = MeasureSpec.getSize(widthMeasureSpec)
        val heightMode = MeasureSpec.getMode(heightMeasureSpec)
        val heightSize = MeasureSpec.getSize(heightMeasureSpec)


        val desiredHeight = (textSize + 2 * verticalSpaceAroundText).toInt()
        val desiredWidth =
            if (isCircleShape) desiredHeight else (textWidth + 2 * horizontalSpaceAroundText)

        val width: Int = when (widthMode) {
            MeasureSpec.EXACTLY -> {
                widthSize
            }

            MeasureSpec.AT_MOST -> {
                desiredWidth
            }

            else -> {
                desiredWidth
            }
        }

        val height: Int = when (heightMode) {
            MeasureSpec.EXACTLY -> {
                heightSize
            }

            MeasureSpec.AT_MOST -> {
                desiredHeight
            }

            else -> {
                desiredHeight
            }
        }

        setMeasuredDimension(width, height)
    }


    override fun onDraw(canvas: Canvas) {
        if (isCircleShape) {
            val radius = (height - paddingVertical * 2) / 2f
            canvas.drawCircle(width / 2f, height / 2f, radius, paintBackground)

            if (drawBorder) {
                canvas.drawCircle(width / 2f, height / 2f, radius, paintBorder)
            }

        } else {

            badgeRoundedRectRadius =
                min(width - paddingVertical * 2, height - paddingHorizontal * 2) * radiusRatio

            canvas.drawRoundRect(
                (paddingHorizontal).toFloat(),
                (paddingVertical).toFloat(),
                (width - paddingHorizontal).toFloat(),
                (height - paddingVertical).toFloat(),
                badgeRoundedRectRadius,
                badgeRoundedRectRadius,
                paintBackground
            )

            if (drawBorder) {
                canvas.drawRoundRect(
                    (paddingHorizontal).toFloat(),
                    (paddingVertical).toFloat(),
                    (width - paddingHorizontal).toFloat(),
                    (height - paddingVertical).toFloat(),
                    badgeRoundedRectRadius,
                    badgeRoundedRectRadius,
                    paintBorder
                )
            }
        }

        super.onDraw(canvas)
    }

    fun setBadgeCount(count: String, showWhenZero: Boolean = false) {
        val badgeCount = count.toIntOrNull()

        badgeCount?.let {
            setBadgeCount(it, showWhenZero)
        }
    }

    fun setBadgeCount(count: Int, showWhenZero: Boolean = false) {
        this.badgeCount = count

        isCircleShape = (text.length <= circleShapeThreshold)

        when {
            count in 1..maxNumber -> {
                text = count.toString()
                visibility = VISIBLE
            }

            count > maxNumber -> {
                text = "$maxNumber+"
                visibility = VISIBLE
            }

            count <= 0 -> {
                text = "0"
                visibility = if (showWhenZero) {
                    VISIBLE
                } else {
                    GONE
                }
            }
        }
    }

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {

        val typedArray = context.obtainStyledAttributes(attrs, R.styleable.BadgeTextView)

        circleShapeThreshold =
            typedArray.getInteger(
                R.styleable.BadgeTextView_badge_circle_threshold,
                circleShapeThreshold
            )

        maxNumber =
            typedArray.getInteger(R.styleable.BadgeTextView_badge_max_number, maxNumber)

        radiusRatio =
            typedArray.getFloat(R.styleable.BadgeTextView_badge_round_corner_ratio, radiusRatio)


        badgeBackgroundColor =
            typedArray.getColor(R.styleable.BadgeTextView_android_background, badgeBackgroundColor)

        paintBackground.color = badgeBackgroundColor

        paddingHorizontal =
            typedArray.getDimensionPixelOffset(
                R.styleable.BadgeTextView_badge_padding_horizontal,
                4.dp
            )
        paddingVertical =
            typedArray.getDimensionPixelOffset(
                R.styleable.BadgeTextView_badge_padding_vertical,
                4.dp
            )

        borderWidth =
            typedArray.getDimensionPixelOffset(
                R.styleable.BadgeTextView_badge_border_width,
                borderWidth.dp
            )
                .toFloat()
        borderColor =
            typedArray.getInteger(R.styleable.BadgeTextView_badge_border_color, borderColor)

        paintBorder.strokeWidth = borderWidth
        paintBorder.color = borderColor

        badgeShadowRadius = typedArray.getDimensionPixelOffset(
            R.styleable.BadgeTextView_badge_shadow_radius,badgeShadowRadius.dp
        ).toFloat()
        badgeShadowXOffset =
            typedArray.getDimensionPixelOffset(
                R.styleable.BadgeTextView_badge_shadow_offset_x,badgeShadowXOffset.dp
            ).toFloat()
        badgeShadowYOffset =
            typedArray.getDimensionPixelOffset(
                R.styleable.BadgeTextView_badge_shadow_offset_y, badgeShadowYOffset.dp
            ).toFloat()
        badgeShadowColor =
            typedArray.getColor(R.styleable.BadgeTextView_badge_shadow_color, badgeShadowColor)

        typedArray.recycle()

        init()
    }

    constructor(context: Context) : super(context) {
        paddingVertical = 4.dp
        paddingHorizontal = 4.dp
        init()
    }

}