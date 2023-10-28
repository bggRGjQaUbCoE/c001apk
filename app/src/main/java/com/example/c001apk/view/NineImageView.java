package com.example.c001apk.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Shader;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.target.Target;
import com.bumptech.glide.request.transition.Transition;
import com.example.c001apk.R;
import com.example.c001apk.util.DisplayUtil;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;


/**
 * Created by hcc on 17/06/28.
 * <p>
 * A nine pictures controls
 */
public class NineImageView extends View {

    private String[] mImageUrls;
    private Bitmap[] mBitmaps;
    private Bitmap mPlaceHolder;
    private int mHorizontalSpace = 10;
    private int mVerticalSpace = 10;
    private int mRadius = 0;
    private int mImageWidth = 0;
    private int mImageHeight = 0;
    private float mRatio = 1f;
    private int mColumns;
    private int mRows;
    private Matrix matrix = new Matrix();
    private final Paint mPaint = new Paint();
    private final Paint mTextPaint = new Paint();
    private final Paint mTextBgPaint = new Paint();
    private OnClickItemListener onClickItemListener;
    private MotionEvent mEventDown;
    private int mDown;
    private RectF[] mDrawRects;
    private boolean haveRule;
    private Target<Bitmap>[] mTargets = new Target[0];


    public NineImageView(Context context) {
        this(context, null);
    }

