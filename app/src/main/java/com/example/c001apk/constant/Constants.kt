package com.example.c001apk.constant

object Constants {
    const val REQUEST_WITH = "XMLHttpRequest"
    const val LOCALE = "zh-CN"
    const val APP_ID = "com.coolapk.market"
    const val DARK_MODE = "0"
    const val CHANNEL = "coolapk"
    const val MODE = "universal"
    const val APP_LABEL = "token://com.coolapk.market/dcf01e569c1e3db93a3d0fcf191a622c"
    const val VERSION_NAME = "13.0.1"
    const val API_VERSION = "13"
    const val VERSION_CODE = "2301171"
    val MANUFACTURER: String = android.os.Build.MANUFACTURER
    val BRAND: String = android.os.Build.BRAND
    val MODEL: String = android.os.Build.MODEL
    val BUILDNUMBER: String = android.os.Build.DISPLAY
    val SDK_INT: String = android.os.Build.VERSION.CODENAME
    val ANDROID_VERSION = android.os.Build.VERSION.RELEASE
    val USER_AGENT =
        //"Dalvik/2.1.0 (Linux; U; Android ${ANDROID_VERSION}; $MODEL $BUILDNUMBER) (#Build; ${BRAND}; $MODEL; $BUILDNUMBER; $ANDROID_VERSION) +CoolMarket/${VERSION_NAME}-${VERSION_CODE}-${MODE}"
        "${System.getProperty("http.agent")} (#Build; ${android.os.Build.BRAND}; ${android.os.Build.MODEL}; ${android.os.Build.DISPLAY}; ${android.os.Build.VERSION.RELEASE}) +CoolMarket/${VERSION_NAME}-${VERSION_CODE}-${MODE}"
}