package com.example.c001apk

import android.annotation.SuppressLint
import android.app.Application
import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatDelegate
import com.absinthe.libraries.utils.utils.Utility
import com.example.c001apk.ui.others.BugHandlerActivity
import com.example.c001apk.util.PrefManager
import dagger.hilt.android.HiltAndroidApp
import net.mikaelzero.mojito.Mojito
import net.mikaelzero.mojito.loader.glide.GlideImageLoader
import net.mikaelzero.mojito.view.sketch.SketchImageLoadFactory
import kotlin.system.exitProcess

@HiltAndroidApp
class MyApplication : Application() {

    override fun onCreate() {
        super.onCreate()

        context = applicationContext

        Utility.init(this)

        AppCompatDelegate.setDefaultNightMode(PrefManager.darkTheme)

        Mojito.initialize(
            GlideImageLoader.with(this),
            SketchImageLoadFactory()
        )

        Thread.setDefaultUncaughtExceptionHandler { _, paramThrowable ->
            val exceptionMessage = android.util.Log.getStackTraceString(paramThrowable)

            val intent = Intent(this, BugHandlerActivity::class.java)
            intent.putExtra("exception_message", exceptionMessage)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            startActivity(intent)

            android.os.Process.killProcess(android.os.Process.myPid())
            exitProcess(10)
        }
    }

    companion object {
        @SuppressLint("StaticFieldLeak")
        lateinit var context: Context
    }

}