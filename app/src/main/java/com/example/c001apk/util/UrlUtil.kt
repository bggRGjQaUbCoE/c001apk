package com.example.c001apk.util

object UrlUtil {

    fun http2https(url: String) =
        if (StringBuilder(url)[4] != 's')
            StringBuilder(url).insert(4, "s").toString()
        else url


}