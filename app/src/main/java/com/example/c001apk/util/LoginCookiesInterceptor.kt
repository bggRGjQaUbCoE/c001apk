package com.example.c001apk.util

import com.example.c001apk.constant.Constants
import com.example.c001apk.util.CookieUtil.SESSID
import com.example.c001apk.util.CookieUtil.isGetCaptcha
import com.example.c001apk.util.CookieUtil.isGetLoginParam
import com.example.c001apk.util.CookieUtil.isTryLogin
import okhttp3.Interceptor
import okhttp3.Request
import okhttp3.Response
import java.io.IOException

internal class LoginCookiesInterceptor : Interceptor {
    @Throws(IOException::class)
    override fun intercept(chain: Interceptor.Chain): Response {
        val builder: Request.Builder = chain.request().newBuilder()
        builder.apply {
            if (isGetLoginParam) {
                isGetLoginParam = false
                addHeader("X-Requested-With", "com.coolapk.market")
                addHeader("X-App-Id", "com.coolapk.market")
                addHeader("Cookie", SESSID)
            } else if (isTryLogin) {
                isTryLogin = false
                addHeader("Cookie", "$SESSID; forward=https://www.coolapk.com")
                addHeader("X-Requested-With", "XMLHttpRequest")
                addHeader("Content-Type", "application/x-www-form-urlencoded")
            } else if (isGetCaptcha) {
                isGetCaptcha = false
                addHeader(
                    "sec-ch-ua",
                    """Android WebView";v="117", "Not;A=Brand";v="8", "Chromium";v="117"""
                )
                addHeader("sec-ch-ua-mobile", "?1")
                addHeader("User-Agent", Constants.USER_AGENT)
                addHeader("sec-ch-ua-platform", "Android")
                addHeader(
                    "Accept",
                    """image/avif,image/webp,image/apng,image/svg+xml,image/*,*/*;q=0.8"""
                )
                addHeader("X-Requested-With", "com.coolapk.market")
                addHeader("Sec-Fetch-Site", "same-origin")
                addHeader("Sec-Fetch-Mode", "no-cors")
                addHeader("Sec-Fetch-Dest", "image")
                addHeader("Referer", "https://account.coolapk.com/auth/login?type=mobile")
                addHeader("Cookie", "$SESSID; forward=https://www.coolapk.com")
            }
        }
        return chain.proceed(builder.build())
    }
}