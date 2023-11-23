package com.example.c001apk.logic.model

import android.graphics.drawable.Drawable


class AppItem {
    var icon: Drawable? = null
    var appName: String = ""
    var packageName: String = ""
    var versionName: String = ""
    var isSystem: Boolean = false
    var lastUpdateTime: Long = 0
}