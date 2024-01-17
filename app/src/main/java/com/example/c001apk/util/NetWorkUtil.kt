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
            val id = if (replace.contains("shareKey"))
                replace.substring(6, replace.indexOf("?shareKey"))
            else replace.replace("/feed/", "")
            IntentUtil.startActivity<FeedActivity>(context) {
                putExtra("id", id)
            }
        } else if (replace.startsWith("#/feed/")) {
            IntentUtil.startActivity<CarouselActivity>(context) {
                putExtra("title", title)
                putExtra("url", url)
            }
        } else if (replace.startsWith("/apk/") || replace.startsWith("/game/")) {
            IntentUtil.startActivity<AppActivity>(context) {
                putExtra("id", replace.replace("/apk/", "").replace("/game/", ""))
            }
        } else if (replace.startsWith("/u/")) {
            IntentUtil.startActivity<UserActivity>(context) {
                putExtra("id", replace.replace("/u/", ""))
            }
        } else if (replace.startsWith("/t/")) {
            if (replace.contains("?type=8")) {
                IntentUtil.startActivity<CoolPicActivity>(context) {
                    putExtra("title", replace.replace("/t/", "").replace("?type=8", ""))
                }
            } else {
                IntentUtil.startActivity<TopicActivity>(context) {
                    putExtra("type", "topic")
                    putExtra(
                        "url",
                        if (replace.contains("?type=")) replace.substring(3, replace.indexOf("?"))
                        else replace.replace("/t/", "")
                    )
                    putExtra(
                        "title",
                        if (replace.contains("?type=")) replace.substring(3, replace.indexOf("?"))
                        else replace.replace("/t/", "")
                    )
                }
            }
        } else if (replace.startsWith("/product/")) {
            IntentUtil.startActivity<TopicActivity>(context) {
                putExtra("type", "product")
                putExtra("title", title)
                putExtra("id", replace.replace("/product/", ""))
            }
        } else if (replace.startsWith("#/page?url=") || replace.startsWith("/page?url=")) {
            IntentUtil.startActivity<CarouselActivity>(context) {
                putExtra("url", replace.replace("#/page?url=", "").replace("/page?url=", ""))
                putExtra("title", title)
            }
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
                IntentUtil.startActivity<WebViewActivity>(context) {
                    putExtra("url", url)
                }
            }
        } else {
            Toast.makeText(context, "unsupported url: $url}", Toast.LENGTH_SHORT).show()
        }
    }

    fun openLinkDyh(type: String, mContext: Context, url: String, id: String, title: String?) {
        when (type) {
            "feedRelation" -> {
                IntentUtil.startActivity<DyhActivity>(mContext) {
                    putExtra("id", id)
                    putExtra("title", title)
                }
            }

            "topic", "product" -> {
                IntentUtil.startActivity<TopicActivity>(mContext) {
                    putExtra("type", type)
                    putExtra("title", title)
                    putExtra("url", url)
                    putExtra("id", id)
                }
            }

            else -> openLink(mContext, url, title)
        }
    }

}