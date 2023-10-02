package com.example.c001apk.util

import android.annotation.SuppressLint
import java.text.SimpleDateFormat


object PubDateUtil {

    fun time(date: String): String? {
        val timestamp1 = date.toLong()
        val timestamp2 = System.currentTimeMillis() / 1000

        val pubtime: String?
        var diffMillis = timestamp2 - timestamp1

        return if (diffMillis < 60) {
            diffMillis.toString() + "秒前"
        } else if (diffMillis < 3600) {
            diffMillis /= 60
            diffMillis.toString() + "分钟前"
        } else if (diffMillis < 24 * 3600) {
            diffMillis /= 3600
            diffMillis.toString() + "小时前"
        } else {
            @SuppressLint("SimpleDateFormat") val format = SimpleDateFormat("M月dd日")
            pubtime = format.format(timestamp1 * 1000)
            pubtime
        }
    }

    fun fullTime(date: String): String? {
        val timestamp1 = date.toLong()
        val pubTime: String?
        @SuppressLint("SimpleDateFormat") val format = SimpleDateFormat("yyyy年M月dd日 HH:mm:ss")
        pubTime = format.format(timestamp1 * 1000)
        return pubTime
    }

}