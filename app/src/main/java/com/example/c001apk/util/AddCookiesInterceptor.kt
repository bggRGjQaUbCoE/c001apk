package com.example.c001apk.util

import com.example.c001apk.constant.Constants.APP_ID
import com.example.c001apk.constant.Constants.REQUEST_WIDTH
import com.example.c001apk.constant.Constants.USER_AGENT
import com.example.c001apk.util.CookieUtil.SESSID
import com.example.c001apk.util.CookieUtil.deviceCode
import com.example.c001apk.util.CookieUtil.token
import okhttp3.Interceptor
import okhttp3.Request
import okhttp3.Response
import java.io.IOException

internal class AddCookiesInterceptor : Interceptor {
    @Throws(IOException::class)
    override fun intercept(chain: Interceptor.Chain): Response {
        val builder: Request.Builder = chain.request().newBuilder()
        builder.apply {
            addHeader("User-Agent", USER_AGENT)
            addHeader("X-Requested-With", REQUEST_WIDTH)
            addHeader("X-Sdk-Int", "33")
            addHeader("X-Sdk-Locale", "zh-CN")
            addHeader("X-App-Id", APP_ID)
            addHeader("X-App-Token", token)
            addHeader("X-App-Version", "13.3.1")
            addHeader("X-App-Code", "2307121")
            addHeader("X-Api-Version", "13")
            addHeader("X-App-Device", deviceCode)
            addHeader("X-Dark-Mode", "0")
            addHeader("X-App-Channel", "coolapk")
            addHeader("X-App-Mode", "universal")
            if (PrefManager.isLogin) addHeader(
                "Cookie",
                "${PrefManager.uid}; ${PrefManager.name}; ${PrefManager.token}"
            )
            else addHeader("Cookie", SESSID)
        }
        return chain.proceed(builder.build())
    }
}