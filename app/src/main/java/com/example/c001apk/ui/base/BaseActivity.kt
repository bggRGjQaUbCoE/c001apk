package com.example.c001apk.ui.base

import android.content.Context
import android.content.res.Resources
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import androidx.viewbinding.ViewBinding
import com.example.c001apk.R
import com.example.c001apk.util.ActivityCollector
import com.example.c001apk.util.PrefManager
import com.example.c001apk.util.ThemeUtils
import com.google.android.material.color.DynamicColors
import rikka.material.app.MaterialActivity
import java.lang.reflect.ParameterizedType

abstract class BaseActivity<VB : ViewBinding> : MaterialActivity() {

    lateinit var binding: VB

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ActivityCollector.addActivity(this)
        val type = javaClass.genericSuperclass as ParameterizedType
        val aClass = type.actualTypeArguments[0] as Class<*>
        val method = aClass.getDeclaredMethod("inflate", LayoutInflater::class.java)
        @Suppress("UNCHECKED_CAST")
        binding = method.invoke(null, layoutInflater) as VB
        setContentView(binding.root)
    }

    override fun attachBaseContext(newBase: Context) {
        val configuration = newBase.resources.configuration
        configuration.fontScale = PrefManager.FONTSCALE.toFloat()
        super.attachBaseContext(newBase.createConfigurationContext(configuration))
    }

    override fun onDestroy() {
        super.onDestroy()
        ActivityCollector.removeActivity(this)
    }

    override fun computeUserThemeKey() =
        ThemeUtils.colorTheme + ThemeUtils.getNightThemeStyleRes(this)

    override fun onApplyTranslucentSystemBars() {
        super.onApplyTranslucentSystemBars()
        window.statusBarColor = Color.TRANSPARENT
        window.navigationBarColor = Color.TRANSPARENT
    }

    override fun onApplyUserThemeResource(theme: Resources.Theme, isDecorView: Boolean) {
        if (ThemeUtils.isSystemAccent)
            DynamicColors.applyToActivityIfAvailable(this)
        else
            theme.applyStyle(ThemeUtils.colorThemeStyleRes, true)
        theme.applyStyle(ThemeUtils.getNightThemeStyleRes(this), true) //blackDarkMode
    }

    override fun finish() {
        super.finish()
        overridePendingTransition(R.anim.left_in, R.anim.right_out)
    }

}
