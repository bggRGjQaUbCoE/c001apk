package com.example.c001apk.constant

import com.example.c001apk.util.TokenDeviceUtils

object Constants {
    const val REQUEST_WITH = "XMLHttpRequest"
    const val LOCALE = "zh-CN"
    const val APP_ID = "com.coolapk.market"
    const val DARK_MODE = "0"
    const val CHANNEL = "coolapk"
    const val MODE = "universal"
    const val APP_LABEL = "token://com.coolapk.market/dcf01e569c1e3db93a3d0fcf191a622c"
    const val VERSION_NAME = "13.3.6"
    const val API_VERSION = "13"
    const val VERSION_CODE = "2310232"
    val DISPLAY: String = android.os.Build.DISPLAY
    val USER_AGENT =
        //"Dalvik/2.1.0 (Linux; U; Android ${ANDROID_VERSION}; $MODEL $BUILDNUMBER) (#Build; ${BRAND}; $MODEL; $BUILDNUMBER; $ANDROID_VERSION) +CoolMarket/${VERSION_NAME}-${VERSION_CODE}-${MODE}"
        "Dalvik/2.1.0 (Linux; U; Android ${TokenDeviceUtils.getLastingAndroidVersionRelease()}; ${TokenDeviceUtils.getLastingModel()} Build/$DISPLAY) (#Build; ${TokenDeviceUtils.getLastingBrand()}; ${TokenDeviceUtils.getLastingModel()}; ${android.os.Build.DISPLAY}; ${TokenDeviceUtils.getLastingAndroidVersionRelease()}) +CoolMarket/${VERSION_NAME}-${VERSION_CODE}-${MODE}"
}