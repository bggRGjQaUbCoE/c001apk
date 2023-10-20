package com.example.c001apk.util

import android.content.Context
import androidx.annotation.StyleRes
import com.example.c001apk.R
import com.google.android.material.color.DynamicColors
import rikka.core.util.ResourceUtils

object ThemeUtils {

    @StyleRes
    fun getNightThemeStyleRes(context: Context): Int {
        return if (PrefManager.blackDarkTheme && ResourceUtils.isNightMode(context.resources.configuration))
            R.style.ThemeOverlay_Black else R.style.ThemeOverlay
    }

    val isSystemAccent
        get() = DynamicColors.isDynamicColorAvailable() && PrefManager.followSystemAccent

    private val colorThemeMap = mapOf(
        "MATERIAL_DEFAULT" to R.style.ThemeOverlay_MaterialDefault,
        "MATERIAL_SAKURA" to R.style.ThemeOverlay_MaterialSakura,
        "MATERIAL_RED" to R.style.ThemeOverlay_MaterialRed,
        "MATERIAL_PINK" to R.style.ThemeOverlay_MaterialPink,
        "MATERIAL_PURPLE" to R.style.ThemeOverlay_MaterialPurple,
        "MATERIAL_DEEP_PURPLE" to R.style.ThemeOverlay_MaterialDeepPurple,
        "MATERIAL_INDIGO" to R.style.ThemeOverlay_MaterialIndigo,
        "MATERIAL_BLUE" to R.style.ThemeOverlay_MaterialBlue,
        "MATERIAL_LIGHT_BLUE" to R.style.ThemeOverlay_MaterialLightBlue,
        "MATERIAL_CYAN" to R.style.ThemeOverlay_MaterialCyan,
        "MATERIAL_TEAL" to R.style.ThemeOverlay_MaterialTeal,
        "MATERIAL_GREEN" to R.style.ThemeOverlay_MaterialGreen,
        "MATERIAL_LIGHT_GREEN" to R.style.ThemeOverlay_MaterialLightGreen,
        "MATERIAL_LIME" to R.style.ThemeOverlay_MaterialLime,
        "MATERIAL_YELLOW" to R.style.ThemeOverlay_MaterialYellow,
        "MATERIAL_AMBER" to R.style.ThemeOverlay_MaterialAmber,
        "MATERIAL_ORANGE" to R.style.ThemeOverlay_MaterialOrange,
        "MATERIAL_DEEP_ORANGE" to R.style.ThemeOverlay_MaterialDeepOrange,
        "MATERIAL_BROWN" to R.style.ThemeOverlay_MaterialBrown,
        "MATERIAL_BLUE_GREY" to R.style.ThemeOverlay_MaterialBlueGrey
    )

    val colorTheme get() = if (isSystemAccent) "SYSTEM" else PrefManager.themeColor
    val colorThemeStyleRes: Int
        @StyleRes get() = colorThemeMap[colorTheme] ?: R.style.ThemeOverlay_MaterialDefault
}