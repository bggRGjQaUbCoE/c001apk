package com.example.c001apk.util

import android.os.Handler
import android.os.Looper
import android.os.MessageQueue

/**
 * From drakeet
 */
fun doOnMainThreadIdle(action: () -> Unit) {
    val handler = Handler(Looper.getMainLooper())

    val idleHandler = MessageQueue.IdleHandler {
        handler.removeCallbacksAndMessages(null)
        action()
        return@IdleHandler false
    }

    fun setupIdleHandler(queue: MessageQueue) {
        queue.addIdleHandler(idleHandler)
    }

    if (Looper.getMainLooper() == Looper.myLooper()) {
        setupIdleHandler(Looper.myQueue())
    } else {
        setupIdleHandler(Looper.getMainLooper().queue)
    }
}