package com.example.c001apk.util

import com.example.c001apk.MyApplication.Companion.context
import com.example.c001apk.constant.Constants.API_VERSION
import com.example.c001apk.constant.Constants.APP_ID
import com.example.c001apk.constant.Constants.CHANNEL
import com.example.c001apk.constant.Constants.DARK_MODE
import com.example.c001apk.constant.Constants.LOCALE
import com.example.c001apk.constant.Constants.MODE
import com.example.c001apk.constant.Constants.REQUEST_WITH
import com.example.c001apk.constant.Constants.SDK_INT
import com.example.c001apk.constant.Constants.USER_AGENT
import com.example.c001apk.constant.Constants.VERSION_CODE
import com.example.c001apk.constant.Constants.VERSION_NAME
import com.example.c001apk.util.CookieUtil.SESSID
import com.example.c001apk.util.TokenDeviceUtils.Companion.getTokenV2
import okhttp3.Interceptor
import okhttp3.Request
import okhttp3.Response
import java.io.IOException

internal class AddCookiesInterceptor : Interceptor {
    @Throws(IOException::class)
    override fun intercept(chain: Interceptor.Chain): Response {
        val builder: Request.Builder = chain.request().newBuilder()

        val deviceCode = if (PrefManager.customToken) PrefManager.xAppDevice
        else TokenDeviceUtils.getLastingDeviceCode(context)

        val token = if (PrefManager.customToken) PrefManager.xAppToken
        else deviceCode.getTokenV2()

        builder.apply {
            addHeader("User-Agent", USER_AGENT)
            addHeader("X-Requested-With", REQUEST_WITH)
            addHeader("X-Sdk-Int", SDK_INT)
            addHeader("X-Sdk-Locale", LOCALE)
            addHeader("X-App-Id", APP_ID)
            addHeader("X-App-Token", token)
            addHeader("X-App-Version", VERSION_NAME)
            addHeader("X-App-Code", VERSION_CODE)
            addHeader("X-Api-Version", API_VERSION)
            addHeader("X-App-Device", deviceCode)
            addHeader("X-Dark-Mode", DARK_MODE)
            addHeader("X-App-Channel", CHANNEL)
            addHeader("X-App-Mode", MODE)
            addHeader("X-App-Supported", VERSION_CODE)
            if (PrefManager.isLogin)
                addHeader(
                    "Cookie",
                    "uid=${PrefManager.uid}; username=${PrefManager.username}; token=${PrefManager.token}"
                )
            else addHeader("Cookie", SESSID)
        }
        return chain.proceed(builder.build())
    }
}