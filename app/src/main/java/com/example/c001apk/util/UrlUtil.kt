package com.example.c001apk.util

fun String.http2https(): String {
    return if (this[4] == 's') this
    else StringBuilder(this).insert(4, 's').toString()
}