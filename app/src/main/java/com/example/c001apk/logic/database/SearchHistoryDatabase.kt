package com.example.c001apk.logic.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.c001apk.logic.dao.SearchHistoryDao
import com.example.c001apk.logic.model.SearchHistory

@Database(version = 1, entities = [SearchHistory::class])
abstract class SearchHistoryDatabase : RoomDatabase() {
    abstract fun searchHistoryDao(): SearchHistoryDao

    companion object {
        private var instance: SearchHistoryDatabase? = null

        @Synchronized
        fun getDatabase(context: Context): SearchHistoryDatabase {
            instance?.let {
                return it
            }
            return Room.databaseBuilder(
                context.applicationContext,
                SearchHistoryDatabase::class.java, "search_history_database"
            )
                .build().apply {
                    instance = this
                }
        }
    }
}