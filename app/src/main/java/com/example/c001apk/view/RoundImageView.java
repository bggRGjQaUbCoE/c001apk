package com.example.c001apk.view;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PathMeasure;
import android.graphics.Shader;
import android.graphics.Typeface;
import android.os.Looper;
import android.text.TextPaint;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.widget.LinearLayout;

import com.example.c001apk.R;
import com.google.android.material.imageview.ShapeableImageView;

/**
 * Update Date : 2020/8/12.
 * Created by ChenBo on 2017/11/10.
 * Email: chenbohc@163.com
 * QQ: 378277548
 * Description: Custom ImageView.
 * Realize round and rounded rectangle function, Add border line display.
 * Mainly through color rendering, The picture was not cropped
 */
@SuppressWarnings("unused")
public class RoundImageView extends ShapeableImageView {

    private static final int LEFT_TOP = 0;
    private static final int LEFT_BOTTOM = 1;
    private static final int RIGHT_TOP = 2;
    private static final int RIGHT_BOTTOM = 3;

    private static final int SANS = 1;
    private static final int SERIF = 2;
    private static final int MONOSPACE = 3;
    /**
     * My paint.
     */
    private Paint mPaint = null;

    /**
     * The text color state list.
     */
    private ColorStateList mTextColorStateList;
    /**
     * The label background color state list.
     */
    private ColorStateList mLabelBackColorStateList;

    private boolean displayLabel = false;

    /**
     * Label text
     */
    private String labelText;
    /**
     * Label text color
     */
    private int textColor = Color.WHITE;
    /**
     * Label text size
     */
    private int textSize = 15;
    /**
     * Label background
     */
    private int labelBackground = Color.parseColor("#00000000");
    /**
     * Label gravity
     */
    private int labelGravity = 2;
    /**
     * Label width
     */
    private int labelWidth = 15;
    /**
     * Distance start location margin
     */
    private int startMargin = 20;

    /**
     * The text paint
     */
    private TextPaint mTextPaint = null;
    /**
     * Text displayed on the label
     */
    private String text;
    /**
     * The gradient color start state color list.
     */
    private ColorStateList mStartColor;
    /**
     * The gradient color center state color list.
     */
    private ColorStateList mCenterColor;
    /**
     * The gradient color end state color list.
     */
    private ColorStateList mEndColor;
    /**
     * The gradient color current start color.
     */
    private int mCurrentStartColor;
    /**
     * The gradient color current center color.
     */
    private int mCurrentCenterColor;
    /**
     * The gradient color current end color.
     */
    private int mCurrentEndColor;
    /**
     * The sRGB colors to be distributed along the gradient line
     */
    private int[] mColors;
    /**
     * May be null. The relative positions [0..1] of
     * each corresponding color in the colors array. If this is null,
     * the the colors are distributed evenly along the gradient line.
     */
    private float[] mPoints;
    /**
     * Use gradient content.
     */
    private int mGradientContent;
    /**
     * Type of gradient. The default type is linear.
     */
    private int mGradientType;
    /**
     * The gradient shader.
     */
    private Shader mGradientShader;
    /**
     * Standard orientation constant.
     * When using a linear gradient, the direction of the gradient.
     */
    private int mOrientation;

    /**
     * The enum Display type.
     */

    /**
     * Instantiates a new Round image view.
     *
     * @param context the Context
     */
    public RoundImageView(Context context) {
        this(context, null);
    }

