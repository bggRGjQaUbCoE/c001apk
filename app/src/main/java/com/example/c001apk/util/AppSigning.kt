package com.example.c001apk.util

import android.content.Context
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.content.pm.Signature
import android.util.Log
import java.security.MessageDigest
import java.util.Locale

/**
 * 获取签名工具类
 */
object AppSigning {
    private const val MD5 = "MD5"
    private const val SHA1 = "SHA1"
    private const val SHA256 = "SHA256"
    private val mSignMap = HashMap<String, ArrayList<String>?>()

    /**
     * 返回一个签名的对应类型的字符串
     *
     * @return 因为一个安装包可以被多个签名文件签名，所以返回一个签名信息的list
     */
    fun getSignInfo(context: Context?, type: String?): ArrayList<String>? {
        if (context == null || type == null) {
            return null
        }
        val packageName = context.packageName ?: return null
        if (mSignMap[type] != null) {
            return mSignMap[type]
        }
        val mList = ArrayList<String>()
        try {
            val signs = getSignatures(context, packageName)!!
            for (sig in signs) {
                var tmp = "error!"
                when (type) {
                    MD5 -> tmp = getSignatureByteString(sig, MD5)
                    SHA1 -> tmp = getSignatureByteString(sig, SHA1)
                    SHA256 -> tmp = getSignatureByteString(sig, SHA256)
                }
                mList.add(tmp)
            }
        } catch (e: Exception) {
            Log.d("getSignInfo", "getSignInfo: " + e.message)
        }
        mSignMap[type] = mList
        return mList
    }

    /**
     * 获取签名sha1值
     */
    fun getSha1(context: Context?): String {
        var res = ""
        val mList = getSignInfo(context, SHA1)
        if (mList != null && mList.size != 0) {
            res = mList[0]
        }
        return res
    }

    /**
     * 获取签名MD5值
     */
    fun getMD5(context: Context?): String {
        var res = ""
        val mList = getSignInfo(context, MD5)
        if (mList != null && mList.size != 0) {
            res = mList[0]
        }
        return res
    }

    /**
     * 获取签名SHA256值
     */
    fun getSHA256(context: Context?): String {
        var res = ""
        val mList = getSignInfo(context, SHA256)
        if (mList != null && mList.size != 0) {
            res = mList[0]
        }
        return res
    }

    /**
     * 返回对应包的签名信息
     */
    private fun getSignatures(context: Context, packageName: String): Array<Signature>? {
        val packageInfo: PackageInfo
        try {
            packageInfo =
                context.packageManager.getPackageInfo(packageName, PackageManager.GET_SIGNATURES)
            return packageInfo.signatures
        } catch (e: Exception) {
            Log.d("getSignInfo", "getSignInfo: " + e.message)
        }
        return null
    }

    /**
     * 获取相应的类型的字符串（把签名的byte[]信息转换成16进制）
     */
    private fun getSignatureString(sig: Signature, type: String): String {
        val hexBytes = sig.toByteArray()
        var fingerprint = "error!"
        try {
            val digest = MessageDigest.getInstance(type)
            val digestBytes = digest.digest(hexBytes)
            val sb = StringBuilder()
            for (digestByte in digestBytes) {
                sb.append(
                    Integer.toHexString(digestByte.toInt() and 0xFF or 0x100)
                        .substring(1, 3)
                )
            }
            fingerprint = sb.toString()
        } catch (e: Exception) {
            Log.d("getSignInfo", "getSignInfo: " + e.message)
        }
        return fingerprint
    }

    /**
     * 获取相应的类型的字符串（把签名的byte[]信息转换成 95:F4:D4:FG 这样的字符串形式）
     */
    private fun getSignatureByteString(sig: Signature, type: String): String {
        val hexBytes = sig.toByteArray()
        var fingerprint = "error!"
        try {
            val digest = MessageDigest.getInstance(type)
            val digestBytes = digest.digest(hexBytes)
            val sb = StringBuilder()
            for (digestByte in digestBytes) {
                sb.append(
                    Integer.toHexString(digestByte.toInt() and 0xFF or 0x100)
                        .substring(1, 3).uppercase(Locale.getDefault())
                )
                sb.append(":")
            }
            fingerprint = sb.substring(0, sb.length - 1)
        } catch (e: Exception) {
            Log.d("getSignInfo", "getSignInfo: " + e.message)
        }
        return fingerprint
    }
}