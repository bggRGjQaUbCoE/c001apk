package com.example.c001apk.ui.fragment.search

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class HistoryDataBaseHelper(private val context: Context, name: String, version: Int) :
    SQLiteOpenHelper(context, name, null, version) {

    private val createSearchHistory = "create table SearchHistory (" +
            "id integer primary key autoincrement, " +
            "keyword text)"


    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL(createSearchHistory)
        //Toast.makeText(context, "Created", Toast.LENGTH_SHORT).show()
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("drop table if exists SearchHistory")
        onCreate(db)
    }


}