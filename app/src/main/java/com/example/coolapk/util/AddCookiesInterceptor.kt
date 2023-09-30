package com.example.coolapk.util

import android.content.Context
import okhttp3.Interceptor
import okhttp3.Request
import okhttp3.Response
import java.io.IOException

internal class AddCookiesInterceptor : Interceptor {
    @Throws(IOException::class)
    override fun intercept(chain: Interceptor.Chain): Response {
        val builder: Request.Builder = chain.request().newBuilder()
        builder.addHeader("Cookie", CookieUtil.SESSID)
        return chain.proceed(builder.build())
    }
}