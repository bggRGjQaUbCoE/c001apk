package com.example.c001apk.logic.database

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class BHistoryDataBaseHelper(context: Context, name: String, version: Int) :
    SQLiteOpenHelper(context, name, null, version) {

    private val createBHistory = "create table BHistory (" +
            "id integer primary key autoincrement, " +
            "fid text," +
            "uid text," +
            "uname text," +
            "avatar text," +
            "device text," +
            "message text," +
            "pubDate text)"


    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL(createBHistory)
        //Toast.makeText(context, "Created", Toast.LENGTH_SHORT).show()
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("drop table if exists BHistory")
        onCreate(db)
    }


}