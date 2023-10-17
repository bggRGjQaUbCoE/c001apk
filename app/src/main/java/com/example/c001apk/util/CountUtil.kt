package com.example.c001apk.util

object CountUtil {

    fun view(view: String): String =
        if (view.toLong() >= 10000)
            String.format("%.1f", view.toDouble() / 10000) + "万"
        else
            view
}