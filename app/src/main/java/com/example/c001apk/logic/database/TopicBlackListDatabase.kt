package com.example.c001apk.logic.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.c001apk.logic.dao.BlackListDao
import com.example.c001apk.logic.model.SearchHistory

@Database(version = 1, entities = [SearchHistory::class])
abstract class TopicBlackListDatabase : RoomDatabase() {
    abstract fun blackListDao(): BlackListDao

    companion object {
        private var instance: TopicBlackListDatabase? = null

        @Synchronized
        fun getDatabase(context: Context): TopicBlackListDatabase {
            instance?.let {
                return it
            }
            return Room.databaseBuilder(
                context.applicationContext,
                TopicBlackListDatabase::class.java, "topic_blacklist_database"
            ).build().apply {
                instance = this
            }
        }
    }
}