package com.example.c001apk.util

import android.annotation.SuppressLint
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date

object DateUtils {
    private const val ONE_MINUTE: Long = 60
    private const val ONE_HOUR: Long = 3600
    private const val ONE_DAY: Long = 86400
    private const val ONE_MONTH: Long = 2592000
    private const val ONE_YEAR: Long = 31104000
    private var calendar: Calendar = Calendar.getInstance()
    val date: String
        /**
         * @return yyyy-mm-dd
         * 2012-12-25
         */
        get() = "$year-$month-$day"

    /**
     * @param format
     * @return yyyy年MM月dd HH:mm
     * MM-dd HH:mm 2012-12-25
     */
    @SuppressLint("SimpleDateFormat")
    fun getDate(format: String?): String {
        val simple = SimpleDateFormat(format)
        return simple.format(calendar.time)
    }

    val dateAndMinute: String
        /**
         * @return yyyy-MM-dd HH:mm
         * 2012-12-29 23:47
         */
        @SuppressLint("SimpleDateFormat")
        get() {
            val simple = SimpleDateFormat("yyyy-MM-dd HH:mm")
            return simple.format(calendar.time)
        }
    val fullDate: String
        /**
         * @return yyyy-MM-dd HH:mm:ss
         * 2012-12-29 23:47:36
         */
        @SuppressLint("SimpleDateFormat")
        get() {
            val simple = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
            return simple.format(calendar.time)
        }

    /**
     * 距离今天多久
     *
     * @param time
     * @return
     */
    @JvmStatic
    @SuppressLint("SimpleDateFormat")
    fun fromToday(time: Long): String {
        //Calendar calendar = Calendar.getInstance();
        //calendar.setTime(date);

        //long time = date.getTime() / 1000;
        val now = Date().time / 1000
        val ago = now - time
        return if (ago == 0L) {
            "刚刚"
        } else if (ago < 60) {
            ago.toString() + "秒前"
        } else if (ago <= ONE_HOUR) (ago / ONE_MINUTE).toString() + "分钟前" else if (ago <= ONE_DAY) (ago / ONE_HOUR).toString() + "小时前" //+ (ago % ONE_HOUR / ONE_MINUTE) + "分钟前";
        else if (ago <= ONE_DAY * 2) "1天前" //+ calendar.get(Calendar.HOUR_OF_DAY) + ":" + calendar.get(Calendar.MINUTE) + ":" + calendar.get(Calendar.SECOND);
        else if (ago <= ONE_DAY * 3) "2天前" //+ calendar.get(Calendar.HOUR_OF_DAY) + ":" + calendar.get(Calendar.MINUTE) + ":" + calendar.get(Calendar.SECOND);
        else if (ago <= ONE_MONTH) {
            val day = ago / ONE_DAY
            day.toString() + "天前" //+ calendar.get(Calendar.HOUR_OF_DAY) + ":" + calendar.get(Calendar.MINUTE) + ":" + calendar.get(Calendar.SECOND);
        } else {
            val date = (time * 1000).toString().toLong()
            val sdf: SimpleDateFormat = if (ago <= ONE_YEAR) {
                SimpleDateFormat("M月d日")
                /*long month = ago / ONE_MONTH;
                long day = ago % ONE_MONTH / ONE_DAY;
                return month + "个月" + day + "天前"
                        + calendar.get(Calendar.HOUR_OF_DAY) + "点"
                        + calendar.get(Calendar.MINUTE) + "分";*/
            } else {
                SimpleDateFormat("yyyy-MM-dd")
                /* long year = ago / ONE_YEAR;
                int month = calendar.get(Calendar.MONTH) + 1;// JANUARY which is 0 so month+1
                return year + "年前" + month + "月" + calendar.get(Calendar.DATE)
                        + "日";*/
            }
            sdf.format(Date(date))
        }
    }

    @SuppressLint("SimpleDateFormat")
    fun getDate(time: Long): String {
        val date = (time * 1000).toString().toLong()
        val sdf: SimpleDateFormat = SimpleDateFormat("yyyy-MM-dd")
        return sdf.format(Date(date))
    }

    /**
     * 距离截止日期还有多长时间
     *
     * @param date
     * @return
     */
    fun fromDeadline(date: Date): String {
        val deadline = date.time / 1000
        val now = Date().time / 1000
        val remain = deadline - now
        return if (remain <= ONE_HOUR) "只剩下" + remain / ONE_MINUTE + "分钟" else if (remain <= ONE_DAY) "只剩下" + remain / ONE_HOUR + "小时" + remain % ONE_HOUR / ONE_MINUTE + "分钟" else {
            val day = remain / ONE_DAY
            val hour =
                remain % ONE_DAY / ONE_HOUR
            val minute =
                remain % ONE_DAY % ONE_HOUR / ONE_MINUTE
            "只剩下" + day + "天" + hour + "小时" + minute + "分钟"
        }
    }

    /**
     * 距离今天的绝对时间
     *
     * @param date
     * @return
     */
    fun toToday(date: Date): String {
        val time = date.time / 1000
        val now = Date().time / 1000
        val ago = now - time
        return if (ago <= ONE_HOUR) (ago / ONE_MINUTE).toString() + "分钟" else if (ago <= ONE_DAY) (ago / ONE_HOUR).toString() + "小时" + ago % ONE_HOUR / ONE_MINUTE + "分钟" else if (ago <= ONE_DAY * 2) "昨天" + (ago - ONE_DAY) / ONE_HOUR + "点" + ((ago - ONE_DAY)
                % ONE_HOUR / ONE_MINUTE) + "分" else if (ago <= ONE_DAY * 3) {
            val hour = ago - ONE_DAY * 2
            "前天" + hour / ONE_HOUR + "点" + hour % ONE_HOUR / ONE_MINUTE + "分"
        } else if (ago <= ONE_MONTH) {
            val day = ago / ONE_DAY
            val hour =
                ago % ONE_DAY / ONE_HOUR
            val minute =
                ago % ONE_DAY % ONE_HOUR / ONE_MINUTE
            day.toString() + "天前" + hour + "点" + minute + "分"
        } else if (ago <= ONE_YEAR) {
            val month = ago / ONE_MONTH
            val day =
                ago % ONE_MONTH / ONE_DAY
            val hour =
                ago % ONE_MONTH % ONE_DAY / ONE_HOUR
            val minute =
                ago % ONE_MONTH % ONE_DAY % ONE_HOUR / ONE_MINUTE
            month.toString() + "个月" + day + "天" + hour + "点" + minute + "分前"
        } else {
            val year = ago / ONE_YEAR
            val month =
                ago % ONE_YEAR / ONE_MONTH
            val day =
                ago % ONE_YEAR % ONE_MONTH / ONE_DAY
            year.toString() + "年前" + month + "月" + day + "天"
        }
    }

    private val year: String
        get() = calendar[Calendar.YEAR].toString()
    private val month: String
        get() {
            val month = calendar[Calendar.MONTH] + 1
            return month.toString()
        }
    private val day: String
        get() = calendar[Calendar.DATE].toString()

    fun get24Hour(): String {
        return calendar[Calendar.HOUR_OF_DAY].toString()
    }

    val minute: String
        get() = calendar[Calendar.MINUTE].toString()
    val second: String
        get() = calendar[Calendar.SECOND].toString()

    @SuppressLint("SimpleDateFormat")
    fun timeStamp2Date(time: Long): String {
        val format = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
        return format.format(time * 1000)
    }
}
