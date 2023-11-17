package com.example.c001apk.util

import android.content.Context
import android.util.Log
import com.example.c001apk.MyApplication
import com.example.c001apk.constant.Constants
import com.example.c001apk.constant.Constants.DISPLAY
import com.example.c001apk.logic.model.DeviceInfo
import com.example.c001apk.util.Utils.Companion.getBase64
import com.example.c001apk.util.Utils.Companion.getMD5


class TokenDeviceUtils {

    companion object {
        private fun DeviceInfo.createDeviceCode(isRaw: Boolean = true): String {
            Log.i("DeviceInfo", "createDeviceCode: $this")
            val byte = "$deviceId; ; ; $mac; $manufacturer; $brand; $model; $display; $oaid"
                .toByteArray(Charsets.UTF_8)
            val b64 = Base64Utils.encode(byte).reversed()
            return Regex("\\r\\n|\\r|\\n|=").replace(b64, "")
        }

        private fun getDeviceCode(): String {
            val deviceId = Utils.randomAndroidId()
            val mac = Utils.randomMacAddress()
            val manufacturer = Utils.randomManufacturer()
            val brand = Utils.randomBrand()
            val model = Utils.randomDeviceModel()
            val display = DISPLAY
            val oaid = Utils.randomOaid()

            return DeviceInfo(deviceId, mac, manufacturer, brand, model, display, oaid).createDeviceCode()
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

        fun getLastingDeviceCode(context: Context): String {
            val sp = context.getSharedPreferences(context.packageName, Context.MODE_PRIVATE)
            return sp.getString("DEVICE_CODE", null).let {
                it ?: getDeviceCode().apply {
                    sp.edit().putString("DEVICE_CODE", this).apply()
                }
            }
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
            val sp = MyApplication.context.getSharedPreferences(MyApplication.context.packageName, Context.MODE_PRIVATE)
            return sp.getString("BRAND", null).let {
                it ?: Utils.randomBrand().apply {
                    sp.edit().putString("BRAND", this).apply()
                }
            }
        }

        fun getLastingModel():String{
            val sp = MyApplication.context.getSharedPreferences(MyApplication.context.packageName, Context.MODE_PRIVATE)
            return sp.getString("MODEL", null).let {
                it ?: Utils.randomDeviceModel().apply {
                    sp.edit().putString("MODEL", this).apply()
                }
            }
        }

        fun getLastingSdkInt():String{
            val sp = MyApplication.context.getSharedPreferences(MyApplication.context.packageName, Context.MODE_PRIVATE)
            return sp.getString("SDK_INT", null).let {
                it ?: Utils.randomSdkInt().apply {
                    sp.edit().putString("SDK_INT", this).apply()
                }
            }
        }

        fun getLastingAndroidVersionRelease():String{
            val sp = MyApplication.context.getSharedPreferences(MyApplication.context.packageName, Context.MODE_PRIVATE)
            return sp.getString("ANDROID_VERSION_RELEASE", null).let {
                it ?: Utils.randomAndroidVersionRelease().apply {
                    sp.edit().putString("ANDROID_VERSION_RELEASE", this).apply()
                }
            }
        }

        fun regenerateDeviceInfo(context: Context) {
            context.getSharedPreferences(context.packageName, Context.MODE_PRIVATE).edit().apply {
                putString("DEVICE_CODE", getDeviceCode())
                putString("INSTALL_TIME", System.currentTimeMillis().toString())
                putString("BRAND", Utils.randomBrand())
                putString("MODEL", Utils.randomDeviceModel())
                putString("SDK_INT", Utils.randomSdkInt())
                putString("ANDROID_VERSION_RELEASE", Utils.randomAndroidVersionRelease())
            }.apply()
        }
    }
}