    public NineImageView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public NineImageView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        if (attrs != null) {
            TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.NineImageView);
            try {
                for (int i = 0; i < a.getIndexCount(); i++) {
                    int attr = a.getIndex(i);
                    if (attr == R.styleable.NineImageView_nineImageHorizontalSpace) {
                        mHorizontalSpace = a.getDimensionPixelSize(attr, mHorizontalSpace);

                    } else if (attr == R.styleable.NineImageView_nineImageVerticalSpace) {
                        mVerticalSpace = a.getDimensionPixelSize(attr, mVerticalSpace);

                    } else if (attr == R.styleable.NineImageView_nineImageRadius) {
                        mRadius = a.getDimensionPixelSize(attr, mRadius);

                    } else if (attr == R.styleable.NineImageView_nineImageRatio) {
                        mRatio = a.getFloat(attr, mRatio);

                    }
                }
            } finally {
                a.recycle();
            }
        }

        mPaint.setAntiAlias(true);
        mTextPaint.setAntiAlias(true);
        mPlaceHolder = BitmapFactory.decodeResource(context.getResources(), R.drawable.image_placeholder);

        setImageUrls(null);
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        for (int i = 0; i < mImageUrls.length; i++) {
            loadBitmap(i, mImageUrls[i]);
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        if (mImageUrls.length != 0) {
            int width = getDefaultSize(getSuggestedMinimumWidth(), widthMeasureSpec);
            mImageWidth = (width - (mColumns - 1) * mHorizontalSpace - getPaddingLeft() - getPaddingRight()) / mColumns;
            mImageHeight = (int) (mImageWidth * mRatio);
            mRows = (int) Math.ceil(mImageUrls.length * 1f / mColumns);
            int height = mImageHeight * mRows + (mRows - 1) * mVerticalSpace + getPaddingTop() + getPaddingBottom();
            setMeasuredDimension(width, height);
        } else {
            super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        }
    }


    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (mBitmaps.length != 0) {
            for (int row = 0; row < mRows; row++) {
                for (int column = 0; column < mColumns; column++) {
                    int i = row * mColumns + column;
                    boolean isGif;
                    if (i >= mImageUrls.length) {
                        break;
                    } else {
                        isGif = mImageUrls[i].toLowerCase().contains(".gif");
                    }
                    Bitmap bitmap = mBitmaps[i];
                    if (bitmap == null) {
                        bitmap = mPlaceHolder;
                    }


                    float left = getPaddingLeft() + column * mHorizontalSpace + column * mImageWidth;
                    float top = getPaddingTop() + row * mVerticalSpace + row * mImageHeight;
                    float scale;
                    float dx = 0, dy = 0;

                    int dwidth = bitmap.getWidth();
                    int dheight = bitmap.getHeight();
                    int vwidth = mImageWidth;
                    int vheight = mImageHeight;
                    if (dwidth * vheight > vwidth * dheight) {
                        scale = (float) vheight / (float) dheight;
                        dx = (vwidth - dwidth * scale) * 0.5f;
                    } else {
                        scale = (float) vwidth / (float) dwidth;
                        dy = (vheight - dheight * scale) * 0.5f;
                    }

                    matrix.setScale(scale, scale);
                    matrix.postTranslate(left + Math.round(dx), top + Math.round(dy));

                    BitmapShader mBitmapShader = new BitmapShader(bitmap, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP);
                    mBitmapShader.setLocalMatrix(matrix);
                    mPaint.setShader(mBitmapShader);
                    RectF rectF = new RectF();
                    rectF.set(left, top, left + mImageWidth, top + mImageHeight);
                    mDrawRects[i] = rectF;
                    canvas.drawRoundRect(rectF, mRadius, mRadius, mPaint);
                    if (isGif) {
                        String gifString = "GIF";
                        mTextPaint.setTextSize(DisplayUtil.dpToPx(12));
                        mTextPaint.setColor(Color.WHITE);
                        Rect bounds = new Rect();
                        mTextPaint.getTextBounds(gifString, 0, gifString.length(), bounds);
                        mTextBgPaint.setColor(Color.BLACK);
                        mTextBgPaint.setAlpha(60);
                        RectF rectB = new RectF();
                        rectB.set(left + DisplayUtil.dpToPx(6), top + DisplayUtil.dpToPx(6),
                                left + DisplayUtil.dpToPx(6) * 2 + bounds.width(), top + DisplayUtil.dpToPx(6) * 2 + bounds.height());
                        canvas.drawRoundRect(rectB, mRadius, mRadius, mTextBgPaint);
                        canvas.drawText(gifString, left + DisplayUtil.dpToPx(9), top + DisplayUtil.dpToPx(9) + bounds.height(), mTextPaint);
                    }
                }
            }
        }
    }


    @Override
    public boolean onTouchEvent(MotionEvent event) {
        boolean isClickItem = false;
        int action = event.getActionMasked();
        switch (action) {
            case MotionEvent.ACTION_DOWN:
                mEventDown = MotionEvent.obtain(event);
                mDown = getClickItem(mEventDown);
                isClickItem = mDown > -1;
                break;
            case MotionEvent.ACTION_UP:
                if (mEventDown != null) {
                    float distance = (float) Math.sqrt(Math.pow((event.getX() - mEventDown.getX()), 2) + Math.pow((event.getY() - mEventDown.getY()), 2));
                    if (distance < ViewConfiguration.getTouchSlop()) {
                        int iUp = getClickItem(event);
                        if (mDown == iUp && iUp > -1) {
                            isClickItem = true;
                            if (onClickItemListener != null) {
                                onClickItemListener.onClick(iUp, new ArrayList<>(Arrays.asList(mImageUrls)));
                            }
                        }
                    }
                }
                break;
        }
        return isClickItem || super.onTouchEvent(event);
    }

    private int getClickItem(MotionEvent event) {
        for (int i = 0; i < mDrawRects.length; i++) {
            if (mDrawRects[i] != null && mDrawRects[i].contains(event.getX(), event.getY())) {
                return i;
            }
        }
        return -1;
    }


    public interface OnClickItemListener {
        void onClick(int i, ArrayList<String> url);
    }

    private void loadBitmap(final int i, final String url) {
        Glide.with(getContext())
                .asBitmap()
                .load(url)
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .override(mImageWidth, mImageHeight)
                .into(mTargets[i]);
    }


    public void setImageUrls(Collection<String> imageUrls) {
        for (Target target : mTargets) {
            Glide.with(getContext()).clear(target);
        }
        if (imageUrls == null) imageUrls = Collections.emptyList();
        int newSize = imageUrls.size();
        mImageUrls = imageUrls.toArray(new String[newSize]);
        mBitmaps = new Bitmap[newSize];
        mDrawRects = new RectF[newSize];
        mTargets = new Target[newSize];
        if (!haveRule) {
            if (imageUrls.size() < 3)
                mColumns = 2;
            else
                mColumns = 3;
        }
        for (int i = 0; i < imageUrls.size(); i++) {
            mTargets[i] = new PositionTarget(i);
        }
        requestLayout();
    }


    public void setColumns(int columns) {
        mColumns = columns;
        haveRule = true;
    }


    public void setOnClickItemListener(OnClickItemListener onClickItemListener) {
        this.onClickItemListener = onClickItemListener;
    }

    private void setImageRect(int pos) {
        int column = pos / mColumns;
        int row = pos % mColumns;

        int left = getPaddingLeft() + column * mHorizontalSpace + column * mImageWidth;
        int top = getPaddingTop() + row * mVerticalSpace + mImageHeight;
        invalidate(left, top, left + mImageWidth, top + mImageHeight);
    }

    private class PositionTarget extends CustomTarget<Bitmap> {
        private final int i;

        PositionTarget(int i) {
            this.i = i;
        }

        @Override
        public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {
            mBitmaps[i] = resource;
            setImageRect(i);
        }

        @Override
        public void onLoadCleared(@Nullable Drawable placeholder) {

        }
    }
}
