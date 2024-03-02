package com.example.c001apk.util

import com.example.c001apk.constant.Constants.APP_ID
import com.example.c001apk.constant.Constants.CHANNEL
import com.example.c001apk.constant.Constants.DARK_MODE
import com.example.c001apk.constant.Constants.LOCALE
import com.example.c001apk.constant.Constants.MODE
import com.example.c001apk.constant.Constants.REQUEST_WITH
import com.example.c001apk.util.CookieUtil.SESSID
import com.example.c001apk.util.TokenDeviceUtils.getTokenV2
import okhttp3.Interceptor
import okhttp3.Response
import java.io.IOException

internal class AddCookiesInterceptor : Interceptor {
    @Throws(IOException::class)
    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()
        val deviceCode = TokenDeviceUtils.getLastingDeviceCode()
        val token = deviceCode.getTokenV2()
        val userCookie = if (PrefManager.isLogin) {
            "uid=${PrefManager.uid}; username=${PrefManager.username}; token=${PrefManager.token}"
        } else SESSID

        val request = originalRequest.newBuilder().apply {
            addHeader("User-Agent", PrefManager.USER_AGENT)
            addHeader("X-Requested-With", REQUEST_WITH)
            addHeader("X-Sdk-Int", PrefManager.SDK_INT)
            addHeader("X-Sdk-Locale", LOCALE)
            addHeader("X-App-Id", APP_ID)
            addHeader("X-App-Token", token)
            addHeader("X-App-Version", PrefManager.VERSION_NAME)
            addHeader("X-App-Code", PrefManager.VERSION_CODE)
            addHeader("X-Api-Version", PrefManager.API_VERSION)
            addHeader("X-App-Device", deviceCode)
            addHeader("X-Dark-Mode", DARK_MODE)
            addHeader("X-App-Channel", CHANNEL)
            addHeader("X-App-Mode", MODE)
            addHeader("X-App-Supported", PrefManager.VERSION_CODE)
            addHeader("Content-Type", "application/x-www-form-urlencoded")
            addHeader("Cookie", userCookie)
        }.build()

        return chain.proceed(request)
    }
}
