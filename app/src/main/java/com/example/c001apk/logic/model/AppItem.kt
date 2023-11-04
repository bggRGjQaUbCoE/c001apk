package com.example.c001apk.logic.model

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable


class AppItem {
    var icon: Drawable = ColorDrawable(Color.TRANSPARENT)
    var appName: String = ""
    var packageName: String = ""
    var versionName: String = ""
    var isSystem: Boolean = false
}
