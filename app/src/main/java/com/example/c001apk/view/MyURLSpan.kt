package com.example.c001apk.view

import android.content.Context
import android.content.Intent
import android.text.TextPaint
import android.text.style.ClickableSpan
import android.view.View
import android.widget.Toast
import cc.shinichi.library.ImagePreview
import cc.shinichi.library.bean.ImageInfo
import com.example.c001apk.ui.activity.feed.FeedActivity
import com.example.c001apk.ui.activity.topic.TopicActivity
import com.example.c001apk.ui.activity.webview.WebViewActivity

internal class MyURLSpan(
    private val mContext: Context,
    private val id: String,
    private val mUrl: String
) :
    ClickableSpan() {
    override fun onClick(widget: View) {
        if (mUrl == "") {
            val intent = Intent(mContext, FeedActivity::class.java)
            intent.putExtra("type", "feed")
            intent.putExtra("id", id)
            mContext.startActivity(intent)
        } else if (StringBuilder(mUrl).substring(0, 3) == "/t/") {
            //Toast.makeText(mContext, "topic", Toast.LENGTH_SHORT).show()
            val intent = Intent(mContext, TopicActivity::class.java)
            intent.putExtra("title", StringBuilder(mUrl).substring(3, mUrl.length - 7).toString())
            mContext.startActivity(intent)
        } else if (StringBuilder(mUrl).substring(0, 3) == "/u/")
            Toast.makeText(mContext, "user", Toast.LENGTH_SHORT).show()
        else if (mUrl.contains("image.coolapk.com")) {
            //Toast.makeText(mContext, "image", Toast.LENGTH_SHORT).show()
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
            //Toast.makeText(mContext, "link", Toast.LENGTH_SHORT).show()
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