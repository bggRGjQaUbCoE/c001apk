package com.example.c001apk.util

import android.os.Handler
import android.os.Looper
import android.widget.Toast
import com.example.c001apk.MyApplication

object ToastUtil {
    fun toast(msg: String) {
        Handler(Looper.getMainLooper()).post {
            Toast.makeText(MyApplication.context, msg, Toast.LENGTH_SHORT).show()
        }
    }
}