package com.example.c001apk.util

import android.annotation.SuppressLint
import android.content.ContentValues
import android.database.sqlite.SQLiteDatabase
import com.example.c001apk.MyApplication.Companion.context
import com.example.c001apk.logic.database.BHistoryDataBaseHelper

object HistoryUtil {

    private val dbHelper = BHistoryDataBaseHelper(context, "BHistory.db", 1)
    private val db: SQLiteDatabase = dbHelper.writableDatabase

    @SuppressLint("Range", "NotifyDataSetChanged")
    fun saveHistory(
        fid: String,
        uid: String,
        uname: String,
        avatar: String,
        device: String,
        message: String,
        pubDate: String
    ) {
        var isExist = false
        val cursor = db.query("BHistory", null, null, null, null, null, null)
        if (cursor.moveToFirst()) {
            do {
                val history = cursor.getString(cursor.getColumnIndex("fid"))
                if (fid == history) {
                    isExist = true
                    break
                }
            } while (cursor.moveToNext())
        }
        cursor.close()

        if (!isExist) {
            val value = ContentValues().apply {
                put("fid", fid)
                put("uid", uid)
                put("uname", uname)
                put("avatar", avatar)
                put("device", device)
                put("message", message)
                put("pubDate", pubDate)
            }
            db.insert("BHistory", null, value)
        }
    }

}