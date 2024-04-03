package com.example.c001apk.util

import android.content.Context.MODE_PRIVATE
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatDelegate
import com.example.c001apk.MyApplication.Companion.context
import com.example.c001apk.constant.Constants

object PrefManager {

    private const val PREF_DARK_THEME = "dark_theme"
    private const val PREF_BLACK_DARK_THEME = "black_dark_theme"
    private const val PREF_FOLLOW_SYSTEM_ACCENT = "follow_system_accent"
    private const val PREF_THEME_COLOR = "theme_color"
    private const val SHOW_EMOJI = "show_emoji"
    private const val UID = "uid"
    private const val NAME = "name"
    private const val TOKEN = "token"

    private val pref = context.getSharedPreferences("settings", MODE_PRIVATE)

    var darkTheme: Int
        get() = pref.getInt(PREF_DARK_THEME, AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
        set(value) = pref.edit().putInt(PREF_DARK_THEME, value).apply()

    var blackDarkTheme: Boolean
        get() = pref.getBoolean(PREF_BLACK_DARK_THEME, false)
        set(value) = pref.edit().putBoolean(PREF_BLACK_DARK_THEME, value).apply()

    var followSystemAccent: Boolean
        get() = pref.getBoolean(PREF_FOLLOW_SYSTEM_ACCENT, true)
        set(value) = pref.edit().putBoolean(PREF_FOLLOW_SYSTEM_ACCENT, value).apply()

    var themeColor: String
        get() = pref.getString(PREF_THEME_COLOR, "MATERIAL_DEFAULT")!!
        set(value) = pref.edit().putString(PREF_THEME_COLOR, value).apply()

    var showEmoji: Boolean
        get() = pref.getBoolean(SHOW_EMOJI, true)
        set(value) = pref.edit().putBoolean(SHOW_EMOJI, value).apply()

    var isLogin: Boolean
        get() = pref.getBoolean("isLogin", false)
        set(value) = pref.edit().putBoolean("isLogin", value).apply()

    var uid: String
        get() = pref.getString(UID, "")!!
        set(value) = pref.edit().putString(UID, value).apply()

    var username: String
        get() = pref.getString(NAME, "")!!
        set(value) = pref.edit().putString(NAME, value).apply()

    var token: String
        get() = pref.getString(TOKEN, "")!!
        set(value) = pref.edit().putString(TOKEN, value).apply()

    var userAvatar: String
        get() = pref.getString("userAvatar", "")!!
        set(value) = pref.edit().putString("userAvatar", value).apply()

    var level: String
        get() = pref.getString("level", "")!!
        set(value) = pref.edit().putString("level", value).apply()

    var experience: String
        get() = pref.getString("experience", "")!!
        set(value) = pref.edit().putString("experience", value).apply()

    var nextLevelExperience: String
        get() = pref.getString("nextLevelExperience", "")!!
        set(value) = pref.edit().putString("nextLevelExperience", value).apply()

    var xAppToken: String
        get() = pref.getString("xAppToken", "")!!
        set(value) = pref.edit().putString("xAppToken", value).apply()

    var xAppDevice: String
        get() = pref.getString("xAppDevice", "")!!
        set(value) = pref.edit().putString("xAppDevice", value).apply()

    var customToken: Boolean
        get() = pref.getBoolean("customToken", false)
        set(value) = pref.edit().putBoolean("customToken", value).apply()

    var VERSION_NAME: String
        get() = pref.getString("VERSION_NAME", Constants.VERSION_NAME)!!
        set(value) = pref.edit().putString("VERSION_NAME", value).apply()

    var API_VERSION: String
        get() = pref.getString("API_VERSION", Constants.API_VERSION)!!
        set(value) = pref.edit().putString("API_VERSION", value).apply()

    var VERSION_CODE: String
        get() = pref.getString("VERSION_CODE", Constants.VERSION_CODE)!!
        set(value) = pref.edit().putString("VERSION_CODE", value).apply()

    var MANUFACTURER: String
        get() = pref.getString("MANUFACTURER", "")!!
        set(value) = pref.edit().putString("MANUFACTURER", value).apply()

    var BRAND: String
        get() = pref.getString("BRAND", "")!!
        set(value) = pref.edit().putString("BRAND", value).apply()

    var MODEL: String
        get() = pref.getString("MODEL", "")!!
        set(value) = pref.edit().putString("MODEL", value).apply()

    var BUILDNUMBER: String
        get() = pref.getString("BUILDNUMBER", "")!!
        set(value) = pref.edit().putString("BUILDNUMBER", value).apply()

    var SDK_INT: String
        get() = pref.getString("SDK_INT", "")!!
        set(value) = pref.edit().putString("SDK_INT", value).apply()

    var ANDROID_VERSION: String
        get() = pref.getString("ANDROID_VERSION", "")!!
        set(value) = pref.edit().putString("ANDROID_VERSION", value).apply()

    var USER_AGENT: String
        get() = pref.getString("USER_AGENT", "")!!
        set(value) = pref.edit().putString("USER_AGENT", value).apply()

    var SZLMID: String
        get() = pref.getString("SZLMID", "")!!
        set(value) = pref.edit().putString("SZLMID", value).apply()

    var isRecordHistory: Boolean
        get() = pref.getBoolean("isRecordHistory", true)
        set(value) = pref.edit().putBoolean("isRecordHistory", value).apply()

    var FONTSCALE: String
        get() = pref.getString("FONTSCALE", "1.00")!!
        set(value) = pref.edit().putString("FONTSCALE", value).apply()

    var isIconMiniCard: Boolean
        get() = pref.getBoolean("isIconMiniCard", true)
        set(value) = pref.edit().putBoolean("isIconMiniCard", value).apply()

    var isOpenLinkOutside: Boolean
        get() = pref.getBoolean("isOpenLinkOutside", false)
        set(value) = pref.edit().putBoolean("isOpenLinkOutside", value).apply()

    var FOLLOWTYPE: String
        get() = pref.getString("FOLLOWTYPE", "all")!!
        set(value) = pref.edit().putString("FOLLOWTYPE", value).apply()

    var imageQuality: String
        get() = pref.getString("imageQuality", "auto")!!
        set(value) = pref.edit().putString("imageQuality", value).apply()

    var isColorFilter: Boolean
        get() = pref.getBoolean("isColorFilter", true)
        set(value) = pref.edit().putBoolean("isColorFilter", value).apply()

    var isCheckUpdate: Boolean
        get() = pref.getBoolean("isCheckUpdate", true)
        set(value) = pref.edit().putBoolean("isCheckUpdate", value).apply()

    fun registerOnSharedPreferenceChangeListener(listener: SharedPreferences.OnSharedPreferenceChangeListener) {
        pref.registerOnSharedPreferenceChangeListener(listener)
    }

    fun unregisterOnSharedPreferenceChangeListener(listener: SharedPreferences.OnSharedPreferenceChangeListener) {
        pref.unregisterOnSharedPreferenceChangeListener(listener)
    }
}