package com.example.c001apk.util

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.Uri
import android.util.Log
import android.widget.Toast
import com.example.c001apk.MyApplication.Companion.context
import com.example.c001apk.ui.activity.AppActivity
import com.example.c001apk.ui.activity.CarouselActivity
import com.example.c001apk.ui.activity.CoolPicActivity
import com.example.c001apk.ui.activity.DyhActivity
import com.example.c001apk.ui.activity.FeedActivity
import com.example.c001apk.ui.activity.TopicActivity
import com.example.c001apk.ui.activity.UserActivity
import com.example.c001apk.ui.activity.WebViewActivity


object NetWorkUtil {

    fun isWifiConnected(): Boolean {
        val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val network: Network? = cm.activeNetwork
        if (network != null) {
            val nc = cm.getNetworkCapabilities(network)
            nc?.let {
                if (nc.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)) {
                    return true
                } else if (nc.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)) {
                    return false
                }
            }
        }
        return false
    }

    fun openLink(context: Context, url: String, title: String?) {
        val replace = url.replace("https://", "")
            .replace("http://", "")
            .replace("www.", "")
            .replace("coolapk1s", "coolapk")
            .replace("coolapk.com", "")

        if (replace.startsWith("/feed/")) {
            val intent = Intent(context, FeedActivity::class.java)
            val id = if (replace.contains("shareKey"))
                replace.substring(6, replace.indexOf("?shareKey"))
            else replace.replace("/feed/", "")
            intent.putExtra("type", "feed")
            intent.putExtra("id", id)
            context.startActivity(intent)
        } else if (replace.startsWith("#/feed/")) {
            val intent = Intent(context, CarouselActivity::class.java)
            intent.putExtra("title", title)
            intent.putExtra("url", url)
            context.startActivity(intent)
        } else if (replace.startsWith("/apk/") || replace.startsWith("/game/")) {
            val intent = Intent(context, AppActivity::class.java)
            intent.putExtra("id", replace.replace("/apk/", "").replace("/game/", ""))
            context.startActivity(intent)
        } else if (replace.startsWith("/u/")) {
            val intent = Intent(context, UserActivity::class.java)
            intent.putExtra("id", replace.replace("/u/", ""))
            context.startActivity(intent)
        } else if (replace.startsWith("/t/")) {
            if (replace.contains("?type=8")) {
                val intent = Intent(context, CoolPicActivity::class.java)
                intent.putExtra("title", replace.replace("/t/", "").replace("?type=8", ""))
                context.startActivity(intent)
            } else {
                val intent = Intent(context, TopicActivity::class.java)
                intent.putExtra("type", "topic")
                intent.putExtra(
                    "url",
                    if (replace.contains("?type=")) replace.substring(3, replace.indexOf("?"))
                    else replace.replace("/t/", "")
                )
                intent.putExtra(
                    "title",
                    if (replace.contains("?type=")) replace.substring(3, replace.indexOf("?"))
                    else replace.replace("/t/", "")
                )
                context.startActivity(intent)
            }
        } else if (replace.startsWith("/product/")) {
            val intent = Intent(context, TopicActivity::class.java)
            intent.putExtra("type", "product")
            intent.putExtra("title", title)
            intent.putExtra("id", replace.replace("/product/", ""))
            context.startActivity(intent)
        } else if (replace.startsWith("#/page?url=") || replace.startsWith("/page?url=")) {
            val intent = Intent(context, CarouselActivity::class.java)
            intent.putExtra("url", replace.replace("#/page?url=", "").replace("/page?url=", ""))
            intent.putExtra("title", title)
            context.startActivity(intent)
        } else if (replace.startsWith("image.coolapk.com")) {
            ImageUtil.startBigImgViewSimple(context, url.http2https())
        } else if (url.startsWith("https://") || url.startsWith("http://")) {
            if (PrefManager.isOpenLinkOutside) {
                val intent = Intent()
                intent.action = Intent.ACTION_VIEW
                intent.data = Uri.parse(url)
                try {
                    context.startActivity(intent)
                } catch (e: ActivityNotFoundException) {
                    Toast.makeText(context, "打开失败", Toast.LENGTH_SHORT).show()
                    Log.w("error", "Activity was not found for intent, $intent")
                }
            } else {
                val intent = Intent(context, WebViewActivity::class.java)
                intent.putExtra("url", url)
                context.startActivity(intent)
            }
        } else {
            Toast.makeText(context, "unsupported url: $url}", Toast.LENGTH_SHORT).show()
        }
    }

    fun openLinkDyh(type: String, mContext: Context, url: String, id: String, title: String?) {
        when (type) {
            "feedRelation" -> {
                val intent = Intent(mContext, DyhActivity::class.java)
                intent.putExtra("id", id)
                intent.putExtra("title", title)
                mContext.startActivity(intent)
            }

            "topic", "product" -> {
                val intent = Intent(mContext, TopicActivity::class.java)
                intent.putExtra("type", type)
                intent.putExtra("title", title)
                intent.putExtra("url", url)
                intent.putExtra("id", id)
                mContext.startActivity(intent)
            }

            else -> openLink(mContext, url, title)
        }
    }

}