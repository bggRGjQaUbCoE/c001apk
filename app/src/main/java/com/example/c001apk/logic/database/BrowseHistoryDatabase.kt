package com.example.c001apk.logic.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.c001apk.logic.dao.BrowseHistoryDao
import com.example.c001apk.logic.model.BrowseHistory

@Database(version = 1, entities = [BrowseHistory::class])
abstract class BrowseHistoryDatabase : RoomDatabase() {
    abstract fun browseHistoryDao(): BrowseHistoryDao

    companion object {
        private var instance: BrowseHistoryDatabase? = null

        @Synchronized
        fun getDatabase(context: Context): BrowseHistoryDatabase {
            instance?.let {
                return it
            }
            return Room.databaseBuilder(
                context.applicationContext,
                BrowseHistoryDatabase::class.java, "browse_history_database"
            )
                .build().apply {
                    instance = this
                }
        }
    }
}