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
import com.example.c001apk.ui.app.AppActivity
import com.example.c001apk.ui.carousel.CarouselActivity
import com.example.c001apk.ui.coolpic.CoolPicActivity
import com.example.c001apk.ui.dyh.DyhActivity
import com.example.c001apk.ui.feed.FeedActivity
import com.example.c001apk.ui.others.WebViewActivity
import com.example.c001apk.ui.topic.TopicActivity
import com.example.c001apk.ui.user.UserActivity


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
        val replace = url
            .replace("coolmarket://", "/")
            .replace("https://", "")
            .replace("http://", "")
            .replace("www.", "")
            .replace("coolapk1s", "coolapk")
            .replace("coolapk.com", "")

        if (replace.startsWith("/feed/")) {
            with(replace.indexOfFirst { it == '?' }) {
                IntentUtil.startActivity<FeedActivity>(context) {
                    putExtra(
                        "id",
                        if (this@with != -1) replace.substring(6, this@with)
                        else replace.substring(6)
                    )
                    if (this@with != -1) {
                        replace.indexOf("rid=").let {
                            if (it != -1) {
                                putExtra("rid", replace.substring(it + 4))
                                putExtra("viewReply", true)
                            }
                        }
                    }
                }
            }
        } else if (replace.startsWith("/picture/")) {
            with(replace.indexOfFirst { it == '?' }) {
                IntentUtil.startActivity<FeedActivity>(context) {
                    putExtra(
                        "id",
                        if (this@with != -1) replace.substring(9, this@with)
                        else replace.substring(9)
                    )
                }
            }
        } else if (replace.startsWith("#/feed/")) { // iconLinkGridCard-coolpic
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
                putExtra("id", replace.substring(3))
            }
        } else if (replace.startsWith("/t/")) {
            if (replace.contains("?type=8")) {
                IntentUtil.startActivity<CoolPicActivity>(context) {
                    putExtra("title", replace.substring(3, replace.indexOfFirst { it == '?' }))
                }
            } else {
                with(replace.indexOfFirst { it == '?' }) {
                    val param = if (this@with != -1) replace.substring(3, this@with)
                    else replace.substring(3)
                    IntentUtil.startActivity<TopicActivity>(context) {
                        putExtra("type", "topic")
                        putExtra("url", param)
                        putExtra("title", param)
                    }
                }
            }
        } else if (replace.startsWith("/product/")) {
            IntentUtil.startActivity<TopicActivity>(context) {
                putExtra("type", "product")
                putExtra("title", title)
                putExtra("id", replace.substring(9))
            }
        } else if (replace.startsWith("#/page?url=") || replace.startsWith("/page?url=")) {
            IntentUtil.startActivity<CarouselActivity>(context) {
                putExtra("url", replace.replace("#/page?url=", "").replace("/page?url=", ""))
                putExtra("title", title)
            }
        } else if (replace.startsWith("image.coolapk.com")) {
            ImageUtil.startBigImgViewSimple(context, url.http2https)
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
            Toast.makeText(context, "unsupported url: $url", Toast.LENGTH_SHORT).show()
            ClipboardUtil.copyText(context, url, false)
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