    /**
     * Instantiates a new Round image view.
     *
     * @param context the Context
     * @param attrs   the AttributeSet
     */
    public RoundImageView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    /**
     * Instantiates a new Round image view.
     *
     * @param context      the Context
     * @param attrs        the AttributeSet
     * @param defStyleAttr the default style Attribute
     */
    public RoundImageView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }

    /**
     * Init.
     *
     * @param ctx   the Context
     * @param attrs the AttributeSet
     */
    private void init(Context ctx, AttributeSet attrs) {
        setLayerType(LAYER_TYPE_HARDWARE, null);
        mPaint = new Paint();
        mTextPaint = new TextPaint();

        if (attrs != null) {
            TypedArray a = ctx.obtainStyledAttributes(attrs, R.styleable.RoundImageView);
            float radius = a.getDimension(R.styleable.RoundImageView_android_radius, 0);
            int index = a.getInt(R.styleable.RoundImageView_displayType, -1);

            displayLabel = a.getBoolean(R.styleable.RoundImageView_displayLabel, displayLabel);
            labelText = a.getString(R.styleable.RoundImageView_android_text);
            mLabelBackColorStateList = a.getColorStateList(R.styleable.RoundImageView_labelBackground);
            textSize = a.getDimensionPixelSize(R.styleable.RoundImageView_android_textSize, textSize);
            mTextColorStateList = a.getColorStateList(R.styleable.RoundImageView_android_textColor);
            labelWidth = a.getDimensionPixelSize(R.styleable.RoundImageView_labelWidth, labelWidth);
            labelGravity = a.getInt(R.styleable.RoundImageView_labelGravity, labelGravity);
            startMargin = a.getDimensionPixelSize(R.styleable.RoundImageView_startMargin, startMargin);

            mStartColor = a.getColorStateList(R.styleable.RoundImageView_android_startColor);
            mCenterColor = a.getColorStateList(R.styleable.RoundImageView_android_centerColor);
            mEndColor = a.getColorStateList(R.styleable.RoundImageView_android_endColor);
            mOrientation = a.getInt(R.styleable.RoundImageView_android_orientation, LinearLayout.HORIZONTAL);

            final int typefaceIndex = a.getInt(R.styleable.RoundImageView_android_typeface, -1);
            final int styleIndex = a.getInt(R.styleable.RoundImageView_android_textStyle, -1);
            setTypefaceFromAttrs(typefaceIndex, styleIndex);

            text = labelText;
            a.recycle();
        }
    }


    @SuppressLint("DrawAllocation")
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        drawLabels(canvas);
        /*if (getDrawable() != null) {
            resetSize(Math.min(getWidth(), getHeight()) / 2f);
            Bitmap bm = Bitmap.createBitmap(getWidth(), getHeight(), Bitmap.Config.ARGB_8888);
            Canvas mCanvas = new Canvas(bm);
            super.onDraw(mCanvas);
            mPaint.reset();
            mPaint.setAntiAlias(true);
            mPaint.setDither(true);
            drawMyContent(mCanvas);
            canvas.drawBitmap(bm, 0, 0, mPaint);
            bm.recycle();
        } else {
            super.onDraw(canvas);
        }*/
    }

    /**
     * Reset Size.
     *
     * @param size the size
     */
    private void resetSize(float size) {
        labelWidth = (int) Math.min(labelWidth, size / 2);
        textSize = Math.min(textSize, labelWidth);
        startMargin = Math.min(startMargin, (int) (size * 2 - getBevelLineLength()));
    }

    /**
     * @return get Bevel line length
     */
    private float getBevelLineLength() {
        return (float) Math.sqrt(Math.pow(labelWidth, 2) * 2);
    }

    private void drawMyContent(Canvas mCanvas) {
        if (displayLabel) drawLabels(mCanvas);
    }

    /**
     * Draw Labels
     *
     * @param mCanvas My canvas
     */
    private void drawLabels(Canvas mCanvas) {
        Path path = new Path();
        Path mTextPath = new Path();
        final float bevelLineLength = getBevelLineLength();

        switch (labelGravity) {
            case LEFT_TOP -> {
                path.moveTo(startMargin, 0);
                path.rLineTo(bevelLineLength, 0);
                path.lineTo(0, startMargin + bevelLineLength);
                path.rLineTo(0, -bevelLineLength);
                path.close();
                mTextPath.moveTo(0, startMargin + bevelLineLength / 2f);
                mTextPath.lineTo(startMargin + bevelLineLength / 2f, 0);
            }
            case LEFT_BOTTOM -> {
                path.moveTo(startMargin, getHeight());
                path.rLineTo(bevelLineLength, 0);
                path.lineTo(0, getHeight() - (startMargin + bevelLineLength));
                path.rLineTo(0, bevelLineLength);
                path.close();
                mTextPath.moveTo(0, getHeight() - (startMargin + bevelLineLength / 2f));
                mTextPath.lineTo(startMargin + bevelLineLength / 2f, getHeight());
            }
            case RIGHT_TOP -> {
                path.moveTo(getWidth() - startMargin, 0);
                path.rLineTo(-bevelLineLength, 0);
                path.lineTo(getWidth(), startMargin + bevelLineLength);
                path.rLineTo(0, -bevelLineLength);
                path.close();
                mTextPath.moveTo(getWidth() - (startMargin + bevelLineLength / 2f), 0);
                mTextPath.lineTo(getWidth(), startMargin + bevelLineLength / 2f);
            }
            case RIGHT_BOTTOM -> {
                path.moveTo(getWidth() - startMargin, getHeight());
                path.rLineTo(-bevelLineLength, 0);
                path.lineTo(getWidth(), getHeight() - (startMargin + bevelLineLength));
                path.rLineTo(0, bevelLineLength);
                path.close();
                mTextPath.moveTo(getWidth() - (startMargin + bevelLineLength / 2f), getHeight());
                mTextPath.lineTo(getWidth(), getHeight() - (startMargin + bevelLineLength / 2f));
            }
        }
        mTextPaint.setAntiAlias(true);
        mTextPaint.setStyle(Paint.Style.FILL);
        mTextPaint.setShader(null);
        mTextPaint.setColor(labelBackground);
        mCanvas.drawPath(path, mTextPaint);

        mTextPaint.setTextSize(textSize);
        mTextPaint.setShader(null);
        mTextPaint.setColor(textColor);
        if (null == text) text = "";
        mTextPaint.setTextAlign(Paint.Align.CENTER);

        float textWidth = mTextPaint.measureText(text);
        PathMeasure pathMeasure = new PathMeasure(mTextPath, false);
        float pathLength = pathMeasure.getLength();
        if (textWidth > pathLength) {//Text length is greater than the length of the drawing area, the text content is cropped. Replace with ellipsis
            float sl = textWidth / text.length();
            float le = textWidth - pathLength;
            int num = (int) Math.floor(le / sl);
            text = text.substring(0, text.length() - (num + 2)) + "...";
        }
        Paint.FontMetricsInt fm = mTextPaint.getFontMetricsInt();
        float v = (fm.bottom - fm.top) / 2f - fm.bottom;
        mCanvas.drawTextOnPath(text, mTextPath, 0, v, mTextPaint);
    }


    /**
     * Sets display label.
     *
     * @param displayLabel the display label
     */
    public void setDisplayLabel(boolean displayLabel) {
        if (this.displayLabel != displayLabel) {
            this.displayLabel = displayLabel;
            if (displayLabel)
                postInvalidate();
        }
    }

    /**
     * Sets label text.
     *
     * @param labelText The label text
     */
    public void setLabelText(String labelText) {
        if (!TextUtils.equals(this.labelText, labelText)) {
            this.labelText = labelText;
            text = labelText;
            if (displayLabel)
                postInvalidate();
        }
    }

    /**
     * Sets text color.
     *
     * @param textColor the text color
     */
    public void setTextColor(int textColor) {
        if (this.textColor != textColor) {
            this.textColor = textColor;
            if (displayLabel)
                postInvalidate();
        }
    }

    /**
     * Sets text color.
     *
     * @param mTextColorStateList the text color state list
     */
    public void setTextColor(ColorStateList mTextColorStateList) {
        this.mTextColorStateList = mTextColorStateList;
        if (displayLabel) {
            invalidate();
        }
    }

    /**
     * Sets text size.
     *
     * @param textSize the text size
     */
    public void setTextSize(int textSize) {
        if (this.textSize != textSize) {
            this.textSize = textSize;
            if (displayLabel)
                postInvalidate();
        }
    }

    /**
     * Sets label background.
     *
     * @param labelBackground the label background
     */
    public void setLabelBackground(int labelBackground) {
        if (this.labelBackground != labelBackground) {
            this.labelBackground = labelBackground;
            if (displayLabel)
                postInvalidate();
        }
    }

    /**
     * Sets label background.
     *
     * @param mLabelBackColorStateList the label background color state list
     */
    public void setLabelBackground(ColorStateList mLabelBackColorStateList) {
        this.mLabelBackColorStateList = mLabelBackColorStateList;
        if (displayLabel) {
            invalidate();
        }
    }

    /**
     * Sets label gravity.
     *
     * @param labelGravity the label gravity
     */
    public void setLabelGravity(int labelGravity) {
        if (this.labelGravity != labelGravity) {
            this.labelGravity = labelGravity;
            if (displayLabel)
                postInvalidate();
        }
    }

    /**
     * Sets label width.
     *
     * @param labelWidth the label width
     */
    public void setLabelWidth(int labelWidth) {
        if (this.labelWidth != labelWidth) {
            this.labelWidth = labelWidth;
            if (displayLabel)
                postInvalidate();
        }
    }

    /**
     * Sets start margin.
     *
     * @param startMargin the start margin
     */
    public void setStartMargin(int startMargin) {
        if (this.startMargin != startMargin) {
            this.startMargin = startMargin;
            if (displayLabel)
                postInvalidate();
        }
    }

    /**
     * Is display label boolean.
     *
     * @return the boolean
     */
    public boolean isDisplayLabel() {
        return displayLabel;
    }

    /**
     * Gets label text.
     *
     * @return the label text
     */
    public String getLabelText() {
        return labelText;
    }

    /**
     * Gets text color.
     *
     * @return the text color
     */
    public int getTextColor() {
        return textColor;
    }

    /**
     * Gets text size.
     *
     * @return the text size
     */
    public int getTextSize() {
        return textSize;
    }

    /**
     * Gets label background.
     *
     * @return the label background
     */
    public int getLabelBackground() {
        return labelBackground;
    }

    /**
     * Gets label gravity.
     *
     * @return the label gravity
     */
    public int getLabelGravity() {
        return labelGravity;
    }

    /**
     * Gets label width.
     *
     * @return the label width
     */
    public int getLabelWidth() {
        return labelWidth;
    }

    /**
     * Gets start margin.
     *
     * @return the start margin
     */
    public int getStartMargin() {
        return startMargin;
    }

    private void setTypefaceFromAttrs(int typefaceIndex, int styleIndex) {
        Typeface tf = switch (typefaceIndex) {
            case SANS -> Typeface.SANS_SERIF;
            case SERIF -> Typeface.SERIF;
            case MONOSPACE -> Typeface.MONOSPACE;
            default -> null;
        };

        setTypeface(tf, styleIndex);
    }

    /**
     * Sets the typeface and style in which the text should be displayed,
     * and turns on the fake bold and italic bits in the Paint if the
     * Typeface that you provided does not have all the bits in the
     * style that you specified.
     *
     * @param tf    the Typeface
     * @param style the style
     */
    public void setTypeface(Typeface tf, int style) {
        if (style > 0) {
            if (tf == null) {
                tf = Typeface.defaultFromStyle(style);
            } else {
                tf = Typeface.create(tf, style);
            }

            setTypeface(tf);
            // now compute what (if any) algorithmic styling is needed
            int typefaceStyle = tf != null ? tf.getStyle() : 0;
            int need = style & ~typefaceStyle;
            mTextPaint.setFakeBoldText((need & Typeface.BOLD) != 0);
            mTextPaint.setTextSkewX((need & Typeface.ITALIC) != 0 ? -0.25f : 0);
        } else {
            mTextPaint.setFakeBoldText(false);
            mTextPaint.setTextSkewX(0);
            setTypeface(tf);
        }
    }

    /**
     * Sets the typeface and style in which the text should be displayed.
     * Note that not all Typeface families actually have bold and italic
     * variants, so you may need to use
     * {@link #setTypeface(Typeface, int)} to get the appearance
     * that you actually want.
     *
     * @param tf the tf
     * @see #getTypeface()
     */
    public void setTypeface(Typeface tf) {
        if (mTextPaint.getTypeface() != tf) {
            mTextPaint.setTypeface(tf);
            if (displayLabel)
                postInvalidate();
        }
    }


    /**
     * Gets the current {@link Typeface} that is used to style the text.
     *
     * @return The current Typeface.
     * @see #setTypeface(Typeface)
     */
    public Typeface getTypeface() {
        return mTextPaint.getTypeface();
    }

    /**
     * Sets gradient type.
     *
     * @param gradientType the gradient type
     */
    public void setGradientType(int gradientType) {
        if (mGradientType != gradientType) {
            mGradientType = gradientType;
            invalidate();
        }
    }

    /**
     * Sets orientation.
     *
     * @param mOrientation the linear shader orientation
     */
    public void setOrientation(int mOrientation) {
        if (this.mOrientation != mOrientation) {
            this.mOrientation = mOrientation;
            invalidate();
        }
    }


    @Override
    public void invalidate() {
        postInvalidate();
    }

    @Override
    public void postInvalidate() {
        if (Looper.myLooper() != Looper.getMainLooper()) {
            super.postInvalidate();
        } else {
            super.invalidate();
        }
    }
}
