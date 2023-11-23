package com.example.c001apk.util

import android.annotation.SuppressLint
import android.content.ContentValues
import android.database.sqlite.SQLiteDatabase
import com.example.c001apk.MyApplication.Companion.context
import com.example.c001apk.logic.database.BlackListDataBaseHelper

object BlackListUtil {

    private val dbHelper = BlackListDataBaseHelper(context, "BlackList.db", 1)
    private val db: SQLiteDatabase = dbHelper.writableDatabase

    @SuppressLint("Range")
    fun checkUid(uid: String): Boolean {
        var isExist = false
        val cursor = db.query("BlackList", null, null, null, null, null, null)
        if (cursor.moveToFirst()) {
            do {
                val history = cursor.getString(cursor.getColumnIndex("uid"))
                if (uid == history) {
                    isExist = true
                    break
                }
            } while (cursor.moveToNext())
        }
        cursor.close()
        return isExist
    }

    @SuppressLint("Range", "NotifyDataSetChanged")
    fun saveUid(uid: String) {
        var isExist = false
        val cursor = db.query("BlackList", null, null, null, null, null, null)
        if (cursor.moveToFirst()) {
            do {
                val history = cursor.getString(cursor.getColumnIndex("uid"))
                if (uid == history) {
                    isExist = true
                    break
                }
            } while (cursor.moveToNext())
        }
        cursor.close()

        if (!isExist) {
            val value = ContentValues().apply {
                put("uid", uid)
            }
            db.insert("BlackList", null, value)
        }
    }

}