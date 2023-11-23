package com.example.c001apk.logic.database

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class BlackListDataBaseHelper(context: Context, name: String, version: Int) :
    SQLiteOpenHelper(context, name, null, version) {

    private val createBlackList = "create table BlackList (" +
            "id integer primary key autoincrement, " +
            "uid text)"


    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL(createBlackList)
        //Toast.makeText(context, "Created", Toast.LENGTH_SHORT).show()
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("drop table if exists BlackList")
        onCreate(db)
    }


}