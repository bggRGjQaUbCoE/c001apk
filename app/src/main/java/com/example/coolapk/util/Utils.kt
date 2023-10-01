package com.example.coolapk.util

import android.content.res.Configuration
import androidx.core.text.HtmlCompat
import java.security.MessageDigest
import java.text.SimpleDateFormat
import java.util.Base64
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.Random


class Utils {
    companion object {
        /**
         * 检测设备宽度是否大于等于800dp
         *
         * @receiver Configuration
         * @return boolean
         */
        fun Configuration.isTable() = this.screenWidthDp >= 800

        /**
         * 二进制数组转十六进制字符串
         *
         * @receiver byte array to be converted
         * @return string containing hex values
         */
        fun ByteArray.byteArrayToHexString(): String {
            val sb = StringBuilder(size * 2)
            for (element in this) {
                val v = element.toInt() and 0xff
                if (v < 16) {
                    sb.append('0')
                }
                sb.append(Integer.toHexString(v))
            }
            return sb.toString().uppercase(Locale.US)
        }

        /**
         * 十六进制字符串转二进制数组
         *
         * @receiver string of hex-encoded values
         * @return decoded byte array
         */
        fun String.hexStringToByteArray(): ByteArray {
            val data = ByteArray(length / 2)
            for (index in indices step 2) {
                data[index / 2] = (
                    (Character.digit(this[index], 16) shl 4) +
                        Character.digit(this[index + 1], 16)
                    ).toByte()
            }
            return data
        }

        /**
         * 根据字符串生成Base64码
         *
         * @receiver 原生字符串
         * @param isRaw 是否省略“=”符号
         * @return Base64码
         */
        fun String.getBase64(isRaw: Boolean = true): String {
            var result = Base64.getEncoder().encodeToString(this.toByteArray())
            if (isRaw) {
                result = result.replace("=", "")
            }
            return result
        }

        /**
         * 根据字符串生成MD5值
         *
         * @receiver 原生字符串
         * @return MD5值
         */
        fun String.getMD5(): String {
            val instance: MessageDigest = MessageDigest.getInstance("MD5")

            val digest = instance.digest(this.toByteArray())

            val sb = StringBuffer()

            for (b in digest) {
                val i = b.toInt() and 0xff
                var hexString = Integer.toHexString(i)
                if (hexString.length < 2) {
                    hexString = "0$hexString"
                }
                sb.append(hexString)
            }

            return sb.toString().replace("-", "")
        }

        /**
         * 随机生成Mac地址
         *
         * @return Mac地址
         */
        fun randomMacAddress(): String {
            val random = Random()
            val sb = StringBuilder()

            for (i in 0..5) {
                if (sb.isNotEmpty()) {
                    sb.append(":")
                }
                val value = random.nextInt(256)
                val element = Integer.toHexString(value)
                if (element.length < 2) {
                    sb.append(0)
                }
                sb.append(element)
            }

            return sb.toString().uppercase()
        }

        /**
         * 拓展Modifier无点击效果点击方法
         *
         * @receiver Modifier
         * @return Modifier
         */
        /*fun Modifier.clickableNoRipple(onClick: () -> Unit) = composed {
            clickable(
                indication = null,
                interactionSource = remember { MutableInteractionSource() }
            ) {
                onClick()
            }
        }*/

        /**
         * 格式化时间戳
         *
         * @receiver 时间戳
         * @return 格式化的时间
         */
        fun Long.secondToDateString(pattern: String = "yyyy-MM-dd HH:mm:ss") =
            SimpleDateFormat(pattern, Locale.CHINA).format(Date(this))

        /**
         * 计算时间差
         *
         * @receiver old timestamp
         * @param currentTime current timestamp
         */
        fun Long.timeStampInterval(currentTime: Long): String {
            val calendarOld = Calendar.getInstance()
            val calendarNow = Calendar.getInstance()

            val dateOld = Date(this)
            val dateNow = Date(currentTime)

            calendarOld.time = dateOld
            calendarNow.time = dateNow

            val dayOld = calendarOld.get(Calendar.DAY_OF_YEAR)
            val dayNow = calendarNow.get(Calendar.DAY_OF_YEAR)

            val hourOld = calendarOld.get(Calendar.HOUR_OF_DAY)
            val hourNow = calendarNow.get(Calendar.HOUR_OF_DAY)

            val minuteOld = calendarOld.get(Calendar.MINUTE)
            val minuteNow = calendarNow.get(Calendar.MINUTE)

            return if (dayOld == dayNow) {
                if (hourOld == hourNow) {
                    "${minuteNow - minuteOld}分钟前"
                } else {
                    "${hourNow - hourOld}小时前"
                }
            } else {
                "${dayNow - dayOld}天前"
            }
        }

        /**
         * 格式化富文本
         *
         * @receiver 原始文本
         * @return 格式化后文本
         */
        fun String.richToString(htmlCompat: Int = HtmlCompat.FROM_HTML_MODE_LEGACY) =
            HtmlCompat.fromHtml(this, htmlCompat).toString()
    }
}