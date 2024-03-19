package com.example.c001apk.util

import android.content.Context
import android.os.Environment
import java.io.File
import java.text.DecimalFormat

object CacheDataManager {
    /**
     * 获取整体缓存大小
     *
     * @param context
     * @return
     * @throws Exception
     */
    @Throws(Exception::class)
    fun getTotalCacheSize(context: Context): String {
        var cacheSize = getFolderSize(context.cacheDir)
        if (Environment.getExternalStorageState() == Environment.MEDIA_MOUNTED) {
            cacheSize += getFolderSize(context.externalCacheDir)
        }
        return getFormatSize(cacheSize)
    }

    /**
     * 获取文件
     * Context.getExternalFilesDir() --> SDCard/Android/data/你的应用的包名/files/ 目录，一般放一些长时间保存的数据
     * Context.getExternalCacheDir() --> SDCard/Android/data/你的应用包名/cache/目录，一般存放临时缓存数据
     *
     * @param file
     * @return
     * @throws Exception
     */
    @Throws(Exception::class)
    fun getFolderSize(file: File?): Long {
        var size: Long = 0
        try {
            file?.listFiles()?.forEach { value ->
                // 如果下面还有文件
                size = if (value.isDirectory()) {
                    size + getFolderSize(value)
                } else {
                    size + value.length()
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return size
    }

    /**
     * 格式化单位
     *
     * @param size
     */
    private fun getFormatSize(size: Long): String {
        val k = 1.shl(10)
        val m = 1.shl(20)
        val g = 1.shl(30)
        val df = DecimalFormat("0.00")
        return if (size >= g) {
            df.format(size.toFloat() / g) + "GB"
        } else if (size >= m) {
            df.format(size.toFloat() / m) + "MB"
        } else if (size >= k) {
            df.format(size.toFloat() / k) + "KB"
        } else {
            "$size B"
        }
    }

    /**
     * 清空方法
     *
     * @param context
     */
    fun clearAllCache(context: Context) {
        deleteDir(context.cacheDir)
        if (Environment.getExternalStorageState() == Environment.MEDIA_MOUNTED) {
            deleteDir(context.externalCacheDir)
        }
    }

    private fun deleteDir(dir: File?): Boolean {
        if (dir != null && dir.isDirectory()) {
            dir.list()?.forEach { child ->
                val success = deleteDir(File(dir, child))
                if (!success) {
                    return false
                }
            }
        }
        assert(dir != null)
        return dir?.delete() ?: false
    }
}
