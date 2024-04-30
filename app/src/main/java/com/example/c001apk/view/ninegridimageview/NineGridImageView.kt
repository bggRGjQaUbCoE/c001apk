package com.example.c001apk.view.ninegridimageview

/*
MIT License

Copyright (c) 2021 Plain

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.
*/


import android.content.Context
import android.util.AttributeSet
import android.view.ViewGroup
import android.widget.ImageView
import androidx.core.content.ContextCompat
import com.example.c001apk.R
import com.example.c001apk.util.ImageUtil
import com.example.c001apk.util.dp
import com.example.c001apk.view.BadgedImageView
import com.google.android.material.shape.RoundedCornerTreatment
import com.google.android.material.shape.ShapeAppearanceModel


class NineGridImageView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : ViewGroup(context, attrs, defStyleAttr) {

    var isCompress = false

    private var urlList: List<String>? = null

    var imgHeight = 1
    var imgWidth = 1

    private var singleWidth = 0
    private var singleHeight = 0

    private var totalWidth = 0

    private var columns: Int = 0
    private var rows: Int = 0

    private var gap: Int = 3.dp

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        val sizeWidth = MeasureSpec.getSize(widthMeasureSpec)
        totalWidth = sizeWidth - paddingLeft - paddingRight
        var defaultWidth = (totalWidth - gap * (3 - 1)) / 3
        if (!urlList.isNullOrEmpty()) {
            val childrenCount = urlList?.size ?: 0
            if (childrenCount == 1) {
                if (isCompress) {
                    singleWidth = totalWidth
                    singleHeight =
                        if (imgHeight >= imgWidth * 22f / 9f)
                            totalWidth * 22 / 9
                        else totalWidth * imgHeight / imgWidth
                } else if (imgHeight < imgWidth) {
                    singleHeight = defaultWidth * 2
                    singleWidth = singleHeight * imgWidth / imgHeight
                    if (singleWidth > totalWidth) {
                        singleWidth = totalWidth
                        singleHeight = singleWidth * imgHeight / imgWidth
                    }
                } else if (imgHeight > imgWidth) {
                    if (imgHeight < imgWidth * 1.5) {
                        singleWidth = defaultWidth * 2
                        singleHeight = singleWidth * imgHeight / imgWidth
                    } else if (imgHeight <= imgWidth * 22f / 9f) {
                        singleWidth = defaultWidth
                        singleHeight = singleWidth * imgHeight / imgWidth
                    } else {
                        singleWidth = defaultWidth
                        singleHeight = singleWidth * 22 / 9
                    }
                } else {
                    singleWidth = defaultWidth * 2
                    singleHeight = defaultWidth * 2
                }
            } else if (childrenCount == 2) {
                defaultWidth = (totalWidth - gap * (2 - 1)) / 2
                singleWidth = defaultWidth
                singleHeight = defaultWidth
            } else {
                singleWidth = defaultWidth
                singleHeight = defaultWidth
            }
            measureChildren(
                MeasureSpec.makeMeasureSpec(singleWidth, MeasureSpec.EXACTLY),
                MeasureSpec.makeMeasureSpec(singleHeight, MeasureSpec.EXACTLY)
            )
            val measureHeight: Int = singleHeight * rows + gap * (rows - 1)
            setMeasuredDimension(sizeWidth, measureHeight)
        }
    }

    override fun onLayout(changed: Boolean, l: Int, t: Int, r: Int, b: Int) {
        layoutChildrenView()
    }

    private fun findPosition(childNum: Int): IntArray {
        val position = IntArray(2)
        for (i in 0 until rows) {
            for (j in 0 until columns) {
                if (i * columns + j == childNum) {
                    position[0] = i //行
                    position[1] = j //列
                    break
                }
            }
        }
        return position
    }

    private fun layoutChildrenView() {
        if (urlList.isNullOrEmpty()) {
            return
        }
        val childrenCount = urlList?.size ?: 0
        for (i in 0 until childrenCount) {
            val position = findPosition(i)
            val left = (singleWidth + gap) * position[1] + paddingLeft
            val top = (singleHeight + gap) * position[0] + paddingTop
            val right = left + singleWidth
            val bottom = top + singleHeight
            val childrenView = getChildAt(i) as ImageView
            if (childrenCount == 1) {
                //只有一张图片
                childrenView.scaleType = ImageView.ScaleType.FIT_CENTER
            } else {
                childrenView.scaleType = ImageView.ScaleType.CENTER_CROP
            }
            urlList?.let { urlList ->
                childrenView.setOnClickListener {
                    ImageUtil.startBigImgView(this, childrenView, urlList, i)
                }
            }
            childrenView.layout(left, top, right, bottom)
        }
    }


    private fun generateChildrenLayout(length: Int) {
        if (length <= 3) {
            rows = 1
            columns = length
        } else if (length <= 6) {
            rows = 2
            columns = 3
            if (length == 4) {
                columns = 2
            }
        } else {
            rows = 3
            columns = 3
        }
    }

    fun getImageViews(): List<ImageView> {
        val imageViews = mutableListOf<ImageView>()
        for (i in 0 until childCount) {
            val imageView = getChildAt(i)
            if (imageView is ImageView) {
                imageViews.add(imageView)
            }
        }
        return imageViews
    }

    fun getImageViewAt(position: Int) = getChildAt(position) as? ImageView

    fun setUrlList(urlList: List<String>?) {
        if (urlList != null) {
            this.urlList = urlList
            generateChildrenLayout(urlList.size)
            removeAllViews()

            urlList.forEach {
                val imageView = BadgedImageView(context)
                val shapePathModel = ShapeAppearanceModel.builder()
                    .setAllCorners(RoundedCornerTreatment())
                    .setAllCornerSizes(12.dp.toFloat())
                    .build()
                imageView.apply {
                    shapeAppearanceModel = shapePathModel
                    strokeWidth = 1.dp.toFloat()
                    strokeColor = ContextCompat.getColorStateList(context, R.color.image_stroke)
                    setPadding(1.dp, 1.dp, 1.dp, 1.dp)
                    setBackgroundColor(context.getColor(R.color.cover))
                    foreground = context.getDrawable(R.drawable.selector_bg_12_trans)
                    scaleType = ImageView.ScaleType.CENTER_CROP
                    val replace = it.replace(".s.jpg", "")
                    val imageLp = ImageUtil.getImageLp(replace)
                    imgWidth = imageLp.first
                    imgHeight = imageLp.second
                    if (replace.endsWith("gif") || imgHeight > imgWidth * 22f / 9f) {
                        setBadge(
                            if (replace.endsWith("gif")) "GIF"
                            else "长图"
                        )
                    }
                }
                addView(imageView, generateDefaultLayoutParams())
                ImageUtil.showIMG(imageView, it)
            }
        }
    }

}