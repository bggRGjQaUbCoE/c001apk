package com.example.c001apk.view

import android.content.Context
import android.text.TextPaint
import android.text.style.ClickableSpan
import android.view.View
import com.example.c001apk.util.ImageUtil
import com.example.c001apk.util.NetWorkUtil.openLink
import com.example.c001apk.util.http2https
import com.google.android.material.color.MaterialColors

class MyURLSpan(
    private val mContext: Context,
    private val mUrl: String,
    private val imgList: List<String>?,
    private val showMoreReply: (() -> Unit)? = null
) :
    ClickableSpan() {

    private var position = 0
    private var uid = ""
    fun setData(position: Int, uid: String) {
        this.position = position
        this.uid = uid
    }

    var isReturn = false
    var isColor = false

    override fun onClick(widget: View) {
        if (mUrl == "") {
            return
        } else if (mUrl.contains("/feed/replyList")) {
            showMoreReply?.let { it() }
        } else if (mUrl.contains("image.coolapk.com")) {
            if (imgList == null) {
                ImageUtil.startBigImgViewSimple(mContext, mUrl.http2https)
            } else {
                ImageUtil.startBigImgViewSimple(mContext, imgList)
            }
        } else {
            openLink(mContext, mUrl, null)
        }
    }

    override fun updateDrawState(ds: TextPaint) {
        super.updateDrawState(ds)
        if (isColor)
            ds.color = MaterialColors.getColor(
                mContext,
                com.google.android.material.R.attr.colorControlNormal,
                0
            ) //设置文本颜色
        ds.isUnderlineText = false //取消下划线
    }
}