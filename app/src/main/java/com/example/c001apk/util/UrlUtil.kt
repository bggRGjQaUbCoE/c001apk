package com.example.c001apk.util

val String.http2https: String
    get() = if (this[4] == 's') this
    else StringBuilder(this).insert(4, 's').toString()