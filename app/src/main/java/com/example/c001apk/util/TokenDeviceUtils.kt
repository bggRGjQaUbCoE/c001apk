package com.example.c001apk.util

import android.content.Context
import com.example.c001apk.MyApplication.Companion.context
import com.example.c001apk.constant.Constants
import com.example.c001apk.util.Utils.Companion.getBase64
import com.example.c001apk.util.Utils.Companion.getMD5
import com.example.c001apk.util.Utils.Companion.randomAndroidVersionRelease
import com.example.c001apk.util.Utils.Companion.randomBrand
import com.example.c001apk.util.Utils.Companion.randomDeviceModel
import com.example.c001apk.util.Utils.Companion.randomManufacturer
import com.example.c001apk.util.Utils.Companion.randomSdkInt
import java.util.Random


class TokenDeviceUtils {

    companion object {
        fun randHexString(@Suppress("SameParameterValue") n: Int): String {
            Random().setSeed(System.currentTimeMillis())
            return (0 until n).joinToString("") {
                Random().nextInt(256).toString(16)
            }.uppercase()
        }

        fun getDeviceCode(regenerate: Boolean): String {
            if (regenerate) {
                PrefManager.apply {
                    MANUFACTURER = randomManufacturer()
                    BRAND = randomBrand()
                    MODEL = randomDeviceModel()
                    BUILDNUMBER = randHexString(16)
                    SDK_INT = randomSdkInt()
                    ANDROID_VERSION = randomAndroidVersionRelease()
                    USER_AGENT = "Dalvik/2.1.0 (Linux; U; Android $ANDROID_VERSION; ${MODEL} ${BUILDNUMBER}) (#Build; ${BRAND}; ${MODEL}; ${BUILDNUMBER}; $ANDROID_VERSION) +CoolMarket/${VERSION_NAME}-${VERSION_CODE}-${Constants.MODE}"
                }
            }
            val szlmId = PrefManager.SZLMID
            val mac = Utils.randomMacAddress()
            val manuFactor = PrefManager.MANUFACTURER
            val brand = PrefManager.BRAND
            val model = PrefManager.MODEL
            val buildNumber = PrefManager.BUILDNUMBER
            return DeviceCode.encode("$szlmId; ; ; $mac; $manuFactor; $brand; $model; $buildNumber; null")
            //DeviceInfo(aid, mac, manuFactor, brand, model, buildNumber, fuck).createDeviceCode()
        }

        fun String.getTokenV2(): String {
            val timeStamp = (System.currentTimeMillis() / 1000f).toString()

            val base64TimeStamp = timeStamp.getBase64()
            val md5TimeStamp = timeStamp.getMD5()
            val md5DeviceCode = this.getMD5()

            val token = "${Constants.APP_LABEL}?$md5TimeStamp$$md5DeviceCode&${Constants.APP_ID}"
            val base64Token = token.getBase64()
            val md5Base64Token = base64Token.getMD5()
            val md5Token = token.getMD5()

            val bcryptSalt = "${"$2a$10$$base64TimeStamp/$md5Token".substring(0, 31)}u"
            val bcryptResult = org.mindrot.jbcrypt.BCrypt.hashpw(md5Base64Token, bcryptSalt)

            return "v2${bcryptResult.getBase64()}"
        }

        fun getLastingDeviceCode(): String {
            if (PrefManager.xAppDevice == "")
                PrefManager.xAppDevice = getDeviceCode(true)
            return PrefManager.xAppDevice
        }

        fun getLastingInstallTime(context: Context): String {
            val sp = context.getSharedPreferences(context.packageName, Context.MODE_PRIVATE)
            return sp.getString("INSTALL_TIME", null).let {
                it ?: System.currentTimeMillis().toString().apply {
                    sp.edit().putString("INSTALL_TIME", this).apply()
                }
            }
        }

        fun getLastingBrand(): String {
            val sp = context.getSharedPreferences(context.packageName, Context.MODE_PRIVATE)
            return sp.getString("BRAND", null).let {
                it ?: Utils.randomBrand().apply {
                    sp.edit().putString("BRAND", this).apply()
                }
            }
        }

        fun getLastingModel(): String {
            val sp = context.getSharedPreferences(context.packageName, Context.MODE_PRIVATE)
            return sp.getString("MODEL", null).let {
                it ?: Utils.randomDeviceModel().apply {
                    sp.edit().putString("MODEL", this).apply()
                }
            }
        }

        fun getLastingSdkInt(): String {
            val sp = context.getSharedPreferences(context.packageName, Context.MODE_PRIVATE)
            return sp.getString("SDK_INT", null).let {
                it ?: Utils.randomSdkInt().apply {
                    sp.edit().putString("SDK_INT", this).apply()
                }
            }
        }

        fun getLastingAndroidVersionRelease(): String {
            val sp = context.getSharedPreferences(context.packageName, Context.MODE_PRIVATE)
            return sp.getString("ANDROID_VERSION_RELEASE", null).let {
                it ?: Utils.randomAndroidVersionRelease().apply {
                    sp.edit().putString("ANDROID_VERSION_RELEASE", this).apply()
                }
            }
        }

        fun regenerateDeviceInfo(context: Context) {
            context.getSharedPreferences(context.packageName, Context.MODE_PRIVATE).edit().apply {
                putString("DEVICE_CODE", getDeviceCode(false))
                putString("INSTALL_TIME", System.currentTimeMillis().toString())
                putString("BRAND", Utils.randomBrand())
                putString("MODEL", Utils.randomDeviceModel())
                putString("SDK_INT", Utils.randomSdkInt())
                putString("ANDROID_VERSION_RELEASE", Utils.randomAndroidVersionRelease())
            }.apply()
        }
    }
}