package com.example.c001apk.view

import android.content.Context
import android.content.Intent
import android.text.TextPaint
import android.text.style.ClickableSpan
import android.view.View
import cc.shinichi.library.ImagePreview
import cc.shinichi.library.bean.ImageInfo
import com.example.c001apk.ui.activity.topic.TopicActivity
import com.example.c001apk.ui.activity.user.UserActivity
import com.example.c001apk.ui.activity.webview.WebViewActivity

internal class MyURLSpan(
    private val mContext: Context,
    private val mUrl: String,
    private val imgList: MutableList<ImageInfo>?
) :
    ClickableSpan() {
    override fun onClick(widget: View) {
        if (mUrl == "" ||  StringBuilder(mUrl).substring(0, 6) == "/feed/") {
            return
        } else if (StringBuilder(mUrl).substring(0, 3) == "/t/") {
            val intent = Intent(mContext, TopicActivity::class.java)
            val index = StringBuilder(mUrl).indexOf("?")
            intent.putExtra("title", StringBuilder(mUrl).substring(3, index).toString())
            mContext.startActivity(intent)
        } else if (StringBuilder(mUrl).substring(0, 3) == "/u/") {
            val intent = Intent(mContext, UserActivity::class.java)
            intent.putExtra("id", mUrl.replace("/u/",""))
            mContext.startActivity(intent)
        } else if (mUrl.contains("image.coolapk.com")) {
            if (imgList == null) {
                val urlList: MutableList<ImageInfo> = ArrayList()
                val imageInfo = ImageInfo()
                imageInfo.thumbnailUrl = "$mUrl.s.jpg"
                imageInfo.originUrl = mUrl
                urlList.add(imageInfo)
                ImagePreview.instance
                    .setContext(mContext)
                    .setImageInfoList(urlList)
                    .setShowCloseButton(true)
                    .setEnableDragClose(true)
                    .setEnableUpDragClose(true)
                    .setFolderName("c001apk")
                    .start()
            } else {
                ImagePreview.instance
                    .setContext(mContext)
                    .setImageInfoList(imgList)
                    .setShowCloseButton(true)
                    .setEnableDragClose(true)
                    .setEnableUpDragClose(true)
                    .setFolderName("c001apk")
                    .start()
            }

        } else {
            val intent = Intent(mContext, WebViewActivity::class.java)
            intent.putExtra("url", mUrl)
            mContext.startActivity(intent)
        }
    }

    override fun updateDrawState(ds: TextPaint) {
        super.updateDrawState(ds)
        //ds.color = Color.parseColor("#1e5494") //设置文本颜色
        ds.isUnderlineText = false //取消下划线
    }
}