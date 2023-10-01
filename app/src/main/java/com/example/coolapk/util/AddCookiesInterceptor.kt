package com.example.coolapk.util

import com.example.coolapk.constant.Constants.APP_ID
import com.example.coolapk.constant.Constants.REQUEST_WIDTH
import com.example.coolapk.util.CookieUtil.SESSID
import com.example.coolapk.util.CookieUtil.deviceCode
import com.example.coolapk.util.CookieUtil.token
import okhttp3.Interceptor
import okhttp3.Request
import okhttp3.Response
import java.io.IOException

internal class AddCookiesInterceptor : Interceptor {
    @Throws(IOException::class)
    override fun intercept(chain: Interceptor.Chain): Response {
        val builder: Request.Builder = chain.request().newBuilder()
        builder.addHeader("X-Requested-With", REQUEST_WIDTH)
        builder.addHeader("X-App-Id", APP_ID)
        builder.addHeader("X-App-Device", deviceCode)
        builder.addHeader("X-App-Token", token)
        builder.addHeader("Cookie", SESSID)
        return chain.proceed(builder.build())
    }
}