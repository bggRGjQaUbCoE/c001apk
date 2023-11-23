package com.example.c001apk.util

import android.util.Base64
import java.nio.charset.Charset
import java.nio.charset.StandardCharsets.UTF_8

object DeviceCode {
    fun encode(deviceInfo: String): String {
        val charset: Charset = UTF_8
        val bytes = deviceInfo.toByteArray(charset)
        val encodeToString = Base64.encodeToString(bytes, 0)
        val replace = StringBuilder(encodeToString).reverse().toString()
        return Regex("\\r\\n|\\r|\\n|=").replace(replace, "")
    }
}