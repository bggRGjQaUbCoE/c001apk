package com.example.c001apk.view

import android.content.Context
import android.content.Intent
import android.text.TextPaint
import android.text.style.ClickableSpan
import android.view.View
import com.example.c001apk.ui.activity.FeedActivity
import com.example.c001apk.ui.activity.TopicActivity
import com.example.c001apk.ui.activity.UserActivity
import com.example.c001apk.ui.activity.WebViewActivity
import com.example.c001apk.view.ninegridimageview.indicator.CircleIndexIndicator
import net.mikaelzero.mojito.Mojito
import net.mikaelzero.mojito.impl.DefaultPercentProgress

internal class MyURLSpan(
    private val mContext: Context,
    private val mUrl: String,
    private val imgList: List<String>?
) :
    ClickableSpan() {
    override fun onClick(widget: View) {
        if (mUrl == "") {
            return
        } else if (mUrl.length >= 6 && StringBuilder(mUrl).substring(0, 6) == "/feed/") {
            return
        } else if (StringBuilder(mUrl).substring(0, 3) == "/t/") {
            val intent = Intent(mContext, TopicActivity::class.java)
            val index = StringBuilder(mUrl).indexOf("?")
            intent.putExtra("url", StringBuilder(mUrl).substring(3, index).toString())
            intent.putExtra("title", StringBuilder(mUrl).substring(3, index).toString())
            intent.putExtra("type", "topic")
            intent.putExtra("id", "")
            mContext.startActivity(intent)
        } else if (StringBuilder(mUrl).substring(0, 3) == "/u/") {
            val intent = Intent(mContext, UserActivity::class.java)
            intent.putExtra("id", mUrl.replace("/u/", ""))
            mContext.startActivity(intent)
        } else if (mUrl.contains("image.coolapk.com")) {
            if (imgList == null) {
                Mojito.start(mContext) {
                    urls(mUrl)
                    progressLoader {
                        DefaultPercentProgress()
                    }
                    setIndicator(CircleIndexIndicator())
                }
            } else {
                Mojito.start(mContext) {
                    urls(imgList)
                    progressLoader {
                        DefaultPercentProgress()
                    }
                    setIndicator(CircleIndexIndicator())
                }
            }
        } else if (mUrl.contains("www.coolapk.com/feed/")) {
            val id = if (mUrl.contains("shareKey")) {
                mUrl.substring(mUrl.lastIndexOf("/feed/") + 6, mUrl.lastIndexOf("?shareKey"))
            }else{
                mUrl.substring(mUrl.lastIndexOf("/feed/") + 6, mUrl.length)
            }
            val intent = Intent(mContext, FeedActivity::class.java)
            intent.putExtra("type", "feed")
            intent.putExtra("id", id)
            intent.putExtra("uid", "")
            intent.putExtra("uname", "")
            mContext.startActivity(intent)
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