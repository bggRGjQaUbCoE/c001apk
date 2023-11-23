package com.example.c001apk.util

import android.content.Context
import com.example.c001apk.constant.Constants
import com.example.c001apk.util.Utils.Companion.getBase64
import com.example.c001apk.util.Utils.Companion.getMD5
import java.util.Random


class TokenDeviceUtils {

    companion object {
        private fun randHexString(@Suppress("SameParameterValue") n: Int): String {
            Random().setSeed(System.currentTimeMillis())
            return (0 until n).joinToString("") {
                Random().nextInt(256).toString(16)
            }.uppercase()
        }

        fun getDeviceCode(): String {
            val szlmId = if (PrefManager.SZLMID == "") randHexString(16) else PrefManager.SZLMID
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
                PrefManager.xAppDevice = getDeviceCode()
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
    }
}